package ru.luxus.votes.core.database

import androidx.room3.RoomDatabase

expect fun createDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>