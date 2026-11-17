package com.example.earlyreader

import com.example.earlyreader.model.StoryBank
import com.example.earlyreader.audio.SpeechStatus
import com.example.earlyreader.ui.welcome.ReadTileInfo
import com.example.earlyreader.ui.welcome.WELCOME_GREETING_SPEECH
import com.example.earlyreader.ui.welcome.WelcomeDestination
import com.example.earlyreader.ui.welcome.NavState
import com.example.earlyreader.ui.welcome.modeFor
import com.example.earlyreader.ui.welcome.readTileInfo
import com.example.earlyreader.ui.welcome.navStateFromSaved
import com.example.earlyreader.ui.welcome.speechNote
import com.example.earlyreader.ui.welcome.toSaved
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
    fun navStateStartsOnTheWelcomePage() {
        val nav = NavState()
        assertTrue(nav.showWelcome)
        assertEquals(AppMode.TRACK_TO_READ, nav.appMode)
        assertFalse(nav.canContinueReading(0))
    }

    @Test
    fun aTileOpensItsScreenAndHomeComesBack() {
        val opened = NavState().openMode(modeFor(WelcomeDestination.STORIES), storyIndex = 2)
        assertFalse(opened.showWelcome)
        assertEquals(AppMode.STORY_MODE, opened.appMode)
        // Stories mode is not Track: the Read tile has nothing to continue.
        assertFalse(opened.canContinueReading(2))

        val home = opened.goHome()
        assertTrue(home.showWelcome)
        // Home keeps the screen underneath, and Home on the Welcome page changes nothing.
        assertEquals(AppMode.STORY_MODE, home.appMode)
        assertEquals(home, home.goHome())
    }

    @Test
    fun readSaysContinueOnlyForAStoryOpenedInTrack() {
        val track = NavState().openMode(modeFor(WelcomeDestination.READ), storyIndex = 0).goHome()
        assertTrue(track.canContinueReading(0))
        assertFalse(track.canContinueReading(1))
        // Choosing another story while on Track starts that story's own Track place.
        val picked = NavState().openMode(AppMode.TRACK_TO_READ, 0).storySelected(3)
        assertTrue(picked.canContinueReading(3))
        // Choosing a story in Stories mode does not.
        val other = NavState().openMode(AppMode.STORY_MODE, 0).storySelected(3)
        assertFalse(other.canContinueReading(3))
        // Moving on from a finished story drops its place.
        assertFalse(picked.trackPlaceDropped(3).canContinueReading(3))
    }

    @Test
    fun resetGoesBackToAFreshWelcomePage() {
        val busy = NavState().openMode(AppMode.TRACK_TO_READ, 4).openMode(AppMode.SENTENCE_LAB, 4)
        assertEquals(NavState(), busy.reset())
    }

    @Test
    fun navStateSurvivesSavingAndRestoring() {
        val nav = NavState(showWelcome = false, appMode = AppMode.SENTENCE_LAB, trackOpened = setOf(0, 3, 12))
        assertEquals(nav, navStateFromSaved(nav.toSaved()))
        assertEquals(NavState(), navStateFromSaved(NavState().toSaved()))
        // A damaged or unknown saved value falls back to the defaults instead of crashing.
        assertEquals(NavState(), navStateFromSaved(emptyList()))
        assertEquals(NavState(), navStateFromSaved(listOf("x", "NOT_A_MODE", "a,b")))
    }

    @Test
    fun readTileStartsTheFirstStoryForANewChild() {
        val info = readTileInfo(titles, lastActiveStoryIndex = 0, trackOpened = emptySet())
        assertEquals(ReadTileInfo(0, "The Red Hen", continuing = false), info)
        assertEquals("Start: The Red Hen", info.caption)
        assertEquals("Read, start The Red Hen", info.description)
    }

    @Test
    fun readTileContinuesOnlyWhereTrackWasOpened() {
        val moved = readTileInfo(titles, lastActiveStoryIndex = 3, trackOpened = setOf(3))
        assertEquals(3, moved.storyIndex)
        assertTrue(moved.continuing)
        assertEquals("Continue: The Cat on the Mat", moved.caption)
        assertEquals("Read, continue The Cat on the Mat", moved.description)
        val first = readTileInfo(titles, lastActiveStoryIndex = 0, trackOpened = setOf(0))
        assertTrue(first.continuing)
        assertEquals("Continue: The Red Hen", first.caption)
        // A later story that Track has not been opened on (a saved story from last time, or one only opened in
        // Stories mode) starts at page 1, so it says Start.
        val fresh = readTileInfo(titles, lastActiveStoryIndex = 3, trackOpened = setOf(0))
        assertFalse(fresh.continuing)
        assertEquals("Start: The Cat on the Mat", fresh.caption)
    }

    @Test
    fun readTileFallsBackToTheFirstStoryForABadIndex() {
        for (bad in listOf(-1, titles.size, 99)) {
            val info = readTileInfo(titles, bad, trackOpened = setOf(bad, 0))
            assertEquals(0, info.storyIndex)
            assertEquals("The Red Hen", info.storyTitle)
            assertFalse(info.continuing)
        }
        val none = readTileInfo(emptyList(), 0, trackOpened = setOf(0))
        assertEquals("", none.caption)
        assertEquals("Read", none.description)
    }

    @Test
    fun readTileWorksWithTheRealStories() {
        val real = StoryBank.allStories.map { it.title }
        for (i in real.indices) {
            val info = readTileInfo(real, i, trackOpened = emptySet())
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
    fun progressLineIsGentleAndHonestAboutRepeats() {
        assertEquals("⭐ 3 of 14 stories finished", welcomeProgressLine(3, 14, 0))
        assertEquals("🔤 1 sound-out", welcomeProgressLine(0, 14, 1))
        assertEquals("🔤 12 sound-outs", welcomeProgressLine(0, 14, 12))
        assertEquals("⭐ 1 of 14 stories finished  ·  🔤 2 sound-outs", welcomeProgressLine(1, 14, 2))
        // Never more finished than there are stories.
        assertEquals("⭐ 14 of 14 stories finished", welcomeProgressLine(20, 14, 0))
    }

    @Test
    fun speechNoteAppearsOnlyWhenSpeechCannotWork() {
        assertNull(speechNote(SpeechStatus.READY))
        assertNull(speechNote(SpeechStatus.STARTING))
        val expected = "No voice on this device yet. A grown-up can fix this in settings."
        assertEquals(expected, speechNote(SpeechStatus.FAILED))
        assertEquals(expected, speechNote(SpeechStatus.NO_LANGUAGE))
    }

    @Test
    fun greetingIsShortAndFriendly() {
        assertEquals("Hi! Let's read!", WELCOME_GREETING_SPEECH)
    }
}
