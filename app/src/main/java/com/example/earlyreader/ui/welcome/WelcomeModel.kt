package com.example.earlyreader.ui.welcome

import com.example.earlyreader.AppMode
import com.example.earlyreader.storage.ProgressStorage

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
 * The last real screen, as it is saved on the device. Anything unknown (a value from an older version, a missing or
 * damaged one) means Track to Read, the main screen. Home and the Welcome page are never saved as a mode, so
 * the saved value always stays the last real screen.
 */
fun appModeFromSaved(saved: String?): AppMode = when (saved) {
    ProgressStorage.MODE_STORY -> AppMode.STORY_MODE
    ProgressStorage.MODE_WORDS -> AppMode.WORD_PRACTICE
    ProgressStorage.MODE_SENTENCES -> AppMode.SENTENCE_LAB
    else -> AppMode.TRACK_TO_READ
}

/** The value saved for [mode]; the inverse of [appModeFromSaved]. */
fun savedModeFor(mode: AppMode): String = when (mode) {
    AppMode.STORY_MODE -> ProgressStorage.MODE_STORY
    AppMode.TRACK_TO_READ -> ProgressStorage.MODE_TRACKING
    AppMode.SENTENCE_LAB -> ProgressStorage.MODE_SENTENCES
    AppMode.WORD_PRACTICE -> ProgressStorage.MODE_WORDS
}

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
 * Decides the Read tile. The child carries on with the story they were on ([lastActiveStoryIndex]) when they have
 * already moved on from the first story or have opened a reading screen since the app started
 * ([hasOpenedReading]); otherwise it starts the first story. An index that is out of range counts as the first story.
 */
fun readTileInfo(titles: List<String>, lastActiveStoryIndex: Int, hasOpenedReading: Boolean): ReadTileInfo {
    if (titles.isEmpty()) return ReadTileInfo(0, "", false)
    val valid = lastActiveStoryIndex in titles.indices
    val index = if (valid) lastActiveStoryIndex else 0
    val continuing = valid && (index > 0 || hasOpenedReading)
    return ReadTileInfo(index, titles[index], continuing)
}

/**
 * The small progress line on the Welcome page, or null when there is nothing to show yet (so a new child sees no
 * numbers at all). Gentle wording, small counts.
 */
fun welcomeProgressLine(finishedStories: Int, totalStories: Int, wordsSounded: Int): String? {
    val parts = mutableListOf<String>()
    if (finishedStories > 0) {
        parts += if (totalStories > 0) {
            "⭐ ${finishedStories.coerceAtMost(totalStories)} of $totalStories stories finished"
        } else {
            "⭐ $finishedStories stories finished"
        }
    }
    if (wordsSounded > 0) {
        parts += if (wordsSounded == 1) "🔤 1 word sounded out" else "🔤 $wordsSounded words sounded out"
    }
    return if (parts.isEmpty()) null else parts.joinToString("  ·  ")
}

/** What the mascot says when it is tapped. Never spoken by itself. */
const val WELCOME_GREETING_SPEECH = "Hi! Let's read!"
