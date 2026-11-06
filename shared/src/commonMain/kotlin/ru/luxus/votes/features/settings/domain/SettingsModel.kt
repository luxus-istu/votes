package ru.luxus.votes.features.settings.domain

/**
 * User-facing app settings. Persistence lives in the
 * `core.database.entity.SettingsEntity` row.
 */
data class SettingsModel(
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val geolocationEnabled: Boolean = false
)
