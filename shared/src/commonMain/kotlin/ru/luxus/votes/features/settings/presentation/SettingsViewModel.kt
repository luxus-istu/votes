package ru.luxus.votes.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import ru.luxus.votes.features.settings.data.SettingsRepository
import ru.luxus.votes.features.settings.domain.SettingsEntity

sealed interface SettingsEvent {
    data class ToggleSound(val enabled: Boolean) : SettingsEvent
    data class ToggleNotifications(val enabled: Boolean) : SettingsEvent
    data class ToggleGeolocation(val enabled: Boolean) : SettingsEvent
}

@Factory
class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(SettingsEntity())
    val settings: StateFlow<SettingsEntity> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            _settings.value = repository.getSettings()
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleSound -> toggleSound(event.enabled)
            is SettingsEvent.ToggleNotifications -> toggleNotifications(event.enabled)
            is SettingsEvent.ToggleGeolocation -> toggleGeolocation(event.enabled)
        }
    }

    private fun toggleSound(enabled: Boolean) {
        val updated = _settings.value.copy(soundEnabled = enabled)
        _settings.value = updated
        viewModelScope.launch { repository.updateSettings(updated) }
    }

    private fun toggleNotifications(enabled: Boolean) {
        val updated = _settings.value.copy(notificationsEnabled = enabled)
        _settings.value = updated
        viewModelScope.launch { repository.updateSettings(updated) }
    }

    private fun toggleGeolocation(enabled: Boolean) {
        val updated = _settings.value.copy(geolocationEnabled = enabled)
        _settings.value = updated
        viewModelScope.launch { repository.updateSettings(updated) }
    }
}
