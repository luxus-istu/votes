package ru.luxus.votes.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import ru.luxus.votes.features.auth.data.AuthRepository
import ru.luxus.votes.features.auth.data.SessionRepository
import ru.luxus.votes.features.auth.domain.UserEntity

sealed interface AuthEvent {
    data object CheckSession : AuthEvent

    data class SubmitLogin(val email: String, val password: String) : AuthEvent
    data class SubmitRegisterParent(
        val email: String,
        val password: String,
        val firstName: String,
        val lastName: String,
    ) : AuthEvent

    data class UpdateProfile(
        val firstName: String,
        val lastName: String,
        val age: Int,
        val role: String,
        val avatar: String
    ) : AuthEvent
    data object Logout : AuthEvent
    data object ClearError : AuthEvent
}

sealed interface AuthState {
    data object Initial : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: UserEntity) : AuthState
    data class Error(val message: String) : AuthState
}

@Factory
class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Initial)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.CheckSession -> checkSession()
            is AuthEvent.SubmitLogin -> submitLogin(event.email, event.password)
            is AuthEvent.SubmitRegisterParent -> submitRegisterParent(
                event.email,
                event.password,
                event.firstName,
                event.lastName
            )
            is AuthEvent.UpdateProfile -> updateProfile(
                event.firstName,
                event.lastName,
                event.age,
                event.role,
                event.avatar
            )
            is AuthEvent.Logout -> logout()
            is AuthEvent.ClearError -> clearError()
        }
    }

    private fun checkSession() {
        viewModelScope.launch {
            val user = repository.checkSession()
            if (user != null) {
                sessionRepository.set(user)
                _state.value = AuthState.Authenticated(user)
            } else if (_state.value !is AuthState.Authenticated) {
                _state.value = AuthState.Initial
            }
        }
    }

    private fun submitLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Поля не могут быть пустыми")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.login(email.trim(), password)
                .onSuccess { user ->
                    sessionRepository.set(user)
                    _state.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    _state.value = AuthState.Error(error.message ?: "Ошибка входа")
                }
        }
    }

    private fun submitRegisterParent(email: String, password: String, firstName: String, lastName: String) {
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            _state.value = AuthState.Error("Все обязательные поля должны быть заполнены")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.registerParent(
                email = email.trim(),
                password = password,
                firstName = firstName.trim(),
                lastName = lastName.trim()
            ).onSuccess { user ->
                sessionRepository.set(user)
                _state.value = AuthState.Authenticated(user)
            }.onFailure { error ->
                _state.value = AuthState.Error(error.message ?: "Ошибка регистрации")
            }
        }
    }

    private fun updateProfile(
        firstName: String,
        lastName: String,
        age: Int,
        role: String,
        avatar: String
    ) {
        val current = (_state.value as? AuthState.Authenticated)?.user ?: return
        // A child session must always stay in the child branch, even if another
        // role is selected in the profile editor
        val roleToSave = if (current.role == AuthRepository.ROLE_CHILD) {
            AuthRepository.ROLE_CHILD
        } else {
            role
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.updateProfile(
                email = current.email,
                firstName = firstName,
                lastName = lastName,
                age = age,
                role = roleToSave,
                avatar = avatar
            ).onSuccess { updatedUser ->
                sessionRepository.set(updatedUser)
                _state.value = AuthState.Authenticated(updatedUser)
            }.onFailure { error ->
                _state.value = AuthState.Error(error.message ?: "Не удалось обновить профиль")
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.logout()
            sessionRepository.set(null)
            _state.value = AuthState.Initial
        }
    }

    private fun clearError() {
        if (_state.value is AuthState.Error) {
            _state.value = AuthState.Initial
        }
    }
}
