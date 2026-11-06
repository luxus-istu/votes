package ru.luxus.votes.features.settings.domain.repository

import ru.luxus.votes.features.settings.domain.SettingsModel

interface SettingsRepository {

    suspend fun getSettings(): SettingsModel

    suspend fun updateSettings(settings: SettingsModel)
}