package ru.luxus.votes.core.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

object AndroidAppContext {
    lateinit var value: Context
}

actual fun createDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder<AppDatabase>(
        context = AndroidAppContext.value,
        name = DATABASE_NAME,
    )
