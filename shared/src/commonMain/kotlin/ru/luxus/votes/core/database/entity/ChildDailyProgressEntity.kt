package ru.luxus.votes.core.database.entity

import androidx.room3.Entity

@Entity(tableName = "ChildDailyProgressTable", primaryKeys = ["childId", "date"])
data class ChildDailyProgressEntity(
    val childId: String,
    val date: String,
    val wordsLearnedCount: Int = 0,
    val gamesPlayedCount: Int = 0,
)
