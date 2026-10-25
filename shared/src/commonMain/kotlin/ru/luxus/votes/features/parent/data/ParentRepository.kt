package ru.luxus.votes.features.parent.data

import org.koin.core.annotation.Single
import ru.luxus.votes.core.database.AppDatabase
import ru.luxus.votes.core.database.entity.ChildLessonProgressEntity
import ru.luxus.votes.core.database.entity.ChildProfileEntity
import ru.luxus.votes.core.database.entity.ChildStatsEntity
import ru.luxus.votes.core.database.entity.ChildWordErrorEntity
import ru.luxus.votes.features.games.data.GamesRepository
import ru.luxus.votes.features.games.domain.WordErrorEntry
import ru.luxus.votes.features.parent.domain.ChildProfileModel
import ru.luxus.votes.calculateAge
import ru.luxus.votes.getTodayDateString

@Single
class ParentRepository(
    private val db: AppDatabase,
    private val gamesRepository: GamesRepository,
) {

    private val childDao = db.childDao()

    suspend fun getAllChildren(): List<ChildProfileModel> {
        val allWords = gamesRepository.defaultWords
        val totalWordsCount = allWords.size

        return childDao.getAllChildren().map { row ->
            val childId = row.id
            val learnedIds = childDao.getChildLessonProgress(childId).toSet()
            val statsRows = childDao.getChildStats(childId)
            val errorRows = childDao.getChildWordErrors(childId)

            var totalAns = 0
            var totalCorr = 0
            val statsMap = mutableMapOf<String, Pair<Int, Int>>()

            statsRows.forEach { stat ->
                totalAns += stat.totalAnswered
                totalCorr += stat.totalCorrect
                statsMap[stat.gameKey] = Pair(stat.totalAnswered, stat.totalCorrect)
            }

            val accuracy = if (totalAns > 0) totalCorr.toFloat() / totalAns else 0f

            val topErrors = errorRows.map {
                WordErrorEntry(
                    wordId = it.wordId,
                    udmurt = it.udmurt,
                    russian = it.russian,
                    errorCount = it.errorCount,
                    successCount = it.successCount,
                )
            }

            // Topic mastery
            val topicMap = mutableMapOf<String, Pair<Int, Int>>()
            allWords.groupBy { it.category }.forEach { (category, wordsInCategory) ->
                val learnedInCategory = wordsInCategory.count { it.id in learnedIds }
                topicMap[category] = Pair(learnedInCategory, wordsInCategory.size)
            }

            ChildProfileModel(
                id = row.id,
                name = row.name,
                lastName = row.lastName,
                age = calculateAge(row.birthDate) ?: row.age,
                avatar = row.avatar,
                targetWordsPerDay = row.targetWordsPerDay,
                createdAt = row.createdAt,
                isActive = row.isActive,
                birthDate = row.birthDate,
                parentEmail = row.parentEmail,
                childEmail = row.childEmail,
                wordsLearnedCount = learnedIds.size,
                totalWordsAvailable = totalWordsCount,
                totalAnswered = totalAns,
                totalCorrect = totalCorr,
                accuracy = accuracy,
                topErrors = topErrors,
                gameStatsMap = statsMap,
                topicMastery = topicMap,
            )
        }
    }

    suspend fun getActiveChild(): ChildProfileModel? {
        val all = getAllChildren()
        return all.firstOrNull { it.isActive } ?: all.firstOrNull()
    }

    /** All child profiles owned by a parent account. */
    suspend fun getChildrenForParent(parentEmail: String): List<ChildProfileModel> {
        val email = parentEmail.trim().lowercase()
        if (email.isBlank()) return emptyList()
        return getAllChildren().filter { it.parentEmail == email }
    }

    /** True when the login email is already used by a user or another child profile. */
    suspend fun isChildEmailTaken(email: String, excludeChildId: String? = null): Boolean {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank()) return false
        val existingChild = childDao.getChildByEmail(normalized)
        if (existingChild != null && existingChild.id != excludeChildId) return true
        return db.userDao().getByEmail(normalized) != null
    }

    suspend fun addChild(
        name: String,
        lastName: String = "",
        birthDate: String,
        avatar: String,
        targetWordsPerDay: Int = 5,
        parentEmail: String = "",
        childEmail: String = "",
        childPassword: String = "",
    ): ChildProfileModel {
        val id = "child_${getTodayDateString()}_${(100..999).random()}"
        val isFirst = childDao.getAllChildren().isEmpty()

        childDao.upsertChild(
            ChildProfileEntity(
                id = id,
                name = name.trim(),
                lastName = lastName.trim(),
                age = calculateAge(birthDate) ?: 7,
                birthDate = birthDate.trim(),
                avatar = avatar,
                targetWordsPerDay = targetWordsPerDay,
                createdAt = getTodayDateString(),
                isActive = isFirst,
                parentEmail = parentEmail.trim().lowercase(),
                childEmail = childEmail.trim().lowercase(),
                childPassword = childPassword,
            )
        )

        return getAllChildren().first { it.id == id }
    }

    suspend fun updateChild(
        id: String,
        name: String,
        lastName: String = "",
        birthDate: String,
        avatar: String,
        targetWordsPerDay: Int,
        fallbackAge: Int? = null,
    ) {
        childDao.updateChild(
            id = id,
            name = name.trim(),
            lastName = lastName.trim(),
            age = calculateAge(birthDate) ?: fallbackAge ?: 7,
            birthDate = birthDate.trim(),
            avatar = avatar,
            targetWordsPerDay = targetWordsPerDay,
        )
    }

    suspend fun updateChildCredentials(
        id: String,
        childEmail: String,
        childPassword: String,
    ) {
        // Empty password means "keep the current one"
        val password = if (childPassword.isBlank()) {
            childDao.getChildById(id)?.childPassword.orEmpty()
        } else {
            childPassword
        }
        childDao.updateChildCredentials(
            id = id,
            childEmail = childEmail.trim().lowercase(),
            childPassword = password,
        )
    }

    suspend fun deleteChild(id: String) {
        childDao.deleteChild(id)
        // If the active child was deleted, activate the first remaining
        val remaining = childDao.getAllChildren()
        if (remaining.isNotEmpty() && remaining.none { it.isActive }) {
            childDao.setActiveChild(remaining.first().id)
        }
    }

    suspend fun recordGameResultForActiveChild(
        gameKey: String,
        isCorrect: Boolean,
        wordId: String?,
        udmurt: String?,
        russian: String?,
    ) {
        val active = childDao.getActiveChild() ?: return
        val childId = active.id

        val existingStat = childDao.getChildStats(childId).firstOrNull { it.gameKey == gameKey }
        childDao.upsertChildStats(
            ChildStatsEntity(
                childId = childId,
                gameKey = gameKey,
                totalAnswered = (existingStat?.totalAnswered ?: 0) + 1,
                totalCorrect = (existingStat?.totalCorrect ?: 0) + if (isCorrect) 1 else 0,
            )
        )

        if (wordId != null && udmurt != null && russian != null) {
            val existingError = childDao.getChildWordErrors(childId).firstOrNull { it.wordId == wordId }
            childDao.upsertChildWordError(
                ChildWordErrorEntity(
                    childId = childId,
                    wordId = wordId,
                    udmurt = udmurt,
                    russian = russian,
                    errorCount = (existingError?.errorCount ?: 0) + if (isCorrect) 0 else 1,
                    successCount = (existingError?.successCount ?: 0) + if (isCorrect) 1 else 0,
                )
            )
        }
    }

    suspend fun recordWordLearnedForActiveChild(wordId: String) {
        val active = childDao.getActiveChild() ?: return
        childDao.markChildWordViewed(ChildLessonProgressEntity(active.id, wordId))
    }
}
