package com.example.earlyreader

import com.example.earlyreader.model.StoryBank
import com.example.earlyreader.storage.KeyValueStore
import com.example.earlyreader.storage.ProgressStorage
import com.example.earlyreader.ui.welcome.ReadTileInfo
import com.example.earlyreader.ui.welcome.WELCOME_GREETING_SPEECH
import com.example.earlyreader.ui.welcome.WelcomeDestination
import com.example.earlyreader.ui.welcome.appModeFromSaved
import com.example.earlyreader.ui.welcome.modeFor
import com.example.earlyreader.ui.welcome.readTileInfo
import com.example.earlyreader.ui.welcome.savedModeFor
import com.example.earlyreader.ui.welcome.welcomeProgressLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WelcomeModelTest {

    private val titles = listOf("The Red Hen", "The Big Pig", "The Sun and the Bug", "The Cat on the Mat")

    @Test
    fun tilesOpenTheRightScreens() {
        assertEquals(AppMode.TRACK_TO_READ, modeFor(WelcomeDestination.READ))
        assertEquals(AppMode.WORD_PRACTICE, modeFor(WelcomeDestination.SOUNDS))
        assertEquals(AppMode.STORY_MODE, modeFor(WelcomeDestination.STORIES))
        assertEquals(AppMode.SENTENCE_LAB, modeFor(WelcomeDestination.SENTENCE_LAB))
        // Every tile opens a different screen.
        assertEquals(4, WelcomeDestination.values().map { modeFor(it) }.toSet().size)
    }

    @Test
    fun savedModeMapsToAScreenAndOldValuesStillWork() {
        assertEquals(AppMode.STORY_MODE, appModeFromSaved(ProgressStorage.MODE_STORY))
        assertEquals(AppMode.WORD_PRACTICE, appModeFromSaved(ProgressStorage.MODE_WORDS))
        assertEquals(AppMode.SENTENCE_LAB, appModeFromSaved(ProgressStorage.MODE_SENTENCES))
        assertEquals(AppMode.TRACK_TO_READ, appModeFromSaved(ProgressStorage.MODE_TRACKING))
        // Unknown, empty, lower-case or missing values (an older version, a damaged file) mean Track to Read.
        assertEquals(AppMode.TRACK_TO_READ, appModeFromSaved("WELCOME"))
        assertEquals(AppMode.TRACK_TO_READ, appModeFromSaved("HOME"))
        assertEquals(AppMode.TRACK_TO_READ, appModeFromSaved("story"))
        assertEquals(AppMode.TRACK_TO_READ, appModeFromSaved(""))
        assertEquals(AppMode.TRACK_TO_READ, appModeFromSaved(null))
    }

    @Test
    fun everyRealModeSurvivesSavingAndLoading() {
        for (mode in AppMode.values()) {
            assertEquals(mode, appModeFromSaved(savedModeFor(mode)))
        }
        // The saved value is always one of the four real modes, never a Welcome value.
        val real = setOf(
            ProgressStorage.MODE_WORDS,
            ProgressStorage.MODE_STORY,
            ProgressStorage.MODE_TRACKING,
            ProgressStorage.MODE_SENTENCES,
        )
        assertEquals(real, AppMode.values().map { savedModeFor(it) }.toSet())
    }

    @Test
    fun lastModeSavedInProgressStorageStillReadsBack() {
        val map = HashMap<String, Any>()
        val store = object : KeyValueStore {
            override fun getInt(key: String, default: Int) = map[key] as? Int ?: default
            override fun putInt(key: String, value: Int) { map[key] = value }
            override fun getString(key: String, default: String) = map[key] as? String ?: default
            override fun putString(key: String, value: String) { map[key] = value }
            @Suppress("UNCHECKED_CAST")
            override fun getStringSet(key: String) = map[key] as? Set<String> ?: emptySet()
            override fun putStringSet(key: String, value: Set<String>) { map[key] = value }
            override fun clear() = map.clear()
        }
        val progress = ProgressStorage(store)
        assertEquals(AppMode.TRACK_TO_READ, appModeFromSaved(progress.lastAppMode))
        progress.lastAppMode = savedModeFor(AppMode.SENTENCE_LAB)
        assertEquals(AppMode.SENTENCE_LAB, appModeFromSaved(progress.lastAppMode))
        progress.lastAppMode = "SOMETHING_FROM_A_FUTURE_VERSION"
        assertEquals(AppMode.TRACK_TO_READ, appModeFromSaved(progress.lastAppMode))
    }

    @Test
    fun readTileStartsTheFirstStoryForANewChild() {
        val info = readTileInfo(titles, lastActiveStoryIndex = 0, hasOpenedReading = false)
        assertEquals(ReadTileInfo(0, "The Red Hen", continuing = false), info)
        assertEquals("Start: The Red Hen", info.caption)
        assertEquals("Read, start The Red Hen", info.description)
    }

    @Test
    fun readTileContinuesTheStoryTheChildWasOn() {
        val moved = readTileInfo(titles, lastActiveStoryIndex = 3, hasOpenedReading = false)
        assertEquals(3, moved.storyIndex)
        assertTrue(moved.continuing)
        assertEquals("Continue: The Cat on the Mat", moved.caption)
        assertEquals("Read, continue The Cat on the Mat", moved.description)
        // Even on the first story, once a reading screen has been opened the child is carrying on.
        val first = readTileInfo(titles, lastActiveStoryIndex = 0, hasOpenedReading = true)
        assertTrue(first.continuing)
        assertEquals("Continue: The Red Hen", first.caption)
    }

    @Test
    fun readTileFallsBackToTheFirstStoryForABadIndex() {
        for (bad in listOf(-1, titles.size, 99)) {
            val info = readTileInfo(titles, bad, hasOpenedReading = true)
            assertEquals(0, info.storyIndex)
            assertEquals("The Red Hen", info.storyTitle)
            assertFalse(info.continuing)
        }
        val none = readTileInfo(emptyList(), 0, hasOpenedReading = true)
        assertEquals("", none.caption)
        assertEquals("Read", none.description)
    }

    @Test
    fun readTileWorksWithTheRealStories() {
        val real = StoryBank.allStories.map { it.title }
        for (i in real.indices) {
            val info = readTileInfo(real, i, hasOpenedReading = false)
            assertEquals(real[i], info.storyTitle)
            assertTrue(info.caption.endsWith(real[i]))
        }
    }

    @Test
    fun progressLineIsHiddenUntilThereIsSomethingToShow() {
        assertNull(welcomeProgressLine(0, 14, 0))
        assertNull(welcomeProgressLine(0, 0, 0))
    }

    @Test
    fun progressLineIsGentle() {
        assertEquals("⭐ 3 of 14 stories finished", welcomeProgressLine(3, 14, 0))
        assertEquals("🔤 1 word sounded out", welcomeProgressLine(0, 14, 1))
        assertEquals("🔤 12 words sounded out", welcomeProgressLine(0, 14, 12))
        assertEquals("⭐ 1 of 14 stories finished  ·  🔤 2 words sounded out", welcomeProgressLine(1, 14, 2))
        // Never more finished than there are stories.
        assertEquals("⭐ 14 of 14 stories finished", welcomeProgressLine(20, 14, 0))
    }

    @Test
    fun greetingIsShortAndFriendly() {
        assertEquals("Hi! Let's read!", WELCOME_GREETING_SPEECH)
    }
}
