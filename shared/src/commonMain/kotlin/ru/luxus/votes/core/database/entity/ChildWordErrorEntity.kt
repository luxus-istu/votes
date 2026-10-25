package ru.luxus.votes.core.database.entity

import androidx.room3.Entity

@Entity(tableName = "ChildWordErrorTable", primaryKeys = ["childId", "wordId"])
data class ChildWordErrorEntity(
    val childId: String,
    val wordId: String,
    val udmurt: String,
    val russian: String,
    val errorCount: Int = 0,
    val successCount: Int = 0,
)
