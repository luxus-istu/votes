package ru.luxus.votes.features.games.domain.repository

import ru.luxus.votes.features.games.domain.DailyProgressEntry
import ru.luxus.votes.features.games.domain.GameStats
import ru.luxus.votes.features.games.domain.WordEntry
import ru.luxus.votes.features.games.domain.WordErrorEntry

interface GamesRepository {

    val defaultWords: List<WordEntry>

    suspend fun getWords(): List<WordEntry>

    suspend fun getWordsByCategory(category: String): List<WordEntry>

    suspend fun getCategories(): List<String>

    suspend fun getWordOfTheDay(): WordEntry

    suspend fun getStats(): GameStats

    suspend fun getStatsForActiveChild(): GameStats

    suspend fun getTopErrorWordsForActiveChild(): List<WordErrorEntry>

    suspend fun saveResult(gameKey: String, isCorrect: Boolean, word: WordEntry? = null)

    suspend fun getViewedWordIds(): Set<String>

    suspend fun getViewedWordIdsForActiveChild(): Set<String>

    suspend fun markWordViewed(wordId: String)

    suspend fun getTopErrorWords(): List<WordErrorEntry>

    suspend fun getDailyProgress(): List<DailyProgressEntry>

    suspend fun getDailyProgressForActiveChild(): List<DailyProgressEntry>

    suspend fun resetAllProgress()
}