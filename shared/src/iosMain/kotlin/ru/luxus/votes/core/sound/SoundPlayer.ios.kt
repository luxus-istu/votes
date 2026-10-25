package ru.luxus.votes.core.sound

import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AudioToolbox.AudioServicesPlaySystemSound

actual class SoundPlayer {

    private val speechSynthesizer = AVSpeechSynthesizer()
    // Resolve the voice once instead of on every tap
    private val russianVoice: AVSpeechSynthesisVoice? =
        AVSpeechSynthesisVoice.voiceWithLanguage("ru-RU")

    actual fun playPronunciation(text: String, phonetic: String) {
        if (text.isBlank()) return
        val utterance = AVSpeechUtterance(string = text).apply {
            russianVoice?.let { voice = it }
            rate = 0.45f
        }
        speechSynthesizer.speakUtterance(utterance)
    }

    actual fun playSuccess() {
        AudioServicesPlaySystemSound(1057u)
    }

    actual fun playError() {
        AudioServicesPlaySystemSound(1053u)
    }

    actual fun playClick() {
        AudioServicesPlaySystemSound(1104u)
    }
}
