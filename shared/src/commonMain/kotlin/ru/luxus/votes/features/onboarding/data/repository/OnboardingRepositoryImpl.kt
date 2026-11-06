package ru.luxus.votes.features.onboarding.data.repository

import org.koin.core.annotation.Singleton
import ru.luxus.votes.core.database.AppDatabase
import ru.luxus.votes.core.database.entity.SettingsEntity
import ru.luxus.votes.features.auth.domain.UserModel
import ru.luxus.votes.features.auth.domain.repository.AuthRepository
import ru.luxus.votes.features.onboarding.domain.repository.OnboardingRepository

/**
 * Persists onboarding completion:
 * - shared app onboarding: once per installation (settings table);
 * - parent onboarding: once per parent account (users table);
 * - child onboarding: once per child profile (child table).
 */
@Singleton(binds = [OnboardingRepository::class])
class OnboardingRepositoryImpl(private val db: AppDatabase) : OnboardingRepository {

    private val settingsDao = db.settingsDao()
    private val userDao = db.userDao()
    private val childDao = db.childDao()

    override suspend fun isAppOnboardingDone(): Boolean = settingsDao.get()?.appOnboardingDone ?: false

    override suspend fun markAppOnboardingDone() {
        val current = settingsDao.get() ?: SettingsEntity()
        settingsDao.upsert(current.copy(appOnboardingDone = true))
    }

    override suspend fun isAccountOnboardingDone(user: UserModel): Boolean = when (user.role) {
        AuthRepository.ROLE_PARENT -> userDao.isOnboardingDone(user.email) ?: false
        // Child sessions run on the active child profile
        else -> childDao.getActiveChild()?.onboardingDone ?: true
    }

    override suspend fun markAccountOnboardingDone(user: UserModel) {
        if (user.role == AuthRepository.ROLE_PARENT) {
            userDao.markOnboardingDone(user.email)
        } else {
            childDao.getActiveChild()?.let { child -> childDao.markChildOnboardingDone(child.id) }
        }
    }
}
