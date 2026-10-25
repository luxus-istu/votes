package ru.luxus.votes.core.database

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

internal const val DATABASE_NAME = "votes.db"

internal fun RoomDatabase.Builder<AppDatabase>.buildWithDriver(): AppDatabase =
    setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
