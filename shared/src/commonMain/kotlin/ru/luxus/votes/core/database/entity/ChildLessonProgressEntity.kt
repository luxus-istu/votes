package ru.luxus.votes.core.database.entity

import androidx.room3.Entity

@Entity(tableName = "ChildLessonProgressTable", primaryKeys = ["childId", "wordId"])
data class ChildLessonProgressEntity(
    val childId: String,
    val wordId: String,
)
