package ru.luxus.votes.core.sound

expect class SoundPlayer {
    fun playPronunciation(text: String, phonetic: String = "")
    fun playSuccess()
    fun playError()
    fun playClick()
}

/**
 * Chooses the string that is actually synthesised. A transcription is a
 * human-readable pronunciation hint, so whenever it is present it takes
 * precedence over the original spelling — Russian TTS cannot read specific
 * Udmurt letters such as ӝ, ӟ, ӥ, ӧ and ӵ.
 *
 * Square brackets and stress marks are display-only, so they are removed
 * before the text reaches the speech engine.
 */
internal fun spokenText(text: String, phonetic: String): String {
    val source = phonetic.ifBlank { text }
    return source
        .replace("[", "")
        .replace("]", "")
        .replace("\u0301", "")
        .trim()
}