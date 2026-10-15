package ru.luxus.votes.features.lessons.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import ru.luxus.votes.features.games.data.GamesRepository
import ru.luxus.votes.features.games.domain.WordEntry
import ru.luxus.votes.features.lessons.domain.LessonProgress

sealed interface LessonsEvent {
    data object LoadData : LessonsEvent
    data class MarkWordViewed(val wordId: String) : LessonsEvent
}

sealed interface LessonsState {
    data object Loading : LessonsState
    data class Loaded(
        val groupedWords: Map<String, List<WordEntry>>,
        val progress: LessonProgress
    ) : LessonsState
}

@Factory
class LessonsViewModel(
    private val gamesRepository: GamesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LessonsState>(LessonsState.Loading)
    val state: StateFlow<LessonsState> = _state.asStateFlow()

    init {
        onEvent(LessonsEvent.LoadData)
    }

    fun onEvent(event: LessonsEvent) {
        when (event) {
            is LessonsEvent.LoadData -> loadData()
            is LessonsEvent.MarkWordViewed -> markWordViewed(event.wordId)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = LessonsState.Loading
            val words = gamesRepository.getWords()
            val viewedIds = gamesRepository.getViewedWordIds()
            val grouped = words.groupBy { it.category }
            val progress = LessonProgress(viewedWordIds = viewedIds)

            _state.value = LessonsState.Loaded(groupedWords = grouped, progress = progress)
        }
    }

    private fun markWordViewed(wordId: String) {
        val current = _state.value as? LessonsState.Loaded ?: return
        gamesRepository.markWordViewed(wordId)
        val newProgress = current.progress.copyWithViewed(wordId)
        _state.value = current.copy(progress = newProgress)
    }
}
