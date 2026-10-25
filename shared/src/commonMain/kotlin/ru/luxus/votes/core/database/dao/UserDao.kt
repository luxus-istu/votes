package ru.luxus.votes.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import ru.luxus.votes.core.database.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM UserTable WHERE email = :email")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM UserTable WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("UPDATE UserTable SET isCurrent = CASE WHEN email = :email THEN 1 ELSE 0 END")
    suspend fun setCurrentUser(email: String)

    @Query("UPDATE UserTable SET isCurrent = 0")
    suspend fun clearCurrentUser()

    @Query("UPDATE UserTable SET onboardingDone = 1 WHERE email = :email")
    suspend fun markOnboardingDone(email: String)

    @Query("SELECT onboardingDone FROM UserTable WHERE email = :email")
    suspend fun isOnboardingDone(email: String): Boolean?

    @Query(
        """
        UPDATE UserTable
        SET firstName = :firstName,
            lastName = :lastName,
            age = :age,
            role = :role,
            avatar = :avatar
        WHERE email = :email
        """
    )
    suspend fun updateProfile(
        email: String,
        firstName: String,
        lastName: String,
        age: Int,
        role: String,
        avatar: String,
    )
}
