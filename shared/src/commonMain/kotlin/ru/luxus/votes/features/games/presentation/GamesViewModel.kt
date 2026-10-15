package ru.luxus.votes.features.games.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Factory
import ru.luxus.votes.features.games.data.GamesRepository
import ru.luxus.votes.features.games.domain.GameStats
import ru.luxus.votes.features.games.domain.WordEntry
import kotlin.random.Random

sealed interface GamesEvent {
    data object LoadStats : GamesEvent
    data class StartGame(val gameKey: String) : GamesEvent
    data class AnswerTranslate(val answer: String) : GamesEvent
    data class AnswerTrueFalse(val userSaysTrue: Boolean) : GamesEvent
    data class AnswerCompile(val assembledWord: String) : GamesEvent
    data object NextQuestion : GamesEvent
}

sealed interface GamesState {
    data object Loading : GamesState
    data class Initial(val stats: GameStats) : GamesState
    data class QuizInProgress(
        val gameKey: String,
        val question: WordEntry,
        val options: List<String>,
        val scrambledLetters: List<Char>,
        val currentIndex: Int,
        val totalQuestions: Int,
        val score: Int
    ) : GamesState
    data class QuizAnswered(
        val isCorrect: Boolean,
        val previous: QuizInProgress,
        val correctAnswer: String
    ) : GamesState
    data class QuizComplete(
        val score: Int,
        val total: Int,
        val gameKey: String
    ) : GamesState
}

@Factory
class GamesViewModel(
    private val repository: GamesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<GamesState>(GamesState.Loading)
    val state: StateFlow<GamesState> = _state.asStateFlow()

    private var currentQuestions: List<WordEntry> = emptyList()

    init {
        onEvent(GamesEvent.LoadStats)
    }

    fun onEvent(event: GamesEvent) {
        when (event) {
            is GamesEvent.LoadStats -> loadStats()
            is GamesEvent.StartGame -> startGame(event.gameKey)
            is GamesEvent.AnswerTranslate -> answerTranslate(event.answer)
            is GamesEvent.AnswerTrueFalse -> answerTrueFalse(event.userSaysTrue)
            is GamesEvent.AnswerCompile -> answerCompile(event.assembledWord)
            is GamesEvent.NextQuestion -> nextQuestion()
        }
    }

    private fun loadStats() {
        val stats = repository.getStats()
        _state.value = GamesState.Initial(stats)
    }

    private fun startGame(gameKey: String) {
        val allWords = repository.getWords()
        val pool = if (gameKey == "compile") {
            allWords.filter { it.udmurt.length <= 10 }
        } else {
            allWords
        }

        currentQuestions = pool.shuffled().take(10)
        if (currentQuestions.isEmpty()) return

        emitQuestion(gameKey = gameKey, index = 0, score = 0)
    }

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
        repository.saveResult(current.gameKey, isCorrect)

        _state.value = GamesState.QuizAnswered(
            isCorrect = isCorrect,
            previous = current,
            correctAnswer = current.question.russian
        )
    }

    private fun answerTrueFalse(userSaysTrue: Boolean) {
        val current = _state.value as? GamesState.QuizInProgress ?: return
        val shownWord = current.options[0]
        val isActuallyCorrect = current.options[1] == "true"
        val isCorrect = userSaysTrue == isActuallyCorrect

        repository.saveResult(current.gameKey, isCorrect)

        val explanation = if (isActuallyCorrect) {
            "✓ «$shownWord» — верно"
        } else {
            "✗ Правильно: «${current.question.udmurt}»"
        }

        _state.value = GamesState.QuizAnswered(
            isCorrect = isCorrect,
            previous = current,
            correctAnswer = explanation
        )
    }

    private fun answerCompile(assembledWord: String) {
        val current = _state.value as? GamesState.QuizInProgress ?: return
        val isCorrect = assembledWord.trim() == current.question.udmurt.trim()

        repository.saveResult(current.gameKey, isCorrect)

        _state.value = GamesState.QuizAnswered(
            isCorrect = isCorrect,
            previous = current,
            correctAnswer = current.question.udmurt
        )
    }

    private fun nextQuestion() {
        val answered = _state.value as? GamesState.QuizAnswered ?: return
        val prev = answered.previous
        val nextIndex = prev.currentIndex + 1
        val newScore = prev.score + if (answered.isCorrect) 1 else 0

        emitQuestion(gameKey = prev.gameKey, index = nextIndex, score = newScore)
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
