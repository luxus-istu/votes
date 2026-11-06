package ru.luxus.votes.features.parent.domain.repository

import ru.luxus.votes.features.parent.domain.ChildProfileModel

interface ParentRepository {

    suspend fun getAllChildren(): List<ChildProfileModel>

    suspend fun getActiveChild(): ChildProfileModel?

    suspend fun getChildrenForParent(parentEmail: String): List<ChildProfileModel>

    suspend fun isChildEmailTaken(email: String, excludeChildId: String? = null): Boolean

    suspend fun addChild(
        name: String,
        lastName: String = "",
        birthDate: String,
        avatar: String,
        targetWordsPerDay: Int = 5,
        parentEmail: String = "",
        childEmail: String = "",
        childPassword: String = "",
    ): ChildProfileModel

    suspend fun updateChild(
        id: String,
        name: String,
        lastName: String = "",
        birthDate: String,
        avatar: String,
        targetWordsPerDay: Int,
        fallbackAge: Int? = null,
    )

    suspend fun updateChildCredentials(id: String, childEmail: String, childPassword: String)

    suspend fun deleteChild(id: String)

    suspend fun recordGameResultForActiveChild(
        gameKey: String,
        isCorrect: Boolean,
        wordId: String?,
        udmurt: String?,
        russian: String?,
    )

    suspend fun recordWordLearnedForActiveChild(wordId: String)
}