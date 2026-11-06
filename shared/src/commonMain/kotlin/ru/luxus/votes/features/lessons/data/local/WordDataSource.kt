package ru.luxus.votes.features.lessons.data.local

import ru.luxus.votes.features.lessons.data.entity.UdmurtLetterEntity

interface WordDataSource {
    suspend fun getAllLetters() : List<UdmurtLetterEntity>
    suspend fun getAllSpecialLetters() : Set<String>
}