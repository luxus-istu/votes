package ru.luxus.votes.features.auth.domain

data class UserEntity(
    val email: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val age: Int = 7,
    val role: String = "Ученик",
    val avatar: String = "🦊"
)
