package ru.luxus.votes.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "WordErrorTable")
data class WordErrorEntity(
    @PrimaryKey val wordId: String,
    val udmurt: String,
    val russian: String,
    val errorCount: Int = 0,
    val successCount: Int = 0,
)
