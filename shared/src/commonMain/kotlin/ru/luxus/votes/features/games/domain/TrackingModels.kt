package ru.luxus.votes.features.games.domain

data class WordErrorEntry(
    val wordId: String,
    val udmurt: String,
    val russian: String,
    val errorCount: Int,
    val successCount: Int
)

data class DailyProgressEntry(
    val date: String,
    val wordsLearnedCount: Int,
    val gamesPlayedCount: Int
)
