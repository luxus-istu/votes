package ru.luxus.votes.features.games.data

import androidx.room3.withWriteTransaction
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import ru.luxus.votes.core.database.AppDatabase
import ru.luxus.votes.core.database.entity.ChildLessonProgressEntity
import ru.luxus.votes.core.database.entity.ChildStatsEntity
import ru.luxus.votes.core.database.entity.ChildWordErrorEntity
import ru.luxus.votes.core.database.entity.DailyProgressEntity
import ru.luxus.votes.core.database.entity.GameStatsEntity
import ru.luxus.votes.core.database.entity.LessonProgressEntity
import ru.luxus.votes.core.database.entity.WordEntity
import ru.luxus.votes.core.database.entity.WordErrorEntity
import ru.luxus.votes.features.games.domain.DailyProgressEntry
import ru.luxus.votes.features.games.domain.GameStats
import ru.luxus.votes.features.games.domain.WordEntry
import ru.luxus.votes.features.games.domain.WordErrorEntry
import ru.luxus.votes.getDayOfYear
import ru.luxus.votes.getTodayDateString

@Single
class GamesRepository(private val db: AppDatabase) {

    private val wordDao = db.wordDao()
    private val gameStatsDao = db.gameStatsDao()
    private val lessonProgressDao = db.lessonProgressDao()
    private val wordErrorDao = db.wordErrorDao()
    private val dailyProgressDao = db.dailyProgressDao()
    private val childDao = db.childDao()

    private val seedMutex = Mutex()
    private var seeded = false

