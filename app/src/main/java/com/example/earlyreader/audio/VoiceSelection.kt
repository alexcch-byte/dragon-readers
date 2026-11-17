package com.example.earlyreader.audio

/** A text-to-speech voice reduced to what the choice depends on, so the logic can be unit tested without Android. */
data class VoiceCandidate(
    val name: String,
    val language: String?,
    val networkRequired: Boolean,
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

object VoiceSelection {
    const val DEFAULT_VOICE_NAME = "Default system voice (US English)"

    /**
     * Prefers an English voice stored on the device (a network voice needs internet and sends the text to
     * a speech service); falls back to any English voice, then to the engine default.
     */
    fun choose(candidates: List<VoiceCandidate>?): VoiceChoice {
        if (candidates == null) return defaultChoice()
        val english = candidates.withIndex().filter { (_, v) -> v.language.equals("en", ignoreCase = true) }
        val chosen = english.firstOrNull { (_, v) -> !v.networkRequired } ?: english.firstOrNull()
            ?: return defaultChoice()
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
}
