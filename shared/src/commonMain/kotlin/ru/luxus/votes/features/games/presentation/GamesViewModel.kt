package ru.luxus.votes.features.games.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import ru.luxus.votes.core.sound.SoundPlayer
import ru.luxus.votes.features.games.domain.repository.GamesRepository
import ru.luxus.votes.features.games.domain.DailyProgressEntry
import ru.luxus.votes.features.games.domain.GameStats
import ru.luxus.votes.features.games.domain.WordEntry
import ru.luxus.votes.features.games.domain.WordErrorEntry
import ru.luxus.votes.features.parent.domain.repository.ParentRepository
import ru.luxus.votes.features.parent.domain.learningPaceLabel
import ru.luxus.votes.features.settings.domain.repository.SettingsRepository
import ru.luxus.votes.core.datetime.getTodayDateString
import kotlin.random.Random

private const val ODD_ONE_OUT_ROUNDS = 5
private const val MEMORY_PAIRS = 6 // 6 pairs = 12 cards

data class MemoryCard(
    val id: Int,
    val wordId: String,
    val text: String,
    val subtext: String = "",
    val emoji: String = "",
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

/** Options of a quiz question plus the transcript of the word shown to the child. */
private data class BuiltOptions(
    val options: List<String>,
    val shownTranscription: String = "",
)

sealed interface GamesEvent {
    data object LoadStats : GamesEvent
    data class StartGame(val gameKey: String) : GamesEvent
    data class AnswerTranslate(val answer: String) : GamesEvent
    data class AnswerOddOneOut(val selectedWord: WordEntry) : GamesEvent
    data class AnswerSorting(val targetCategory: String) : GamesEvent
    data class FlipMemoryCard(val index: Int) : GamesEvent
    data class AnswerTrueFalse(val userSaysTrue: Boolean) : GamesEvent
    data class AnswerCompile(val assembledWord: String) : GamesEvent
    data class PlayAudio(val text: String, val phonetic: String = "") : GamesEvent
    data class MarkWordViewed(val wordId: String) : GamesEvent
    data object NextQuestion : GamesEvent
    data object ResetProgress : GamesEvent
}

sealed interface GamesState {
    data object Loading : GamesState
    data class Initial(
        val stats: GameStats,
        val topErrorWords: List<WordErrorEntry> = emptyList(),
        val dailyProgress: List<DailyProgressEntry> = emptyList(),
        val wordOfTheDay: WordEntry? = null,
        val viewedWordIds: Set<String> = emptySet(),
        val learningPace: String = "Обычный",
        val dailyWordGoal: Int = 5,
        val wordsLearnedToday: Int = 0,
    ) : GamesState

    data class QuizInProgress(
        val gameKey: String,
        val question: WordEntry,
        val options: List<String>,
        /**
         * Transcription of the word the child actually sees. In "Правда или
         * ложь" the shown word may be a decoy, so its transcript must not be
         * taken from [question].
         */
        val shownTranscription: String = "",
        val oddWords: List<WordEntry> = emptyList(),
        val oddTarget: WordEntry? = null,
        val scrambledLetters: List<Char> = emptyList(),
        val currentIndex: Int,
        val totalQuestions: Int,
        val score: Int,
        val hintLevel: Int = 0,
        val feedback: String? = null,
    ) : GamesState

    data class SortingInProgress(
        val categoryA: String,
        val categoryB: String,
        val items: List<WordEntry>,
        val currentItemIndex: Int,
        val score: Int,
        val total: Int,
        val lastFeedback: String? = null,
        val lastWasCorrect: Boolean? = null
    ) : GamesState

    data class MemoryInProgress(
        val cards: List<MemoryCard>,
        val flippedIndices: List<Int>,
        val moves: Int,
        val matchesFound: Int,
        val totalPairs: Int,
        val isLocked: Boolean = false,
        val gameId: Int = 0,
    ) : GamesState

    data class QuizAnswered(
        val isCorrect: Boolean,
        val previous: QuizInProgress,
        val correctAnswer: String,
        val explanation: String = ""
    ) : GamesState

    data class QuizComplete(
        val score: Int,
        val total: Int,
        val gameKey: String
    ) : GamesState
}

@KoinViewModel
class GamesViewModel(
    private val repository: GamesRepository,
    private val parentRepository: ParentRepository,
    private val soundPlayer: SoundPlayer,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<GamesState>(GamesState.Loading)
    val state: StateFlow<GamesState> = _state.asStateFlow()

    private var currentQuestions: List<WordEntry> = emptyList()

    /** All seeds are valid distractors even when questions use only learned words. */
    private var answerPool: List<WordEntry> = emptyList()

    /** Category pool for "odd one out" rounds so every round uses the same source. */
    private var oddOneOutPool: List<WordEntry> = emptyList()

    init {
        loadStats()
    }

    fun onEvent(event: GamesEvent) {
        when (event) {
            is GamesEvent.LoadStats -> loadStats()
            is GamesEvent.StartGame -> startGame(event.gameKey)
            is GamesEvent.AnswerTranslate -> answerTranslate(event.answer)
            is GamesEvent.AnswerOddOneOut -> answerOddOneOut(event.selectedWord)
            is GamesEvent.AnswerSorting -> answerSorting(event.targetCategory)
            is GamesEvent.FlipMemoryCard -> flipMemoryCard(event.index)
            is GamesEvent.AnswerTrueFalse -> answerTrueFalse(event.userSaysTrue)
            is GamesEvent.AnswerCompile -> answerCompile(event.assembledWord)
            is GamesEvent.PlayAudio -> playAudio(event.text, event.phonetic)
            is GamesEvent.MarkWordViewed -> markWordViewed(event.wordId)
            is GamesEvent.NextQuestion -> nextQuestion()
            is GamesEvent.ResetProgress -> resetProgress()
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val dailyProgress = repository.getDailyProgressForActiveChild()
            val activeChild = parentRepository.getActiveChild()
            val goal = activeChild?.targetWordsPerDay ?: 5
            val today = getTodayDateString()
            _state.value = GamesState.Initial(
                stats = repository.getStatsForActiveChild(),
                topErrorWords = repository.getTopErrorWordsForActiveChild(),
                dailyProgress = dailyProgress,
                wordOfTheDay = repository.getWordOfTheDay(),
                viewedWordIds = repository.getViewedWordIdsForActiveChild(),
                learningPace = learningPaceLabel(goal),
                dailyWordGoal = goal,
                wordsLearnedToday = dailyProgress.firstOrNull { it.date == today }?.wordsLearnedCount ?: 0,
            )
        }
    }

    private fun playSound(type: String) {
        viewModelScope.launch {
            if (!settingsRepository.getSettings().soundEnabled) return@launch
            when (type) {
                "success" -> soundPlayer.playSuccess()
                "error" -> soundPlayer.playError()
                "click" -> soundPlayer.playClick()
            }
        }
    }

    private fun playAudio(text: String, phonetic: String = "") {
        viewModelScope.launch {
            if (!settingsRepository.getSettings().soundEnabled) return@launch
            soundPlayer.playPronunciation(text, phonetic)
        }
    }

    private fun markWordViewed(wordId: String) {
        viewModelScope.launch {
            repository.markWordViewed(wordId)
            loadStats()
        }
    }

    private fun startGame(gameKey: String) {
        viewModelScope.launch {
            val allWords = repository.getWords()
            if (allWords.isEmpty()) return@launch

            // Questions come from words the child has already studied whenever
            // possible; a beginner is never quizzed on unseen vocabulary.
            val learnedIds = repository.getViewedWordIdsForActiveChild()
            val familiar = allWords.filter { it.id in learnedIds }
            answerPool = allWords

            when (gameKey) {
                "memory" -> startMemoryGame(familiar, allWords)
                "sorting" -> startSortingGame(familiar, allWords)
                "oddoneout" -> startOddOneOutGame(familiar, allWords)
                "compile" -> {
                    val source = if (familiar.size >= 4) familiar else allWords
                    currentQuestions = source
                        .filter { it.udmurt.length in 3..10 && !it.udmurt.contains(" ") }
                        .shuffled()
                        .take(5)
                    if (currentQuestions.isEmpty()) {
                        currentQuestions = allWords
                            .filter { it.udmurt.length in 3..10 && !it.udmurt.contains(" ") }
                            .take(5)
                    }
                    emitQuestion(gameKey, 0, 0)
                }
                else -> {
                    val source = if (familiar.isNotEmpty()) familiar else allWords
                    currentQuestions = source.shuffled().take(6)
                    emitQuestion(gameKey, 0, 0)
                }
            }
        }
    }

    // --- GAME 1, 5, 6: Quiz-based Questions (Translate, Compile, TrueFalse) ---

    private fun emitQuestion(gameKey: String, index: Int, score: Int) {
        if (index >= currentQuestions.size) {
            _state.value = GamesState.QuizComplete(
                score = score,
                total = currentQuestions.size,
                gameKey = gameKey
            )
            return
        }

        val question = currentQuestions[index]
        // "True / False" should pair two studied words, so a beginner can tell
        // that the shown word really has a different meaning. Other games may
        // use the full vocabulary as distractors.
        val optionPool = if (gameKey == "truefalse") currentQuestions else answerPool
        val builtOptions = buildOptions(gameKey, question, optionPool.ifEmpty { currentQuestions })
        val scrambled = if (gameKey == "compile") {
            question.udmurt.replace(" ", "").toCharArray().toList().shuffled()
        } else {
            emptyList()
        }

        _state.value = GamesState.QuizInProgress(
            gameKey = gameKey,
            question = question,
            options = builtOptions.options,
            shownTranscription = builtOptions.shownTranscription,
            scrambledLetters = scrambled,
            currentIndex = index,
            totalQuestions = currentQuestions.size,
            score = score
        )
    }

    private fun answerTranslate(answer: String) {
        val current = _state.value as? GamesState.QuizInProgress ?: return
        val isCorrect = answer == current.question.russian
        viewModelScope.launch { repository.saveResult(current.gameKey, isCorrect, current.question) }

        if (isCorrect) playSound("success") else playSound("error")

        _state.value = GamesState.QuizAnswered(
            isCorrect = isCorrect,
            previous = current,
            correctAnswer = current.question.russian,
            explanation = "«${current.question.udmurt}» переводится как «${current.question.russian}»"
        )
    }

    private fun answerTrueFalse(userSaysTrue: Boolean) {
        val current = _state.value as? GamesState.QuizInProgress ?: return
        val shownWord = current.options[0]
        val isActuallyCorrect = current.options.getOrNull(1) == "true"
        val isCorrect = userSaysTrue == isActuallyCorrect

        viewModelScope.launch { repository.saveResult(current.gameKey, isCorrect, current.question) }

        if (isCorrect) playSound("success") else playSound("error")

        val explanation = if (isActuallyCorrect) {
            "Верно. «${current.question.udmurt}» означает «${current.question.russian}»"
        } else {
            "На самом деле «${current.question.udmurt}» означает «${current.question.russian}»"
        }

        _state.value = GamesState.QuizAnswered(
            isCorrect = isCorrect,
            previous = current,
            correctAnswer = explanation
        )
    }

    private fun answerCompile(assembledWord: String) {
        val current = _state.value as? GamesState.QuizInProgress ?: return
        val target = current.question.udmurt.replace(" ", "")
        val isCorrect = assembledWord.trim().equals(target.trim(), ignoreCase = true)

        if (isCorrect) {
            playSound("success")
            viewModelScope.launch { repository.saveResult(current.gameKey, true, current.question) }
            _state.value = GamesState.QuizAnswered(
                isCorrect = true,
                previous = current,
                correctAnswer = current.question.udmurt,
                explanation = "Правильное написание: «${current.question.udmurt}» (${current.question.transcription})"
            )
            return
        }

        playSound("error")
        val maxHintLevel = 3

        if (current.hintLevel + 1 >= maxHintLevel) {
            // Two thirds are already revealed – show the full answer and move on
            viewModelScope.launch { repository.saveResult(current.gameKey, false, current.question) }
            _state.value = GamesState.QuizAnswered(
                isCorrect = false,
                previous = current,
                correctAnswer = current.question.udmurt,
                explanation = "Правильное написание: «${current.question.udmurt}» (${current.question.transcription})"
            )
        } else {
            // Stay on the same word and open the next third of it
            val nextHint = current.hintLevel + 1
            _state.value = current.copy(
                hintLevel = nextHint,
                feedback = if (nextHint == 1) {
                    "Не совсем. Открыли первую треть слова."
                } else {
                    "Попробуй ещё. Открыли две трети слова."
                }
            )
        }
    }

    // --- GAME 2: Find the Odd One Out (Найди лишнее слово) ---

    private fun startOddOneOutGame(familiar: List<WordEntry>, allWords: List<WordEntry>) {
        // The three "belonging" words come from a category the child has
        // already studied; the odd word may be new.
        val familiarCategories = familiar.groupBy { it.category }.filter { it.value.size >= 3 }
        oddOneOutPool = if (familiarCategories.isNotEmpty()) familiar else allWords

        emitOddOneOutQuestion(0, 0)
    }

    private fun emitOddOneOutQuestion(index: Int, score: Int) {
        val categories = oddOneOutPool.groupBy { it.category }.filter { it.value.size >= 3 }
        val categoryKeys = categories.keys.toList().shuffled()

        if (index >= ODD_ONE_OUT_ROUNDS || categoryKeys.isEmpty()) {
            _state.value = GamesState.QuizComplete(
                score = score,
                total = ODD_ONE_OUT_ROUNDS,
                gameKey = "oddoneout"
            )
            return
        }

        val mainCat = categoryKeys[index % categoryKeys.size]

        val threeWords = categories[mainCat]?.shuffled()?.take(3) ?: emptyList()
        val oddWord = answerPool
            .groupBy { it.category }
            .filterKeys { it != mainCat }
            .values
            .filter { it.isNotEmpty() }
            .shuffled()
            .firstOrNull()
            ?.shuffled()
            ?.firstOrNull()
            ?: return

        val fourWords = (threeWords + oddWord).shuffled()

        _state.value = GamesState.QuizInProgress(
            gameKey = "oddoneout",
            question = oddWord,
            options = emptyList(),
            oddWords = fourWords,
            oddTarget = oddWord,
            currentIndex = index,
            totalQuestions = ODD_ONE_OUT_ROUNDS,
            score = score
        )
    }

    private fun answerOddOneOut(selectedWord: WordEntry) {
        val current = _state.value as? GamesState.QuizInProgress ?: return
        val target = current.oddTarget ?: return
        val isCorrect = selectedWord.id == target.id

        viewModelScope.launch { repository.saveResult("oddoneout", isCorrect, target) }

        if (isCorrect) playSound("success") else playSound("error")

        val explanation = if (isCorrect) {
            "Верно. «${target.udmurt}» (${target.russian}) относится к теме «${target.category}». Остальные слова из других тем."
        } else {
            "«${target.udmurt}» (${target.russian}) лишнее, потому что относится к теме «${target.category}»."
        }

        _state.value = GamesState.QuizAnswered(
            isCorrect = isCorrect,
            previous = current,
            correctAnswer = target.udmurt,
            explanation = explanation
        )
    }

    // --- GAME 3: Sorting (Сортировка) ---

    private fun startSortingGame(familiar: List<WordEntry>, allWords: List<WordEntry>) {
        // Use studied categories when the child already knows at least two of
        // them; otherwise fall back to the full vocabulary.
        val familiarCategories = familiar.groupBy { it.category }.filter { it.value.size >= 2 }
        val source = if (familiarCategories.size >= 2) familiar else allWords
        val categories = source
            .groupBy { it.category }
            .filter { it.value.size >= 2 }
            .keys
            .toList()
            .shuffled()

        if (categories.size < 2) return

        val categoryA = categories[0]
        val categoryB = categories[1]
        val catAWords = source.filter { it.category == categoryA }.shuffled().take(3)
        val catBWords = source.filter { it.category == categoryB }.shuffled().take(3)

        val combined = (catAWords + catBWords).shuffled()

        _state.value = GamesState.SortingInProgress(
            categoryA = categoryA,
            categoryB = categoryB,
            items = combined,
            currentItemIndex = 0,
            score = 0,
            total = combined.size
        )
    }

    private fun answerSorting(targetCategory: String) {
        val current = _state.value as? GamesState.SortingInProgress ?: return
        if (current.currentItemIndex >= current.items.size) return

        val item = current.items[current.currentItemIndex]
        val isCorrect = item.category == targetCategory

        viewModelScope.launch { repository.saveResult("sorting", isCorrect, item) }

        if (isCorrect) playSound("success") else playSound("error")

        val newScore = current.score + if (isCorrect) 1 else 0
        val nextIndex = current.currentItemIndex + 1

        if (nextIndex >= current.total) {
            _state.value = GamesState.QuizComplete(
                score = newScore,
                total = current.total,
                gameKey = "sorting"
            )
        } else {
            _state.value = current.copy(
                currentItemIndex = nextIndex,
                score = newScore,
                lastFeedback = if (isCorrect) "Верно!" else "«${item.udmurt}» относится к категории «${item.category}»",
                lastWasCorrect = isCorrect
            )
        }
    }

    // --- GAME 4: Memory (Найди пару) ---

    private fun startMemoryGame(familiar: List<WordEntry>, allWords: List<WordEntry>) {
        // Classic memory board: a pair is two IDENTICAL cards of the same
        // word («куать» + «куать»), never a word mixed with its translation.
        // Phrases do not fit on a card, so only single words are dealt. The
        // Udmurt spelling must also be unique on the board: two look-alike
        // cards from different entries would read as a pair yet be rejected.
        // Studied words fill the board first; the rest of the vocabulary
        // completes the full 12-card set.
        fun fitsOnCard(word: WordEntry) =
            word.udmurt.isNotBlank() && !word.udmurt.contains(" ")

        val familiarIds = familiar.map { it.id }.toSet()
        val studiedFirst =
            familiar.filter { fitsOnCard(it) }.shuffled() +
                allWords.filter { it.id !in familiarIds && fitsOnCard(it) }.shuffled()
        val selectedWords = studiedFirst
            .distinctBy { it.id }
            .distinctBy { it.udmurt.trim().lowercase() }
            .take(MEMORY_PAIRS)
        var cardId = 0
        val cards = mutableListOf<MemoryCard>()

        selectedWords.forEach { word ->
            // Each word is dealt twice as two identical cards
            repeat(2) {
                cards.add(
                    MemoryCard(
                        id = cardId++,
                        wordId = word.id,
                        text = word.udmurt,
                        subtext = word.transcription,
                        emoji = word.emoji,
                        isFlipped = false,
                        isMatched = false
                    )
                )
            }
        }

        _state.value = GamesState.MemoryInProgress(
            cards = cards.shuffled(),
            flippedIndices = emptyList(),
            moves = 0,
            matchesFound = 0,
            totalPairs = selectedWords.size,
            isLocked = false,
            gameId = Random.nextInt()
        )
    }

    private fun flipMemoryCard(index: Int) {
        val current = _state.value as? GamesState.MemoryInProgress ?: return
        if (current.isLocked) return

        val card = current.cards.getOrNull(index) ?: return
        if (card.isFlipped || card.isMatched) return
        if (current.flippedIndices.contains(index)) return

        playSound("click")

        val updatedCards = current.cards.toMutableList()
        updatedCards[index] = card.copy(isFlipped = true)

        val newFlipped = (current.flippedIndices + index).distinct()

        if (newFlipped.size < 2) {
            _state.value = current.copy(
                cards = updatedCards,
                flippedIndices = newFlipped
            )
            return
        }

        val firstIdx = newFlipped[0]
        val secondIdx = newFlipped[1]
        val firstCard = updatedCards[firstIdx]
        val secondCard = updatedCards[secondIdx]
        val newMoves = current.moves + 1

        // A pair is two identical cards of the same word.
        val isMatch = firstCard.id != secondCard.id &&
            firstCard.wordId == secondCard.wordId

        if (isMatch) {
            playSound("success")
            updatedCards[firstIdx] = firstCard.copy(isMatched = true)
            updatedCards[secondIdx] = secondCard.copy(isMatched = true)
            val newMatches = current.matchesFound + 1

            viewModelScope.launch { repository.saveResult("memory", true) }

            if (newMatches == current.totalPairs) {
                _state.value = GamesState.QuizComplete(
                    score = current.totalPairs,
                    total = current.totalPairs,
                    gameKey = "memory"
                )
            } else {
                _state.value = current.copy(
                    cards = updatedCards,
                    flippedIndices = emptyList(),
                    moves = newMoves,
                    matchesFound = newMatches
                )
            }
        } else {
            playSound("error")
            viewModelScope.launch { repository.saveResult("memory", false) }
            _state.value = current.copy(
                cards = updatedCards,
                flippedIndices = newFlipped,
                moves = newMoves,
                isLocked = true
            )

            val gameId = current.gameId
            viewModelScope.launch {
                delay(1000)
                // Apply the reset only if the same game is still active, and always
                // use the latest state so a quick restart can't corrupt the board
                val latest = _state.value as? GamesState.MemoryInProgress ?: return@launch
                if (latest.gameId != gameId) return@launch
                if (firstIdx !in latest.cards.indices || secondIdx !in latest.cards.indices) return@launch

                val resetCards = latest.cards.toMutableList()
                if (resetCards[firstIdx].isMatched || resetCards[secondIdx].isMatched) return@launch
                resetCards[firstIdx] = resetCards[firstIdx].copy(isFlipped = false)
                resetCards[secondIdx] = resetCards[secondIdx].copy(isFlipped = false)

                _state.value = latest.copy(
                    cards = resetCards,
                    flippedIndices = emptyList(),
                    isLocked = false
                )
            }
        }
    }

    private fun nextQuestion() {
        val answered = _state.value as? GamesState.QuizAnswered ?: return
        val prev = answered.previous
        val nextIndex = prev.currentIndex + 1
        val newScore = prev.score + if (answered.isCorrect) 1 else 0

        if (prev.gameKey == "oddoneout") {
            emitOddOneOutQuestion(nextIndex, newScore)
        } else {
            emitQuestion(gameKey = prev.gameKey, index = nextIndex, score = newScore)
        }
    }

    private fun resetProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            loadStats()
        }
    }

    private fun buildOptions(
        gameKey: String,
        question: WordEntry,
        pool: List<WordEntry>
    ): BuiltOptions {
        if (gameKey == "truefalse") {
            val showCorrect = Random.nextBoolean()
            if (showCorrect) {
                return BuiltOptions(
                    options = listOf(question.udmurt, "true"),
                    shownTranscription = question.transcription
                )
            }
            // The wrong word must not share the shown translation, or the
            // statement would accidentally be true. Its own transcription is
            // kept so the child hears exactly the word shown on screen.
            val wrong = pool
                .filter { it.id != question.id && it.russian != question.russian }
                .shuffled()
                .firstOrNull()
            return if (wrong != null) {
                BuiltOptions(
                    options = listOf(wrong.udmurt, "false"),
                    shownTranscription = wrong.transcription
                )
            } else {
                BuiltOptions(options = listOf("слово", "false"))
            }
        }

        // Three options are enough for a beginner; duplicate translations of
        // different Udmurt words would make the question ambiguous.
        val distractors = pool
            .filter { it.id != question.id && it.russian != question.russian }
            .distinctBy { it.russian }
            .shuffled()
            .take(2)
            .map { it.russian }
        return BuiltOptions(options = (listOf(question.russian) + distractors).distinct().shuffled())
    }
}
