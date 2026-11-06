package ru.luxus.votes.features.lessons.data.repository

import org.koin.core.annotation.Singleton
import ru.luxus.votes.features.lessons.data.local.WordDataSource
import ru.luxus.votes.features.lessons.domain.model.UdmurtLetterModel
import ru.luxus.votes.features.lessons.domain.repository.WordRepository

@Singleton(binds = [WordRepository::class])
class WordRepositoryImpl(
    private val localDataSource: WordDataSource,
) : WordRepository {

    override suspend fun getAllLetters(): List<UdmurtLetterModel> =
        localDataSource.getAllLetters().map { it.toModel() }

    override suspend fun getAllSpecialLetters(): Set<String> =
        localDataSource.getAllSpecialLetters()
}
