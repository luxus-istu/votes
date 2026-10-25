package ru.luxus.votes.core.sound

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import ru.luxus.votes.core.database.AndroidAppContext
import java.io.File
import java.util.Locale

private const val TAG = "UdmurtTTS"

actual class SoundPlayer {

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)

    private var textToSpeech: TextToSpeech? = null
    private var speechReady = false
    private var pendingSpeech: String? = null
    private var engineStartTime: Long = 0L

    init {
        // Boot the TTS engine during app startup. Previously it was created lazily on
        // the first tap, which made the user wait for engine initialisation.
        createEngine()
    }

    private fun createEngine() {
        val context = runCatching { AndroidAppContext.value }.getOrNull() ?: return
        engineStartTime = System.currentTimeMillis()
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            onEngineInitialized(status)
        }
    }

    private fun onEngineInitialized(status: Int) {
        val tts = textToSpeech
        Log.d(TAG, "engine init status=$status in ${System.currentTimeMillis() - engineStartTime} ms")
        if (status != TextToSpeech.SUCCESS || tts == null) {
            // Engine failed – drop it so the next tap retries initialisation
            speechReady = false
            textToSpeech = null
            return
        }

        // Important: prefer a LOCAL installed Russian voice. Network voices need a
        // server round-trip and can stall pronunciation for many seconds offline.
        val chosenVoice = pickLocalRussianVoice(tts)
        if (chosenVoice != null) {
            val result = tts.setVoice(chosenVoice)
            Log.d(
                TAG,
                "voice=${chosenVoice.name} locale=${chosenVoice.locale} " +
                    "network=${chosenVoice.isNetworkConnectionRequired} setVoice=$result"
            )
        } else {
            val russian = Locale.forLanguageTag("ru-RU")
            val languageResult = tts.setLanguage(russian)
            Log.d(TAG, "no explicit voice, setLanguage(ru-RU)=$languageResult")
            if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                languageResult == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts.language = Locale.getDefault()
            }
        }

        tts.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )

        speechReady = true
        warmUpEngine(tts)

        // Speak the request that was made while the engine was still starting
        pendingSpeech?.let { pending ->
            pendingSpeech = null
            speak(tts, pending)
        }
    }

    /** Chooses an installed local Russian voice (never a network one when possible). */
    private fun pickLocalRussianVoice(tts: TextToSpeech): Voice? {
        val voices = runCatching { tts.voices }.getOrNull() ?: return null
        val russianVoices = voices.filter { it.locale.language == "ru" }
        if (russianVoices.isEmpty()) return null

        fun Voice.isInstalled(): Boolean =
            features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) != true

        return russianVoices.firstOrNull { !it.isNetworkConnectionRequired && it.isInstalled() }
            ?: russianVoices.firstOrNull { !it.isNetworkConnectionRequired }
            ?: russianVoices.firstOrNull { it.isInstalled() }
            ?: russianVoices.first()
    }

    /**
     * Synthesises a word into the cache on startup. This loads the voice data in the
     * background so the first real pronunciation doesn't pay that cost.
     */
    private fun warmUpEngine(tts: TextToSpeech) {
        val context = runCatching { AndroidAppContext.value }.getOrNull() ?: return
        runCatching {
            val target = File(context.cacheDir, "tts_warmup.wav")
            tts.synthesizeToFile("ӟечбуресь", null, target, "tts-warmup")
        }.onFailure { Log.w(TAG, "warm-up failed", it) }
    }

    actual fun playPronunciation(text: String, phonetic: String) {
        if (text.isBlank()) return

        val tts = textToSpeech
        if (tts == null || !speechReady) {
            // Engine not ready yet: remember only the newest request and make sure
            // engine startup is running.
            pendingSpeech = text
            if (tts == null) createEngine()
            return
        }

        speak(tts, text)
    }

    private fun speak(tts: TextToSpeech, text: String) {
        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "udmurt-${text.hashCode()}")
        Log.d(TAG, "speak(\"$text\") result=$result")
    }

    actual fun playSuccess() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150)
    }

    actual fun playError() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 200)
    }

    actual fun playClick() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
    }
}
