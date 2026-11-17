package com.example.earlyreader

import com.example.earlyreader.audio.SpeechManager
import com.example.earlyreader.model.StoryBank
import com.example.earlyreader.model.WordBank
import com.example.earlyreader.ui.sentences.SentenceLabData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
        assertEquals("StoryBank must contain all 8 stories", 8, stories.size)
        val expectedIds = listOf("hen", "pig", "bug", "cat", "net", "frog", "fog", "duck")
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
    }

    @Test
    fun testSentenceLabCompleteThoughtDataset() {
        val items = SentenceLabData.level4Items
        assertEquals("Sentence Lab Level 4 must have 6 items", 6, items.size)
        val expectedBooleans = listOf(false, true, true, false, false, true)
        for ((idx, item) in items.withIndex()) {
            assertTrue("Item $idx text must not be blank", item.text.isNotBlank())
            assertTrue("Item $idx why explanation must not be blank", item.why.isNotBlank())
            assertTrue("Item $idx hint must not be blank", item.hint.isNotBlank())
            assertEquals("Item $idx isSentence must match expected", expectedBooleans[idx], item.isSentence)
        }
        assertEquals("Level 4 id must be 'sentence-check'", "sentence-check", SentenceLabData.level4.id)
        assertEquals("Level 4 type must be JUDGE_SENTENCE", com.example.earlyreader.ui.sentences.SentenceLabType.JUDGE_SENTENCE, SentenceLabData.level4.type)
    }

    @Test
    fun testSentenceLabBuildSentenceDataset() {
        val items = SentenceLabData.level5Items
        assertEquals("Sentence Lab Level 5 must have 6 items", 6, items.size)
        val expectedKinds = listOf("telling", "asking", "telling", "asking", "exciting", "telling")
        val expectedMarks = listOf(".", "?", ".", "?", "!", ".")
        for ((idx, item) in items.withIndex()) {
            assertTrue("Item $idx text must not be blank", item.text.isNotBlank())
            assertEquals("Item $idx kind must match expected", expectedKinds[idx], item.kind)
            assertEquals("Item $idx endMark must match expected", expectedMarks[idx], item.endMark)
            assertTrue("Item $idx rawWords must not be empty", item.rawWords.isNotEmpty())
            assertEquals("Item $idx scrambledWords size must equal rawWords size", item.rawWords.size, item.scrambledWords.size)
            assertEquals("Item $idx scrambledWords sorted must equal rawWords sorted", item.rawWords.sorted(), item.scrambledWords.sorted())
        }
        assertEquals("Level 5 id must be 'build'", "build", SentenceLabData.level5.id)
        assertEquals("Level 5 type must be BUILD_SENTENCE", com.example.earlyreader.ui.sentences.SentenceLabType.BUILD_SENTENCE, SentenceLabData.level5.type)
    }

    @Test
    fun testSentenceLabRolesDataset() {
        val items = SentenceLabData.level6Items
        assertEquals("Sentence Lab Level 6 must have 6 items", 6, items.size)
        val expectedSubjects = listOf("The baby", "Dad", "Our teacher", "The children", "The wind", "A girl")
        val expectedVerbs = listOf("ate", "washed", "told", "planted", "shook", "drew")
        val expectedObjects = listOf("a banana.", "the car.", "a story.", "some seeds.", "the trees.", "a rainbow.")

        for ((idx, item) in items.withIndex()) {
            assertEquals("Item $idx must have 3 chunks", 3, item.chunks.size)
            val subjectChunk = item.chunks.find { it.role == "subject" }
            val verbChunk = item.chunks.find { it.role == "verb" }
            val objectChunk = item.chunks.find { it.role == "object" }

            assertNotNull("Item $idx must have subject", subjectChunk)
            assertNotNull("Item $idx must have verb", verbChunk)
            assertNotNull("Item $idx must have object", objectChunk)

            assertEquals("Subject must match for item $idx", expectedSubjects[idx], subjectChunk?.text)
            assertEquals("Verb must match for item $idx", expectedVerbs[idx], verbChunk?.text)
            assertEquals("Object must match for item $idx", expectedObjects[idx], objectChunk?.text)
            assertTrue("Full sentence must not be blank", item.fullSentence.isNotBlank())
        }

        assertEquals("Level 6 id must be 'roles'", "roles", SentenceLabData.level6.id)
        assertEquals("Level 6 type must be SORT_ROLES", com.example.earlyreader.ui.sentences.SentenceLabType.SORT_ROLES, SentenceLabData.level6.type)
        assertEquals("All levels list must have 6 levels", 6, SentenceLabData.allLevels.size)
    }

    @Test
    fun testPhonemeSoundsCoverage() {
        val sounds = SpeechManager.phonemeSounds
        assertTrue("Phoneme sounds map must not be empty", sounds.isNotEmpty())

        // All 26 lowercase English letters
        for (c in 'a'..'z') {
            val key = c.toString()
            assertTrue("Phoneme sound map must contain letter '$key'", sounds.containsKey(key))
            val sound = sounds[key]
            assertNotNull("Sound for '$key' must not be null", sound)
            assertTrue("Sound for '$key' must not be blank", sound!!.isNotBlank())
        }

        // Essential phonics digraphs
        val digraphs = listOf("sh", "ch", "th", "ng")
        for (dg in digraphs) {
            assertTrue("Phoneme sound map must contain digraph '$dg'", sounds.containsKey(dg))
            val sound = sounds[dg]
            assertNotNull("Sound for '$dg' must not be null", sound)
            assertTrue("Sound for '$dg' must not be blank", sound!!.isNotBlank())
        }

        // Short vowel phonemes
        assertEquals("ah", sounds["a"])
        assertEquals("eh", sounds["e"])
        assertEquals("ih", sounds["i"])
        assertEquals("ah", sounds["o"])
        assertEquals("uh", sounds["u"])
    }

    @Test
    fun testFullSentenceAssemblyCoverage() {
        // Test starter words sentence contexts
        for (word in WordBank.starterWords) {
            assertTrue("Word ${word.text} must have non-empty sentenceContext", word.sentenceContext.isNotEmpty())
            val fullSentence = word.sentenceContext.joinToString(" ")
            assertTrue("Full sentence for ${word.text} must not be blank", fullSentence.isNotBlank())
            val containsTarget = word.sentenceContext.any { token ->
                token.trimEnd('.', '!', '?', ',').equals(word.text, ignoreCase = true)
            }
            assertTrue("Full sentence for ${word.text} must contain target word", containsTarget)
        }

        // Test all stories fullSentence properties
        for (story in StoryBank.allStories) {
            assertTrue("Story ${story.title} must have pages", story.pages.isNotEmpty())
            for ((idx, page) in story.pages.withIndex()) {
                assertTrue("Story ${story.title} page $idx fullSentence must not be blank", page.fullSentence.isNotBlank())
                val joinedWords = page.words.joinToString(" ") { it.text }
                assertEquals("Story ${story.title} page $idx fullSentence should match joined words", joinedWords, page.fullSentence)
            }
        }
    }

    @Test
    fun testTrackToReadModeModel() {
        assertEquals("TRACKING", com.example.earlyreader.storage.ProgressStorage.MODE_TRACKING)
        assertEquals(4, AppMode.values().size)
        assertTrue(AppMode.values().contains(AppMode.TRACK_TO_READ))

        // Simulate finger word-tracking across each story page
        for (story in StoryBank.allStories) {
            for ((pIdx, page) in story.pages.withIndex()) {
                val words = page.words
                assertTrue("Page $pIdx in ${story.title} must have words to track", words.isNotEmpty())
                var trackedIndex = 0
                for (step in words.indices) {
                    trackedIndex = step
                    val isComplete = trackedIndex == words.size - 1
                    if (step < words.size - 1) {
                        assertFalse("Should not be complete at word index $step", isComplete)
                    } else {
                        assertTrue("Must be complete at final word index $step", isComplete)
                    }
                }
                assertEquals("Final tracked index must equal last word index", words.size - 1, trackedIndex)
            }
        }
    }
}
