package com.example.earlyreader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.earlyreader.audio.SpeechManager
import com.example.earlyreader.audio.VoiceSelection
import com.example.earlyreader.model.PageData
import com.example.earlyreader.model.StoryBank
import com.example.earlyreader.model.StoryData
import com.example.earlyreader.model.WordBank
import com.example.earlyreader.model.WordData
import com.example.earlyreader.storage.ProgressStorage
import com.example.earlyreader.ui.scrub.FingerSliderThumb
import com.example.earlyreader.ui.scrub.SegmentedScrubBar
import com.example.earlyreader.ui.scrub.ThumbHeight
import com.example.earlyreader.ui.scrub.ThumbWidth
import com.example.earlyreader.ui.sentences.SentenceLabScreen
import com.example.earlyreader.ui.theme.EarlyReaderTheme
import com.example.earlyreader.ui.theme.SegmentEmpty
import com.example.earlyreader.ui.theme.TextDark
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AppMode {
    WORD_PRACTICE,
    STORY_MODE,
    TRACK_TO_READ,
    SENTENCE_LAB,
}

class MainActivity : ComponentActivity() {
    private var speechManager: SpeechManager? = null
    private lateinit var progressStorage: ProgressStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val speech = SpeechManager(this)
        speechManager = speech
        progressStorage = ProgressStorage(this)

        setContent {
            EarlyReaderTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    EarlyReaderApp(
                        words = WordBank.starterWords,
                        stories = StoryBank.allStories,
                        progressStorage = progressStorage,
                        speechManager = speech,
                        onSpeakWord = { text -> speech.speakWord(text) },
                        onSpeakWordQueued = { text -> speech.speakWord(text, queue = true) },
                        onSpeakPhoneme = { segment, flush -> speech.speakPhoneme(segment, flush) },
                        onStopSpeech = { speech.stop() },
                        onAwaitSpeech = { speech.awaitIdle() },
                    )
                }
            }
        }
    }

    // Leaving the app (or locking the screen) must not leave a word or sentence still being said.
    override fun onStop() {
        speechManager?.stop()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager?.shutdown()
        speechManager = null
    }
}

data class MathChallenge(
    val question: String,
    val answer: Int,
    val options: List<Int>,
)

fun generateMathChallenge(): MathChallenge {
    val a = (2..5).random()
    val b = (2..5).random()
    val sum = a + b
    val incorrect1 = if (sum > 4) sum - 1 else sum + 3
    val incorrect2 = sum + 2
    return MathChallenge(
        question = "For grown-ups: $a + $b = ?",
        answer = sum,
        options = listOf(incorrect1, sum, incorrect2).shuffled(),
    )
}

