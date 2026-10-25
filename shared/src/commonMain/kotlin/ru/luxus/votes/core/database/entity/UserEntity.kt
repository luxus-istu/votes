package ru.luxus.votes.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "UserTable")
data class UserEntity(
    @PrimaryKey val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val age: Int = 7,
    val role: String = "Ученик",
    val avatar: String = "🦊",
    val isCurrent: Boolean = false,
    // Role-specific onboarding, counted once per parent account
    val onboardingDone: Boolean = false,
)