    // Complete curriculum materials provided by the case study:
    // "Зеч-а бур-а удмурт кыл?" (Материалы к кейсу.docx)
    val defaultWords = listOf(
        // 1. Приветствие (Greetings)
        WordEntry("g1", "ӟечбуресь", "[дзечбурэ́сь]", "здравствуйте", "приветствие", "👋"),
        WordEntry("g2", "умоесь", "[умо́есь]", "здравствуйте", "приветствие", "🤝"),
        WordEntry("g3", "чырткем", "[чырткэ́м]", "привет", "приветствие", "🙋"),
        WordEntry("g4", "салам", "[сала́м]", "привет", "приветствие", "😊"),
        WordEntry("g5", "бур ӵукна", "[бур чукна́]", "доброе утро", "приветствие", "🌅"),
        WordEntry("g6", "тау", "[та́у]", "спасибо", "приветствие", "🙏"),
        WordEntry("g7", "ӟеч лу", "[дзеч лу]", "до свидания", "приветствие", "👋"),

        // 2. Числа 1-10 (Numbers 1-10)
        WordEntry("n1", "одӥг", "[оды́г]", "один (1)", "числа 1-10", "1️⃣"),
        WordEntry("n2", "кык", "[кык]", "два (2)", "числа 1-10", "2️⃣"),
        WordEntry("n3", "куинь", "[куинь]", "три (3)", "числа 1-10", "3️⃣"),
        WordEntry("n4", "ньыль", "[ньыль]", "четыре (4)", "числа 1-10", "4️⃣"),
        WordEntry("n5", "вить", "[вить]", "пять (5)", "числа 1-10", "5️⃣"),
        WordEntry("n6", "куать", "[куать]", "шесть (6)", "числа 1-10", "6️⃣"),
        WordEntry("n7", "сизьым", "[си́зьым]", "семь (7)", "числа 1-10", "7️⃣"),
        WordEntry("n8", "тямыс", "[тя́мыс]", "восемь (8)", "числа 1-10", "8️⃣"),
        WordEntry("n9", "укмыс", "[у́кмыс]", "девять (9)", "числа 1-10", "9️⃣"),
        WordEntry("n10", "дас", "[дас]", "десять (10)", "числа 1-10", "🔟"),

        // 3. Дикие животные (Wild Animals)
        WordEntry("wld1", "гондыр", "[гонды́р]", "медведь", "дикие животные", "🐻"),
        WordEntry("wld2", "кион", "[кио́н]", "волк", "дикие животные", "🐺"),
        WordEntry("wld3", "койык", "[ко́йык]", "лось", "дикие животные", "🦌"),
        WordEntry("wld4", "ӟичы", "[зи́чы]", "лисица", "дикие животные", "🦊"),
        WordEntry("wld5", "лудкеч", "[лудкэ́ч]", "заяц", "дикие животные", "🐰"),
        WordEntry("wld6", "коньы", "[ко́ньы]", "белка", "дикие животные", "🐿️"),
        WordEntry("wld7", "ӵушъял", "[чушъя́л]", "ёж", "дикие животные", "🦔"),
        WordEntry("wld8", "кырпарсь", "[кырпа́рсь]", "кабан", "дикие животные", "🐗"),

        // 4. Домашние животные (Pets & Domestic Animals)
        WordEntry("pet1", "пуны", "[пуны́]", "собака", "домашние животные", "🐕"),
        WordEntry("pet2", "скал", "[скал]", "корова", "домашние животные", "🐄"),
        WordEntry("pet3", "курег", "[курэ́г]", "курица", "домашние животные", "🐔"),
        WordEntry("pet4", "вал", "[вал]", "лошадь", "домашние животные", "🐎"),
        WordEntry("pet5", "ыж", "[ыж]", "овца", "домашние животные", "🐑"),
        WordEntry("pet6", "така", "[така́]", "баран", "домашние животные", "🐏"),
        WordEntry("pet7", "ош", "[ош]", "бык", "домашние животные", "🐂"),
        WordEntry("pet8", "атас", "[ата́с]", "петух", "домашние животные", "🐓"),
        WordEntry("pet9", "парсь", "[парсь]", "свинья", "домашние животные", "🐷"),
        WordEntry("pet10", "кеч", "[кеч]", "коза", "домашние животные", "🐐"),
        WordEntry("pet11", "курка", "[ку́рка]", "индейка", "домашние животные", "🦃"),

        // 5. Продукты и еда (Food & Dishes)
        WordEntry("fd1", "нянь", "[нянь]", "хлеб", "продукты", "🍞"),
        WordEntry("fd2", "йӧл", "[йол]", "молоко", "продукты", "🥛"),
        WordEntry("fd3", "ӝук", "[джук]", "каша", "продукты", "🥣"),
        WordEntry("fd4", "пельнянь", "[пельня́нь]", "пельмени", "продукты", "🥟"),
        WordEntry("fd5", "шыд", "[шыд]", "суп", "продукты", "🍲"),
        WordEntry("fd6", "кукей", "[куке́й]", "яйцо", "продукты", "🥚"),
        WordEntry("fd7", "сӥль", "[силь]", "мясо", "продукты", "🥩"),
        WordEntry("fd8", "вӧй", "[вой]", "масло", "продукты", "🧈"),
        WordEntry("fd9", "сиён", "[сиё́н]", "еда", "продукты", "🍽️"),
        WordEntry("fd10", "табань", "[таба́нь]", "блин", "продукты", "🥞"),

        // 6. Одежда (Clothing)
        WordEntry("cl1", "дэрем", "[дэрэ́м]", "рубаха / платье", "одежда", "👗"),
        WordEntry("cl2", "азькышет", "[азькышэ́т]", "передник / фартук", "одежда", "🥻"),
        WordEntry("cl3", "кышет", "[кышэ́т]", "платок", "одежда", "🧣"),

        // 7. Моя семья (My Family)
        WordEntry("fam1", "анай", "[ана́й]", "мама", "моя семья", "👩"),
        WordEntry("fam2", "атай", "[ата́й]", "папа", "моя семья", "👨"),
        WordEntry("fam3", "агай", "[ага́й]", "старший брат / дядя", "моя семья", "👦"),
        WordEntry("fam4", "апай", "[апа́й]", "старшая сестра", "моя семья", "👧"),
        WordEntry("fam5", "атай-анай", "[атай-анай]", "родители", "моя семья", "👨‍👩‍👧"),
        WordEntry("fam6", "песянай", "[песяна́й]", "бабушка", "моя семья", "👵"),
        WordEntry("fam7", "песятай", "[песята́й]", "дедушка", "моя семья", "👴"),
        WordEntry("fam8", "кузпал", "[кузпа́л]", "супруг(а)", "моя семья", "👫"),
        WordEntry("fam9", "кышно", "[кышно́]", "жена", "моя семья", "👰"),
        WordEntry("fam10", "нылы", "[нылы́]", "дочь", "моя семья", "👧"),
        WordEntry("fam11", "пие", "[пи́е]", "сын", "моя семья", "👦"),

        // 8. Овощи (Vegetables)
        WordEntry("veg1", "сугон", "[суго́н]", "лук", "овощи", "🧅"),
        WordEntry("veg2", "кубиста", "[куби́ста]", "капуста", "овощи", "🥬"),
        WordEntry("veg3", "гордкушман", "[гордкушма́н]", "свекла", "овощи", "🥔"),
        WordEntry("veg4", "кешыр", "[кешы́р]", "морковь", "овощи", "🥕"),
        WordEntry("veg5", "кияр", "[кия́р]", "огурец", "овощи", "🥒"),

        // 9. Цвета (Colors)
        WordEntry("col1", "тӧдьы", "[то́дьы]", "белый", "цвета", "⚪"),
        WordEntry("col2", "сьӧд", "[сьод]", "чёрный", "цвета", "⚫"),
        WordEntry("col3", "лыз", "[лыз]", "синий", "цвета", "🔵"),
        WordEntry("col4", "ӵуж", "[чуж]", "жёлтый", "цвета", "🟡"),
        WordEntry("col5", "вож", "[вож]", "зелёный", "цвета", "🟢"),
        WordEntry("col6", "льӧль", "[льол]", "розовый", "цвета", "🌸"),
        WordEntry("col7", "горд", "[горд]", "красный", "цвета", "🔴"),

        // 10. Ягоды (Berries)
        WordEntry("ber1", "шупу", "[шупу́]", "калина", "ягоды", "🍒"),
        WordEntry("ber2", "ягмульыпу", "[ягмульыпу́]", "брусника", "ягоды", "🫐"),
        WordEntry("ber3", "узы", "[узы́]", "земляника", "ягоды", "🍓"),
        WordEntry("ber4", "боры", "[боры́]", "клубника", "ягоды", "🍓"),
        WordEntry("ber5", "нюрмульы", "[нюрму́льы]", "клюква", "ягоды", "🍒"),
        WordEntry("ber6", "лызмульы", "[лызму́льы]", "голубика", "ягоды", "🫐"),

        // 11. Полезные фразы (Useful Phrases)
        WordEntry("ph1", "Мынам воже потэ", "[Мына́м во́же потэ́]", "Я злюсь", "фразы", "😤"),
        WordEntry("ph2", "Мон шумпотӥсько", "[Мон шумпоти́сько]", "Я рада / Я рад", "фразы", "😄"),
        WordEntry("ph3", "Мон кышкасько", "[Мон кышка́сько]", "Мне страшно", "фразы", "😨"),
        WordEntry("ph4", "Мон тонэ яратӥсько", "[Мон тонэ́ ярати́сько]", "Я тебя люблю", "фразы", "❤️"),
        WordEntry("ph5", "Ойдолэ тодматском!", "[Ойдолэ́ тодматско́м!]", "Давайте познакомимся!", "фразы", "🤝"),
        WordEntry("ph6", "Мынам нимы...", "[Мына́м нимы́...]", "Меня зовут...", "фразы", "🏷️"),
        WordEntry("ph7", "Мон уг валаськы", "[Мон уг вала́ськы]", "Я не понимаю", "фразы", "🤷")
    )

