package ru.luxus.votes.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "DailyProgressTable")
data class DailyProgressEntity(
    @PrimaryKey val date: String,
    val wordsLearnedCount: Int = 0,
    val gamesPlayedCount: Int = 0,
)
