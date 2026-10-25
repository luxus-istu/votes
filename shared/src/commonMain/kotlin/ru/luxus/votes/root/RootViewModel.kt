package ru.luxus.votes.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import ru.luxus.votes.features.auth.data.AuthRepository
import ru.luxus.votes.features.auth.data.SessionRepository
import ru.luxus.votes.features.auth.domain.UserEntity
import ru.luxus.votes.features.onboarding.data.OnboardingRepository

sealed interface RootState {
    data object Loading : RootState

    /** Shared onboarding, shown once per app installation. */
    data object AppOnboarding : RootState

    data object Unauthorized : RootState

    /** Role-specific onboarding, shown once per parent / child account. */
    data class Onboarding(val isParent: Boolean) : RootState

    data class Authorized(val isParent: Boolean) : RootState
}

@KoinViewModel
class RootViewModel(
    private val sessionRepository: SessionRepository,
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<RootState>(RootState.Loading)
    val state: StateFlow<RootState> = _state.asStateFlow()

    /** Bumped whenever onboarding is completed so the state is re-evaluated. */
    private val onboardingRevision = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            // Restore the persisted session first (no splash screen needed)
            sessionRepository.restore()
            combine(sessionRepository.currentUser, onboardingRevision) { user, _ -> user }
                .collect { user -> _state.value = resolve(user) }
        }
    }

    private suspend fun resolve(user: UserEntity?): RootState {
        if (!onboardingRepository.isAppOnboardingDone()) {
            return RootState.AppOnboarding
        }
        if (user == null) {
            return RootState.Unauthorized
        }
        val isParent = user.role == AuthRepository.ROLE_PARENT
        if (!onboardingRepository.isAccountOnboardingDone(user)) {
            return RootState.Onboarding(isParent)
        }
        return RootState.Authorized(isParent)
    }

    /** Called when the shared (first-launch) onboarding is finished or skipped. */
    fun onAppOnboardingFinished() {
        viewModelScope.launch {
            onboardingRepository.markAppOnboardingDone()
            onboardingRevision.value += 1
        }
    }

    /** Called when the parent/child account onboarding is finished or skipped. */
    fun onAccountOnboardingFinished() {
        viewModelScope.launch {
            sessionRepository.currentUser.value?.let { user ->
                onboardingRepository.markAccountOnboardingDone(user)
            }
            onboardingRevision.value += 1
        }
    }
}
