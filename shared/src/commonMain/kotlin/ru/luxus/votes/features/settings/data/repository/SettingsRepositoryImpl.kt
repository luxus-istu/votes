package ru.luxus.votes.features.settings.data.repository

import org.koin.core.annotation.Singleton
import ru.luxus.votes.core.database.AppDatabase
import ru.luxus.votes.core.database.entity.SettingsEntity
import ru.luxus.votes.features.settings.domain.SettingsModel
import ru.luxus.votes.features.settings.domain.repository.SettingsRepository

@Singleton(binds = [SettingsRepository::class])
class SettingsRepositoryImpl(private val db: AppDatabase) : SettingsRepository {

    private val settingsDao = db.settingsDao()

    override suspend fun getSettings(): SettingsModel {
        val row = settingsDao.get() ?: return SettingsModel()
        return SettingsModel(
            soundEnabled = row.soundEnabled,
            notificationsEnabled = row.notificationsEnabled,
            geolocationEnabled = row.geolocationEnabled,
        )
    }

    override suspend fun updateSettings(settings: SettingsModel) {
        val existing = settingsDao.get()
        settingsDao.upsert(
            SettingsEntity(
                soundEnabled = settings.soundEnabled,
                notificationsEnabled = settings.notificationsEnabled,
                geolocationEnabled = settings.geolocationEnabled,
                // Device onboarding flag and the saved child session must survive edits
                appOnboardingDone = existing?.appOnboardingDone ?: false,
                currentChildId = existing?.currentChildId.orEmpty(),
            )
        )
    }
}
