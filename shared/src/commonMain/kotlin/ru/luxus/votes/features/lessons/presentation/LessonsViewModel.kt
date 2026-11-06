package ru.luxus.votes.features.lessons.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import ru.luxus.votes.core.sound.SoundPlayer
import ru.luxus.votes.features.games.domain.repository.GamesRepository
import ru.luxus.votes.features.games.domain.WordEntry
import ru.luxus.votes.features.lessons.domain.LessonProgress
import ru.luxus.votes.features.lessons.domain.model.UdmurtLetterModel
import ru.luxus.votes.features.lessons.domain.repository.WordRepository
import ru.luxus.votes.features.settings.domain.repository.SettingsRepository

sealed interface LessonsEvent {
    data object LoadData : LessonsEvent
    data class MarkWordViewed(val wordId: String) : LessonsEvent
    data class PlayAudio(val text: String, val phonetic: String = "") : LessonsEvent
}

sealed interface LessonsState {
    data object Loading : LessonsState
    data class Loaded(
        val letters: List<UdmurtLetterModel> = emptyList(),
        val specialLetters: Set<String> = emptySet(),
        val groupedWords: Map<String, List<WordEntry>> = emptyMap(),
        val progress: LessonProgress = LessonProgress(),
    ) : LessonsState
    data class Error(val message: String) : LessonsState
}

@KoinViewModel
class LessonsViewModel(
    private val gamesRepository: GamesRepository,
    private val soundPlayer: SoundPlayer,
    private val settingsRepository: SettingsRepository,
    private val wordRepository: WordRepository,
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
            is LessonsEvent.PlayAudio -> playAudio(event.text, event.phonetic)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = LessonsState.Loading
            try {
                val grouped = gamesRepository.getWords().groupBy { it.category }
                val viewedIds = gamesRepository.getViewedWordIdsForActiveChild()
                val letters = wordRepository.getAllLetters()
                val specialLetters = wordRepository.getAllSpecialLetters()

                _state.value = LessonsState.Loaded(
                    letters = letters,
                    specialLetters = specialLetters,
                    groupedWords = grouped,
                    progress = LessonProgress(viewedIds),
                )
            } catch (e: Exception) {
                _state.value = LessonsState.Error(e.message ?: "Не удалось загрузить слова")
            }
        }
    }

    private fun markWordViewed(wordId: String) {
        viewModelScope.launch {
            // Optimistic update so the checkmark appears instantly on the card
            _state.update { current ->
                (current as? LessonsState.Loaded)?.copy(
                    progress = current.progress.copyWithViewed(wordId)
                ) ?: current
            }

            gamesRepository.markWordViewed(wordId)
            if (settingsRepository.getSettings().soundEnabled) {
                soundPlayer.playSuccess()
            }
        }
    }

    private fun playAudio(text: String, phonetic: String = "") {
        viewModelScope.launch {
            if (settingsRepository.getSettings().soundEnabled) {
                soundPlayer.playPronunciation(text, phonetic)
            }
        }
    }
}
