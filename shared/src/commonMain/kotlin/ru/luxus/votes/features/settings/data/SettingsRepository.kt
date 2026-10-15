package ru.luxus.votes.features.settings.data

import org.koin.core.annotation.Single
import ru.luxus.votes.db.VotesDatabase
import ru.luxus.votes.features.settings.domain.SettingsEntity

@Single
class SettingsRepository(private val db: VotesDatabase) {

    fun getSettings(): SettingsEntity {
        val row = db.votesDatabaseQueries.getSettings().executeAsOneOrNull()
            ?: return SettingsEntity()

        return SettingsEntity(
            soundEnabled = row.soundEnabled == 1L,
            notificationsEnabled = row.notificationsEnabled == 1L,
            geolocationEnabled = row.geolocationEnabled == 1L
        )
    }

    fun updateSettings(settings: SettingsEntity) {
        db.votesDatabaseQueries.insertOrUpdateSettings(
            soundEnabled = if (settings.soundEnabled) 1 else 0,
            notificationsEnabled = if (settings.notificationsEnabled) 1 else 0,
            geolocationEnabled = if (settings.geolocationEnabled) 1 else 0
        )
    }
}
