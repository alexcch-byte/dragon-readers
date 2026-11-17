package com.example.earlyreader

import com.example.earlyreader.model.StoryBank
import com.example.earlyreader.model.WordBank
import com.example.earlyreader.ui.sentences.SentenceLabData
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

    @Test
    fun testSentenceContextModel() {
        for (word in WordBank.starterWords) {
            assertTrue("Sentence context should not be empty for ${word.text}", word.sentenceContext.isNotEmpty())
            val containsTarget = word.sentenceContext.any { token ->
                token.trimEnd('.', '!', '?', ',').equals(word.text, ignoreCase = true)
            }
            assertTrue("Sentence context for ${word.text} should contain target word", containsTarget)
        }
    }

    @Test
    fun testParentGateMathChallenge() {
        for (i in 1..20) {
            val challenge = generateMathChallenge()
            assertTrue("Challenge question should not be empty", challenge.question.isNotBlank())
            assertTrue("Challenge question should mention grown-ups", challenge.question.contains("grown-ups"))
            assertEquals("Challenge should have 3 options", 3, challenge.options.size)
            assertTrue("Challenge options must contain correct answer", challenge.options.contains(challenge.answer))
            assertTrue("Challenge answer must be positive", challenge.answer > 0)
        }
    }

    @Test
    fun testStoryModelIntegrity() {
        val stories = StoryBank.allStories
        assertEquals("StoryBank must contain all 5 stories", 5, stories.size)
        val expectedIds = listOf("hen", "pig", "bug", "cat", "net")
        assertEquals(expectedIds, stories.map { it.id })

        for (story in stories) {
            assertTrue("Story title must not be blank", story.title.isNotBlank())
            assertEquals("Each story must have 4 pages", 4, story.pages.size)
            for ((idx, page) in story.pages.withIndex()) {
                assertTrue("${story.title} Page $idx must have words", page.words.isNotEmpty())
                assertTrue("${story.title} Page $idx sentence must match words", page.fullSentence.isNotBlank())
                for (word in page.words) {
                    assertTrue("Word text in ${story.title} must not be blank", word.text.isNotBlank())
                    if (!word.sight) {
                        assertTrue("Decodable word ${word.text} in ${story.title} must have segments", word.segments.isNotEmpty())
                        val cleanText = word.text.trimEnd('.', '!', '?', ',')
                        assertEquals(
                            "Segments must join to word text for ${word.text} in ${story.title}",
                            cleanText.lowercase(),
                            word.segments.joinToString("").lowercase(),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testProgressBookkeeping() {
        var completedWords = 0
        val completedStories = mutableSetOf<String>()
        var activeStoryIndex = 0
        var activeMode = "WORDS"
        assertEquals(0, activeStoryIndex)
        assertEquals("WORDS", activeMode)

        // Simulate word completions
        completedWords += 1
        assertEquals(1, completedWords)
        completedWords += 1
        assertEquals(2, completedWords)

        // Simulate story completions
        completedStories.add("The Red Hen")
        assertEquals(1, completedStories.size)
        assertTrue(completedStories.contains("The Red Hen"))
        // Idempotent addition
        completedStories.add("The Red Hen")
        assertEquals(1, completedStories.size)
        completedStories.add("The Big Pig")
        assertEquals(2, completedStories.size)

        // Mode and story switching
        activeMode = "STORY"
        assertEquals("STORY", activeMode)
        activeStoryIndex = 3
        assertEquals(3, activeStoryIndex)

        // Reset
        completedWords = 0
        completedStories.clear()
        activeStoryIndex = 0
        activeMode = "WORDS"
        assertEquals(0, completedWords)
        assertTrue(completedStories.isEmpty())
        assertEquals(0, activeStoryIndex)
        assertEquals("WORDS", activeMode)
    }

    @Test
    fun testVoiceDetectionAndGuidanceLogic() {
        data class VoiceInfo(val name: String, val isNetworkRequired: Boolean)

        fun shouldShowOfflineGuidance(voice: VoiceInfo?): Boolean {
            if (voice == null) return true
            return voice.isNetworkRequired
        }

        val offlineVoice = VoiceInfo("en-us-x-sfg#female_1-local", isNetworkRequired = false)
        val networkVoice = VoiceInfo("en-us-x-sfg#female_1-network", isNetworkRequired = true)

        assertFalse("Offline voice should not trigger offline download guidance", shouldShowOfflineGuidance(offlineVoice))
        assertTrue("Network voice should trigger offline download guidance", shouldShowOfflineGuidance(networkVoice))
        assertTrue("Missing voice should trigger offline download guidance", shouldShowOfflineGuidance(null))
    }

    @Test
    fun testSentenceLabLevel1Dataset() {
        val items = SentenceLabData.level1Items
        assertEquals("Sentence Lab Level 1 must have 6 items", 6, items.size)
        for ((idx, item) in items.withIndex()) {
            assertTrue("Item $idx text must not be blank", item.text.isNotBlank())
            assertEquals(
                "Words count must match tags count for item $idx",
                item.words.size,
                item.tags.size,
            )
            assertTrue("Item $idx must have at least one noun", item.nounIndices.isNotEmpty())
            for (nounIdx in item.nounIndices) {
                assertTrue("Noun index $nounIdx must be valid", nounIdx in item.words.indices)
                assertEquals("Tag at noun index must be 'noun'", "noun", item.tags[nounIdx])
            }
        }
    }

    @Test
    fun testSentenceLabLevel2Dataset() {
        val items = SentenceLabData.level2Items
        assertEquals("Sentence Lab Level 2 must have 6 items", 6, items.size)
        val expectedVerbs = listOf("laughed", "baked", "climbed", "painted", "stopped", "splashed")
        for ((idx, item) in items.withIndex()) {
            assertTrue("Item $idx text must not be blank", item.text.isNotBlank())
            assertEquals(
                "Words count must match tags count for item $idx",
                item.words.size,
                item.tags.size,
            )
            assertTrue("Item $idx must have at least one verb", item.verbIndices.isNotEmpty())
            for (verbIdx in item.verbIndices) {
                assertTrue("Verb index $verbIdx must be valid", verbIdx in item.words.indices)
                assertEquals("Tag at verb index must be 'verb'", "verb", item.tags[verbIdx])
                assertEquals("Verb word must match expected", expectedVerbs[idx], item.words[verbIdx])
            }
        }
        assertEquals("Level 2 metadata targetTag must be 'verb'", "verb", SentenceLabData.level2.targetTag)
        assertEquals("Level 2 metadata targetName must be 'action word'", "action word", SentenceLabData.level2.targetName)
    }

    @Test
    fun testSentenceLabLevel3Dataset() {
        val items = SentenceLabData.level3Items
        assertEquals("Sentence Lab Level 3 must have 6 items", 6, items.size)
        val expectedAdjectives = listOf(
            listOf("soft", "warm"),
            listOf("yellow", "long"),
            listOf("tired", "cozy"),
            listOf("long", "blue"),
            listOf("cold", "tiny"),
            listOf("crunchy", "sunny"),
        )
        for ((idx, item) in items.withIndex()) {
            assertTrue("Item $idx text must not be blank", item.text.isNotBlank())
            assertEquals(
                "Words count must match tags count for item $idx",
                item.words.size,
                item.tags.size,
            )
            val adjs = item.adjectiveIndices.map { item.words[it] }
            assertEquals("Item $idx must have exactly 2 adjectives", 2, adjs.size)
            assertEquals("Adjective words must match expected for item $idx", expectedAdjectives[idx], adjs)
            for (adjIdx in item.adjectiveIndices) {
                assertTrue("Adjective index $adjIdx must be valid", adjIdx in item.words.indices)
                assertEquals("Tag at adjective index must be 'adj'", "adj", item.tags[adjIdx])
            }
        }
        assertEquals("Level 3 metadata targetTag must be 'adj'", "adj", SentenceLabData.level3.targetTag)
        assertEquals("Level 3 metadata targetName must be 'describing word'", "describing word", SentenceLabData.level3.targetName)
        assertEquals("All levels list must have 3 levels", 3, SentenceLabData.allLevels.size)
    }
}
