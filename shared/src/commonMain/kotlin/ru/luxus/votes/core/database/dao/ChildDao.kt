package ru.luxus.votes.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Upsert
import ru.luxus.votes.core.database.entity.ChildLessonProgressEntity
import ru.luxus.votes.core.database.entity.ChildProfileEntity
import ru.luxus.votes.core.database.entity.ChildStatsEntity
import ru.luxus.votes.core.database.entity.ChildWordErrorEntity

@Dao
interface ChildDao {

    @Query("SELECT * FROM ChildProfileTable ORDER BY createdAt ASC")
    suspend fun getAllChildren(): List<ChildProfileEntity>

    @Query("SELECT * FROM ChildProfileTable WHERE id = :id")
    suspend fun getChildById(id: String): ChildProfileEntity?

    @Query("SELECT * FROM ChildProfileTable WHERE parentEmail = :parentEmail ORDER BY createdAt ASC")
    suspend fun getChildrenByParent(parentEmail: String): List<ChildProfileEntity>

    @Query("SELECT * FROM ChildProfileTable WHERE childEmail = :childEmail LIMIT 1")
    suspend fun getChildByEmail(childEmail: String): ChildProfileEntity?

    @Query("UPDATE ChildProfileTable SET onboardingDone = 1 WHERE id = :id")
    suspend fun markChildOnboardingDone(id: String)

    @Query("SELECT * FROM ChildProfileTable WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveChild(): ChildProfileEntity?

    @Upsert
    suspend fun upsertChild(child: ChildProfileEntity)

    @Query("UPDATE ChildProfileTable SET isActive = CASE WHEN id = :childId THEN 1 ELSE 0 END")
    suspend fun setActiveChild(childId: String)

    @Query("DELETE FROM ChildProfileTable WHERE id = :id")
    suspend fun deleteChild(id: String)

    @Query(
        """
        UPDATE ChildProfileTable
        SET name = :name,
            lastName = :lastName,
            age = :age,
            birthDate = :birthDate,
            avatar = :avatar,
            targetWordsPerDay = :targetWordsPerDay
        WHERE id = :id
        """
    )
    suspend fun updateChild(
        id: String,
        name: String,
        lastName: String,
        age: Int,
        birthDate: String,
        avatar: String,
        targetWordsPerDay: Int,
    )

    @Query(
        """
        UPDATE ChildProfileTable
        SET childEmail = :childEmail,
            childPassword = :childPassword
        WHERE id = :id
        """
    )
    suspend fun updateChildCredentials(
        id: String,
        childEmail: String,
        childPassword: String,
    )

    @Query("SELECT * FROM ChildStatsTable WHERE childId = :childId")
    suspend fun getChildStats(childId: String): List<ChildStatsEntity>

    @Upsert
    suspend fun upsertChildStats(stats: ChildStatsEntity)

    @Query("SELECT wordId FROM ChildLessonProgressTable WHERE childId = :childId")
    suspend fun getChildLessonProgress(childId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markChildWordViewed(progress: ChildLessonProgressEntity)

    @Query("SELECT * FROM ChildWordErrorTable WHERE childId = :childId ORDER BY errorCount DESC LIMIT 10")
    suspend fun getChildWordErrors(childId: String): List<ChildWordErrorEntity>

    @Upsert
    suspend fun upsertChildWordError(error: ChildWordErrorEntity)
}
