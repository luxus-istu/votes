package ru.luxus.votes.features.parent.domain

import ru.luxus.votes.features.games.domain.WordErrorEntry

data class ChildProfileModel(
    val id: String,
    val name: String,
    val lastName: String = "",
    val age: Int,
    val avatar: String,
    val targetWordsPerDay: Int = 5,
    val createdAt: String,
    val isActive: Boolean = false,
    val birthDate: String = "",
    val parentEmail: String = "",
    val childEmail: String = "",
    val wordsLearnedCount: Int = 0,
    val totalWordsAvailable: Int = 75,
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val accuracy: Float = 0f,
    val topErrors: List<WordErrorEntry> = emptyList(),
    val gameStatsMap: Map<String, Pair<Int, Int>> = emptyMap(), // gameKey -> Pair(answered, correct)
    val topicMastery: Map<String, Pair<Int, Int>> = emptyMap() // category -> Pair(learned, total)
)

/** Friendly name for the daily learning pace (replaces "words per day" wording). */
fun learningPaceLabel(wordsPerDay: Int): String = when {
    wordsPerDay <= 3 -> "Спокойный"
    wordsPerDay <= 5 -> "Обычный"
    else -> "Интенсивный"
}