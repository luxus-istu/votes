package ru.luxus.votes.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import ru.luxus.votes.core.database.entity.DailyProgressEntity

@Dao
interface DailyProgressDao {

    @Query("SELECT * FROM DailyProgressTable WHERE date = :date")
    suspend fun getByDate(date: String): DailyProgressEntity?

    @Query("SELECT * FROM DailyProgressTable ORDER BY date DESC LIMIT 14")
    suspend fun getRecent(): List<DailyProgressEntity>

    @Upsert
    suspend fun upsert(progress: DailyProgressEntity)

    @Query("DELETE FROM DailyProgressTable")
    suspend fun deleteAll()
}
