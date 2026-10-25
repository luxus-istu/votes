package ru.luxus.votes.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import ru.luxus.votes.core.database.entity.GameStatsEntity

@Dao
interface GameStatsDao {

    @Query("SELECT * FROM GameStatsTable WHERE gameKey = :gameKey")
    suspend fun getForGame(gameKey: String): GameStatsEntity?

    @Query("SELECT * FROM GameStatsTable")
    suspend fun getAll(): List<GameStatsEntity>

    @Upsert
    suspend fun upsert(stats: GameStatsEntity)

    @Query("DELETE FROM GameStatsTable")
    suspend fun deleteAll()
}
