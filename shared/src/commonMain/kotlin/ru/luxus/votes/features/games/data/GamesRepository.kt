package ru.luxus.votes.features.games.data

import org.koin.core.annotation.Single
import ru.luxus.votes.db.VotesDatabase
import ru.luxus.votes.features.games.domain.GameStats
import ru.luxus.votes.features.games.domain.WordEntry

@Single
class GamesRepository(private val db: VotesDatabase) {

    private val defaultWords = listOf(
        // Greetings
        WordEntry("g1", "ӟечбуресь", "[дзечбурэсь]", "здравствуйте", "приветствие", "👋"),
        WordEntry("g2", "чырткем", "[чырткэм]", "привет", "приветствие", "🙋"),
        WordEntry("g3", "тау", "[тау]", "спасибо", "приветствие", "🙏"),
        WordEntry("g4", "ӟеч лу", "[дзеч лу]", "до свидания", "приветствие", "👋"),
        WordEntry("g5", "бур ӵукна", "[бур чукна]", "доброе утро", "приветствие", "🌅"),
        WordEntry("g6", "салам", "[салам]", "привет", "приветствие", "😊"),

        // Numbers
        WordEntry("n1", "одӥг", "[одыг]", "один", "числа", "1️⃣"),
        WordEntry("n2", "кык", "[кык]", "два", "числа", "2️⃣"),
        WordEntry("n3", "куинь", "[куинь]", "три", "числа", "3️⃣"),
        WordEntry("n4", "ньыль", "[ньыль]", "четыре", "числа", "4️⃣"),
        WordEntry("n5", "визь", "[визь]", "пять", "числа", "5️⃣"),
        WordEntry("n6", "куать", "[куать]", "шесть", "числа", "6️⃣"),
        WordEntry("n7", "сизьым", "[сизьым]", "семь", "числа", "7️⃣"),
        WordEntry("n8", "тямыс", "[тямыс]", "восемь", "числа", "8️⃣"),
        WordEntry("n9", "укмыс", "[укмыс]", "девять", "числа", "9️⃣"),
        WordEntry("n10", "дас", "[дас]", "десять", "числа", "🔟"),

        // Family
        WordEntry("f1", "анай", "[анай]", "мама", "семья", "👩"),
        WordEntry("f2", "атай", "[атай]", "папа", "семья", "👨"),
        WordEntry("f3", "вын", "[вын]", "брат", "семья", "👦"),
        WordEntry("f4", "сузэр", "[сузэр]", "сестра", "семья", "👧"),
        WordEntry("f5", "песянай", "[песянай]", "бабушка", "семья", "👵"),
        WordEntry("f6", "песятай", "[песятай]", "дедушка", "семья", "👴"),

        // Food & Drink
        WordEntry("fd1", "нянь", "[нянь]", "хлеб", "еда", "🍞"),
        WordEntry("fd2", "ву", "[ву]", "вода", "еда", "💧"),
        WordEntry("fd3", "шай", "[шай]", "чай", "еда", "☕"),
        WordEntry("fd4", "эмезь", "[эмезь]", "малина", "еда", "🫐"),
        WordEntry("fd5", "перепеч", "[перепэч]", "перепеч", "еда", "🥧"),

        // Animals
        WordEntry("a1", "пуны", "[пуны]", "собака", "животные", "🐕"),
        WordEntry("a2", "кочыш", "[кочыш]", "кошка", "животные", "🐈"),
        WordEntry("a3", "вало", "[вало]", "лошадь", "животные", "🐎"),
        WordEntry("a4", "скал", "[скал]", "корова", "животные", "🐄"),
        WordEntry("a5", "кион", "[кион]", "волк", "животные", "🐺"),
        WordEntry("a6", "гондыр", "[гондыр]", "медведь", "животные", "🐻"),

        // Nature & World
        WordEntry("w1", "шунды", "[шунды]", "солнце", "природа", "☀️"),
        WordEntry("w2", "толэзь", "[толэзь]", "луна/месяц", "природа", "🌙"),
        WordEntry("w3", "ин", "[ин]", "небо", "природа", "☁️"),
        WordEntry("w4", "музъем", "[музъем]", "земля", "природа", "🌍"),
        WordEntry("w5", "зоор", "[зоор]", "дождь", "природа", "🌧️"),
        WordEntry("w6", "лымы", "[лымы]", "снег", "природа", "❄️")
    )

    init {
        // Seed words into DB if not present
        val queries = db.votesDatabaseQueries
        val existingWords = queries.getAllWords().executeAsList()
        if (existingWords.isEmpty()) {
            defaultWords.forEach { w ->
                queries.insertWord(w.id, w.udmurt, w.transcription, w.russian, w.category, w.emoji)
            }
        }
    }

    fun getWords(): List<WordEntry> {
        val rows = db.votesDatabaseQueries.getAllWords().executeAsList()
        if (rows.isEmpty()) return defaultWords
        return rows.map {
            WordEntry(
                id = it.id,
                udmurt = it.udmurt,
                transcription = it.transcription,
                russian = it.russian,
                category = it.category,
                emoji = it.emoji
            )
        }
    }

    fun getStats(): GameStats {
        val rows = db.votesDatabaseQueries.getAllGameStats().executeAsList()
        var totalAnswered = 0
        var totalCorrect = 0
        val correctMap = mutableMapOf<String, Int>()
        val totalMap = mutableMapOf<String, Int>()

        rows.forEach { row ->
            val ans = row.totalAnswered.toInt()
            val corr = row.totalCorrect.toInt()
            totalAnswered += ans
            totalCorrect += corr
            correctMap[row.gameKey] = corr
            totalMap[row.gameKey] = ans
        }

        return GameStats(
            totalAnswered = totalAnswered,
            totalCorrect = totalCorrect,
            correctPerGame = correctMap,
            totalPerGame = totalMap
        )
    }

    fun saveResult(gameKey: String, isCorrect: Boolean) {
        val queries = db.votesDatabaseQueries
        val existing = queries.getStatsForGame(gameKey).executeAsOneOrNull()

        val newAnswered = (existing?.totalAnswered ?: 0) + 1
        val newCorrect = (existing?.totalCorrect ?: 0) + if (isCorrect) 1 else 0

        queries.insertOrUpdateStats(gameKey, newAnswered, newCorrect)
    }

    fun getViewedWordIds(): Set<String> {
        return db.votesDatabaseQueries.getViewedWordIds().executeAsList().toSet()
    }

    fun markWordViewed(wordId: String) {
        db.votesDatabaseQueries.markWordViewed(wordId)
    }
}
