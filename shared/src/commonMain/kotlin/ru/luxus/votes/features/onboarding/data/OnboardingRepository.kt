package ru.luxus.votes.features.onboarding.data

import org.koin.core.annotation.Single
import ru.luxus.votes.core.database.AppDatabase
import ru.luxus.votes.core.database.entity.SettingsEntity as SettingsRow
import ru.luxus.votes.features.auth.data.AuthRepository
import ru.luxus.votes.features.auth.domain.UserEntity

/**
 * Persists onboarding completion:
 * - shared app onboarding: once per installation (settings table);
 * - parent onboarding: once per parent account (users table);
 * - child onboarding: once per child profile (child table).
 */
@Single
class OnboardingRepository(private val db: AppDatabase) {

    private val settingsDao = db.settingsDao()
    private val userDao = db.userDao()
    private val childDao = db.childDao()

    suspend fun isAppOnboardingDone(): Boolean = settingsDao.get()?.appOnboardingDone ?: false

    suspend fun markAppOnboardingDone() {
        val current = settingsDao.get() ?: SettingsRow()
        settingsDao.upsert(current.copy(appOnboardingDone = true))
    }

    suspend fun isAccountOnboardingDone(user: UserEntity): Boolean = when (user.role) {
        AuthRepository.ROLE_PARENT -> userDao.isOnboardingDone(user.email) ?: false
        // Child sessions run on the active child profile
        else -> childDao.getActiveChild()?.onboardingDone ?: true
    }

    suspend fun markAccountOnboardingDone(user: UserEntity) {
        if (user.role == AuthRepository.ROLE_PARENT) {
            userDao.markOnboardingDone(user.email)
        } else {
            childDao.getActiveChild()?.let { child -> childDao.markChildOnboardingDone(child.id) }
        }
    }
}
