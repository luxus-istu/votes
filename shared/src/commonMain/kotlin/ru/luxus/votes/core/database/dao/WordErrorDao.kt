package ru.luxus.votes.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import ru.luxus.votes.core.database.entity.WordErrorEntity

@Dao
interface WordErrorDao {

    @Query("SELECT * FROM WordErrorTable")
    suspend fun getAll(): List<WordErrorEntity>

    @Query("SELECT * FROM WordErrorTable WHERE errorCount > 0 ORDER BY errorCount DESC LIMIT 10")
    suspend fun getTopErrors(): List<WordErrorEntity>

    @Query("SELECT * FROM WordErrorTable WHERE wordId = :wordId")
    suspend fun getById(wordId: String): WordErrorEntity?

    @Upsert
    suspend fun upsert(error: WordErrorEntity)

    @Query("DELETE FROM WordErrorTable")
    suspend fun deleteAll()
}
