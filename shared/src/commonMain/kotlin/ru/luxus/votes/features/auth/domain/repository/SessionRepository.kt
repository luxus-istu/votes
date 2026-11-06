package ru.luxus.votes.features.auth.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.luxus.votes.features.auth.domain.UserModel

interface SessionRepository {

    val currentUser: StateFlow<UserModel?>

    suspend fun restore(): UserModel?

    suspend fun login(email: String, password: String): Result<UserModel>

    suspend fun registerParent(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): Result<UserModel>

    fun set(user: UserModel?)

    suspend fun updateProfile(
        email: String,
        firstName: String,
        lastName: String,
        age: Int,
        role: String,
        avatar: String,
    ): Result<UserModel>

    suspend fun logout()
}