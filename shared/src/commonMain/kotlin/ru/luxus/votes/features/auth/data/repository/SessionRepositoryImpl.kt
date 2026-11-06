package ru.luxus.votes.features.auth.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Singleton
import ru.luxus.votes.features.auth.domain.UserModel
import ru.luxus.votes.features.auth.domain.repository.AuthRepository
import ru.luxus.votes.features.auth.domain.repository.SessionRepository

/**
 * Local session repository. Keeps the currently authenticated user in memory and
 * persists the session through [AuthRepository] (which is backed by Room).
 */
@Singleton(binds = [SessionRepository::class])
class SessionRepositoryImpl(private val authRepository: AuthRepository) : SessionRepository {

    private val _currentUser = MutableStateFlow<UserModel?>(null)
    override val currentUser: StateFlow<UserModel?> = _currentUser.asStateFlow()

    override suspend fun restore(): UserModel? {
        val user = authRepository.checkSession()
        _currentUser.value = user
        return user
    }

    /** Parent login: email + password */
    override suspend fun login(email: String, password: String): Result<UserModel> =
        authRepository.login(email, password).onSuccess { _currentUser.value = it }

    /** Parent registration ("Я родитель") */
    override suspend fun registerParent(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): Result<UserModel> =
        authRepository.registerParent(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
        ).onSuccess { _currentUser.value = it }

    /** Direct session set (kept for session bookkeeping) */
    override fun set(user: UserModel?) {
        _currentUser.value = user
    }

    override suspend fun updateProfile(
        email: String,
        firstName: String,
        lastName: String,
        age: Int,
        role: String,
        avatar: String,
    ): Result<UserModel> = authRepository.updateProfile(
        email = email,
        firstName = firstName,
        lastName = lastName,
        age = age,
        role = role,
        avatar = avatar,
    ).onSuccess { _currentUser.value = it }

    override suspend fun logout() {
        authRepository.logout()
        _currentUser.value = null
    }
}
