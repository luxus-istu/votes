package ru.luxus.votes.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "ChildProfileTable")
data class ChildProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lastName: String = "",
    val age: Int = 7,
    val avatar: String = "🦊",
    val targetWordsPerDay: Int = 5,
    val createdAt: String,
    val isActive: Boolean = false,
    // Full date of birth (dd.MM.yyyy) – age on cards is derived from it
    val birthDate: String = "",
    // Parent account that owns this child profile (empty for legacy/device-local profiles)
    val parentEmail: String = "",
    // Child's own login credentials, set by the parent on the dashboard
    val childEmail: String = "",
    val childPassword: String = "",
    // Onboarding for this child account, counted separately per child
    val onboardingDone: Boolean = false,
)
