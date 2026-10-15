package ru.luxus.votes.features.games.domain

data class GameStats(
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val correctPerGame: Map<String, Int> = emptyMap(),
    val totalPerGame: Map<String, Int> = emptyMap()
) {
    val accuracy: Double
        get() = if (totalAnswered == 0) 0.0 else totalCorrect.toDouble() / totalAnswered

    fun copyWithResult(gameKey: String, isCorrect: Boolean): GameStats {
        val newCorrectPerGame = correctPerGame.toMutableMap()
        val newTotalPerGame = totalPerGame.toMutableMap()

        newTotalPerGame[gameKey] = (newTotalPerGame[gameKey] ?: 0) + 1
        if (isCorrect) {
            newCorrectPerGame[gameKey] = (newCorrectPerGame[gameKey] ?: 0) + 1
        }

        return GameStats(
            totalAnswered = totalAnswered + 1,
            totalCorrect = totalCorrect + if (isCorrect) 1 else 0,
            correctPerGame = newCorrectPerGame,
            totalPerGame = newTotalPerGame
        )
    }
}