@Composable
fun WordScreen(
    words: List<WordData> = WordBank.starterWords,
    onSpeakWord: (String) -> Unit = {},
    onSpeakPhoneme: (String, Boolean) -> Unit = { _, _ -> },
) {
    EarlyReaderApp(
        words = words,
        stories = StoryBank.allStories,
        onSpeakWord = onSpeakWord,
        onSpeakPhoneme = onSpeakPhoneme,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EarlyReaderApp(
    words: List<WordData> = WordBank.starterWords,
    stories: List<StoryData> = StoryBank.allStories,
    progressStorage: ProgressStorage? = null,
    speechManager: SpeechManager? = null,
    // Says a word or sentence now, cutting off whatever is playing (help button, tapping a sentence).
    onSpeakWord: (String) -> Unit = {},
    // Says the word that completes a scrub AFTER the sounds already queued, instead of cutting the last one off.
    onSpeakWordQueued: (String) -> Unit = onSpeakWord,
    // (segment, startsNewScrub): only a new scrub from the start flushes the speech queue.
    onSpeakPhoneme: (String, Boolean) -> Unit = { segment, flush -> speechManager?.speakPhoneme(segment, flush) },
    onStopSpeech: () -> Unit = { speechManager?.stop() },
    // Returns when what was just asked for has been said, or after a timeout: stories move on from this, so
    // progress never depends on speech actually happening.
    onAwaitSpeech: suspend () -> Unit = { speechManager?.awaitIdle() ?: delay(600) },
) {
    val initialMode = when (progressStorage?.lastAppMode) {
        ProgressStorage.MODE_STORY -> AppMode.STORY_MODE
        ProgressStorage.MODE_TRACKING -> AppMode.TRACK_TO_READ
        ProgressStorage.MODE_SENTENCES -> AppMode.SENTENCE_LAB
        else -> AppMode.WORD_PRACTICE
    }
    var appMode by rememberSaveable { mutableStateOf(initialMode) }
    var selectedStoryIndex by rememberSaveable {
        mutableIntStateOf(progressStorage?.lastActiveStoryIndex ?: 0)
    }
    var wordsCompletedCount by rememberSaveable {
        mutableIntStateOf(progressStorage?.completedWordsCount ?: 0)
    }
    var completedStories by remember {
        mutableStateOf(progressStorage?.completedStoryTitles ?: emptySet())
    }

    var showStoryPicker by remember { mutableStateOf(false) }
    var showMathGate by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var mathChallenge by remember { mutableStateOf(generateMathChallenge()) }
    var showResetConfirm by remember { mutableStateOf(false) }
    // Keeps each screen's saved state (page, word, level) while it is off screen, so switching tabs and back
    // carries on where the child was instead of starting again at page 1.
    val saveableStateHolder = rememberSaveableStateHolder()

    var practiceResetKey by rememberSaveable { mutableIntStateOf(0) }
    var storyResetKey by rememberSaveable { mutableIntStateOf(0) }

    val currentStory = stories[selectedStoryIndex % stories.size]

    val handleModeChange: (AppMode) -> Unit = { mode ->
        if (mode != appMode) onStopSpeech()
        appMode = mode
        progressStorage?.lastAppMode = when (mode) {
            AppMode.STORY_MODE -> ProgressStorage.MODE_STORY
            AppMode.TRACK_TO_READ -> ProgressStorage.MODE_TRACKING
            AppMode.SENTENCE_LAB -> ProgressStorage.MODE_SENTENCES
            AppMode.WORD_PRACTICE -> ProgressStorage.MODE_WORDS
        }
    }

    val handleStorySelect: (Int) -> Unit = { idx ->
        onStopSpeech()
        selectedStoryIndex = idx
        progressStorage?.lastActiveStoryIndex = idx
    }

    // After a story is finished, its saved page/word state is dropped, so coming back later starts it fresh.
    val handleNextStory: () -> Unit = {
        saveableStateHolder.removeState("story-$selectedStoryIndex-$storyResetKey")
        saveableStateHolder.removeState("track-$selectedStoryIndex-$storyResetKey")
        handleStorySelect((selectedStoryIndex + 1) % stories.size)
    }

    val handleWordComplete: () -> Unit = {
        wordsCompletedCount++
        progressStorage?.recordWordCompleted()
    }

    val handleStoryComplete: (StoryData) -> Unit = { s ->
        progressStorage?.recordStoryCompleted(s.title)
        completedStories = progressStorage?.completedStoryTitles ?: (completedStories + s.title)
    }

    val handleResetProgress: () -> Unit = {
        progressStorage?.resetAll()
        wordsCompletedCount = 0
        completedStories = emptySet()
        selectedStoryIndex = 0
        practiceResetKey++
        storyResetKey++
        showResetConfirm = false
        showSettingsDialog = false
    }

    val handleHomeClick: () -> Unit = {
        onStopSpeech()
        saveableStateHolder.removeState("story-$selectedStoryIndex-$storyResetKey")
        saveableStateHolder.removeState("track-$selectedStoryIndex-$storyResetKey")
        selectedStoryIndex = 0
        progressStorage?.lastActiveStoryIndex = 0
        practiceResetKey++
        storyResetKey++
        showStoryPicker = false
        showSettingsDialog = false
        showMathGate = false
        showResetConfirm = false
        handleModeChange(AppMode.WORD_PRACTICE)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Home button, mode selector, and adult parent gate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Dedicated Home button at top start
                Surface(
                    onClick = handleHomeClick,
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = "Home: return to start" },
                ) {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "🏠 Home",
                            style = MaterialTheme.typography.labelLarge,
                            color = VioletPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mode switcher: every chip is at least 48dp tall.
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ModeChip("Words", appMode == AppMode.WORD_PRACTICE) { handleModeChange(AppMode.WORD_PRACTICE) }
                    ModeChip("Stories", appMode == AppMode.STORY_MODE) { handleModeChange(AppMode.STORY_MODE) }
                    ModeChip("Track", appMode == AppMode.TRACK_TO_READ) { handleModeChange(AppMode.TRACK_TO_READ) }
                    ModeChip("Sentence Lab", appMode == AppMode.SENTENCE_LAB) { handleModeChange(AppMode.SENTENCE_LAB) }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        mathChallenge = generateMathChallenge()
                        showMathGate = true
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = "For grown-ups: settings and voice info"
                        },
                ) {
                    Text(text = "⚙️", fontSize = 20.sp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                val screenKey = when (appMode) {
                    AppMode.WORD_PRACTICE -> "words-$practiceResetKey"
                    AppMode.STORY_MODE -> "story-$selectedStoryIndex-$storyResetKey"
                    AppMode.TRACK_TO_READ -> "track-$selectedStoryIndex-$storyResetKey"
                    AppMode.SENTENCE_LAB -> "lab-$practiceResetKey"
                }
                saveableStateHolder.SaveableStateProvider(screenKey) {
                    when (appMode) {
                        AppMode.WORD_PRACTICE -> {
                            WordPracticeScreen(
                                words = words,
                                onSpeakWord = onSpeakWord,
                                onSpeakWordQueued = onSpeakWordQueued,
                                onSpeakPhoneme = onSpeakPhoneme,
                                onWordCompleted = handleWordComplete,
                            )
                        }
                        AppMode.STORY_MODE -> {
                            StoryModeScreen(
                                story = currentStory,
                                onSpeakWord = onSpeakWord,
                                onSpeakWordQueued = onSpeakWordQueued,
                                onSpeakPhoneme = onSpeakPhoneme,
                                onAwaitSpeech = onAwaitSpeech,
                                onOpenStoryPicker = { showStoryPicker = true },
                                onStoryCompleted = { handleStoryComplete(currentStory) },
                                onNextStory = handleNextStory,
                            )
                        }
                        AppMode.TRACK_TO_READ -> {
                            TrackToReadScreen(
                                story = currentStory,
                                onSpeakWord = onSpeakWord,
                                onAwaitSpeech = onAwaitSpeech,
                                onOpenStoryPicker = { showStoryPicker = true },
                                onStoryCompleted = { handleStoryComplete(currentStory) },
                                onNextStory = handleNextStory,
                            )
                        }
                        AppMode.SENTENCE_LAB -> {
                            SentenceLabScreen(
                                initialLevelIndex = progressStorage?.lastSentenceLabLevel ?: 0,
                                completedLevels = progressStorage?.completedSentenceLevels ?: emptySet(),
                                onSpeakSentence = onSpeakWord,
                                onLevelSelected = { idx ->
                                    progressStorage?.lastSentenceLabLevel = idx
                                },
                                onLevelCompleted = { levelId ->
                                    progressStorage?.recordSentenceLevelCompleted(levelId)
                                },
                            )
                        }
                    }
                }
            }
        }

        // Story Picker Dialog
        if (showStoryPicker) {
            AlertDialog(
                onDismissRequest = { showStoryPicker = false },
                title = { Text(text = "Choose a Story", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        stories.forEachIndexed { idx, s ->
                            val isSelected = idx == (selectedStoryIndex % stories.size)
                            val isCompleted = completedStories.contains(s.title)
                            Button(
                                onClick = {
                                    handleStorySelect(idx)
                                    showStoryPicker = false
                                },
                                colors = if (isSelected) {
                                    ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                                } else {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                            ) {
                                Text(
                                    text = "${idx + 1}. ${s.title}" + if (isCompleted) " ⭐" else "",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp,
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showStoryPicker = false }) {
                        Text(text = "Cancel")
                    }
                },
            )
        }

        // Math challenge dialog for grown-ups
        if (showMathGate) {
            AlertDialog(
                onDismissRequest = { showMathGate = false },
                title = { Text(text = "For grown-ups", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = "Please answer this quick question to open settings:")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = mathChallenge.question,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            mathChallenge.options.forEach { opt ->
                                Button(
                                    onClick = {
                                        if (opt == mathChallenge.answer) {
                                            showMathGate = false
                                            showSettingsDialog = true
                                        } else {
                                            // A wrong answer closes the gate and the next try is a different question,
                                            // so guessing among the three options can't open it.
                                            showMathGate = false
                                            mathChallenge = generateMathChallenge()
                                        }
                                    },
                                    modifier = Modifier.height(48.dp),
                                ) {
                                    Text(text = "$opt", fontSize = 18.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showMathGate = false }) {
                        Text(text = "Cancel")
                    }
                },
            )
        }

        // Adult dashboard dialog
        if (showSettingsDialog) {
            val context = LocalContext.current
            val voiceName = speechManager?.voiceName ?: "Default US English"
            val isOfflineVoice = speechManager?.isOfflineVoice ?: true
            val isNetworkOnly = speechManager?.isNetworkVoiceOnly ?: false

            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text(text = "Settings & Session Progress", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = "Early Reader - Adult Dashboard", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "• TTS Voice: $voiceName (${if (isOfflineVoice) "offline ready" else "network required"})")
                        if (VoiceSelection.needsOfflineGuidance(isOfflineVoice, isNetworkOnly)) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tip: For reliable offline play, download an offline voice in Android Settings -> Accessibility -> Text-to-speech output.",
                                color = WarmAccent,
                                fontSize = 12.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
                                } catch (_: Exception) {
                                    try {
                                        context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                                    } catch (_: Exception) {}
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        ) {
                            Text(text = "⚙️ System TTS Settings", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "• Current Mode: ${when (appMode) {
                            AppMode.WORD_PRACTICE -> "Word Practice"
                            AppMode.STORY_MODE -> "Story Mode (${currentStory.title})"
                            AppMode.TRACK_TO_READ -> "Track to Read (${currentStory.title})"
                            AppMode.SENTENCE_LAB -> "Sentence Lab"
                        }}")
                        Text(text = "• Story Suite: ${stories.size} decodable stories available")
                        Text(text = "• Total words completed: $wordsCompletedCount")
                        Text(text = "• Completed stories: ${completedStories.size} of ${stories.size}")
                        if (completedStories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            completedStories.forEach { title ->
                                Text(text = "  ✓ $title", color = VioletPrimary, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.height(48.dp),
                        ) {
                            Text(text = "Reset all progress")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showSettingsDialog = false }, modifier = Modifier.height(48.dp)) {
                        Text(text = "Close")
                    }
                },
            )
        }

        // Resetting wipes every star and count, so it has to be confirmed.
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text(text = "Reset all progress?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(text = "This clears every completed word, story and Sentence Lab level on this device. It cannot be undone.")
                },
                confirmButton = {
                    Button(onClick = handleResetProgress, modifier = Modifier.height(48.dp)) {
                        Text(text = "Yes, reset everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }, modifier = Modifier.height(48.dp)) {
                        Text(text = "Keep my progress")
                    }
                },
            )
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) VioletPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun WordPracticeScreen(
    words: List<WordData> = WordBank.starterWords,
    onSpeakWord: (String) -> Unit = {},
    onSpeakWordQueued: (String) -> Unit = onSpeakWord,
    onSpeakPhoneme: (String, Boolean) -> Unit = { _, _ -> },
    onWordCompleted: () -> Unit = {},
) {
    var wordIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentWord = words[wordIndex % words.size]

    var pictureRevealed by rememberSaveable { mutableStateOf(false) }
    var showReward by rememberSaveable { mutableStateOf(false) }
    var completed by rememberSaveable { mutableStateOf(false) }
    var attempt by rememberSaveable { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Sentence context across the top
            if (currentWord.sentenceContext.isNotEmpty()) {
                val fullSentenceText = currentWord.sentenceContext.joinToString(" ")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSpeakWord(fullSentenceText) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .semantics {
                            contentDescription = "Sentence: $fullSentenceText. Tap to hear full sentence"
                        },
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        currentWord.sentenceContext.forEach { token ->
                            val cleanToken = token.trimEnd('.', '!', '?', ',')
                            val isActive = cleanToken.equals(currentWord.text, ignoreCase = true)
                            Text(
                                text = token,
                                style = MaterialTheme.typography.headlineMedium,
                                fontSize = 24.sp,
                                color = if (isActive) WarmAccent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .drawBehind {
                                        if (isActive) {
                                            val strokeWidth = 3.5.dp.toPx()
                                            val y = size.height - strokeWidth / 2f
                                            drawLine(
                                                color = WarmAccent,
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = strokeWidth,
                                            )
                                        }
                                    }
                                    .padding(horizontal = 2.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { onSpeakWord(fullSentenceText) }
                            .semantics {
                                contentDescription = "Read full sentence: $fullSentenceText"
                            },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "🔊 Read sentence",
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = pictureRevealed && currentWord.img != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
            ) {
                IconButton(
                    onClick = { onSpeakWord(currentWord.text) },
                    modifier = Modifier
                        .size(72.dp)
                        .padding(bottom = 8.dp)
                        .semantics {
                            contentDescription = "Picture of ${currentWord.text}. Tap to hear it"
                        },
                ) {
                    Text(text = currentWord.img ?: "", fontSize = 48.sp)
                }
            }

            key(wordIndex, attempt) {
                SegmentedScrubBar(
                    segments = currentWord.segments,
                    onDragStart = { pictureRevealed = true },
                    onSegmentSound = onSpeakPhoneme,
                    onWordComplete = {
                        showReward = true
                        completed = true
                        pictureRevealed = true
                        onWordCompleted()
                        // Queued, so the whole word is said after the last sound instead of cutting it off.
                        onSpeakWordQueued(currentWord.text)
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Help button (speaker icon) to hear the word read aloud
            IconButton(
                onClick = { onSpeakWord(currentWord.text) },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(VioletPrimary)
                    .semantics {
                        contentDescription = "Hear this word"
                    },
            ) {
                Text(text = "🔊", fontSize = 24.sp)
            }

            if (completed) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = {
                            attempt++
                            completed = false
                            showReward = false
                            pictureRevealed = false
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .widthIn(min = 130.dp),
                    ) {
                        Text(text = "Again", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            wordIndex = (wordIndex + 1) % words.size
                            attempt = 0
                            completed = false
                            showReward = false
                            pictureRevealed = false
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .widthIn(min = 140.dp),
                    ) {
                        Text(text = "Next word ▸", fontSize = 20.sp)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showReward,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
        ) {
            Text(
                text = "⭐",
                fontSize = 64.sp,
                modifier = Modifier.semantics {
                    contentDescription = "Great job!"
                    liveRegion = LiveRegionMode.Polite
                },
            )
        }

        LaunchedEffect(showReward) {
            if (showReward) {
                delay(1200)
                showReward = false
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoryModeScreen(
    story: StoryData = StoryBank.redHenStory,
    onSpeakWord: (String) -> Unit = {},
    onSpeakWordQueued: (String) -> Unit = onSpeakWord,
    onSpeakPhoneme: (String, Boolean) -> Unit = { _, _ -> },
    onAwaitSpeech: suspend () -> Unit = { delay(600) },
    onOpenStoryPicker: () -> Unit = {},
    onStoryCompleted: () -> Unit = {},
    onNextStory: () -> Unit = {},
) {
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    var activeWordIndex by rememberSaveable { mutableIntStateOf(0) }
    var pageCompleted by rememberSaveable { mutableStateOf(false) }
    var pictureRevealed by rememberSaveable { mutableStateOf(false) }
    var showReward by rememberSaveable { mutableStateOf(false) }
    var attempt by rememberSaveable { mutableIntStateOf(0) }
    // The current word is done and the story is waiting for it to be said before moving on. A second tap on a
    // tricky word while this is set does nothing, so a double tap can no longer skip a word.
    var pendingAdvance by rememberSaveable { mutableStateOf(false) }

    val currentPage = story.pages[pageIndex % story.pages.size]
    val safeWordIndex = activeWordIndex.coerceIn(0, currentPage.words.size - 1)
    val currentWord = currentPage.words[safeWordIndex]
    val cleanSpoken = currentWord.text.trimEnd('.', '!', '?', ',')

    // Moves on once the word has been said (or the wait times out). Driven by this saved flag rather than by one
    // particular utterance ending, so leaving the screen and coming back (the flag is restored) still carries on.
    LaunchedEffect(pendingAdvance, pageIndex, activeWordIndex, pageCompleted) {
        if (pendingAdvance && !pageCompleted) {
            onAwaitSpeech()
            pendingAdvance = false
            if (activeWordIndex + 1 < currentPage.words.size) {
                activeWordIndex++
                pictureRevealed = false
            } else {
                pageCompleted = true
                showReward = true
            }
        }
    }

    // Finishing the last page completes the story, however the child leaves (Next story, Read again, or a tab).
    LaunchedEffect(pageCompleted, pageIndex) {
        if (pageCompleted && pageIndex == story.pages.size - 1) onStoryCompleted()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Interactive Story & Page Indicator (tapping opens story picker)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .heightIn(min = 48.dp)
                    .clickable { onOpenStoryPicker() }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${story.title} • Page ${pageIndex + 1} of ${story.pages.size} ▾",
                    style = MaterialTheme.typography.titleSmall,
                    color = VioletPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sentence Context across the top (using FlowRow to wrap gracefully on mobile)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSpeakWord(currentPage.fullSentence) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .semantics {
                        contentDescription = "Sentence: ${currentPage.fullSentence}. Tap to hear full sentence"
                    },
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center,
                ) {
                    currentPage.words.forEachIndexed { idx, w ->
                        val isDone = pageCompleted || idx < activeWordIndex
                        val isActive = !pageCompleted && idx == activeWordIndex
                        Text(
                            text = w.text,
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 24.sp,
                            color = when {
                                isActive -> WarmAccent
                                isDone -> VioletPrimary
                                else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .clickable { onSpeakWord(w.text.trimEnd('.', '!', '?', ',')) }
                                .drawBehind {
                                    if (isActive) {
                                        val strokeWidth = 3.5.dp.toPx()
                                        val y = size.height - strokeWidth / 2f
                                        drawLine(
                                            color = WarmAccent,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth,
                                        )
                                    }
                                }
                                .padding(horizontal = 2.dp, vertical = 4.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { onSpeakWord(currentPage.fullSentence) }
                        .semantics {
                            contentDescription = "Read full sentence: ${currentPage.fullSentence}"
                        },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "🔊 Read full sentence",
                            style = MaterialTheme.typography.labelMedium,
                            color = VioletPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Picture Reveal
            AnimatedVisibility(
                visible = pictureRevealed && currentWord.img != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
            ) {
                IconButton(
                    onClick = { onSpeakWord(cleanSpoken) },
                    modifier = Modifier
                        .size(72.dp)
                        .padding(bottom = 8.dp)
                        .semantics {
                            contentDescription = "Picture of $cleanSpoken. Tap to hear it"
                        },
                ) {
                    Text(text = currentWord.img ?: "", fontSize = 48.sp)
                }
            }

            if (pageCompleted) {
                // Page Finished state
                Text(
                    text = if (pageIndex == story.pages.size - 1) "Story complete! 🌟" else "Page read! 🎉",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = VioletPrimary,
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = { onSpeakWord(currentPage.fullSentence) },
                        modifier = Modifier.height(52.dp),
                    ) {
                        Text(text = "Read page 🔊", fontSize = 17.sp)
                    }

                    if (pageIndex < story.pages.size - 1) {
                        Button(
                            onClick = {
                                pageIndex++
                                activeWordIndex = 0
                                pageCompleted = false
                                pictureRevealed = false
                                showReward = false
                                attempt = 0
                            },
                            modifier = Modifier.height(52.dp),
                        ) {
                            Text(text = "Next page ▸", fontSize = 17.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                pageIndex = 0
                                activeWordIndex = 0
                                pageCompleted = false
                                pictureRevealed = false
                                showReward = false
                                attempt = 0
                            },
                            modifier = Modifier.height(52.dp),
                        ) {
                            Text(text = "Read again ↺", fontSize = 17.sp)
                        }

                        Button(
                            onClick = { onNextStory() },
                            modifier = Modifier.height(52.dp),
                        ) {
                            Text(text = "Next story ▸", fontSize = 17.sp)
                        }
                    }
                }
            } else {
                // Word interaction
                if (currentWord.sight) {
                    // Tricky Word card / button matching web prototype
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(2.dp, WarmAccent),
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .clickable {
                                if (!pendingAdvance) {
                                    onSpeakWord(cleanSpoken)
                                    pendingAdvance = true
                                }
                            }
                            .semantics {
                                contentDescription = "Tricky word: ${currentWord.text}. Tap to hear it and continue"
                            },
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 36.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Tricky word",
                                style = MaterialTheme.typography.labelLarge,
                                color = WarmAccent,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentWord.text,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap to hear 🔊",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    // Decodable word: SegmentedScrubBar
                    key(pageIndex, activeWordIndex, attempt) {
                        SegmentedScrubBar(
                            segments = currentWord.segments,
                            onDragStart = { pictureRevealed = true },
                            onSegmentSound = onSpeakPhoneme,
                            onWordComplete = {
                                // Queued behind the last sound, then the story moves on when it has been said.
                                onSpeakWordQueued(cleanSpoken)
                                pictureRevealed = true
                                showReward = true
                                pendingAdvance = true
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    IconButton(
                        onClick = { onSpeakWord(cleanSpoken) },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(VioletPrimary)
                            .semantics {
                                contentDescription = "Hear this word"
                            },
                    ) {
                        Text(text = "🔊", fontSize = 24.sp)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showReward,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
        ) {
            Text(
                text = "⭐",
                fontSize = 64.sp,
                modifier = Modifier.semantics {
                    contentDescription = "Great job!"
                    liveRegion = LiveRegionMode.Polite
                },
            )
        }

        LaunchedEffect(showReward) {
            if (showReward) {
                delay(1200)
                showReward = false
            }
        }
    }
}

fun calculateTrackedWordIndex(progress: Float, wordCount: Int): Int {
    if (wordCount <= 0) return 0
    val clamped = progress.coerceIn(0f, 1f)
    return ((clamped * wordCount).toInt()).coerceIn(0, wordCount - 1)
}

fun calculateSnappedTrackProgress(wordIndex: Int, wordCount: Int): Float {
    if (wordCount <= 1) return 1f
    val clampedIdx = wordIndex.coerceIn(0, wordCount - 1)
    return clampedIdx.toFloat() / (wordCount - 1).toFloat()
}

/**
 * The tracked word may move at most ONE word past the furthest word reached so far, so a single tap on the right
 * end of the bar (or on the last word) cannot finish the sentence: the child has to slide along it.
 */
fun limitTrackedWordIndex(desiredIndex: Int, reachedIndex: Int): Int =
    desiredIndex.coerceAtMost(reachedIndex + 1)

/** Where a tracker move came from: a finger on the bar, or a tap on a word chip (which can never finish the page). */
enum class TrackSource { BAR, CHIP }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackToReadScreen(
    story: StoryData = StoryBank.redHenStory,
    onSpeakWord: (String) -> Unit = {},
    onAwaitSpeech: suspend () -> Unit = { delay(450) },
    onOpenStoryPicker: () -> Unit = {},
    onStoryCompleted: () -> Unit = {},
    onNextStory: () -> Unit = {},
) {
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    var trackProgress by rememberSaveable { mutableFloatStateOf(0f) }
    var trackedWordIndex by rememberSaveable { mutableIntStateOf(0) }
    // The furthest word reached on this page (see limitTrackedWordIndex); also decides when a chip's picture shows.
    var reachedWordIndex by rememberSaveable { mutableIntStateOf(0) }
    var speakWhenTracked by rememberSaveable { mutableStateOf(false) } // false = Highlight Only (default)
    var pageCompleted by rememberSaveable { mutableStateOf(false) }
    var showReward by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val currentPage = story.pages[pageIndex % story.pages.size]
    val wordCount = currentPage.words.size

    val updateTrackProgress: (Float, TrackSource) -> Unit = { rawProgress, source ->
        if (wordCount > 0) {
            val clamped = rawProgress.coerceIn(0f, 1f)
            val prevIdx = trackedWordIndex
            val nextIdx = limitTrackedWordIndex(calculateTrackedWordIndex(clamped, wordCount), reachedWordIndex)
            trackedWordIndex = nextIdx
            trackProgress = calculateSnappedTrackProgress(nextIdx, wordCount)
            if (nextIdx > reachedWordIndex) reachedWordIndex = nextIdx

            val wordChanged = nextIdx != prevIdx || source == TrackSource.CHIP
            if (wordChanged && speakWhenTracked) {
                val word = currentPage.words[nextIdx]
                val cleanSpoken = word.text.trimEnd('.', '!', '?', ',')
                onSpeakWord(cleanSpoken)
            }

            // Only the bar can finish the page, and only by arriving at the last word the allowed way.
            if (!pageCompleted && source == TrackSource.BAR && nextIdx == wordCount - 1) {
                pageCompleted = true
                trackProgress = 1f
                showReward = true
                if (speakWhenTracked) {
                    coroutineScope.launch {
                        onAwaitSpeech()
                        onSpeakWord(currentPage.fullSentence)
                    }
                }
            }
        }
    }

    // Finishing the last page completes the story, however the child leaves (Next story, or a tab).
    LaunchedEffect(pageCompleted, pageIndex) {
        if (pageCompleted && pageIndex == story.pages.size - 1) onStoryCompleted()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Interactive Story & Page Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .heightIn(min = 48.dp)
                    .clickable { onOpenStoryPicker() }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "👆 Track to Read • ${story.title} • Page ${pageIndex + 1} of ${story.pages.size} ▾",
                    style = MaterialTheme.typography.titleSmall,
                    color = VioletPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Toggle: Highlight Only (Silent) vs Speak Words + Read Full Sentence
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (!speakWhenTracked) VioletPrimary else Color.Transparent)
                            .clickable { speakWhenTracked = false }
                            .padding(horizontal = 10.dp)
                            .semantics {
                                contentDescription = "Highlight Only mode: silent word tracking"
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "🔇 Highlight Only",
                            color = if (!speakWhenTracked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (speakWhenTracked) VioletPrimary else Color.Transparent)
                            .clickable { speakWhenTracked = true }
                            .padding(horizontal = 10.dp)
                            .semantics {
                                contentDescription = "Speak Words mode: pronounce each word when tracked"
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "🔊 Speak Words",
                            color = if (speakWhenTracked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable { onSpeakWord(currentPage.fullSentence) }
                        .semantics {
                            contentDescription = "Read full sentence: ${currentPage.fullSentence}"
                        },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "🔊 Read sentence",
                            style = MaterialTheme.typography.labelMedium,
                            color = VioletPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (pageCompleted) "You tracked the whole sentence! 🎉" else "Slide the tracker bar across the sentence:",
                style = MaterialTheme.typography.bodyMedium,
                color = if (pageCompleted) VioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(14.dp))

            var barWidthPx by remember { mutableIntStateOf(0) }
            val animatedProgress by animateFloatAsState(targetValue = trackProgress.coerceIn(0f, 1f), label = "trackJump")
            val thumbWidth = ThumbWidth
            val thumbHeight = ThumbHeight
            val thumbWidthPx = with(LocalDensity.current) { thumbWidth.toPx() }
            val thumbOffsetPx = ((barWidthPx - thumbWidthPx).coerceAtLeast(0f) * animatedProgress).roundToInt()
            val tickIntervals = (wordCount - 1).coerceAtLeast(1)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Sentence Tracking Chips — Per-word text + dedicated pill capsule directly underneath each word
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        currentPage.words.forEachIndexed { idx, word ->
                            val isDone = pageCompleted || idx < trackedWordIndex
                            val isActive = !pageCompleted && idx == trackedWordIndex
                            val isPassed = pageCompleted || idx < reachedWordIndex
                            val isTricky = word.sight

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                    .clickable {
                                        updateTrackProgress((idx + 0.5f) / wordCount.toFloat(), TrackSource.CHIP)
                                    }
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                                    .semantics {
                                        contentDescription = "${word.text}. " + when {
                                            isActive -> "Current word"
                                            isDone -> "Read"
                                            else -> "Upcoming word"
                                        }
                                    },
                            ) {
                                // Word text with active bottom underline and picture
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .drawBehind {
                                            if (isActive) {
                                                val strokeWidth = 3.5.dp.toPx()
                                                val y = size.height - strokeWidth / 2f
                                                drawLine(
                                                    color = WarmAccent,
                                                    start = Offset(0f, y),
                                                    end = Offset(size.width, y),
                                                    strokeWidth = strokeWidth,
                                                )
                                            }
                                        }
                                        .padding(bottom = 3.dp),
                                ) {
                                    if (word.img != null) {
                                        Text(
                                            text = word.img,
                                            fontSize = 24.sp,
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .alpha(if (isPassed) 1f else 0f),
                                        )
                                    }
                                    Text(
                                        text = word.text,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isActive -> TextDark
                                            isDone -> VioletPrimary
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f)
                                        },
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Dedicated Per-Word Pill Capsule directly underneath the word
                                val pillBg = when {
                                    isTricky && (isActive || isDone) -> Color(0xFFF97316)
                                    isTricky -> Color(0xFFFFEDD5)
                                    isActive -> TextDark
                                    isDone -> VioletPrimary
                                    else -> Color.White
                                }
                                val pillBorder = when {
                                    isTricky -> Color(0xFFFB923C)
                                    isActive -> TextDark
                                    isDone -> VioletPrimary
                                    else -> Color(0xFFCBD5E1)
                                }

                                Box(
                                    modifier = Modifier
                                        .widthIn(min = 40.dp)
                                        .height(13.dp)
                                        .clip(RoundedCornerShape(6.5.dp))
                                        .background(pillBg)
                                        .border(1.5.dp, pillBorder, RoundedCornerShape(6.5.dp)),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Stepped Word-to-Word Jumping Tracker Bar with Pointed Thumb
                    Box(
                        modifier = Modifier
                            .widthIn(max = 380.dp)
                            .fillMaxWidth(0.9f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(SegmentEmpty)
                            .border(2.dp, VioletPrimary, RoundedCornerShape(28.dp))
                            .onSizeChanged { barWidthPx = it.width }
                            .pointerInput(pageIndex, wordCount, speakWhenTracked) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val pointerId = down.id
                                    if (barWidthPx > 0) {
                                        updateTrackProgress(down.position.x / barWidthPx.toFloat(), TrackSource.BAR)
                                    }
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                        if (!change.pressed) break
                                        change.consume()
                                        if (barWidthPx > 0) {
                                            updateTrackProgress(change.position.x / barWidthPx.toFloat(), TrackSource.BAR)
                                        }
                                    }
                                }
                            }
                            .semantics {
                                contentDescription = "Scrolling sentence tracker bar. Word ${trackedWordIndex + 1} of $wordCount"
                            },
                    ) {
                        // Stepped progress fill jumping from word stop to word stop
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .background(VioletPrimary),
                        )

                        // Word stop tick dividers
                        Row(modifier = Modifier.fillMaxSize()) {
                            repeat(tickIntervals) { idx ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(Color.Transparent),
                                ) {
                                    if (idx < tickIntervals - 1) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .width(2.dp)
                                                .fillMaxHeight()
                                                .background(Color.White.copy(alpha = 0.5f)),
                                        )
                                    }
                                }
                            }
                        }

                        // Stepped pointed thumb jumping from word stop to word stop pointing directly up at the word
                        FingerSliderThumb(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset { IntOffset(thumbOffsetPx, 0) }
                                .size(width = thumbWidth, height = thumbHeight),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Floating purple checkmark button (✓) to advance or complete sentence
                Surface(
                    onClick = {
                        if (pageIndex < story.pages.size - 1) {
                            pageIndex++
                            trackProgress = 0f
                            trackedWordIndex = 0
                            reachedWordIndex = 0
                            pageCompleted = false
                        } else {
                            pageCompleted = true
                            showReward = true
                            onStoryCompleted()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = VioletPrimary,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(52.dp)
                        .semantics { contentDescription = "Next page or complete sentence" },
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pageCompleted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = {
                            trackProgress = 0f
                            trackedWordIndex = 0
                            reachedWordIndex = 0
                            pageCompleted = false
                            showReward = false
                        },
                        modifier = Modifier.height(52.dp),
                    ) {
                        Text(text = "Track again ↺", fontSize = 17.sp)
                    }

                    if (pageIndex < story.pages.size - 1) {
                        Button(
                            onClick = {
                                pageIndex++
                                trackProgress = 0f
                                trackedWordIndex = 0
                                reachedWordIndex = 0
                                pageCompleted = false
                                showReward = false
                            },
                            modifier = Modifier.height(52.dp),
                        ) {
                            Text(text = "Next page ▸", fontSize = 17.sp)
                        }
                    } else {
                        Button(
                            onClick = { onNextStory() },
                            modifier = Modifier.height(52.dp),
                        ) {
                            Text(text = "Next story ▸", fontSize = 17.sp)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showReward,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
        ) {
            Text(
                text = "⭐",
                fontSize = 64.sp,
                modifier = Modifier.semantics {
                    contentDescription = "Great job tracking the sentence!"
                    liveRegion = LiveRegionMode.Polite
                },
            )
        }

        LaunchedEffect(showReward) {
            if (showReward) {
                delay(1400)
                showReward = false
            }
        }
    }
}

@Preview(name = "Phone Portrait - Word Practice", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewWordPracticePhone() {
    EarlyReaderTheme {
        WordPracticeScreen(
            words = WordBank.starterWords,
        )
    }
}

@Preview(
    name = "Tablet Landscape - Story Mode",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    showBackground = true,
    backgroundColor = 0xFFFAF7F2,
)
@Composable
fun PreviewStoryModeTablet() {
    EarlyReaderTheme {
        StoryModeScreen(
            story = StoryBank.redHenStory,
        )
    }
}

@Preview(name = "Full App - Word Practice", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewEarlyReaderApp() {
    EarlyReaderTheme {
        EarlyReaderApp(
            words = WordBank.starterWords,
            stories = StoryBank.allStories,
        )
    }
}
