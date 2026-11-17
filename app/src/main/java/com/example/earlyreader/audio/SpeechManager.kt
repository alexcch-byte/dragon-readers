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

    /** Whether speech can work: still starting, ready, no engine, or no English language data. */
    @Volatile
    var speechStatus: SpeechStatus = SpeechStatus.STARTING
        private set

    var voiceName: String? = null
        private set

    var isOfflineVoice: Boolean = false
        private set

    var isNetworkVoiceOnly: Boolean = false
        private set

    /** An utterance handed to the engine that has not finished: when it was queued and about how long it should take. */
    private data class Active(val queuedAt: Long, val expectedMs: Long)

    /** Ids of utterances handed to the engine that have not finished. */
    private val active = ConcurrentHashMap<String, Active>()

    private data class Pending(val text: String, val flush: Boolean, val isPhoneme: Boolean, val rate: Float)

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
            speechStatus = SpeechStatus.FAILED
            synchronized(this) { pending = null }
            return
        }
        val languageStatus = try {
            VoiceSelection.statusForLanguageResult(engine.setLanguage(Locale.US))
        } catch (_: Exception) {
            SpeechStatus.NO_LANGUAGE
        }
        engine.setSpeechRate(WORD_RATE)
        engine.setOnUtteranceProgressListener(progressListener)

        val choice = try {
            val voices = engine.voices?.toList()
            val choice = VoiceSelection.choose(
                voices?.map {
                    VoiceCandidate(
                        name = it.name,
                        language = it.locale?.language,
                        networkRequired = it.isNetworkConnectionRequired,
                        country = it.locale?.country,
                        installed = !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED),
                    )
                },
            )
            choice.index?.let { engine.voice = voices!![it] }
            choice
        } catch (_: Exception) {
            VoiceSelection.defaultChoice()
        }
        voiceName = choice.name
        isOfflineVoice = choice.isOffline
        isNetworkVoiceOnly = choice.isNetworkOnly
        // An installed English voice was found and selected, so English works whatever the language call said.
        speechStatus = if (choice.index != null) SpeechStatus.READY else languageStatus
        isInitialized = true

        // Something was asked for while the engine was starting: say the newest request now instead of dropping it.
        val waiting = synchronized(this) { pending.also { pending = null } }
        if (waiting != null) play(engine, waiting.text, waiting.flush, waiting.isPhoneme, waiting.rate)
    }

    /**
     * Says one word at the normal word speed. With [queue] false (the default, for a help tap) whatever is
     * playing is cut off; with [queue] true (the word that completes a scrub) it waits for the sounds before it.
     */
    fun speakWord(text: String, queue: Boolean = false) {
        enqueue(spokenForm(text), flush = !queue, isPhoneme = false, rate = WORD_RATE)
    }

    /** Reads a whole story or practice sentence slowly (half the word speed), cutting off whatever is playing. */
    fun speakSentence(text: String) {
        enqueue(spokenForm(text), flush = true, isPhoneme = false, rate = SENTENCE_RATE)
    }

    /** Reads an instruction line (Sentence Lab) at a comfortable speed, a little slower than a word, cutting off what is playing. */
    fun speakPrompt(text: String) {
        enqueue(spokenForm(text), flush = true, isPhoneme = false, rate = PROMPT_RATE)
    }

    /**
     * Says the sound of one segment. It is queued behind the sounds before it, so every sound of a drag is heard in
     * full; [flush] is true only for the first sound of a new scrub from the start of the word (it clears
     * anything still queued from the last one).
     */
    fun speakPhoneme(segment: String, flush: Boolean = false) {
        if (segment.isBlank()) return
        val sound = phonemeSounds[segment.lowercase()] ?: segment
        enqueue(sound, flush = flush, isPhoneme = true, rate = WORD_RATE)
    }

    private fun enqueue(text: String, flush: Boolean, isPhoneme: Boolean, rate: Float) {
        if (text.isBlank()) return
        val engine = tts ?: return
        if (initFailed) return
        if (!isInitialized) {
            // Not ready yet: keep the newest request and say it as soon as the engine is up.
            synchronized(this) { pending = Pending(text, flush, isPhoneme, rate) }
            return
        }
        play(engine, text, flush, isPhoneme, rate)
    }

    private fun play(engine: TextToSpeech, text: String, flush: Boolean, isPhoneme: Boolean, rate: Float) {
        pruneStale()
        // A child dragging faster than the sounds can be said: one sound plays and one waits; more are dropped
        // rather than piling up and being heard long after the finger has moved on.
        if (isPhoneme && !flush && queuedPhonemes() >= MAX_QUEUED_PHONEMES) return
        if (flush) active.clear()
        val id = (if (isPhoneme) "p" else "w") + (++counter)
        active[id] = Active(System.currentTimeMillis(), estimatedSpeechMs(text, rate))
        val mode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        engine.setSpeechRate(rate)   // applies to this utterance; words and sentences use different speeds
        if (engine.speak(text, mode, null, id) == TextToSpeech.ERROR) active.remove(id)
    }

    private fun queuedPhonemes(): Int = active.keys.count { it.startsWith("p") }

    // An utterance whose "done" callback never arrived is dropped after a while, but a long slow sentence that is
    // still being said is never dropped early: its cutoff grows with its estimated length.
    private fun pruneStale() {
        val now = System.currentTimeMillis()
        active.entries.removeIf { now - it.value.queuedAt > staleAfterMs(it.value.expectedMs) }
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
     * Without an explicit [timeoutMs] a word waits at most [AWAIT_TIMEOUT_MS] and a long slow sentence waits long
     * enough for its estimated length (see [awaitTimeoutMs]).
     */
    suspend fun awaitIdle(timeoutMs: Long? = null) {
        val start = System.currentTimeMillis()
        val limit = timeoutMs ?: awaitTimeoutMs(active.values.maxOfOrNull { it.expectedMs } ?: 0L)
        delay(MIN_WAIT_MS)
        while (!isIdle() && System.currentTimeMillis() - start < limit) delay(POLL_MS)
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
        private const val WORD_RATE = 0.8f
        private const val SENTENCE_RATE = 0.4f   // half the word speed
        private const val PROMPT_RATE = 0.6f     // instructions: a bit slower than a word, not as slow as a story sentence
        private const val STALE_AFTER_MS = 8_000L
        const val AWAIT_TIMEOUT_MS = 4_000L
        // Rough speaking time per character at rate 1.0 (a typical voice says about 11 characters a second);
        // speech at a lower rate takes proportionally longer. Only used to size the time-outs below, never to pace speech.
        private const val MS_PER_CHAR_AT_FULL_RATE = 90L

        /** About how long [text] takes to say at the engine speech [rate] (1.0 = normal), in milliseconds. */
        fun estimatedSpeechMs(text: String, rate: Float): Long =
            (text.length * MS_PER_CHAR_AT_FULL_RATE / rate.coerceAtLeast(0.1f)).toLong()

        /** How long a queued utterance of [expectedMs] may go without finishing before it is assumed lost. */
        fun staleAfterMs(expectedMs: Long): Long = maxOf(STALE_AFTER_MS, expectedMs * 2 + 4_000L)

        /** How long [awaitIdle] waits for speech of [expectedMs]: at least [AWAIT_TIMEOUT_MS], longer for a long sentence. */
        fun awaitTimeoutMs(expectedMs: Long): Long = maxOf(AWAIT_TIMEOUT_MS, expectedMs * 3 / 2 + 1_000L)
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
            "sh" to "shh", "ch" to "chuh", "th" to "thuh", "ng" to "ing", "nk" to "ink",
            "ck" to "kuh", "ll" to "luh", "ss" to "sss", "ff" to "fff", "zz" to "zzz",
        )

        /** Pairs of adjacent single-letter segments that must be one segment instead (checked by the unit tests). */
        val digraphs: Set<String> = setOf("ck", "ll", "ss", "ff", "zz", "ng", "nk", "sh", "ch", "th", "qu")
    }
}
