package com.example.earlyreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.earlyreader.audio.SpeechManager
import com.example.earlyreader.model.WordBank
import com.example.earlyreader.model.WordData
import com.example.earlyreader.ui.scrub.SegmentedScrubBar
import com.example.earlyreader.ui.theme.EarlyReaderTheme
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private var speechManager: SpeechManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val speech = SpeechManager(this)
        speechManager = speech

        setContent {
            EarlyReaderTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WordScreen(
                        words = WordBank.starterWords,
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
    var wordIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentWord = words[wordIndex % words.size]

    var pictureRevealed by rememberSaveable { mutableStateOf(false) }
    var showReward by rememberSaveable { mutableStateOf(false) }
    var completed by rememberSaveable { mutableStateOf(false) }
    var wordsCompletedCount by rememberSaveable { mutableIntStateOf(0) }
    // The bar finishes for good once the word is complete (its progress and "locked" flag are saved).
    // Bumping this builds a brand new bar, so "Again" gives the child another go.
    var attempt by rememberSaveable { mutableIntStateOf(0) }

    var showMathGate by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var mathChallenge by remember { mutableStateOf(generateMathChallenge()) }
    var gateError by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top bar for grown-ups
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Early Reader",
                style = MaterialTheme.typography.titleMedium,
                color = VioletPrimary,
                fontWeight = FontWeight.Bold,
            )
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Sentence context across the top with active word emphasized (Milestone 3 parity)
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
                        wordsCompletedCount++
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
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
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

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text(text = "Settings & Voice Info", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = "Early Reader - Adult Dashboard", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "• TTS Voice: English (US) 0.8x rate, offline preferred")
                        Text(text = "• Word Bank: ${words.size} decodable CVC words")
                        Text(text = "• Completed in this session: $wordsCompletedCount words")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                wordIndex = 0
                                attempt = 0
                                completed = false
                                showReward = false
                                pictureRevealed = false
                                wordsCompletedCount = 0
                                showSettingsDialog = false
                            },
                        ) {
                            Text(text = "Reset word progress")
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
