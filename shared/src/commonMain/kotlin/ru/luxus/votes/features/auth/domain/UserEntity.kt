package ru.luxus.votes.features.auth.domain

data class UserEntity(
    val email: String,
    val firstName: String,
    val lastName: String,
    val gender: String
)
