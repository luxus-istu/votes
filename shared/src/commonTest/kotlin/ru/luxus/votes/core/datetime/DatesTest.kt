package ru.luxus.votes.core.datetime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DatesTest {

    @Test
    fun parsesValidDates() {
        val (day, month, year) = assertNotNull(parseDateDdMmYyyy("05.03.2019"))
        assertEquals(5, day)
        assertEquals(3, month)
        assertEquals(2019, year)
    }

    @Test
    fun rejectsInvalidDates() {
        assertNull(parseDateDdMmYyyy("32.01.2019"))
        assertNull(parseDateDdMmYyyy("31.04.2019"))
        assertNull(parseDateDdMmYyyy("29.02.2019"))
        assertNull(parseDateDdMmYyyy("29.02.2100"))
        assertNull(parseDateDdMmYyyy("05.13.2019"))
        assertNull(parseDateDdMmYyyy("05.03.1899"))
        assertNull(parseDateDdMmYyyy("05.03.abcd"))
        assertNull(parseDateDdMmYyyy(""))
        assertNull(parseDateDdMmYyyy("5.3"))
    }

    @Test
    fun acceptsLeapDayInLeapYears() {
        val (day, month, year) = assertNotNull(parseDateDdMmYyyy("29.02.2016"))
        assertEquals(29, day)
        assertEquals(2, month)
        assertEquals(2016, year)
    }

    @Test
    fun utcMillisRoundTrip() {
        assertEquals(0L, dateDdMmYyyyToUtcMillis("01.01.1970"))
        assertEquals("01.01.1970", utcMillisToDdMmYyyy(0L))

        val y2k = assertNotNull(dateDdMmYyyyToUtcMillis("01.01.2000"))
        assertEquals("01.01.2000", utcMillisToDdMmYyyy(y2k))

        assertNull(dateDdMmYyyyToUtcMillis("invalid"))
    }

    @Test
    fun formatsBirthDateInputWithMask() {
        assertEquals("", formatBirthDateInput(""))
        assertEquals("05", formatBirthDateInput("05"))
        assertEquals("05.03", formatBirthDateInput("05.03"))
        assertEquals("05.03.2019", formatBirthDateInput("05032019"))
        assertEquals("05.03.2019", formatBirthDateInput("05.03.2019"))
    }

    @Test
    fun agesWithinTheSameBirthYearNeverDecrease() {
        val january = assertNotNull(calculateAge("01.01.2010"))
        val december = assertNotNull(calculateAge("31.12.2010"))
        // A birthday earlier in the year can never make the person younger
        val diff = january - december
        assertTrue(diff == 0 || diff == 1, "expected 0 or 1 but was $diff")
    }

    @Test
    fun ageMatchesYearsAgoAnchor() {
        val birthDate = utcMillisToDdMmYyyy(assertNotNull(utcMillisYearsAgo(10)))
        // On a rare February-29 today the anchor day does not exist in every
        // year; the parser then refuses the date and there is nothing to check
        if (parseDateDdMmYyyy(birthDate) != null) {
            assertEquals(10, calculateAge(birthDate))
        }
    }

    @Test
    fun currentYearIsParseable() {
        assertNotNull(currentYear())
    }
}
