package com.example.earlyreader.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    var isInitialized: Boolean = false
        private set

    var voiceName: String? = null
        private set

    var isOfflineVoice: Boolean = false
        private set

    var isNetworkVoiceOnly: Boolean = false
        private set

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
                        voiceName = chosen.name
                        isOfflineVoice = !chosen.isNetworkConnectionRequired
                        isNetworkVoiceOnly = chosen.isNetworkConnectionRequired
                    } else {
                        voiceName = "Default system voice (US English)"
                        isOfflineVoice = true
                        isNetworkVoiceOnly = false
                    }
                } else {
                    voiceName = "Default system voice (US English)"
                    isOfflineVoice = true
                    isNetworkVoiceOnly = false
                }
            } catch (_: Exception) {
                voiceName = "Default system voice (US English)"
                isOfflineVoice = true
                isNetworkVoiceOnly = false
            }
            isInitialized = true
        }
    }

    fun speakWord(text: String) {
        if (!isInitialized || text.isBlank()) return
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word_${System.currentTimeMillis()}")
    }

    fun speakPhoneme(segment: String) {
        if (!isInitialized || segment.isBlank()) return
        val sound = phonemeSounds[segment.lowercase()] ?: segment
        tts?.stop()
        tts?.speak(sound, TextToSpeech.QUEUE_FLUSH, null, "phoneme_${System.currentTimeMillis()}")
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

    companion object {
        val phonemeSounds: Map<String, String> = mapOf(
            "a" to "ah", "e" to "eh", "i" to "ih", "o" to "ah", "u" to "uh",
            "b" to "buh", "c" to "kuh", "d" to "duh", "f" to "fff", "g" to "guh",
            "h" to "huh", "j" to "juh", "k" to "kuh", "l" to "luh", "m" to "mmm",
            "n" to "nnn", "p" to "puh", "q" to "kwuh", "qu" to "kwuh", "r" to "ruh", "s" to "sss",
            "t" to "tuh", "v" to "vuh", "w" to "wuh", "x" to "ks", "y" to "yuh", "z" to "zzz",
            "sh" to "shh", "ch" to "chuh", "th" to "th", "ng" to "ng",
        )
    }
}
