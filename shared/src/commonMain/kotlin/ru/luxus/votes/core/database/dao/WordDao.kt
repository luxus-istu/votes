package ru.luxus.votes.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import ru.luxus.votes.core.database.entity.WordEntity

@Dao
interface WordDao {

    @Query("SELECT * FROM WordTable")
    suspend fun getAll(): List<WordEntity>

    @Query("SELECT * FROM WordTable WHERE category = :category")
    suspend fun getByCategory(category: String): List<WordEntity>

    @Upsert
    suspend fun upsert(word: WordEntity)

    @Upsert
    suspend fun upsertAll(words: List<WordEntity>)
}
