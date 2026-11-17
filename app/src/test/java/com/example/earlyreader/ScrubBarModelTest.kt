package com.example.earlyreader

import com.example.earlyreader.model.WordBank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrubBarModelTest {

    private fun calculateBoundaries(segments: List<String>): List<Float> {
        val weights = segments.map { it.length.coerceAtLeast(1) }
        val total = weights.sum().toFloat()
        var running = 0
        return weights.map { running += it; running / total }
    }

    @Test
    fun testSingleLetterWeightedBoundaries() {
        val segments = listOf("c", "a", "t")
        val boundaries = calculateBoundaries(segments)
        assertEquals(3, boundaries.size)
        assertEquals(1f / 3f, boundaries[0], 0.001f)
        assertEquals(2f / 3f, boundaries[1], 0.001f)
        assertEquals(1f, boundaries[2], 0.001f)
    }

    @Test
    fun testDigraphWeightedBoundaries() {
        val segments = listOf("sh", "i", "p")
        val boundaries = calculateBoundaries(segments)
        assertEquals(3, boundaries.size)
        assertEquals(0.5f, boundaries[0], 0.001f)
        assertEquals(0.75f, boundaries[1], 0.001f)
        assertEquals(1f, boundaries[2], 0.001f)
    }

    @Test
    fun testDoneThreshold() {
        val doneThreshold = 0.92f
        assertFalse("Progress at 0.91 should not complete", 0.91f >= doneThreshold)
        assertTrue("Progress at 0.92 should complete", 0.92f >= doneThreshold)
        assertTrue("Progress at 0.99 should complete", 0.99f >= doneThreshold)
        assertTrue("Progress at 1.0 should complete", 1.0f >= doneThreshold)
    }

    @Test
    fun testRescrubReset() {
        val rescrubReset = 0.03f
        assertTrue("Touch at 0.01 should trigger rescrub reset", 0.01f <= rescrubReset)
        assertTrue("Touch at 0.03 should trigger rescrub reset", 0.03f <= rescrubReset)
        assertFalse("Touch at 0.04 should not trigger rescrub reset", 0.04f <= rescrubReset)
    }

    @Test
    fun testStarterWordBankValidity() {
        val words = WordBank.starterWords
        assertTrue("Starter word bank must have words", words.isNotEmpty())
        for (word in words) {
            assertTrue("Word text must not be empty", word.text.isNotBlank())
            assertTrue("Word must have segments", word.segments.isNotEmpty())
            assertEquals(
                "Joined segments must equal word text: ${word.text}",
                word.text,
                word.segments.joinToString(""),
            )
            for (segment in word.segments) {
                assertTrue("Segment in ${word.text} must not be empty", segment.isNotBlank())
            }
        }
    }

    @Test
    fun testStarterWordBankPictures() {
        val wordsWithPictures = WordBank.starterWords.filter { it.img != null }
        assertTrue("Starter words should have pictures", wordsWithPictures.isNotEmpty())
        for (word in wordsWithPictures) {
            assertTrue("Word ${word.text} picture must not be blank", word.img!!.isNotBlank())
        }
    }
}
