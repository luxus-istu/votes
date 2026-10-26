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
    val emoji: String,
    val requiredWords: Int,
)

val GAME_UNLOCKS = listOf(
    GameUnlock(
        key = "translate",
        title = "1. Выбери перевод",
        description = "Выбери правильный русский перевод из трёх вариантов",
        emoji = "🎯",
        requiredWords = 3,
    ),
    GameUnlock(
        key = "truefalse",
        title = "2. Правда или Ложь",
        description = "Оцени, верно ли слово соответствует переводу",
        emoji = "⚡",
        requiredWords = 6,
    ),
    GameUnlock(
        key = "oddoneout",
        title = "3. Найди лишнее слово",
        description = "Три слова из одной темы, а одно — лишнее",
        emoji = "🔍",
        requiredWords = 9,
    ),
    GameUnlock(
        key = "sorting",
        title = "4. Сортировка",
        description = "Разложи слова по правильным категориям",
        emoji = "🧺",
        requiredWords = 12,
    ),
    GameUnlock(
        key = "memory",
        title = "5. Найди пару",
        description = "Открывай карточки и находи пары: слово — перевод",
        emoji = "🃏",
        requiredWords = 15,
    ),
    GameUnlock(
        key = "compile",
        title = "6. Собери слово",
        description = "Составь удмуртское слово из перемешанных букв",
        emoji = "🔤",
        requiredWords = 18,
    ),
)

fun isGameUnlocked(gameKey: String, learnedWords: Int): Boolean =
    GAME_UNLOCKS.firstOrNull { it.key == gameKey }
        ?.let { learnedWords >= it.requiredWords }
        ?: false

fun unlockedGameKeys(learnedWords: Int): List<String> =
    GAME_UNLOCKS.filter { learnedWords >= it.requiredWords }.map { it.key }
