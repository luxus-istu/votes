package ru.luxus.votes.features.games.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import ru.luxus.votes.core.sound.SoundPlayer
import ru.luxus.votes.features.games.data.GamesRepository
import ru.luxus.votes.features.games.domain.DailyProgressEntry
import ru.luxus.votes.features.games.domain.GameStats
import ru.luxus.votes.features.games.domain.WordEntry
import ru.luxus.votes.features.games.domain.WordErrorEntry
import ru.luxus.votes.features.parent.data.ParentRepository
import ru.luxus.votes.features.parent.domain.learningPaceLabel
import ru.luxus.votes.features.settings.data.SettingsRepository
import ru.luxus.votes.getTodayDateString
import kotlin.random.Random

data class MemoryCard(
    val id: Int,
    val wordId: String,
    val text: String,
    val subtext: String = "",
    val emoji: String = "",
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false,
    /** 0 = Udmurt word side, 1 = Russian translation side */
    val face: Int = 0
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
        val learningPace: String = "Обычный",
        val dailyWordGoal: Int = 5,
        val wordsLearnedToday: Int = 0,
    ) : GamesState

    data class QuizInProgress(
        val gameKey: String,
        val question: WordEntry,
        val options: List<String>,
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
        val lastFeedback: String? = null
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

@Factory
class GamesViewModel(
    private val repository: GamesRepository,
    private val parentRepository: ParentRepository,
    private val soundPlayer: SoundPlayer,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<GamesState>(GamesState.Loading)
    val state: StateFlow<GamesState> = _state.asStateFlow()

    private var currentQuestions: List<WordEntry> = emptyList()

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
            is GamesEvent.NextQuestion -> nextQuestion()
            is GamesEvent.ResetProgress -> resetProgress()
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            val dailyProgress = repository.getDailyProgress()
            val activeChild = parentRepository.getActiveChild()
            val goal = activeChild?.targetWordsPerDay ?: 5
            val today = getTodayDateString()
            _state.value = GamesState.Initial(
                stats = repository.getStats(),
                topErrorWords = repository.getTopErrorWords(),
                dailyProgress = dailyProgress,
                wordOfTheDay = repository.getWordOfTheDay(),
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

    private fun startGame(gameKey: String) {
        viewModelScope.launch {
            val allWords = repository.getWords()
            if (allWords.isEmpty()) return@launch

            when (gameKey) {
                "memory" -> startMemoryGame(allWords)
                "sorting" -> startSortingGame(allWords)
                "oddoneout" -> startOddOneOutGame(allWords)
                "compile" -> {
                    currentQuestions = allWords.filter { it.udmurt.length in 3..10 && !it.udmurt.contains(" ") }.shuffled().take(8)
                    if (currentQuestions.isEmpty()) currentQuestions = allWords.take(8)
                    emitQuestion(gameKey, 0, 0)
                }
                else -> {
                    currentQuestions = allWords.shuffled().take(10)
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
        val options = buildOptions(gameKey, question, currentQuestions)
        val scrambled = if (gameKey == "compile") {
            question.udmurt.replace(" ", "").toCharArray().toList().shuffled()
        } else {
            emptyList()
        }

        _state.value = GamesState.QuizInProgress(
            gameKey = gameKey,
            question = question,
            options = options,
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
            "✓ Верно! «${current.question.udmurt}» действительно означает «${current.question.russian}»"
        } else {
            "✗ На самом деле «${current.question.udmurt}» — это «${current.question.russian}»"
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
                    "Не совсем! Открыли первую треть слова."
                } else {
                    "Попробуй ещё — открыли две трети слова."
                }
            )
        }
    }

    // --- GAME 2: Find the Odd One Out (Найди лишнее слово) ---

    private fun startOddOneOutGame(allWords: List<WordEntry>) {
        val categories = allWords.groupBy { it.category }.filter { it.value.size >= 3 }
        val categoryKeys = categories.keys.toList().shuffled()
        if (categoryKeys.size < 2) return

        currentQuestions = categoryKeys.take(8).mapNotNull { catA ->
            val otherCat = categoryKeys.firstOrNull { it != catA } ?: return@mapNotNull null
            val threeFromA = categories[catA]?.shuffled()?.take(3) ?: return@mapNotNull null
            val oneFromB = categories[otherCat]?.shuffled()?.firstOrNull() ?: return@mapNotNull null
            oneFromB // target odd word
        }

        emitOddOneOutQuestion(0, 0, allWords)
    }

    private fun emitOddOneOutQuestion(index: Int, score: Int, allWords: List<WordEntry>) {
        val categories = allWords.groupBy { it.category }.filter { it.value.size >= 3 }
        val categoryKeys = categories.keys.toList().shuffled()

        if (index >= 8 || categoryKeys.size < 2) {
            _state.value = GamesState.QuizComplete(
                score = score,
                total = 8,
                gameKey = "oddoneout"
            )
            return
        }

        val mainCat = categoryKeys[index % categoryKeys.size]
        val otherCat = categoryKeys.first { it != mainCat }

        val threeWords = categories[mainCat]?.shuffled()?.take(3) ?: emptyList()
        val oddWord = categories[otherCat]?.shuffled()?.firstOrNull() ?: return

        val fourWords = (threeWords + oddWord).shuffled()

        _state.value = GamesState.QuizInProgress(
            gameKey = "oddoneout",
            question = oddWord,
            options = emptyList(),
            oddWords = fourWords,
            oddTarget = oddWord,
            currentIndex = index,
            totalQuestions = 8,
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
            "🎉 Отлично! «${target.udmurt}» (${target.russian}) относится к теме «${target.category}», а остальные слова — к другой теме!"
        } else {
            "Лишнее слово — «${target.udmurt}» (${target.russian}), так как оно относится к категории «${target.category}»!"
        }

        _state.value = GamesState.QuizAnswered(
            isCorrect = isCorrect,
            previous = current,
            correctAnswer = target.udmurt,
            explanation = explanation
        )
    }

    // --- GAME 3: Sorting (Сортировка) ---

    private fun startSortingGame(allWords: List<WordEntry>) {
        // Pairs of contrasting categories
        val candidatePairs = listOf(
            Pair("дикие животные", "домашние животные"),
            Pair("продукты", "овощи"),
            Pair("моя семья", "одежда")
        )
        val selectedPair = candidatePairs.shuffled().first()
        val catAWords = allWords.filter { it.category == selectedPair.first }.shuffled().take(4)
        val catBWords = allWords.filter { it.category == selectedPair.second }.shuffled().take(4)

        val combined = (catAWords + catBWords).shuffled()

        _state.value = GamesState.SortingInProgress(
            categoryA = selectedPair.first,
            categoryB = selectedPair.second,
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
                lastFeedback = if (isCorrect) "✓ Верно!" else "✗ «${item.udmurt}» относится к: ${item.category}"
            )
        }
    }

    // --- GAME 4: Memory (Найди пару) ---

    private fun startMemoryGame(allWords: List<WordEntry>) {
        // Unique words only, so every pair is guaranteed to have two different faces
        val selectedWords = allWords
            .shuffled()
            .distinctBy { it.id }
            .take(6) // 6 pairs = 12 cards
        var cardId = 0
        val cards = mutableListOf<MemoryCard>()

        selectedWords.forEach { word ->
            // Card 1: Udmurt word
            cards.add(
                MemoryCard(
                    id = cardId++,
                    wordId = word.id,
                    text = word.udmurt,
                    subtext = word.transcription,
                    emoji = "📖",
                    isFlipped = false,
                    isMatched = false,
                    face = 0
                )
            )
            // Card 2: Russian translation + Emoji
            cards.add(
                MemoryCard(
                    id = cardId++,
                    wordId = word.id,
                    text = word.russian,
                    subtext = "",
                    emoji = word.emoji,
                    isFlipped = false,
                    isMatched = false,
                    face = 1
                )
            )
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

        // A pair is valid only when both cards reference the same word AND show
        // opposite faces (Udmurt word side + Russian translation side).
        val isMatch = firstCard.id != secondCard.id &&
            firstCard.wordId == secondCard.wordId &&
            firstCard.face != secondCard.face

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
            viewModelScope.launch {
                emitOddOneOutQuestion(nextIndex, newScore, repository.getWords())
            }
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
    ): List<String> {
        if (gameKey == "truefalse") {
            val showCorrect = Random.nextBoolean()
            return if (showCorrect) {
                listOf(question.udmurt, "true")
            } else {
                val wrong = pool.filter { it.id != question.id }.shuffled().firstOrNull()?.udmurt ?: "слово"
                listOf(wrong, "false")
            }
        }

        val distractors = pool.filter { it.id != question.id }.shuffled().take(3).map { it.russian }
        return (listOf(question.russian) + distractors).shuffled()
    }
}
