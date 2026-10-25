package ru.luxus.votes.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "WordTable")
data class WordEntity(
    @PrimaryKey val id: String,
    val udmurt: String,
    val transcription: String,
    val russian: String,
    val category: String,
    val emoji: String,
)
