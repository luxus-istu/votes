package ru.luxus.votes.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "GameStatsTable")
data class GameStatsEntity(
    @PrimaryKey val gameKey: String,
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
)
