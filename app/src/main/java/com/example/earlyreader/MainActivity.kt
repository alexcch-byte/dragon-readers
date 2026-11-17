package com.example.earlyreader

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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.earlyreader.audio.SpeechManager
import com.example.earlyreader.model.PageData
import com.example.earlyreader.model.StoryBank
import com.example.earlyreader.model.StoryData
import com.example.earlyreader.model.WordBank
import com.example.earlyreader.model.WordData
import com.example.earlyreader.ui.scrub.SegmentedScrubBar
import com.example.earlyreader.ui.theme.EarlyReaderTheme
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppMode {
    WORD_PRACTICE,
    STORY_MODE,
}

class MainActivity : ComponentActivity() {
    private var speechManager: SpeechManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val speech = SpeechManager(this)
        speechManager = speech

        setContent {
            EarlyReaderTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    EarlyReaderApp(
                        words = WordBank.starterWords,
                        story = StoryBank.redHenStory,
                        onSpeakWord = { text -> speech.speakWord(text) },
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
) {
    EarlyReaderApp(
        words = words,
        story = StoryBank.redHenStory,
        onSpeakWord = onSpeakWord,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EarlyReaderApp(
    words: List<WordData> = WordBank.starterWords,
    story: StoryData = StoryBank.redHenStory,
    onSpeakWord: (String) -> Unit = {},
) {
    var appMode by rememberSaveable { mutableStateOf(AppMode.WORD_PRACTICE) }
    var wordsCompletedCount by rememberSaveable { mutableIntStateOf(0) }
    var storiesCompletedCount by rememberSaveable { mutableIntStateOf(0) }

    var showMathGate by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var mathChallenge by remember { mutableStateOf(generateMathChallenge()) }
    var gateError by remember { mutableStateOf(false) }

    var practiceResetKey by rememberSaveable { mutableIntStateOf(0) }
    var storyResetKey by rememberSaveable { mutableIntStateOf(0) }

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
                // Mode Toggle
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
                            .clickable { appMode = AppMode.WORD_PRACTICE }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "Word Practice",
                            color = if (appMode == AppMode.WORD_PRACTICE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (appMode == AppMode.STORY_MODE) VioletPrimary else Color.Transparent)
                            .clickable { appMode = AppMode.STORY_MODE }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "Story Mode",
                            color = if (appMode == AppMode.STORY_MODE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
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
                    modifier = Modifier.semantics {
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
                                onWordCompleted = { wordsCompletedCount++ },
                            )
                        }
                    }
                    AppMode.STORY_MODE -> {
                        key(storyResetKey) {
                            StoryModeScreen(
                                story = story,
                                onSpeakWord = onSpeakWord,
                                onStoryCompleted = { storiesCompletedCount++ },
                            )
                        }
                    }
                }
            }
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
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text(text = "Settings & Voice Info", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = "Early Reader - Adult Dashboard", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "• TTS Voice: English (US) 0.8x rate, offline preferred")
                        Text(text = "• Current Mode: ${if (appMode == AppMode.WORD_PRACTICE) "Word Practice" else "Story Mode (${story.title})"}")
                        Text(text = "• Completed words in session: $wordsCompletedCount")
                        Text(text = "• Completed stories in session: $storiesCompletedCount")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                practiceResetKey++
                                storyResetKey++
                                wordsCompletedCount = 0
                                storiesCompletedCount = 0
                                showSettingsDialog = false
                            },
                        ) {
                            Text(text = "Reset word & story progress")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showSettingsDialog = false }) {
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
    onStoryCompleted: () -> Unit = {},
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
            // Page Indicator
            Text(
                text = "${story.title} • Page ${pageIndex + 1} of ${story.pages.size}",
                style = MaterialTheme.typography.titleSmall,
                color = VioletPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

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
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = { onSpeakWord(currentPage.fullSentence) },
                        modifier = Modifier.height(52.dp),
                    ) {
                        Text(text = "Read page 🔊", fontSize = 18.sp)
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
                            Text(text = "Next page ▸", fontSize = 18.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                onStoryCompleted()
                                pageIndex = 0
                                activeWordIndex = 0
                                pageCompleted = false
                                pictureRevealed = false
                                showReward = false
                                attempt = 0
                            },
                            modifier = Modifier.height(52.dp),
                        ) {
                            Text(text = "Read again ↺", fontSize = 18.sp)
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
