package ru.luxus.votes.core.sound

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The speech engine is Russian, so it can only read the transcription of an
 * Udmurt word. These tests guard the rule that the stored transcription is
 * what gets synthesised — exactly as recorded in the vocabulary.
 */
class SpokenTextTest {

    @Test
    fun transcriptionIsSpokenInsteadOfUdmurtSpelling() {
        // Udmurt-specific letters (ӟ, ӝ, ӥ, ӧ, ӵ) are unreadable for a
        // Russian voice: the audio must use the recorded transcription.
        assertEquals("дзечбурэсь", spokenText("ӟечбуресь", "[дзечбурэ\u0301сь]"))
        assertEquals("джук", spokenText("ӝук", "[джук]"))
        assertEquals("чуж", spokenText("ӵуж", "[чуж]"))
    }

    @Test
    fun displayOnlyMarksAreStripped() {
        // Square brackets and combining stress marks are display-only.
        assertEquals("одыг", spokenText("одӥг", "[оды\u0301г]"))
        assertEquals("атай-анай", spokenText("атай-анай", "[атай-анай]"))
    }

    @Test
    fun blankTranscriptionFallsBackToTheShownWord() {
        assertEquals("ӟичы", spokenText("ӟичы", ""))
        assertEquals("ӟичы", spokenText("ӟичы", "   "))
    }
}
