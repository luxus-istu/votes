package ru.luxus.votes.features.settings.domain

data class SettingsEntity(
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val geolocationEnabled: Boolean = false
)
