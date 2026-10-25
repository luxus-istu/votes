package ru.luxus.votes.core.sound

expect class SoundPlayer {
    fun playPronunciation(text: String, phonetic: String = "")
    fun playSuccess()
    fun playError()
    fun playClick()
}
