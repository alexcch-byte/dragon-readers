package com.example.earlyreader.ui.welcome

import com.example.earlyreader.AppMode
import com.example.earlyreader.audio.SpeechStatus

/** The four big tiles on the Welcome page, in the order they are shown. */
enum class WelcomeDestination { READ, SOUNDS, STORIES, SENTENCE_LAB }

/** Which screen each Welcome tile opens. */
fun modeFor(destination: WelcomeDestination): AppMode = when (destination) {
    WelcomeDestination.READ -> AppMode.TRACK_TO_READ
    WelcomeDestination.SOUNDS -> AppMode.WORD_PRACTICE
    WelcomeDestination.STORIES -> AppMode.STORY_MODE
    WelcomeDestination.SENTENCE_LAB -> AppMode.SENTENCE_LAB
}

/**
 * Where the child is in the app, as a pure value so the Home / Back / Reset / tile transitions can be unit tested.
 * The Welcome page is not a mode: it is shown over the modes ([showWelcome]) and each screen keeps its place underneath.
 * [trackOpened] holds the stories the child has opened Track to Read on (the screen the Read tile opens), so the Read
 * tile only says "Continue" for a story whose Track screen really has a place to carry on from.
 */
data class NavState(
    val showWelcome: Boolean = true,
    val appMode: AppMode = AppMode.TRACK_TO_READ,
    val trackOpened: Set<Int> = emptySet(),
) {
    /** A Welcome tile or a mode chip was tapped for [mode] while [storyIndex] is the selected story. */
    fun openMode(mode: AppMode, storyIndex: Int): NavState = copy(
        showWelcome = false,
        appMode = mode,
        trackOpened = if (mode == AppMode.TRACK_TO_READ) trackOpened + storyIndex else trackOpened,
    )

    /** Home (or Back) opens the Welcome page; it changes nothing else. */
    fun goHome(): NavState = if (showWelcome) this else copy(showWelcome = true)

    /** Another story was picked: on the Track screen that story is now the one being tracked. */
    fun storySelected(storyIndex: Int): NavState =
        if (!showWelcome && appMode == AppMode.TRACK_TO_READ) copy(trackOpened = trackOpened + storyIndex) else this

    /** [storyIndex] was finished and moved on from: its saved Track place is dropped, so there is nothing to continue. */
    fun trackPlaceDropped(storyIndex: Int): NavState = copy(trackOpened = trackOpened - storyIndex)

    /** "Reset all progress": nothing to continue, back on the Welcome page. */
    fun reset(): NavState = NavState()

    /** Whether the Read tile for [storyIndex] has a Track place to carry on from. */
    fun canContinueReading(storyIndex: Int): Boolean = storyIndex in trackOpened
}

/** Saves a [NavState] as plain values (it lives in rememberSaveable, so it survives rotation and process death). */
internal fun NavState.toSaved(): List<Any> = listOf(showWelcome, appMode.name, trackOpened.sorted().joinToString(","))

internal fun navStateFromSaved(saved: List<Any>): NavState = NavState(
    showWelcome = saved.getOrNull(0) as? Boolean ?: true,
    appMode = AppMode.values().firstOrNull { it.name == saved.getOrNull(1) } ?: AppMode.TRACK_TO_READ,
    trackOpened = (saved.getOrNull(2) as? String).orEmpty().split(',').mapNotNull { it.toIntOrNull() }.toSet(),
)

/** What the "Read" tile opens: which story, and whether the child is carrying on (so it says "Continue"). */
data class ReadTileInfo(val storyIndex: Int, val storyTitle: String, val continuing: Boolean) {
    /** The small line under the label. */
    val caption: String
        get() = when {
            storyTitle.isEmpty() -> ""
            continuing -> "Continue: $storyTitle"
            else -> "Start: $storyTitle"
        }

    /** What a screen reader says, e.g. "Read, continue The Cat on the Mat". */
    val description: String
        get() = when {
            storyTitle.isEmpty() -> "Read"
            continuing -> "Read, continue $storyTitle"
            else -> "Read, start $storyTitle"
        }
}

/**
 * Decides the Read tile, which opens Track to Read on the story the child was on ([lastActiveStoryIndex]). It says
 * "Continue" only when [trackOpened] (the stories whose Track screen the child has opened) includes that story, so the
 * caption is true: Track keeps its own place per story, and a story only opened in Stories mode, or one left from a
 * previous run of the app, starts again at page 1 ("Start"). An index that is out of range counts as the first story.
 */
fun readTileInfo(titles: List<String>, lastActiveStoryIndex: Int, trackOpened: Set<Int>): ReadTileInfo {
    if (titles.isEmpty()) return ReadTileInfo(0, "", false)
    val valid = lastActiveStoryIndex in titles.indices
    val index = if (valid) lastActiveStoryIndex else 0
    return ReadTileInfo(index, titles[index], valid && index in trackOpened)
}

/**
 * The small progress line on the Welcome page, or null when there is nothing to show yet (so a new child sees no
 * numbers at all). Gentle wording, small counts.
 */
fun welcomeProgressLine(finishedStories: Int, totalStories: Int, soundOuts: Int): String? {
    val parts = mutableListOf<String>()
    if (finishedStories > 0) {
        parts += if (totalStories > 0) {
            "⭐ ${finishedStories.coerceAtMost(totalStories)} of $totalStories stories finished"
        } else {
            "⭐ $finishedStories stories finished"
        }
    }
    if (soundOuts > 0) {
        // Every finished word counts, including the same word done again, so it is "sound-outs", not "words".
        parts += if (soundOuts == 1) "🔤 1 sound-out" else "🔤 $soundOuts sound-outs"
    }
    return if (parts.isEmpty()) null else parts.joinToString("  ·  ")
}

/**
 * A small, calm note for the Welcome page when speech cannot work on this device, so a silent app is not a mystery.
 * Null while the engine is READY or still STARTING (nothing is wrong yet).
 */
fun speechNote(status: SpeechStatus): String? = when (status) {
    SpeechStatus.FAILED, SpeechStatus.NO_LANGUAGE -> "No voice on this device yet. A grown-up can fix this in settings."
    SpeechStatus.READY, SpeechStatus.STARTING -> null
}

/** What the mascot says when it is tapped. Never spoken by itself. */
const val WELCOME_GREETING_SPEECH = "Hi! Let's read!"
