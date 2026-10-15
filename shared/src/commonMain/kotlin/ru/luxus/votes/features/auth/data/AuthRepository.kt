package ru.luxus.votes.features.auth.data

import org.koin.core.annotation.Single
import ru.luxus.votes.db.VotesDatabase
import ru.luxus.votes.features.auth.domain.UserEntity

@Single
class AuthRepository(private val db: VotesDatabase) {

    init {
        // Pre-seed demo user if database is empty
        val queries = db.votesDatabaseQueries
        val existing = queries.getUserByEmail("user@example.com").executeAsOneOrNull()
        if (existing == null) {
            queries.insertUser(
                email = "user@example.com",
                password = "123456",
                firstName = "Иван",
                lastName = "Иванов",
                gender = "Мужской",
                isCurrent = 0
            )
        }
    }

    suspend fun checkSession(): UserEntity? {
        val row = db.votesDatabaseQueries.getCurrentUser().executeAsOneOrNull() ?: return null
        return UserEntity(
            email = row.email,
            firstName = row.firstName,
            lastName = row.lastName,
            gender = row.gender
        )
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val queries = db.votesDatabaseQueries
        val user = queries.getUserByEmail(email).executeAsOneOrNull()
            ?: return Result.failure(Exception("Пользователь с таким email не найден"))

        if (user.password != password) {
            return Result.failure(Exception("Неверный пароль"))
        }

        queries.setCurrentUser(email)

        val entity = UserEntity(
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            gender = user.gender
        )
        return Result.success(entity)
    }

    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        gender: String
    ): Result<UserEntity> {
        val queries = db.votesDatabaseQueries
        val existing = queries.getUserByEmail(email).executeAsOneOrNull()
        if (existing != null) {
            return Result.failure(Exception("Пользователь уже существует"))
        }

        queries.insertUser(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            gender = gender,
            isCurrent = 1
        )
        queries.setCurrentUser(email)

        val newUser = UserEntity(
            email = email,
            firstName = firstName,
            lastName = lastName,
            gender = gender
        )
        return Result.success(newUser)
    }

    suspend fun loginAnonymous(): Result<UserEntity> {
        val guestEmail = "guest@udmurt.app"
        val queries = db.votesDatabaseQueries

        queries.insertUser(
            email = guestEmail,
            password = "",
            firstName = "Гость",
            lastName = "",
            gender = "Не указан",
            isCurrent = 1
        )
        queries.setCurrentUser(guestEmail)

        val guestUser = UserEntity(
            email = guestEmail,
            firstName = "Гость",
            lastName = "",
            gender = "Не указан"
        )
        return Result.success(guestUser)
    }

    suspend fun logout() {
        db.votesDatabaseQueries.clearCurrentUser()
    }
}