    private suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return
            val existingWords = wordDao.getAll()
            if (existingWords.size < defaultWords.size) {
                wordDao.upsertAll(defaultWords.map { it.toEntity() })
            }
            seeded = true
        }
    }

    suspend fun getWords(): List<WordEntry> {
        ensureSeeded()
        val rows = wordDao.getAll()
        return if (rows.isEmpty()) defaultWords else rows.map { it.toDomain() }
    }

    suspend fun getWordsByCategory(category: String): List<WordEntry> {
        ensureSeeded()
        val rows = wordDao.getByCategory(category)
        return if (rows.isEmpty()) defaultWords.filter { it.category == category } else rows.map { it.toDomain() }
    }

    suspend fun getCategories(): List<String> = getWords().map { it.category }.distinct()

    suspend fun getWordOfTheDay(): WordEntry {
        val words = getWords()
        if (words.isEmpty()) return defaultWords.first()
        return words[getDayOfYear() % words.size]
    }

    suspend fun getStats(): GameStats {
        val rows = gameStatsDao.getAll()
        var totalAnswered = 0
        var totalCorrect = 0
        val correctMap = mutableMapOf<String, Int>()
        val totalMap = mutableMapOf<String, Int>()

        rows.forEach { row ->
            totalAnswered += row.totalAnswered
            totalCorrect += row.totalCorrect
            correctMap[row.gameKey] = row.totalCorrect
            totalMap[row.gameKey] = row.totalAnswered
        }

        return GameStats(
            totalAnswered = totalAnswered,
            totalCorrect = totalCorrect,
            correctPerGame = correctMap,
            totalPerGame = totalMap,
        )
    }

    suspend fun saveResult(gameKey: String, isCorrect: Boolean, word: WordEntry? = null) =
        db.withWriteTransaction {
            val existing = gameStatsDao.getForGame(gameKey)
            gameStatsDao.upsert(
                GameStatsEntity(
                    gameKey = gameKey,
                    totalAnswered = (existing?.totalAnswered ?: 0) + 1,
                    totalCorrect = (existing?.totalCorrect ?: 0) + if (isCorrect) 1 else 0,
                )
            )

            // Increment daily games played
            val today = getTodayDateString()
            val daily = dailyProgressDao.getByDate(today)
            dailyProgressDao.upsert(
                DailyProgressEntity(
                    date = today,
                    wordsLearnedCount = daily?.wordsLearnedCount ?: 0,
                    gamesPlayedCount = (daily?.gamesPlayedCount ?: 0) + 1,
                )
            )

            // Record error or success for the word
            if (word != null) {
                val existingError = wordErrorDao.getById(word.id)
                wordErrorDao.upsert(
                    WordErrorEntity(
                        wordId = word.id,
                        udmurt = word.udmurt,
                        russian = word.russian,
                        errorCount = (existingError?.errorCount ?: 0) + if (isCorrect) 0 else 1,
                        successCount = (existingError?.successCount ?: 0) + if (isCorrect) 1 else 0,
                    )
                )
            }

            // Record for active child profile in parent system
            val activeChild = childDao.getActiveChild()
            if (activeChild != null) {
                val childId = activeChild.id
                val existingStat = childDao.getChildStats(childId).firstOrNull { it.gameKey == gameKey }
                childDao.upsertChildStats(
                    ChildStatsEntity(
                        childId = childId,
                        gameKey = gameKey,
                        totalAnswered = (existingStat?.totalAnswered ?: 0) + 1,
                        totalCorrect = (existingStat?.totalCorrect ?: 0) + if (isCorrect) 1 else 0,
                    )
                )

                if (word != null) {
                    val existingError = childDao.getChildWordErrors(childId).firstOrNull { it.wordId == word.id }
                    childDao.upsertChildWordError(
                        ChildWordErrorEntity(
                            childId = childId,
                            wordId = word.id,
                            udmurt = word.udmurt,
                            russian = word.russian,
                            errorCount = (existingError?.errorCount ?: 0) + if (isCorrect) 0 else 1,
                            successCount = (existingError?.successCount ?: 0) + if (isCorrect) 1 else 0,
                        )
                    )
                }
            }
        }

    suspend fun getViewedWordIds(): Set<String> {
        ensureSeeded()
        return lessonProgressDao.getViewedWordIds().toSet()
    }

    suspend fun markWordViewed(wordId: String) = db.withWriteTransaction {
        lessonProgressDao.markViewed(LessonProgressEntity(wordId))

        childDao.getActiveChild()?.let { activeChild ->
            childDao.markChildWordViewed(ChildLessonProgressEntity(activeChild.id, wordId))
        }

        // Increment daily words learned
        val today = getTodayDateString()
        val daily = dailyProgressDao.getByDate(today)
        dailyProgressDao.upsert(
            DailyProgressEntity(
                date = today,
                wordsLearnedCount = (daily?.wordsLearnedCount ?: 0) + 1,
                gamesPlayedCount = daily?.gamesPlayedCount ?: 0,
            )
        )
    }

    suspend fun getTopErrorWords(): List<WordErrorEntry> {
        ensureSeeded()
        return wordErrorDao.getTopErrors().map {
            WordErrorEntry(
                wordId = it.wordId,
                udmurt = it.udmurt,
                russian = it.russian,
                errorCount = it.errorCount,
                successCount = it.successCount,
            )
        }
    }

    suspend fun getDailyProgress(): List<DailyProgressEntry> {
        ensureSeeded()
        return dailyProgressDao.getRecent().map {
            DailyProgressEntry(
                date = it.date,
                wordsLearnedCount = it.wordsLearnedCount,
                gamesPlayedCount = it.gamesPlayedCount,
            )
        }
    }

    suspend fun resetAllProgress() = db.withWriteTransaction {
        gameStatsDao.deleteAll()
        lessonProgressDao.deleteAll()
        wordErrorDao.deleteAll()
        dailyProgressDao.deleteAll()
    }

    private fun WordEntry.toEntity() = WordEntity(
        id = id,
        udmurt = udmurt,
        transcription = transcription,
        russian = russian,
        category = category,
        emoji = emoji,
    )

    private fun WordEntity.toDomain() = WordEntry(
        id = id,
        udmurt = udmurt,
        transcription = transcription,
        russian = russian,
        category = category,
        emoji = emoji,
    )
}
