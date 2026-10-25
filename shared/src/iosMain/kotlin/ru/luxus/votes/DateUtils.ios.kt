package ru.luxus.votes

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun getTodayDateString(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "dd.MM.yyyy"
    }
    return formatter.stringFromDate(NSDate())
}

actual fun getDayOfYear(): Int {
    val calendar = NSCalendar.currentCalendar
    return calendar.ordinalityOfUnit(
        NSCalendarUnitDay,
        inUnit = NSCalendarUnitYear,
        forDate = NSDate(),
    ).toInt()
}
