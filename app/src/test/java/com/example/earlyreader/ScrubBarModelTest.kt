package com.example.earlyreader

import com.example.earlyreader.audio.SpeechManager
import com.example.earlyreader.audio.SpeechStatus
import com.example.earlyreader.audio.VoiceCandidate
import com.example.earlyreader.audio.VoiceSelection
import com.example.earlyreader.audio.spokenForm
import com.example.earlyreader.model.StoryBank
import com.example.earlyreader.model.WordBank
import com.example.earlyreader.storage.KeyValueStore
import com.example.earlyreader.storage.ProgressStorage
import com.example.earlyreader.ui.scrub.DONE_THRESHOLD
import com.example.earlyreader.ui.scrub.RESCRUB_RESET
import com.example.earlyreader.ui.scrub.ThumbHeight
import com.example.earlyreader.ui.scrub.ThumbWidth
import com.example.earlyreader.ui.scrub.segmentBoundaries
import com.example.earlyreader.ui.sentences.SentenceLabData
import com.example.earlyreader.ui.sentences.buildTokens
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.roundToInt

class ScrubBarModelTest {

    // The real boundary maths used by the scrub bar (internal in SegmentedScrubBar.kt), not a copy.
    private fun calculateBoundaries(segments: List<String>): List<Float> = segmentBoundaries(segments)

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
        // The real constant the scrub bar uses (same value as DONE in docs/index.html)
        assertEquals(0.92f, DONE_THRESHOLD, 0.0001f)
        assertFalse("Progress at 0.91 should not complete", 0.91f >= DONE_THRESHOLD)
        assertTrue("Progress at 0.92 should complete", 0.92f >= DONE_THRESHOLD)
        assertTrue("Progress at 1.0 should complete", 1.0f >= DONE_THRESHOLD)
    }

    @Test
    fun testRescrubReset() {
        assertEquals(0.03f, RESCRUB_RESET, 0.0001f)
        assertTrue("Touch at 0.01 should trigger rescrub reset", 0.01f <= RESCRUB_RESET)
        assertFalse("Touch at 0.04 should not trigger rescrub reset", 0.04f <= RESCRUB_RESET)
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
        assertEquals("StoryBank must contain all 14 stories", 14, stories.size)
        val expectedIds = listOf(
            "hen", "pig", "bug", "cat", "net", "frog", "fog", "duck",
            "fish", "chick", "king", "buzz", "camp", "sand",
        )
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

    /** An in-memory stand-in for SharedPreferences, so the real ProgressStorage logic runs in a plain unit test. */
    private class FakeStore : KeyValueStore {
        val ints = mutableMapOf<String, Int>()
        val strings = mutableMapOf<String, String>()
        val sets = mutableMapOf<String, Set<String>>()
        override fun getInt(key: String, default: Int) = ints[key] ?: default
        override fun putInt(key: String, value: Int) { ints[key] = value }
        override fun getString(key: String, default: String) = strings[key] ?: default
        override fun putString(key: String, value: String) { strings[key] = value }
        override fun getStringSet(key: String): Set<String> = sets[key] ?: emptySet()
        override fun putStringSet(key: String, value: Set<String>) { sets[key] = value }
        override fun clear() { ints.clear(); strings.clear(); sets.clear() }
    }

    @Test
    fun testProgressStorageBookkeeping() {
        val progress = ProgressStorage(FakeStore())
        assertEquals(0, progress.completedWordsCount)
        assertEquals(0, progress.lastActiveStoryIndex)
        assertEquals(ProgressStorage.MODE_TRACKING, progress.lastAppMode)
        assertTrue(progress.completedStoryTitles.isEmpty())

        progress.recordWordCompleted()
        progress.recordWordCompleted()
        assertEquals(2, progress.completedWordsCount)

        progress.recordStoryCompleted("The Red Hen")
        progress.recordStoryCompleted("The Red Hen") // recording the same story twice is a no-op
        assertEquals(setOf("The Red Hen"), progress.completedStoryTitles)
        progress.recordStoryCompleted("The Big Pig")
        assertEquals(setOf("The Red Hen", "The Big Pig"), progress.completedStoryTitles)

        progress.recordSentenceLevelCompleted("nouns")
        progress.recordSentenceLevelCompleted("nouns")
        assertEquals(setOf("nouns"), progress.completedSentenceLevels)

        progress.lastAppMode = ProgressStorage.MODE_STORY
        progress.lastActiveStoryIndex = 3
        progress.lastSentenceLabLevel = 4
        assertEquals(ProgressStorage.MODE_STORY, progress.lastAppMode)
        assertEquals(3, progress.lastActiveStoryIndex)
        assertEquals(4, progress.lastSentenceLabLevel)

        progress.resetAll()
        assertEquals(0, progress.completedWordsCount)
        assertTrue(progress.completedStoryTitles.isEmpty())
        assertTrue(progress.completedSentenceLevels.isEmpty())
        assertEquals(0, progress.lastActiveStoryIndex)
        assertEquals(ProgressStorage.MODE_TRACKING, progress.lastAppMode)
    }

    @Test
    fun testVoiceSelectionPrefersOfflineEnglish() {
        val voices = listOf(
            VoiceCandidate("fr-fr-x-local", "fr", networkRequired = false),
            VoiceCandidate("en-us-x-sfg#female_1-network", "en", networkRequired = true),
            VoiceCandidate("en-us-x-sfg#female_1-local", "en", networkRequired = false),
        )
        val choice = VoiceSelection.choose(voices)
        assertEquals(2, choice.index)
        assertEquals("en-us-x-sfg#female_1-local", choice.name)
        assertTrue(choice.isOffline)
        assertFalse(choice.isNetworkOnly)
        assertFalse("An offline voice needs no download guidance", choice.needsOfflineGuidance)
    }

    @Test
    fun testVoiceSelectionNetworkOnlyAndMissingVoices() {
        val networkOnly = VoiceSelection.choose(
            listOf(
                VoiceCandidate("de-de-x-local", "de", networkRequired = false),
                VoiceCandidate("en-us-x-sfg#male_1-network", "en", networkRequired = true),
            ),
        )
        assertEquals(1, networkOnly.index)
        assertTrue(networkOnly.isNetworkOnly)
        assertFalse(networkOnly.isOffline)
        assertTrue("A network-only voice should trigger the offline-voice tip", networkOnly.needsOfflineGuidance)

        // No English voice at all (or no voice list): fall back to the engine default, which is treated as offline.
        val noEnglish: List<List<VoiceCandidate>?> = listOf(
            null,
            emptyList(),
            listOf(VoiceCandidate("es-es-x-local", "es", networkRequired = false)),
        )
        for (none in noEnglish) {
            val choice = VoiceSelection.choose(none)
            assertEquals(null, choice.index)
            assertEquals(VoiceSelection.DEFAULT_VOICE_NAME, choice.name)
            assertTrue(choice.isOffline)
            assertFalse(choice.needsOfflineGuidance)
        }
        assertTrue(VoiceSelection.needsOfflineGuidance(isOffline = false, isNetworkOnly = false))
        assertTrue(VoiceSelection.needsOfflineGuidance(isOffline = true, isNetworkOnly = true))
        assertFalse(VoiceSelection.needsOfflineGuidance(isOffline = true, isNetworkOnly = false))
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
        val items = SentenceLabData.level5Items
        assertEquals("Sentence Lab Level 5 must have 6 items", 6, items.size)
        val expectedBooleans = listOf(false, true, true, false, false, true)
        for ((idx, item) in items.withIndex()) {
            assertTrue("Item $idx text must not be blank", item.text.isNotBlank())
            assertTrue("Item $idx why explanation must not be blank", item.why.isNotBlank())
            assertTrue("Item $idx hint must not be blank", item.hint.isNotBlank())
            assertEquals("Item $idx isSentence must match expected", expectedBooleans[idx], item.isSentence)
        }
        assertEquals("Level 5 id must be 'sentence-check'", "sentence-check", SentenceLabData.level5.id)
        assertEquals("Level 5 type must be JUDGE_SENTENCE", com.example.earlyreader.ui.sentences.SentenceLabType.JUDGE_SENTENCE, SentenceLabData.level5.type)
    }

    @Test
    fun testSentenceLabBuildSentenceDataset() {
        val items = SentenceLabData.level6Items
        assertEquals("Sentence Lab Level 6 must have 6 items", 6, items.size)
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
        assertEquals("Level 6 id must be 'build'", "build", SentenceLabData.level6.id)
        assertEquals("Level 6 type must be BUILD_SENTENCE", com.example.earlyreader.ui.sentences.SentenceLabType.BUILD_SENTENCE, SentenceLabData.level6.type)
    }

    @Test
    fun testSentenceLabRolesDataset() {
        val items = SentenceLabData.level4Items
        assertEquals("Sentence Lab Level 4 must have 6 items", 6, items.size)
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

        assertEquals("Level 4 id must be 'roles'", "roles", SentenceLabData.level4.id)
        assertEquals("Level 4 type must be SORT_ROLES", com.example.earlyreader.ui.sentences.SentenceLabType.SORT_ROLES, SentenceLabData.level4.type)
        assertEquals("All levels list must have 6 levels", 6, SentenceLabData.allLevels.size)
        // Same order as the web Sentence Lab: nouns, verbs, adjectives, roles, sentence?, build.
        assertEquals(
            listOf("nouns", "verbs", "adjectives", "roles", "sentence-check", "build"),
            SentenceLabData.allLevels.map { it.id },
        )
        for ((idx, level) in SentenceLabData.allLevels.withIndex()) {
            assertTrue("Level ${idx + 1} title must start with its number", level.title.startsWith("Level ${idx + 1}:"))
            assertTrue("Level ${idx + 1} short name must start with its number", level.shortName.startsWith("${idx + 1}."))
        }
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

        // Essential phonics digraphs, and the doubles / digraphs the stories use as ONE segment
        val digraphs = listOf("sh", "ch", "th", "ng", "nk", "ck", "ll", "ss", "ff", "zz", "qu")
        for (dg in digraphs) {
            assertTrue("Phoneme sound map must contain digraph '$dg'", sounds.containsKey(dg))
            val sound = sounds[dg]
            assertNotNull("Sound for '$dg' must not be null", sound)
            assertTrue("Sound for '$dg' must not be blank", sound!!.isNotBlank())
        }

        // Short vowel phonemes: all five must differ, so "hat" and "hot" (and "cat" and "cot") sound out differently
        assertEquals("ah", sounds["a"])
        assertEquals("eh", sounds["e"])
        assertEquals("ih", sounds["i"])
        assertEquals("aw", sounds["o"])
        assertEquals("uh", sounds["u"])
        assertNotEquals("a and o must not be sounded the same", sounds["a"], sounds["o"])
        assertEquals("All five short vowels must have different sounds", 5, listOf("a", "e", "i", "o", "u").map { sounds[it] }.toSet().size)
    }

    @Test
    fun testEveryStorySegmentHasASoundAndNoDigraphIsSplit() {
        // Runs over the real story data (StoryBank), not a copy.
        val sounds = SpeechManager.phonemeSounds
        var decodableWords = 0
        for (story in StoryBank.allStories) {
            for ((pIdx, page) in story.pages.withIndex()) {
                for (word in page.words.filter { !it.sight }) {
                    decodableWords++
                    val where = "${story.title} page ${pIdx + 1} word '${word.text}'"
                    for (segment in word.segments) {
                        assertTrue("$where: no sound for segment '$segment'", sounds.containsKey(segment.lowercase()))
                    }
                    for ((a, b) in word.segments.zipWithNext()) {
                        val pair = (a + b).lowercase()
                        assertFalse(
                            "$where: '$a' + '$b' must be ONE segment '$pair' (two letters would be sounded twice)",
                            pair in SpeechManager.digraphs,
                        )
                    }
                }
            }
        }
        assertTrue("There must be decodable words to check", decodableWords > 0)

        // The re-segmented words the critic found sounded twice
        fun segmentsOf(text: String) = StoryBank.allStories.flatMap { it.pages }.flatMap { it.words }
            .first { it.text.trimEnd('.', '!', '?', ',').equals(text, ignoreCase = true) }.segments
        assertEquals(listOf("d", "u", "ck"), segmentsOf("duck"))
        assertEquals(listOf("b", "e", "ll"), segmentsOf("bell"))
        assertEquals(listOf("r", "a", "ng"), segmentsOf("rang"))
        assertEquals(listOf("qu", "i", "ck"), segmentsOf("quick"))
        assertEquals(listOf("th", "i", "ck"), segmentsOf("thick"))
        assertEquals(listOf("o", "n"), segmentsOf("on"))
        assertEquals(listOf("f", "i", "sh"), segmentsOf("fish"))
        assertEquals(listOf("ch", "i", "ck"), segmentsOf("chick"))
        assertEquals(listOf("k", "i", "ng"), segmentsOf("king"))
        assertEquals(listOf("p", "i", "nk"), segmentsOf("pink"))
        assertEquals(listOf("b", "u", "zz"), segmentsOf("buzz"))
        assertEquals(listOf("c", "a", "m", "p"), segmentsOf("camp"))
        assertEquals(listOf("s", "a", "n", "d"), segmentsOf("sand"))
        assertEquals(listOf("sh", "e", "ll"), segmentsOf("shell"))
    }

    @Test
    fun testStoriesDifficultyTiersAndMockupSentence() {
        val stories = StoryBank.allStories
        assertEquals(14, stories.size)

        // Story 14 Page 2 must have the exact sentence from the reference mockup: "Mom ran on the sand."
        val sandStory = stories.first { it.id == "sand" }
        assertEquals("Mom ran on the sand.", sandStory.pages[1].fullSentence)

        // Verify key progression across tiers
        val fishStory = stories.first { it.id == "fish" }
        assertTrue(fishStory.pages.flatMap { it.words }.any { it.segments.contains("sh") })

        val kingStory = stories.first { it.id == "king" }
        assertTrue(kingStory.pages.flatMap { it.words }.any { it.segments.contains("nk") })
        assertTrue(kingStory.pages.flatMap { it.words }.any { it.segments.contains("ng") })

        val buzzStory = stories.first { it.id == "buzz" }
        assertTrue(buzzStory.pages.flatMap { it.words }.any { it.segments.contains("zz") })
        assertTrue(buzzStory.pages.flatMap { it.words }.any { it.segments.contains("ss") })

        val campStory = stories.first { it.id == "camp" }
        assertTrue(campStory.pages.flatMap { it.words }.any { it.text.contains("camp") })
    }

    /** One word of a story as written in docs/index.html: {t:"hen", img:"🐔", seg:["h","e","n"]} or {t:"The", sight:true}. */
    private data class WebWord(val text: String, val sight: Boolean, val img: String?, val seg: List<String>)

    private data class WebStory(val id: String, val title: String, val pages: List<List<WebWord>>)

    private fun readWebStories(html: String): List<WebStory> {
        val lines = html.lines()
        val start = lines.indexOfFirst { it.contains("const STORIES = [") }
        require(start >= 0) { "STORIES not found in docs/index.html" }
        val wordRe = Regex("""\{t:"([^"]*)"(.*)\}""")
        val imgRe = Regex("""img:"([^"]*)"""")
        val segRe = Regex("""seg:\[([^\]]*)\]""")
        val stories = mutableListOf<WebStory>()
        var id = ""
        var title = ""
        var pages = mutableListOf<List<WebWord>>()
        var page: MutableList<WebWord>? = null
        for (line in lines.drop(start + 1)) {
            val t = line.trim()
            if (t == "];") break
            when {
                t.startsWith("id:") -> id = t.substringAfter('"').substringBeforeLast('"')
                t.startsWith("title:") -> title = t.substringAfter('"').substringBeforeLast('"')
                t == "pages: [" -> pages = mutableListOf()
                t == "[" -> page = mutableListOf()
                t == "]," || t == "]" -> {
                    if (page != null) {
                        pages.add(page)
                        page = null
                    } else {
                        stories.add(WebStory(id, title, pages.toList()))
                    }
                }
                t.startsWith("{t:") -> {
                    val m = wordRe.find(t) ?: error("Unreadable word line: $t")
                    val rest = m.groupValues[2]
                    page!!.add(
                        WebWord(
                            text = m.groupValues[1],
                            sight = rest.contains("sight:true"),
                            img = imgRe.find(rest)?.groupValues?.get(1),
                            seg = segRe.find(rest)?.groupValues?.get(1)
                                ?.split(",")?.map { it.trim().trim('"') }?.filter { it.isNotEmpty() }
                                ?: emptyList(),
                        ),
                    )
                }
            }
        }
        return stories
    }

    @Test
    fun testStoriesAreTheSameOnWebAndAndroid() {
        // Reads the real web reader (docs/index.html). Unit tests run in app/, so the web page is one level up.
        val webFile = listOf(File("../docs/index.html"), File("docs/index.html")).firstOrNull { it.isFile }
        // A hard failure, not a skip: a parity check that silently does not run is worse than none.
        assertNotNull("docs/index.html must be reachable from the test working directory (app/ or the repo root)", webFile)
        val web = readWebStories(webFile!!.readText(Charsets.UTF_8))

        assertEquals("Same story ids", StoryBank.allStories.map { it.id }, web.map { it.id })
        for ((story, webStory) in StoryBank.allStories.zip(web)) {
            assertEquals("Title of ${story.id}", story.title, webStory.title)
            assertEquals("Page count of ${story.title}", story.pages.size, webStory.pages.size)
            for ((pIdx, pair) in story.pages.zip(webStory.pages).withIndex()) {
                val (page, webPage) = pair
                val where = "${story.title} page ${pIdx + 1}"
                assertEquals("$where: same words", page.words.map { it.text }, webPage.map { it.text })
                for ((word, webWord) in page.words.zip(webPage)) {
                    assertEquals("$where '${word.text}': tricky flag", word.sight, webWord.sight)
                    assertEquals("$where '${word.text}': picture", word.img, webWord.img)
                    assertEquals(
                        "$where '${word.text}': sounds",
                        word.segments.map { it.lowercase() },
                        webWord.seg.map { it.lowercase() },
                    )
                }
            }
        }
    }

    @Test
    fun testTheArticleIsSpokenAsUhOnItsOwnButSentencesAreLeftAlone() {
        assertEquals("uh", spokenForm("A"))
        assertEquals("uh", spokenForm("a"))
        assertEquals("uh", spokenForm("a."))
        assertEquals("cat", spokenForm("cat"))
        assertEquals("at", spokenForm("at"))
        // A whole sentence is read as written: the voice says the article correctly in context.
        assertEquals("A hen sat. The hen is red.", spokenForm("A hen sat. The hen is red."))
        assertEquals("", spokenForm(""))
    }

    @Test
    fun testScreenReaderStepsMoveExactlyOneWord() {
        // Forward one word at a time, back one word at a time, and never out of range.
        assertEquals(1, accessibilityStepIndex(trackedIndex = 0, wordCount = 5, target = 1f))
        assertEquals(3, accessibilityStepIndex(trackedIndex = 2, wordCount = 5, target = 1f))
        assertEquals(1, accessibilityStepIndex(trackedIndex = 2, wordCount = 5, target = 0f))
        assertEquals(4, accessibilityStepIndex(trackedIndex = 4, wordCount = 5, target = 1f))
        assertEquals(0, accessibilityStepIndex(trackedIndex = 0, wordCount = 5, target = 0f))
        assertEquals(2, accessibilityStepIndex(trackedIndex = 2, wordCount = 5, target = 0.5f))
        // Stepping along a whole page finishes it in wordCount - 1 steps, the same as sliding.
        var index = 0
        var steps = 0
        while (index < 6) {
            index = limitTrackedWordIndex(accessibilityStepIndex(index, 7, 1f), index)
            steps++
        }
        assertEquals(6, steps)
    }

    @Test
    fun testTrackThumbOffsetStaysInsideTheBar() {
        // The thumb travels the bar minus its own width, so it is always fully inside at both ends.
        for (barWidth in listOf(300, 400, 600, 800)) {
            val thumb = 150f
            assertEquals(0, trackThumbOffsetPx(barWidth, thumb, 0f))
            assertEquals(((barWidth - thumb) / 2f).roundToInt(), trackThumbOffsetPx(barWidth, thumb, 0.5f))
            assertEquals((barWidth - thumb).roundToInt(), trackThumbOffsetPx(barWidth, thumb, 1f))
            assertEquals((barWidth - thumb).roundToInt(), trackThumbOffsetPx(barWidth, thumb, 1.5f))
            assertEquals(0, trackThumbOffsetPx(barWidth, thumb, -0.5f))
        }
        // A bar narrower than the thumb pins it at the start instead of going negative.
        assertEquals(0, trackThumbOffsetPx(100, 150f, 1f))
        assertEquals(56.dp, ThumbWidth)
        assertEquals(48.dp, ThumbHeight)
    }

    @Test
    fun testHasIsATrickyWordEverywhere() {
        // "has" ends in /z/, which sounding it out gets wrong, so it is a sight word in every story.
        val hasWords = StoryBank.allStories.flatMap { it.pages }.flatMap { it.words }.filter { it.text == "has" }
        assertTrue("Stories use 'has'", hasWords.isNotEmpty())
        assertTrue("Every 'has' is a tricky word", hasWords.all { it.sight && it.segments.isEmpty() })
    }

    @Test
    fun testNoPictureNeedsAPhoneNewerThanAndroid8() {
        // minSdk 26 phones show newer emoji as empty boxes, so pictures stay in the old, widely available set.
        val newer = listOf("🪽", "🪵", "🟫", "🧶", "🦘")
        val pictures = StoryBank.allStories.flatMap { it.pages }.flatMap { it.words }.mapNotNull { it.img } +
            WordBank.starterWords.mapNotNull { it.img }
        for (picture in pictures) assertFalse("$picture may not draw on older phones", picture in newer)
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
    fun testTrackedWordCanOnlyAdvanceOneWordPastTheFurthestReached() {
        // Nothing reached yet: a tap far to the right gets at most the second word, never the end of the sentence
        assertEquals(1, limitTrackedWordIndex(desiredIndex = 9, reachedIndex = 0))
        assertEquals(0, limitTrackedWordIndex(desiredIndex = 0, reachedIndex = 0))
        // Moving back, or staying, is never limited
        assertEquals(2, limitTrackedWordIndex(desiredIndex = 2, reachedIndex = 5))
        // Walking along the sentence one word at a time gets to the end
        var reached = 0
        var steps = 0
        while (reached < 7 && steps < 100) {
            reached = maxOf(reached, limitTrackedWordIndex(desiredIndex = 7, reachedIndex = reached))
            steps++
        }
        assertEquals(7, reached)
        assertEquals("A page of 8 words takes at least 7 moves", 7, steps)
    }

    @Test
    fun testTrackToReadModeModel() {
        assertEquals("TRACKING", com.example.earlyreader.storage.ProgressStorage.MODE_TRACKING)
        assertEquals(4, AppMode.values().size)
        assertTrue(AppMode.values().contains(AppMode.TRACK_TO_READ))

        // Validate continuous scrolling tracker progress-to-word-index mapping
        assertEquals(0, calculateTrackedWordIndex(0f, 0))
        assertEquals(0, calculateTrackedWordIndex(-0.5f, 5))
        assertEquals(0, calculateTrackedWordIndex(0f, 5))
        assertEquals(0, calculateTrackedWordIndex(0.19f, 5))
        assertEquals(1, calculateTrackedWordIndex(0.20f, 5))
        assertEquals(2, calculateTrackedWordIndex(0.50f, 5))
        assertEquals(4, calculateTrackedWordIndex(0.99f, 5))
        assertEquals(4, calculateTrackedWordIndex(1.0f, 5))
        assertEquals(4, calculateTrackedWordIndex(1.5f, 5))

        // Validate discrete word-to-word snapped slider stops
        assertEquals(1.0f, calculateSnappedTrackProgress(0, 1), 0.001f)
        assertEquals(0.0f, calculateSnappedTrackProgress(0, 5), 0.001f)
        assertEquals(0.25f, calculateSnappedTrackProgress(1, 5), 0.001f)
        assertEquals(0.50f, calculateSnappedTrackProgress(2, 5), 0.001f)
        assertEquals(0.75f, calculateSnappedTrackProgress(3, 5), 0.001f)
        assertEquals(1.0f, calculateSnappedTrackProgress(4, 5), 0.001f)

        // Simulate stepped word-to-word jumping slider across each story page
        for (story in StoryBank.allStories) {
            for ((pIdx, page) in story.pages.withIndex()) {
                val words = page.words
                assertTrue("Page $pIdx in ${story.title} must have words to track", words.isNotEmpty())
                var trackedIndex = 0
                for (step in words.indices) {
                    val progress = (step + 0.5f) / words.size.toFloat()
                    trackedIndex = calculateTrackedWordIndex(progress, words.size)
                    val snapped = calculateSnappedTrackProgress(trackedIndex, words.size)
                    assertEquals("Tracked index must match step $step", step, trackedIndex)
                    if (step == 0 && words.size > 1) {
                        assertEquals(0f, snapped, 0.001f)
                    } else if (step == words.size - 1) {
                        assertEquals(1f, snapped, 0.001f)
                    }
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

    @Test
    fun testTrickySightWordsCoverageInStories() {
        // Ensure tricky words have sight = true so they receive the orange pill capsule
        val trickyWords = mutableListOf<String>()
        for (story in StoryBank.allStories) {
            for (page in story.pages) {
                for (word in page.words) {
                    if (word.sight) {
                        trickyWords.add(word.text.lowercase())
                    }
                }
            }
        }
        assertTrue("Stories must have tricky sight words (e.g. 'the')", trickyWords.isNotEmpty())
        assertTrue("Tricky words should include 'the'", trickyWords.any { it.contains("the") })
    }

    @Test
    fun testVoiceSelectionPrefersUsThenAnyEnglishAndSkipsNotInstalled() {
        // A US voice beats a British one, in both orders.
        val gbFirst = listOf(
            VoiceCandidate("en-gb-x-local", "en", networkRequired = false, country = "GB"),
            VoiceCandidate("en-us-x-local", "en", networkRequired = false, country = "US"),
        )
        assertEquals(1, VoiceSelection.choose(gbFirst).index)
        assertEquals(0, VoiceSelection.choose(gbFirst.reversed()).index)

        // No US voice: any English one will do.
        val onlyAu = listOf(
            VoiceCandidate("fr-fr-x-local", "fr", networkRequired = false, country = "FR"),
            VoiceCandidate("en-au-x-local", "en", networkRequired = false, country = "AU"),
        )
        assertEquals(1, VoiceSelection.choose(onlyAu).index)

        // Offline still beats network, even when the network voice is US and the offline one is not.
        val offlineGb = VoiceSelection.choose(
            listOf(
                VoiceCandidate("en-us-x-network", "en", networkRequired = true, country = "US"),
                VoiceCandidate("en-gb-x-local", "en", networkRequired = false, country = "GB"),
            ),
        )
        assertEquals(1, offlineGb.index)
        assertTrue(offlineGb.isOffline)

        // A voice that is listed but not downloaded ("notInstalled") is never picked.
        val notInstalled = VoiceSelection.choose(
            listOf(
                VoiceCandidate("en-us-x-missing", "en", networkRequired = false, country = "US", installed = false),
                VoiceCandidate("en-gb-x-local", "en", networkRequired = false, country = "GB"),
            ),
        )
        assertEquals(1, notInstalled.index)

        // Only a not-installed voice: fall back to the engine default instead.
        val onlyMissing = VoiceSelection.choose(
            listOf(VoiceCandidate("en-us-x-missing", "en", networkRequired = false, country = "US", installed = false)),
        )
        assertEquals(null, onlyMissing.index)
        assertEquals(VoiceSelection.DEFAULT_VOICE_NAME, onlyMissing.name)
    }

    @Test
    fun testSpeechStatusMessagesAreHonest() {
        // Android's TextToSpeech.LANG_MISSING_DATA = -1, LANG_NOT_SUPPORTED = -2; 0, 1, 2 mean the language works.
        assertEquals(SpeechStatus.NO_LANGUAGE, VoiceSelection.statusForLanguageResult(-1))
        assertEquals(SpeechStatus.NO_LANGUAGE, VoiceSelection.statusForLanguageResult(-2))
        assertEquals(SpeechStatus.READY, VoiceSelection.statusForLanguageResult(0))
        assertEquals(SpeechStatus.READY, VoiceSelection.statusForLanguageResult(1))
        assertEquals(SpeechStatus.READY, VoiceSelection.statusForLanguageResult(2))

        val failed = VoiceSelection.describe(SpeechStatus.FAILED, "x", isOffline = false)
        assertTrue(failed, failed.contains("No speech engine found"))
        assertFalse("A missing engine must not be described as a network problem", failed.contains("network"))
        val noLanguage = VoiceSelection.describe(SpeechStatus.NO_LANGUAGE, "x", isOffline = true)
        assertTrue(noLanguage, noLanguage.contains("Language data missing") && noLanguage.contains("English voice"))
        val starting = VoiceSelection.describe(SpeechStatus.STARTING, "x", isOffline = false)
        assertFalse("A starting engine must not be reported as needing the network", starting.contains("network"))
        assertEquals("Voice A (offline ready)", VoiceSelection.describe(SpeechStatus.READY, "Voice A", isOffline = true))
        assertTrue(VoiceSelection.describe(SpeechStatus.READY, "Voice A", isOffline = false).contains("network"))
    }

    @Test
    fun testRepeatedTapsCannotRunThroughTheTrackerButDraggingCan() {
        val min = TRACK_MIN_ADVANCE_INTERVAL_MS
        // A first touch right after an advance is held back; after the interval it may step on.
        assertFalse(trackMayAdvance(isFirstTouch = true, dragged = false, nowMs = 1_000, lastAdvanceMs = 1_000 - (min - 1)))
        assertTrue(trackMayAdvance(isFirstTouch = true, dragged = false, nowMs = 1_000, lastAdvanceMs = 1_000 - min))
        // Wobbling on a tap (no real drag, not the first touch) never steps on.
        assertFalse(trackMayAdvance(isFirstTouch = false, dragged = false, nowMs = 10_000, lastAdvanceMs = 0))
        // A real drag is faster, but still only one new word per drag interval.
        val drag = TRACK_MIN_DRAG_ADVANCE_INTERVAL_MS
        assertFalse(trackMayAdvance(isFirstTouch = false, dragged = true, nowMs = 1_001, lastAdvanceMs = 1_000))
        assertFalse(trackMayAdvance(isFirstTouch = false, dragged = true, nowMs = 1_000, lastAdvanceMs = 1_000 - (drag - 1)))
        assertTrue(trackMayAdvance(isFirstTouch = false, dragged = true, nowMs = 1_000, lastAdvanceMs = 1_000 - drag))
        assertTrue(drag < min)

        // trackNextIndex: one past the furthest word reached at most, none when not allowed, back is free.
        assertEquals(1, trackNextIndex(desiredIndex = 9, reachedIndex = 0, mayAdvance = true))
        assertEquals(0, trackNextIndex(desiredIndex = 9, reachedIndex = 0, mayAdvance = false))
        assertEquals(3, trackNextIndex(desiredIndex = 9, reachedIndex = 3, mayAdvance = false))
        assertEquals(1, trackNextIndex(desiredIndex = 1, reachedIndex = 5, mayAdvance = false))
        assertEquals(0, trackNextIndex(desiredIndex = 0, reachedIndex = 5, mayAdvance = true))

        // Simulate mashing the right end of the bar every 100 ms on an 8-word page: it steps on at most once per 300 ms.
        val words = 8
        var reached = 0
        var last = -10_000L
        var now = 0L
        repeat(10) {
            val may = trackMayAdvance(isFirstTouch = true, dragged = false, nowMs = now, lastAdvanceMs = last)
            val next = trackNextIndex(words - 1, reached, may)
            if (next > reached) { reached = next; last = now }
            now += 100
        }
        assertTrue("10 quick taps (1 second) must not finish an 8-word page, reached $reached", reached < words - 1)
        assertEquals(4, reached)   // taps at 0, 300, 600 and 900 ms each step on one word
    }

    @Test
    fun testAPokeOrWobbleThatPassesTheTouchSlopCannotFinishAPageInAFewMilliseconds() {
        // The finger lands, passes the touch slop at once and then sits at the far right end of a 7-word bar while
        // events arrive every 8 ms (a 120 Hz screen). It must still take at least 6 drag intervals to reach the end.
        val words = 7
        var reached = 0
        var last = -10_000L
        var now = 0L
        var finishedAt = -1L
        while (now < 2_000 && finishedAt < 0) {
            val may = trackMayAdvance(isFirstTouch = now == 0L, dragged = now > 0L, nowMs = now, lastAdvanceMs = last)
            val next = trackNextIndex(words - 1, reached, may)
            if (next > reached) { reached = next; last = now }
            if (reached == words - 1) finishedAt = now
            now += 8
        }
        assertTrue("finished at $finishedAt ms", finishedAt >= (words - 2) * TRACK_MIN_DRAG_ADVANCE_INTERVAL_MS)

        // A steady slide along the page (one new word every 150 ms) is never held back.
        var slideReached = 0
        var slideLast = -10_000L
        for (step in 1 until words) {
            val t = step * 150L
            val may = trackMayAdvance(isFirstTouch = false, dragged = true, nowMs = t, lastAdvanceMs = slideLast)
            val next = trackNextIndex(step, slideReached, may)
            assertEquals(step, next)
            slideReached = next
            slideLast = t
        }
    }

    @Test
    fun testSpeechTimeoutsGrowWithTheLengthOfASlowSentence() {
        // Words keep the old fixed limits...
        val word = SpeechManager.estimatedSpeechMs("hat", 0.8f)
        assertTrue(word < 1_000)
        assertEquals(8_000L, SpeechManager.staleAfterMs(word))
        assertEquals(4_000L, SpeechManager.awaitTimeoutMs(word))
        // ...but a 14-word sentence at half the sentence speed (0.4) takes well over the old 8 s / 4 s limits.
        val sentence = "The big red hen sat on the old wet log by the little hut."
        val ms = SpeechManager.estimatedSpeechMs(sentence, 0.4f)
        assertTrue("estimate $ms ms", ms > 8_000)
        assertTrue(SpeechManager.staleAfterMs(ms) > ms)
        assertTrue(SpeechManager.awaitTimeoutMs(ms) > ms)
        // Slower speech never estimates shorter than faster speech.
        assertTrue(SpeechManager.estimatedSpeechMs(sentence, 0.4f) > SpeechManager.estimatedSpeechMs(sentence, 0.8f))
    }

    @Test
    fun testBuildSentenceSillySentences() {
        assertEquals(listOf("The", "cat", "sat", "."), buildTokens("The cat sat."))
        assertEquals(listOf("Do", "you", "like", "soup", "?"), buildTokens("Do you like soup?"))
        val items = SentenceLabData.level6.buildItems
        val bus = items.first { it.text == "The bus stops at our school." }
        assertTrue("The bus sentence has silly alternatives, like the web Sentence Lab", bus.silly.isNotEmpty())
        // Every silly alternative is made of exactly the same words and mark as the answer, so it can be built from the same tiles.
        for (item in items) {
            val answer = buildTokens(item.text).sorted()
            for (other in item.silly) {
                assertEquals("'$other' must use the same words as '${item.text}'", answer, buildTokens(other).sorted())
                assertNotEquals("An alternative must differ from the answer", buildTokens(item.text), buildTokens(other))
            }
        }
    }
}
