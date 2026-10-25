package ru.luxus.votes.features.auth.data

import org.koin.core.annotation.Single
import ru.luxus.votes.core.database.AppDatabase
import ru.luxus.votes.core.database.entity.ChildProfileEntity
import ru.luxus.votes.core.database.entity.UserEntity as UserRow
import ru.luxus.votes.features.auth.domain.UserEntity
import ru.luxus.votes.features.parent.data.ParentRepository

@Single
class AuthRepository(
    private val db: AppDatabase,
    private val parentRepository: ParentRepository,
) {

    private val userDao = db.userDao()
    private val childDao = db.childDao()

    suspend fun checkSession(): UserEntity? = userDao.getCurrentUser()?.toDomain()

    /**
     * Login for parents and children:
     * 1. parent account (users table) – email + password
     * 2. child profile (created by a parent on the dashboard) – email + password
     */
    suspend fun login(email: String, password: String): Result<UserEntity> {
        val normalizedEmail = email.trim().lowercase()

        val parent = userDao.getByEmail(normalizedEmail)
        if (parent != null) {
            if (parent.password.isNotEmpty() && parent.password != password) {
                return Result.failure(Exception("Неверный пароль"))
            }
            userDao.setCurrentUser(parent.email)
            return Result.success(parent.toDomain())
        }

        val child = childDao.getChildByEmail(normalizedEmail)
            ?: return Result.failure(Exception("Пользователь с таким email не найден"))

        if (child.childPassword.isEmpty() || child.childPassword != password) {
            return Result.failure(Exception("Неверный пароль"))
        }

        childDao.setActiveChild(child.id)
        // Child sessions are not persisted: after an app restart the login screen is shown
        userDao.clearCurrentUser()
        return Result.success(child.toSessionUser())
    }

    /** Parent registration: email + password */
    suspend fun registerParent(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): Result<UserEntity> {
        val normalizedEmail = email.trim().lowercase()
        if (userDao.getByEmail(normalizedEmail) != null) {
            return Result.failure(Exception("Пользователь с таким email уже зарегистрирован"))
        }
        if (childDao.getChildByEmail(normalizedEmail) != null) {
            return Result.failure(Exception("Этот email уже используется профилем ребёнка"))
        }
        if (password.length < 4) {
            return Result.failure(Exception("Пароль должен содержать минимум 4 символа"))
        }

        val newUser = UserEntity(
            email = normalizedEmail,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            gender = "Не указан",
            age = 30,
            role = ROLE_PARENT,
            avatar = "👩",
        )
        userDao.upsert(newUser.toRow(password = password, isCurrent = true))
        userDao.setCurrentUser(normalizedEmail)
        return Result.success(newUser)
    }

    suspend fun updateProfile(
        email: String,
        firstName: String,
        lastName: String,
        age: Int,
        role: String,
        avatar: String,
    ): Result<UserEntity> {
        if (role == ROLE_CHILD) {
            // Child session: update the child profile instead of a user row
            val child = childDao.getActiveChild()
                ?: return Result.failure(Exception("Активный профиль ребёнка не найден"))
            val current = childDao.getChildById(child.id)

            parentRepository.updateChild(
                id = child.id,
                name = firstName.trim(),
                lastName = lastName.trim(),
                birthDate = current?.birthDate.orEmpty(),
                avatar = avatar,
                targetWordsPerDay = current?.targetWordsPerDay ?: 5,
                fallbackAge = age,
            )
            return Result.success(
                UserEntity(
                    email = email,
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    gender = "Не указан",
                    age = age,
                    role = ROLE_CHILD,
                    avatar = avatar,
                )
            )
        }

        val existing = userDao.getByEmail(email)
            ?: return Result.failure(Exception("Пользователь не найден"))

        userDao.updateProfile(
            email = email,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            age = age,
            role = role,
            avatar = avatar,
        )

        return Result.success(
            existing.toDomain().copy(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                age = age,
                role = role,
                avatar = avatar,
            )
        )
    }

    suspend fun logout() {
        userDao.clearCurrentUser()
    }

    /** A session user built from a child profile (children have no user table row) */
    private fun ChildProfileEntity.toSessionUser() = UserEntity(
        email = parentEmail,
        firstName = name,
        lastName = lastName,
        gender = "Не указан",
        age = age,
        role = ROLE_CHILD,
        avatar = avatar,
    )

    private fun UserRow.toDomain() = UserEntity(
        email = email,
        firstName = firstName,
        lastName = lastName,
        gender = gender,
        age = age,
        role = role,
        avatar = avatar,
    )

    private fun UserEntity.toRow(password: String, isCurrent: Boolean) = UserRow(
        email = email,
        password = password,
        firstName = firstName,
        lastName = lastName,
        gender = gender,
        age = age,
        role = role,
        avatar = avatar,
        isCurrent = isCurrent,
    )

    companion object {
        const val ROLE_PARENT = "Родитель"
        const val ROLE_CHILD = "Ученик"
    }
}
