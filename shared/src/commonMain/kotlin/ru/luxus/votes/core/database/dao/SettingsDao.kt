package ru.luxus.votes.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import ru.luxus.votes.core.database.entity.SettingsEntity

@Dao
interface SettingsDao {

    @Query("SELECT * FROM SettingsTable WHERE id = 1")
    suspend fun get(): SettingsEntity?

    @Upsert
    suspend fun upsert(settings: SettingsEntity)
}
