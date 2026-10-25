package ru.luxus.votes.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import ru.luxus.votes.core.database.entity.LessonProgressEntity

@Dao
interface LessonProgressDao {

    @Query("SELECT wordId FROM LessonProgressTable")
    suspend fun getViewedWordIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markViewed(progress: LessonProgressEntity)

    @Query("DELETE FROM LessonProgressTable")
    suspend fun deleteAll()
}
