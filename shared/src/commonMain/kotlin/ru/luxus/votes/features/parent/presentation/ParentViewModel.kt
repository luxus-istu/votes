package ru.luxus.votes.features.parent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import ru.luxus.votes.core.sound.SoundPlayer
import ru.luxus.votes.features.auth.data.AuthRepository
import ru.luxus.votes.features.parent.data.ParentRepository
import ru.luxus.votes.features.parent.domain.ChildProfileModel
import ru.luxus.votes.features.parent.domain.learningPaceLabel

data class ParentState(
    val children: List<ChildProfileModel> = emptyList(),
    val selectedChildId: String? = null,
    val reportCopiedNotification: Boolean = false,
    val notification: String? = null,
    val formSaved: Boolean = false
) {
    val selectedChild: ChildProfileModel?
        get() = children.firstOrNull { it.id == selectedChildId }
            ?: children.firstOrNull { it.isActive }
            ?: children.firstOrNull()

    val totalLearnedAcrossAll: Int
        get() = children.sumOf { it.wordsLearnedCount }

    val averageAccuracyAcrossAll: Float
        get() {
            val withAttempts = children.filter { it.totalAnswered > 0 }
            return if (withAttempts.isNotEmpty()) withAttempts.map { it.accuracy }.average().toFloat() else 0.85f
        }
}

sealed interface ParentEvent {
    data class SelectChild(val id: String) : ParentEvent
    data class AddChild(
        val name: String,
        val lastName: String,
        val birthDate: String,
        val avatar: String,
        val targetWords: Int,
        val childEmail: String,
        val childPassword: String,
    ) : ParentEvent
    data class UpdateChild(
        val id: String,
        val name: String,
        val lastName: String,
        val birthDate: String,
        val avatar: String,
        val targetWords: Int,
        val childEmail: String,
        val childPassword: String,
    ) : ParentEvent
    data class DeleteChild(val id: String) : ParentEvent
    data class PlayAudio(val udmurt: String, val transcription: String) : ParentEvent
    data object DismissNotification : ParentEvent
    data object ConsumeFormSaved : ParentEvent
}

