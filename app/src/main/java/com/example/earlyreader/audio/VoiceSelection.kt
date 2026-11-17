package com.example.earlyreader.audio

/**
 * A text-to-speech voice reduced to what the choice depends on, so the logic can be unit tested without Android.
 * [country] is the voice's region ("US", "GB", ...) and [installed] is false for a voice whose data is not downloaded
 * (the engine lists it with the "notInstalled" feature).
 */
data class VoiceCandidate(
    val name: String,
    val language: String?,
    val networkRequired: Boolean,
    val country: String? = null,
    val installed: Boolean = true,
)

/** The outcome of picking a voice. [index] is the position in the candidate list (null: engine default). */
data class VoiceChoice(
    val index: Int?,
    val name: String,
    val isOffline: Boolean,
    val isNetworkOnly: Boolean,
) {
    /** The adult dashboard shows the "download an offline voice" tip when this is true. */
    val needsOfflineGuidance: Boolean get() = VoiceSelection.needsOfflineGuidance(isOffline, isNetworkOnly)
}

/** Whether speech can be expected to work: set by [SpeechManager] once the engine has answered. */
enum class SpeechStatus {
    /** The engine is still starting up. */
    STARTING,

    /** The engine started and English (US) is available. */
    READY,

    /** There is no usable text-to-speech engine on the device (or it failed to start). */
    FAILED,

    /** The engine started but has no English language data. */
    NO_LANGUAGE,
}

object VoiceSelection {
    const val DEFAULT_VOICE_NAME = "Default system voice (US English)"

    /**
     * Picks the voice: English only, skipping voices that are not downloaded. A voice stored on the device is
     * preferred over a network voice (which needs internet and sends the text to a speech service); within that,
     * a US voice comes first, then any other English one. Falls back to the engine default when nothing fits.
     */
    fun choose(candidates: List<VoiceCandidate>?): VoiceChoice {
        if (candidates == null) return defaultChoice()
        val english = candidates.withIndex().filter { (_, v) ->
            v.language.equals("en", ignoreCase = true) && v.installed
        }
        val chosen = english.minByOrNull { (_, v) ->
            (if (v.networkRequired) 2 else 0) + (if (v.country.equals("US", ignoreCase = true)) 0 else 1)
        } ?: return defaultChoice()
        return VoiceChoice(
            index = chosen.index,
            name = chosen.value.name,
            isOffline = !chosen.value.networkRequired,
            isNetworkOnly = chosen.value.networkRequired,
        )
    }

    fun defaultChoice() = VoiceChoice(index = null, name = DEFAULT_VOICE_NAME, isOffline = true, isNetworkOnly = false)

    /** True when the voice in use needs the network, so the adult should be pointed at the offline-voice setting. */
    fun needsOfflineGuidance(isOffline: Boolean, isNetworkOnly: Boolean): Boolean = isNetworkOnly || !isOffline

    /** The status for the result of setting the engine's language (TextToSpeech.LANG_MISSING_DATA is -1, LANG_NOT_SUPPORTED -2). */
    fun statusForLanguageResult(result: Int): SpeechStatus =
        if (result == LANG_MISSING_DATA || result == LANG_NOT_SUPPORTED) SpeechStatus.NO_LANGUAGE else SpeechStatus.READY

    /** The one line the grown-ups' dashboard shows about speech: honest about a missing engine or missing language data. */
    fun describe(status: SpeechStatus, voiceName: String, isOffline: Boolean): String = when (status) {
        SpeechStatus.STARTING -> "Speech engine is still starting. Close and reopen this screen in a moment."
        SpeechStatus.FAILED ->
            "No speech engine found. Install or enable a text-to-speech engine in Android settings."
        SpeechStatus.NO_LANGUAGE ->
            "Language data missing: install an English voice in the text-to-speech settings."
        SpeechStatus.READY -> "$voiceName (${if (isOffline) "offline ready" else "uses the network"})"
    }

    private const val LANG_MISSING_DATA = -1
    private const val LANG_NOT_SUPPORTED = -2
}
