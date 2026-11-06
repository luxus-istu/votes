package ru.luxus.votes.features.onboarding.domain.repository

import ru.luxus.votes.features.auth.domain.UserModel

interface OnboardingRepository {

    suspend fun isAppOnboardingDone(): Boolean

    suspend fun markAppOnboardingDone()

    suspend fun isAccountOnboardingDone(user: UserModel): Boolean

    suspend fun markAccountOnboardingDone(user: UserModel)
}