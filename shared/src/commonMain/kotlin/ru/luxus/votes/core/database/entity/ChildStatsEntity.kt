package ru.luxus.votes.core.database.entity

import androidx.room3.Entity

@Entity(tableName = "ChildStatsTable", primaryKeys = ["childId", "gameKey"])
data class ChildStatsEntity(
    val childId: String,
    val gameKey: String,
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
)
