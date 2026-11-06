package ru.luxus.votes.features.auth.domain.repository

import ru.luxus.votes.features.auth.domain.UserModel

interface AuthRepository {

    suspend fun checkSession(): UserModel?

    suspend fun login(email: String, password: String): Result<UserModel>

    suspend fun registerParent(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): Result<UserModel>

    suspend fun updateProfile(
        email: String,
        firstName: String,
        lastName: String,
        age: Int,
        role: String,
        avatar: String,
    ): Result<UserModel>

    suspend fun logout()

    companion object {
        const val ROLE_PARENT = "Родитель"
        const val ROLE_CHILD = "Ученик"
    }
}