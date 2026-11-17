package com.example.earlyreader.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts ?: return
            engine.language = Locale.US
            engine.setSpeechRate(0.8f)

            try {
                val voices = engine.voices
                if (voices != null) {
                    val enVoices = voices.filter { it.locale != null && it.locale.language.equals("en", ignoreCase = true) }
                    val offlineVoice = enVoices.firstOrNull { !it.isNetworkConnectionRequired }
                    val chosen = offlineVoice ?: enVoices.firstOrNull()
                    if (chosen != null) {
                        engine.voice = chosen
                    }
                }
            } catch (_: Exception) {
                // Fall back to default Locale.US set above
            }
            isInitialized = true
        }
    }

    fun speakWord(text: String) {
        if (!isInitialized || text.isBlank()) return
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word_${System.currentTimeMillis()}")
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
