package ru.luxus.votes.features.settings.data

import org.koin.core.annotation.Single
import ru.luxus.votes.core.database.AppDatabase
import ru.luxus.votes.features.settings.domain.SettingsEntity
import ru.luxus.votes.core.database.entity.SettingsEntity as SettingsRow

@Single
class SettingsRepository(private val db: AppDatabase) {

    private val settingsDao = db.settingsDao()

    suspend fun getSettings(): SettingsEntity {
        val row = settingsDao.get() ?: return SettingsEntity()
        return SettingsEntity(
            soundEnabled = row.soundEnabled,
            notificationsEnabled = row.notificationsEnabled,
            geolocationEnabled = row.geolocationEnabled,
        )
    }

    suspend fun updateSettings(settings: SettingsEntity) {
        val existing = settingsDao.get()
        settingsDao.upsert(
            SettingsRow(
                soundEnabled = settings.soundEnabled,
                notificationsEnabled = settings.notificationsEnabled,
                geolocationEnabled = settings.geolocationEnabled,
                // Device onboarding flag must survive settings edits
                appOnboardingDone = existing?.appOnboardingDone ?: false,
            )
        )
    }
}
