package com.example.earlyreader.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Android text-to-speech for the reader.
 *
 * Sounds and words are QUEUED (QUEUE_ADD) so back-to-back sounds in a drag are all heard and the word
 * that finishes a scrub never cuts off the last sound. Only a user-asked sentence/word (the help button,
 * a tap on the sentence) or the start of a new scrub from the beginning flushes whatever is playing.
 * Progress never depends on speech: callers wait with [awaitIdle], which gives up after a timeout.
 */
class SpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    @Volatile
    var isInitialized: Boolean = false
        private set

    @Volatile
    private var initFailed = false

    var voiceName: String? = null
        private set

    var isOfflineVoice: Boolean = false
        private set

    var isNetworkVoiceOnly: Boolean = false
        private set

    /** Ids of utterances handed to the engine that have not finished, with the time they were queued. */
    private val active = ConcurrentHashMap<String, Long>()

    private data class Pending(val text: String, val flush: Boolean, val isPhoneme: Boolean)

    /** The one utterance asked for before the engine finished starting up (the newest wins). */
    private var pending: Pending? = null
    private var counter = 0L

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {}

        override fun onDone(utteranceId: String?) {
            utteranceId?.let { active.remove(it) }
        }

        @Deprecated("Deprecated in the framework; kept because it is abstract", ReplaceWith("onError(utteranceId, errorCode)"))
        override fun onError(utteranceId: String?) {
            utteranceId?.let { active.remove(it) }
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            utteranceId?.let { active.remove(it) }
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            utteranceId?.let { active.remove(it) }
        }
    }

    override fun onInit(status: Int) {
        val engine = tts ?: return
        if (status != TextToSpeech.SUCCESS) {
            initFailed = true
            synchronized(this) { pending = null }
            return
        }
        engine.language = Locale.US
        engine.setSpeechRate(0.8f)
        engine.setOnUtteranceProgressListener(progressListener)

        val choice = try {
            val voices = engine.voices?.toList()
            val choice = VoiceSelection.choose(
                voices?.map { VoiceCandidate(it.name, it.locale?.language, it.isNetworkConnectionRequired) },
            )
            choice.index?.let { engine.voice = voices!![it] }
            choice
        } catch (_: Exception) {
            VoiceSelection.defaultChoice()
        }
        voiceName = choice.name
        isOfflineVoice = choice.isOffline
        isNetworkVoiceOnly = choice.isNetworkOnly
        isInitialized = true

        // Something was asked for while the engine was starting: say the newest request now instead of dropping it.
        val waiting = synchronized(this) { pending.also { pending = null } }
        if (waiting != null) play(engine, waiting.text, waiting.flush, waiting.isPhoneme)
    }

    /**
     * Says a whole word or sentence. With [queue] false (the default, for a help or sentence tap) whatever is
     * playing is cut off; with [queue] true (the word that completes a scrub) it waits for the sounds before it.
     */
    fun speakWord(text: String, queue: Boolean = false) {
        enqueue(text, flush = !queue, isPhoneme = false)
    }

    /**
     * Says the sound of one segment. It is queued behind the sounds before it, so every sound of a drag is heard in
     * full; [flush] is true only for the first sound of a new scrub from the start of the word (it clears
     * anything still queued from the last one).
     */
    fun speakPhoneme(segment: String, flush: Boolean = false) {
        if (segment.isBlank()) return
        val sound = phonemeSounds[segment.lowercase()] ?: segment
        enqueue(sound, flush = flush, isPhoneme = true)
    }

    private fun enqueue(text: String, flush: Boolean, isPhoneme: Boolean) {
        if (text.isBlank()) return
        val engine = tts ?: return
        if (initFailed) return
        if (!isInitialized) {
            // Not ready yet: keep the newest request and say it as soon as the engine is up.
            synchronized(this) { pending = Pending(text, flush, isPhoneme) }
            return
        }
        play(engine, text, flush, isPhoneme)
    }

    private fun play(engine: TextToSpeech, text: String, flush: Boolean, isPhoneme: Boolean) {
        pruneStale()
        // A child dragging faster than the sounds can be said: one sound plays and one waits; more are dropped
        // rather than piling up and being heard long after the finger has moved on.
        if (isPhoneme && !flush && queuedPhonemes() >= MAX_QUEUED_PHONEMES) return
        if (flush) active.clear()
        val id = (if (isPhoneme) "p" else "w") + (++counter)
        active[id] = System.currentTimeMillis()
        val mode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        if (engine.speak(text, mode, null, id) == TextToSpeech.ERROR) active.remove(id)
    }

    private fun queuedPhonemes(): Int = active.keys.count { it.startsWith("p") }

    private fun pruneStale() {
        val cutoff = System.currentTimeMillis() - STALE_AFTER_MS
        active.entries.removeIf { it.value < cutoff }
    }

    /** True when nothing is playing or waiting (or speech can never happen), so there is nothing to wait for. */
    fun isIdle(): Boolean {
        if (tts == null || initFailed) return true
        pruneStale()
        val waitingForEngine = !isInitialized && synchronized(this) { pending != null }
        return !waitingForEngine && active.isEmpty()
    }

    /**
     * Suspends until what was just asked for has been said (plus a short pause so it doesn't feel rushed), but never
     * longer than [timeoutMs]: a story advances when this returns, so a muted, broken or slow engine can't stall it.
     */
    suspend fun awaitIdle(timeoutMs: Long = AWAIT_TIMEOUT_MS) {
        val start = System.currentTimeMillis()
        delay(MIN_WAIT_MS)
        while (!isIdle() && System.currentTimeMillis() - start < timeoutMs) delay(POLL_MS)
        delay(AFTER_SPEECH_GAP_MS)
    }

    fun stop() {
        synchronized(this) { pending = null }
        active.clear()
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    companion object {
        private const val MAX_QUEUED_PHONEMES = 2
        private const val STALE_AFTER_MS = 8_000L
        private const val AWAIT_TIMEOUT_MS = 4_000L
        private const val MIN_WAIT_MS = 250L
        private const val POLL_MS = 50L
        private const val AFTER_SPEECH_GAP_MS = 200L

        // Short synthetic-phonics respellings (a bare letter would be read by its name). Speech engines cannot
        // say an isolated short a (/æ/), so "ah" stays for a; the short o is respelled "aw" so that "hat" and
        // "hot" (and "cat"/"cot") no longer sound out the same. A digraph or double letter is ONE entry
        // (ck, ll, ng, qu, ...), and stories segment words that way, so it makes one sound.
        val phonemeSounds: Map<String, String> = mapOf(
            "a" to "ah", "e" to "eh", "i" to "ih", "o" to "aw", "u" to "uh",
            "b" to "buh", "c" to "kuh", "d" to "duh", "f" to "fff", "g" to "guh",
            "h" to "huh", "j" to "juh", "k" to "kuh", "l" to "luh", "m" to "mmm",
            "n" to "nnn", "p" to "puh", "q" to "kwuh", "qu" to "kwuh", "r" to "ruh", "s" to "sss",
            "t" to "tuh", "v" to "vuh", "w" to "wuh", "x" to "ks", "y" to "yuh", "z" to "zzz",
            "sh" to "shh", "ch" to "chuh", "th" to "thuh", "ng" to "ing",
            "ck" to "kuh", "ll" to "luh", "ss" to "sss", "ff" to "fff", "zz" to "zzz",
        )

        /** Pairs of adjacent single-letter segments that must be one segment instead (checked by the unit tests). */
        val digraphs: Set<String> = setOf("ck", "ll", "ss", "ff", "zz", "ng", "nk", "sh", "ch", "th", "qu")
    }
}
