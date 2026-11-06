package ru.luxus.votes.features.auth.domain

/**
 * Domain model of the signed-in user. Persistence lives in the
 * `core.database.entity.UserEntity` row; this model deliberately
 * exposes no password or session flags.
 */
data class UserModel(
    val email: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val age: Int = 7,
    val role: String = "Ученик",
    val avatar: String = "🦊"
)
