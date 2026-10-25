package ru.luxus.votes.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "LessonProgressTable")
data class LessonProgressEntity(
    @PrimaryKey val wordId: String,
)
