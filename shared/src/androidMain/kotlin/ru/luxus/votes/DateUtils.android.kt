package ru.luxus.votes

import java.util.Calendar
import java.util.Locale

actual fun getTodayDateString(): String {
    val calendar = Calendar.getInstance()
    return String.format(
        Locale.US,
        "%02d.%02d.%04d",
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.YEAR),
    )
}

actual fun getDayOfYear(): Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
