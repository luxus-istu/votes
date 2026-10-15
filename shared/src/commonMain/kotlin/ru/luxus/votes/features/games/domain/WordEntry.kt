package ru.luxus.votes.features.games.domain

data class WordEntry(
    val id: String,
    val udmurt: String,
    val transcription: String,
    val russian: String,
    val category: String,
    val emoji: String
)
