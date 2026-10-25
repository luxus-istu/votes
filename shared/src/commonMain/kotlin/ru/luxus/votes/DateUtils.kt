package ru.luxus.votes

/**
 * Local date used for daily progress keys, formatted as `dd.MM.yyyy`.
 */
expect fun getTodayDateString(): String

/**
 * Current day of the year, used to pick the word of the day.
 */
expect fun getDayOfYear(): Int

private val DAYS_IN_MONTH = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

/** Parses a `dd.MM.yyyy` string, returning (day, month, year) or null when invalid. */
fun parseDateDdMmYyyy(value: String): Triple<Int, Int, Int>? {
    val parts = value.trim().split(".")
    if (parts.size != 3) return null
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    if (month !in 1..12) return null
    if (year !in 1900..2100) return null
    val isLeap = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
    val maxDay = if (month == 2 && isLeap) 29 else DAYS_IN_MONTH[month - 1]
    if (day !in 1..maxDay) return null
    return Triple(day, month, year)
}

/** Age in full years for a `dd.MM.yyyy` birth date, or null when the date is invalid. */
fun calculateAge(birthDate: String): Int? {
    val (day, month, year) = parseDateDdMmYyyy(birthDate) ?: return null
    val todayParts = getTodayDateString().trim().split(".")
    if (todayParts.size != 3) return null
    val todayDay = todayParts[0].toIntOrNull() ?: return null
    val todayMonth = todayParts[1].toIntOrNull() ?: return null
    val todayYear = todayParts[2].toIntOrNull() ?: return null

    var age = todayYear - year
    if (todayMonth < month || (todayMonth == month && todayDay < day)) {
        age--
    }
    return age.takeIf { it >= 0 }
}

/**
 * Formats raw input into a `dd.MM.yyyy` mask while typing
 * (digits only, dots inserted automatically).
 */
fun formatBirthDateInput(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(8)
    val builder = StringBuilder()
    digits.forEachIndexed { index, char ->
        if (index == 2 || index == 4) builder.append('.')
        builder.append(char)
    }
    return builder.toString()
}

private const val MILLIS_PER_DAY = 86_400_000L

/** Days since 1970-01-01 for a proleptic Gregorian date (Howard Hinnant's algorithm). */
private fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    var y = year.toLong()
    if (month <= 2) y -= 1
    val era = (if (y >= 0) y else y - 399) / 400
    val yearOfEra = y - era * 400
    val monthShifted = month + if (month > 2) -3 else 9
    val dayOfYear = (153 * monthShifted + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146097 + dayOfEra - 719468
}

/** Inverse of [daysFromCivil]. */
private fun civilFromDays(days: Long): Triple<Int, Int, Int> {
    val z = days + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val dayOfEra = z - era * 146097
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    val year = yearOfEra + era * 400
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthShifted = (5 * dayOfYear + 2) / 153
    val day = dayOfYear - (153 * monthShifted + 2) / 5 + 1
    val month = monthShifted + if (monthShifted < 10) 3 else -9
    val adjustedYear = year + if (month <= 2) 1 else 0
    return Triple(adjustedYear.toInt(), month.toInt(), day.toInt())
}

private fun floorDiv(value: Long, divisor: Long): Long {
    val quotient = value / divisor
    return if (value % divisor != 0L && (value xor divisor) < 0) quotient - 1 else quotient
}

/** Converts a `dd.MM.yyyy` date to UTC-midnight millis (Material3 DatePicker format). */
fun dateDdMmYyyyToUtcMillis(value: String): Long? {
    val (day, month, year) = parseDateDdMmYyyy(value) ?: return null
    return daysFromCivil(year, month, day) * MILLIS_PER_DAY
}

/** Converts UTC-midnight millis to a `dd.MM.yyyy` string. */
fun utcMillisToDdMmYyyy(utcMillis: Long): String {
    val (year, month, day) = civilFromDays(floorDiv(utcMillis, MILLIS_PER_DAY))
    return buildString {
        append(day.toString().padStart(2, '0')).append('.')
        append(month.toString().padStart(2, '0')).append('.')
        append(year)
    }
}

/** Age in full years for a DatePicker UTC-midnight timestamp. */
fun ageFromUtcMillis(utcMillis: Long): Int? = calculateAge(utcMillisToDdMmYyyy(utcMillis))

/** UTC-midnight millis of the same calendar day [years] years ago. */
fun utcMillisYearsAgo(years: Int): Long? {
    val parts = getTodayDateString().trim().split(".")
    if (parts.size != 3) return null
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    return daysFromCivil(year - years, month, day) * MILLIS_PER_DAY
}

/** Current year from the system date, if available. */
fun currentYear(): Int? = getTodayDateString().trim().split(".").getOrNull(2)?.toIntOrNull()
