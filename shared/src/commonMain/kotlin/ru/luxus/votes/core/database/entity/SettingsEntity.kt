package ru.luxus.votes.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "SettingsTable")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val geolocationEnabled: Boolean = false,
    // Device-wide onboarding (shown once per app installation)
    val appOnboardingDone: Boolean = false,
)
