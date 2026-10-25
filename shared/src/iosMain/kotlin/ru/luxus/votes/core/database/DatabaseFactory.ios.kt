package ru.luxus.votes.core.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun createDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder<AppDatabase>(name = iosDbPath())

private fun iosDbPath(): String {
    val documentsDirectory = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true,
    ).first() as String
    return "$documentsDirectory/$DATABASE_NAME"
}
