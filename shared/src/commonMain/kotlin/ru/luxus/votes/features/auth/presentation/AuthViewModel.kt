package ru.luxus.votes.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import ru.luxus.votes.features.auth.data.AuthRepository
import ru.luxus.votes.features.auth.domain.UserEntity

sealed interface AuthEvent {
    data object CheckSession : AuthEvent
    data class SubmitLogin(val email: String, val password: String) : AuthEvent
    data class SubmitRegister(
        val email: String,
        val password: String,
        val firstName: String,
        val lastName: String,
        val gender: String
    ) : AuthEvent
    data object SubmitAnonymous : AuthEvent
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
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Initial)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.CheckSession -> checkSession()
            is AuthEvent.SubmitLogin -> submitLogin(event.email, event.password)
            is AuthEvent.SubmitRegister -> submitRegister(
                event.email,
                event.password,
                event.firstName,
                event.lastName,
                event.gender
            )
            is AuthEvent.SubmitAnonymous -> submitAnonymous()
            is AuthEvent.Logout -> logout()
            is AuthEvent.ClearError -> clearError()
        }
    }

    private fun checkSession() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val user = repository.checkSession()
            if (user != null) {
                _state.value = AuthState.Authenticated(user)
            } else {
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
                    _state.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    _state.value = AuthState.Error(error.message ?: "Ошибка входа")
                }
        }
    }

    private fun submitRegister(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        gender: String
    ) {
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            _state.value = AuthState.Error("Все поля обязательны")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.register(
                email = email.trim(),
                password = password,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                gender = gender.ifBlank { "Не указан" }
            ).onSuccess { user ->
                _state.value = AuthState.Authenticated(user)
            }.onFailure { error ->
                _state.value = AuthState.Error(error.message ?: "Ошибка регистрации")
            }
        }
    }

    private fun submitAnonymous() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.loginAnonymous().onSuccess { user ->
                _state.value = AuthState.Authenticated(user)
            }.onFailure { error ->
                _state.value = AuthState.Error(error.message ?: "Ошибка анонимного входа")
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repository.logout()
            _state.value = AuthState.Initial
        }
    }

    private fun clearError() {
        if (_state.value is AuthState.Error) {
            _state.value = AuthState.Initial
        }
    }
}
