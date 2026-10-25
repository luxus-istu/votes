package ru.luxus.votes.core.database

import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL

internal const val DATABASE_NAME = "votes.db"

/** Persist the current child session across app updates/restarts. */
private val MIGRATION_7_8 = object : Migration(7, 8) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE SettingsTable ADD COLUMN currentChildId TEXT NOT NULL DEFAULT ''"
        )
    }
}

/** Per-child daily progress so siblings don't share the daily goal. */
private val MIGRATION_8_9 = object : Migration(8, 9) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `ChildDailyProgressTable` (" +
                "`childId` TEXT NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`wordsLearnedCount` INTEGER NOT NULL, " +
                "`gamesPlayedCount` INTEGER NOT NULL, " +
                "PRIMARY KEY(`childId`, `date`))"
        )
    }
}

internal fun RoomDatabase.Builder<AppDatabase>.buildWithDriver(): AppDatabase =
    setDriver(BundledSQLiteDriver())
        .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
        // Fallback is only used for very old incompatible schemas; regular
        // app updates run the migrations above and keep accounts and sessions.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