@Single
class ParentViewModel(
    private val repository: ParentRepository,
    private val authRepository: AuthRepository,
    private val soundPlayer: SoundPlayer
) : ViewModel() {

    private val _state = MutableStateFlow(ParentState())
    val state: StateFlow<ParentState> = _state.asStateFlow()

    init {
        loadChildren()
    }

    fun loadChildren() {
        viewModelScope.launch { refreshChildren() }
    }

    private suspend fun refreshChildren() {
        val parentEmail = authRepository.checkSession()?.email
        val list = if (parentEmail.isNullOrBlank()) {
            repository.getAllChildren()
        } else {
            repository.getChildrenForParent(parentEmail)
        }
        _state.update { current ->
            val ids = list.map { it.id }.toSet()
            val selId = listOfNotNull(
                current.selectedChildId,
                list.firstOrNull { it.isActive }?.id,
                list.firstOrNull()?.id
            ).firstOrNull { it in ids }
            current.copy(
                children = list,
                selectedChildId = selId
            )
        }
    }

    fun onEvent(event: ParentEvent) {
        when (event) {
            is ParentEvent.SelectChild -> {
                _state.update { it.copy(selectedChildId = event.id) }
            }
            is ParentEvent.AddChild -> {
                viewModelScope.launch {
                    if (repository.isChildEmailTaken(event.childEmail)) {
                        _state.update {
                            it.copy(notification = "Этот email уже используется другим профилем")
                        }
                        soundPlayer.playError()
                        return@launch
                    }
                    val parentEmail = authRepository.checkSession()?.email.orEmpty()
                    repository.addChild(
                        name = event.name,
                        lastName = event.lastName,
                        birthDate = event.birthDate,
                        avatar = event.avatar,
                        targetWordsPerDay = event.targetWords,
                        parentEmail = parentEmail,
                        childEmail = event.childEmail,
                        childPassword = event.childPassword,
                    )
                    refreshChildren()
                    _state.update { it.copy(formSaved = true) }
                    soundPlayer.playSuccess()
                }
            }
            is ParentEvent.UpdateChild -> {
                viewModelScope.launch {
                    if (repository.isChildEmailTaken(event.childEmail, excludeChildId = event.id)) {
                        _state.update {
                            it.copy(notification = "Этот email уже используется другим профилем")
                        }
                        soundPlayer.playError()
                        return@launch
                    }
                    repository.updateChild(
                        id = event.id,
                        name = event.name,
                        lastName = event.lastName,
                        birthDate = event.birthDate,
                        avatar = event.avatar,
                        targetWordsPerDay = event.targetWords,
                    )
                    repository.updateChildCredentials(event.id, event.childEmail, event.childPassword)
                    refreshChildren()
                    _state.update { it.copy(formSaved = true) }
                    soundPlayer.playSuccess()
                }
            }
            is ParentEvent.DeleteChild -> {
                viewModelScope.launch {
                    repository.deleteChild(event.id)
                    refreshChildren()
                    _state.update { it.copy(formSaved = true) }
                }
            }
            is ParentEvent.PlayAudio -> {
                viewModelScope.launch {
                    soundPlayer.playPronunciation(event.udmurt, event.transcription)
                }
            }
            is ParentEvent.DismissNotification -> {
                _state.update { it.copy(reportCopiedNotification = false, notification = null) }
            }
            is ParentEvent.ConsumeFormSaved -> {
                _state.update { it.copy(formSaved = false) }
            }
        }
    }

    fun generateTeacherReport(child: ChildProfileModel): String {
        val gameNames = mapOf(
            "translate" to "Выбери перевод",
            "oddoneout" to "Найди лишнее слово",
            "sorting" to "Сортировка",
            "memory" to "Найди пару (Memory)",
            "compile" to "Собери слово",
            "truefalse" to "Правда / Ложь"
        )

        val gamesText = child.gameStatsMap.entries.joinToString("\n") { (key, pair) ->
            val name = gameNames[key] ?: key
            val pct = if (pair.first > 0) (pair.second * 100 / pair.first) else 0
            "  • $name: ${pair.second} из ${pair.first} верных ($pct%)"
        }

        val errorsText = if (child.topErrors.isNotEmpty()) {
            child.topErrors.take(5).joinToString(", ") { "${it.udmurt} (${it.russian}): ${it.errorCount} ош." }
        } else {
            "Трудностей не зафиксировано, материал усваивается уверенно"
        }

        val masteryPct = if (child.totalWordsAvailable > 0) (child.wordsLearnedCount * 100 / child.totalWordsAvailable) else 0

        return """
УЧЕБНЫЙ ОТЧЁТ ПО УДМУРТСКОМУ ЯЗЫКУ
Приложение: «Ӟеч-а бур-а удмурт кыл?»
Программа: Сохранение и развитие удмуртского языка (Постановление № 748)

Ученик: ${listOf(child.name, child.lastName).filter { it.isNotBlank() }.joinToString(" ")} (${child.age} лет, талисман: ${child.avatar})
Дата рождения: ${child.birthDate.ifBlank { "не указана" }}
Темп обучения: ${learningPaceLabel(child.targetWordsPerDay).lowercase()}
Дата регистрации: ${child.createdAt}

ОСВОЕНИЕ ПРОГРАММЫ (1-Й ЭТАП):
  • Изучено слов: ${child.wordsLearnedCount} из ${child.totalWordsAvailable} ($masteryPct%)
  • Общая точность ответов: ${(child.accuracy * 100).toInt()}% (${child.totalCorrect} из ${child.totalAnswered})

РЕЗУЛЬТАТЫ ПО МИНИ-ИГРАМ:
$gamesText

СЛОВА ДЛЯ ДОПОЛНИТЕЛЬНОГО ПОВТОРЕНИЯ:
  $errorsText

МЕТОДИЧЕСКАЯ РЕКОМЕНДАЦИЯ РОДИТЕЛЮ И УЧИТЕЛЮ:
  Ребёнку рекомендуется закрепить удмуртские буквы (ӝ, ӟ, ӥ, ӧ, ӵ) и уделить 5-10 минут игре «Собери слово» и «Сортировка».
        """.trimIndent()
    }
}
