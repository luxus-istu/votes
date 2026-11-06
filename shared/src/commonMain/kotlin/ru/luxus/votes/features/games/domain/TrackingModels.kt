package ru.luxus.votes.features.games.domain

data class WordErrorEntry(
    val wordId: String,
    val udmurt: String,
    /** Stored transcription, so error lists can play the word exactly like the vocabulary does. */
    val transcription: String = "",
    val russian: String,
    val errorCount: Int,
    val successCount: Int
)

data class DailyProgressEntry(
    val date: String,
    val wordsLearnedCount: Int,
    val gamesPlayedCount: Int
)
