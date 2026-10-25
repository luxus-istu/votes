package ru.luxus.votes.core.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import ru.luxus.votes.core.database.dao.ChildDao
import ru.luxus.votes.core.database.dao.DailyProgressDao
import ru.luxus.votes.core.database.dao.GameStatsDao
import ru.luxus.votes.core.database.dao.LessonProgressDao
import ru.luxus.votes.core.database.dao.SettingsDao
import ru.luxus.votes.core.database.dao.UserDao
import ru.luxus.votes.core.database.dao.WordDao
import ru.luxus.votes.core.database.dao.WordErrorDao
import ru.luxus.votes.core.database.entity.ChildLessonProgressEntity
import ru.luxus.votes.core.database.entity.ChildProfileEntity
import ru.luxus.votes.core.database.entity.ChildStatsEntity
import ru.luxus.votes.core.database.entity.ChildWordErrorEntity
import ru.luxus.votes.core.database.entity.DailyProgressEntity
import ru.luxus.votes.core.database.entity.GameStatsEntity
import ru.luxus.votes.core.database.entity.LessonProgressEntity
import ru.luxus.votes.core.database.entity.SettingsEntity
import ru.luxus.votes.core.database.entity.UserEntity
import ru.luxus.votes.core.database.entity.WordEntity
import ru.luxus.votes.core.database.entity.WordErrorEntity

@Database(
    entities = [
        UserEntity::class,
        WordEntity::class,
        GameStatsEntity::class,
        LessonProgressEntity::class,
        WordErrorEntity::class,
        DailyProgressEntity::class,
        SettingsEntity::class,
        ChildProfileEntity::class,
        ChildStatsEntity::class,
        ChildLessonProgressEntity::class,
        ChildWordErrorEntity::class,
    ],
    version = 8,
    exportSchema = false,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun wordDao(): WordDao
    abstract fun gameStatsDao(): GameStatsDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun wordErrorDao(): WordErrorDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun settingsDao(): SettingsDao
    abstract fun childDao(): ChildDao
}
