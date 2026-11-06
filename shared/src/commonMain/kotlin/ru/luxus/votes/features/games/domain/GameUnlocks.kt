package ru.luxus.votes.features.games.domain

/**
 * Games open gradually as the child studies words. This keeps a beginner from
 * being dropped into all six games at once and guarantees that every game is
 * played with vocabulary the child has already met in the flashcards.
 */
data class GameUnlock(
    val key: String,
    val title: String,
    val description: String,
    val requiredWords: Int,
)

val GAME_UNLOCKS = listOf(
    GameUnlock(
        key = "translate",
        title = "Выбери перевод",
        description = "Выбери правильный русский перевод из трёх вариантов",
        requiredWords = 3,
    ),
    GameUnlock(
        key = "truefalse",
        title = "Правда или ложь",
        description = "Реши, подходит ли перевод к слову",
        requiredWords = 6,
    ),
    GameUnlock(
        key = "oddoneout",
        title = "Найди лишнее слово",
        description = "Три слова из одной темы и одно из другой",
        requiredWords = 9,
    ),
    GameUnlock(
        key = "sorting",
        title = "Сортировка",
        description = "Разложи слова по двум категориям",
        requiredWords = 12,
    ),
    GameUnlock(
        key = "memory",
        title = "Найди пару",
        description = "Открой две карточки и найди одинаковые слова",
        requiredWords = 15,
    ),
    GameUnlock(
        key = "compile",
        title = "Собери слово",
        description = "Составь удмуртское слово из перемешанных букв",
        requiredWords = 18,
    ),
)

fun isGameUnlocked(gameKey: String, learnedWords: Int): Boolean =
    GAME_UNLOCKS.firstOrNull { it.key == gameKey }
        ?.let { learnedWords >= it.requiredWords }
        ?: false

fun unlockedGameKeys(learnedWords: Int): List<String> =
    GAME_UNLOCKS.filter { learnedWords >= it.requiredWords }.map { it.key }