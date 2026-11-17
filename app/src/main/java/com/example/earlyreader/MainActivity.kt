package com.example.earlyreader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.earlyreader.audio.SpeechManager
import com.example.earlyreader.model.PageData
import com.example.earlyreader.model.StoryBank
import com.example.earlyreader.model.StoryData
import com.example.earlyreader.model.WordBank
import com.example.earlyreader.model.WordData
import com.example.earlyreader.storage.ProgressStorage
import com.example.earlyreader.ui.scrub.SegmentedScrubBar
import com.example.earlyreader.ui.sentences.SentenceLabScreen
import com.example.earlyreader.ui.theme.EarlyReaderTheme
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppMode {
    WORD_PRACTICE,
    STORY_MODE,
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
                        onSpeakPhoneme = { segment -> speech.speakPhoneme(segment) },
                    )
                }
            }
        }
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
    onSpeakPhoneme: (String) -> Unit = {},
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
    onSpeakWord: (String) -> Unit = {},
    onSpeakPhoneme: (String) -> Unit = { speechManager?.speakPhoneme(it) },
) {
    val initialMode = when (progressStorage?.lastAppMode) {
        ProgressStorage.MODE_STORY -> AppMode.STORY_MODE
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
    var gateError by remember { mutableStateOf(false) }

    var practiceResetKey by rememberSaveable { mutableIntStateOf(0) }
    var storyResetKey by rememberSaveable { mutableIntStateOf(0) }

    val currentStory = stories[selectedStoryIndex % stories.size]

    val handleModeChange: (AppMode) -> Unit = { mode ->
        appMode = mode
        progressStorage?.lastAppMode = when (mode) {
            AppMode.STORY_MODE -> ProgressStorage.MODE_STORY
            AppMode.SENTENCE_LAB -> ProgressStorage.MODE_SENTENCES
            AppMode.WORD_PRACTICE -> ProgressStorage.MODE_WORDS
        }
    }

    val handleStorySelect: (Int) -> Unit = { idx ->
        selectedStoryIndex = idx
        progressStorage?.lastActiveStoryIndex = idx
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
        showSettingsDialog = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with mode selector and adult parent gate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 3-Mode Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (appMode == AppMode.WORD_PRACTICE) VioletPrimary else Color.Transparent)
                            .clickable { handleModeChange(AppMode.WORD_PRACTICE) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "Words",
                            color = if (appMode == AppMode.WORD_PRACTICE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (appMode == AppMode.STORY_MODE) VioletPrimary else Color.Transparent)
                            .clickable { handleModeChange(AppMode.STORY_MODE) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "Stories",
                            color = if (appMode == AppMode.STORY_MODE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (appMode == AppMode.SENTENCE_LAB) VioletPrimary else Color.Transparent)
                            .clickable { handleModeChange(AppMode.SENTENCE_LAB) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "Sentence Lab",
                            color = if (appMode == AppMode.SENTENCE_LAB) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                IconButton(
                    onClick = {
                        mathChallenge = generateMathChallenge()
                        gateError = false
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
                when (appMode) {
                    AppMode.WORD_PRACTICE -> {
                        key(practiceResetKey) {
                            WordPracticeScreen(
                                words = words,
                                onSpeakWord = onSpeakWord,
                                onSpeakPhoneme = onSpeakPhoneme,
                                onWordCompleted = handleWordComplete,
                            )
                        }
                    }
                    AppMode.STORY_MODE -> {
                        key(selectedStoryIndex, storyResetKey) {
                            StoryModeScreen(
                                story = currentStory,
                                onSpeakWord = onSpeakWord,
                                onSpeakPhoneme = onSpeakPhoneme,
                                onOpenStoryPicker = { showStoryPicker = true },
                                onStoryCompleted = { handleStoryComplete(currentStory) },
                                onNextStory = {
                                    val nextIdx = (selectedStoryIndex + 1) % stories.size
                                    handleStorySelect(nextIdx)
                                },
                            )
                        }
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
                        if (gateError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Not quite, try again.", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            mathChallenge.options.forEach { opt ->
                                Button(
                                    onClick = {
                                        if (opt == mathChallenge.answer) {
                                            showMathGate = false
                                            showSettingsDialog = true
                                        } else {
                                            gateError = true
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
                        if (isNetworkOnly || !isOfflineVoice) {
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
                        Text(text = "• Current Mode: ${if (appMode == AppMode.WORD_PRACTICE) "Word Practice" else "Story Mode (${currentStory.title})"}")
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
                            onClick = handleResetProgress,
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
    }
}

@Composable
fun WordPracticeScreen(
    words: List<WordData> = WordBank.starterWords,
    onSpeakWord: (String) -> Unit = {},
    onSpeakPhoneme: (String) -> Unit = {},
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
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    currentWord.sentenceContext.forEachIndexed { idx, token ->
                        val cleanToken = token.trimEnd('.', '!', '?', ',')
                        val isActive = cleanToken.equals(currentWord.text, ignoreCase = true)
                        Text(
                            text = if (idx == currentWord.sentenceContext.size - 1) token else "$token ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = if (isActive) 28.sp else 22.sp,
                            color = if (isActive) WarmAccent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        )
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
                        onSpeakWord(currentWord.text)
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
    onSpeakPhoneme: (String) -> Unit = {},
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

    val coroutineScope = rememberCoroutineScope()
    val currentPage = story.pages[pageIndex % story.pages.size]
    val safeWordIndex = activeWordIndex.coerceIn(0, currentPage.words.size - 1)
    val currentWord = currentPage.words[safeWordIndex]
    val cleanSpoken = currentWord.text.trimEnd('.', '!', '?', ',')

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
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
            ) {
                currentPage.words.forEachIndexed { idx, w ->
                    val isDone = pageCompleted || idx < activeWordIndex
                    val isActive = !pageCompleted && idx == activeWordIndex
                    Text(
                        text = "${w.text} ",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = if (isActive) 28.sp else 22.sp,
                        color = when {
                            isActive -> WarmAccent
                            isDone -> VioletPrimary
                            else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                        },
                        fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSpeakWord(w.text.trimEnd('.', '!', '?', ',')) }
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                    )
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
                            onClick = {
                                onStoryCompleted()
                                onNextStory()
                            },
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
                                onSpeakWord(cleanSpoken)
                                coroutineScope.launch {
                                    delay(500)
                                    if (activeWordIndex + 1 < currentPage.words.size) {
                                        activeWordIndex++
                                        pictureRevealed = false
                                    } else {
                                        pageCompleted = true
                                        showReward = true
                                    }
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
                                onSpeakWord(cleanSpoken)
                                pictureRevealed = true
                                showReward = true
                                coroutineScope.launch {
                                    delay(600)
                                    if (activeWordIndex + 1 < currentPage.words.size) {
                                        activeWordIndex++
                                        pictureRevealed = false
                                    } else {
                                        pageCompleted = true
                                    }
                                }
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
