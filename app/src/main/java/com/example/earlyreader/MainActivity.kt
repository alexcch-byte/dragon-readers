package com.example.earlyreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.earlyreader.audio.SpeechManager
import com.example.earlyreader.model.WordBank
import com.example.earlyreader.model.WordData
import com.example.earlyreader.ui.scrub.SegmentedScrubBar
import com.example.earlyreader.ui.theme.EarlyReaderTheme
import com.example.earlyreader.ui.theme.VioletPrimary
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

@Composable
fun WordScreen(
    words: List<WordData> = WordBank.starterWords,
    onSpeakWord: (String) -> Unit = {},
) {
    var wordIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentWord = words[wordIndex % words.size]

    var showReward by rememberSaveable { mutableStateOf(false) }
    var completed by rememberSaveable { mutableStateOf(false) }
    // The bar finishes for good once the word is complete (its progress and "locked" flag are saved).
    // Bumping this builds a brand new bar, so "Again" gives the child another go.
    var attempt by rememberSaveable { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            key(wordIndex, attempt) {
                SegmentedScrubBar(
                    segments = currentWord.segments,
                    onWordComplete = {
                        showReward = true
                        completed = true
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
    }
}
