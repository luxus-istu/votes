package ru.luxus.votes.features.auth.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single
import ru.luxus.votes.features.auth.domain.UserEntity

/**
 * Local session repository. Keeps the currently authenticated user in memory and
 * persists the session through [AuthRepository] (which is backed by Room).
 */
@Single
class SessionRepository(private val authRepository: AuthRepository) {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    suspend fun restore(): UserEntity? {
        val user = authRepository.checkSession()
        _currentUser.value = user
        return user
    }

    /** Parent login: email + password */
    suspend fun login(email: String, password: String): Result<UserEntity> =
        authRepository.login(email, password).onSuccess { _currentUser.value = it }

    /** Parent registration ("Я родитель") */
    suspend fun registerParent(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): Result<UserEntity> =
        authRepository.registerParent(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
        ).onSuccess { _currentUser.value = it }

    /** Direct session set (kept for session bookkeeping) */
    fun set(user: UserEntity?) {
        _currentUser.value = user
    }

    suspend fun updateProfile(
        email: String,
        firstName: String,
        lastName: String,
        age: Int,
        role: String,
        avatar: String,
    ): Result<UserEntity> = authRepository.updateProfile(
        email = email,
        firstName = firstName,
        lastName = lastName,
        age = age,
        role = role,
        avatar = avatar,
    ).onSuccess { _currentUser.value = it }

    suspend fun logout() {
        authRepository.logout()
        _currentUser.value = null
    }
}
