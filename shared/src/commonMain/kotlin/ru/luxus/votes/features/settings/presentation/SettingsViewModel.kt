package ru.luxus.votes.features.settings.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _settings = MutableStateFlow(repository.getSettings())
    val settings: StateFlow<SettingsEntity> = _settings.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleSound -> toggleSound(event.enabled)
            is SettingsEvent.ToggleNotifications -> toggleNotifications(event.enabled)
            is SettingsEvent.ToggleGeolocation -> toggleGeolocation(event.enabled)
        }
    }

    private fun toggleSound(enabled: Boolean) {
        val updated = _settings.value.copy(soundEnabled = enabled)
        repository.updateSettings(updated)
        _settings.value = updated
    }

    private fun toggleNotifications(enabled: Boolean) {
        val updated = _settings.value.copy(notificationsEnabled = enabled)
        repository.updateSettings(updated)
        _settings.value = updated
    }

    private fun toggleGeolocation(enabled: Boolean) {
        val updated = _settings.value.copy(geolocationEnabled = enabled)
        repository.updateSettings(updated)
        _settings.value = updated
    }
}
