package ru.luxus.votes.features.lessons.domain.repository

import ru.luxus.votes.features.lessons.domain.model.UdmurtLetterModel

interface WordRepository {
    suspend fun getAllLetters() : List<UdmurtLetterModel>
    suspend fun getAllSpecialLetters() : Set<String>
}