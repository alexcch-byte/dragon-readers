package com.example.earlyreader.ui.sentences

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.earlyreader.ui.theme.EarlyReaderTheme
import com.example.earlyreader.ui.theme.TextDark
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent
import kotlinx.coroutines.delay

data class SentenceLabItem(
    val text: String,
    val tags: List<String>,
) {
    val words: List<String> = text.split(" ")
    val nounIndices: Set<Int> = tags.mapIndexedNotNull { idx, tag ->
        if (tag == "noun") idx else null
    }.toSet()
}

object SentenceLabData {
    val level1Items: List<SentenceLabItem> = listOf(
        SentenceLabItem(
            text = "The girl fed the hungry rabbit.",
            tags = listOf("det", "noun", "verb", "det", "adj", "noun"),
        ),
        SentenceLabItem(
            text = "Dad cooked soup in the kitchen.",
            tags = listOf("noun", "verb", "noun", "prep", "det", "noun"),
        ),
        SentenceLabItem(
            text = "The teacher put books on the shelf.",
            tags = listOf("det", "noun", "verb", "noun", "prep", "det", "noun"),
        ),
        SentenceLabItem(
            text = "A tall tree grew beside the river.",
            tags = listOf("det", "adj", "noun", "verb", "prep", "det", "noun"),
        ),
        SentenceLabItem(
            text = "My brother found a coin under the table.",
            tags = listOf("det", "noun", "verb", "det", "noun", "prep", "det", "noun"),
        ),
        SentenceLabItem(
            text = "The farmer carried apples to the market.",
            tags = listOf("det", "noun", "verb", "noun", "prep", "det", "noun"),
        ),
    )
}

val NounHighlightBg = Color(0xFFDBEAFE)
val NounHighlightBorder = Color(0xFF2563EB)
val NounHighlightText = Color(0xFF1E40AF)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceLabScreen(
    items: List<SentenceLabItem> = SentenceLabData.level1Items,
    onSpeakSentence: (String) -> Unit = {},
    onLevelCompleted: () -> Unit = {},
) {
    var itemIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedIndices by rememberSaveable { mutableStateOf(setOf<Int>()) }
    var nudgeMessage by remember { mutableStateOf<String?>(null) }
    var isShowingSolution by rememberSaveable { mutableStateOf(false) }
    var showReward by rememberSaveable { mutableStateOf(false) }
    var levelFinished by rememberSaveable { mutableStateOf(false) }

    val curItem = items[itemIndex % items.size]
    val totalNouns = curItem.nounIndices.size
    val foundCount = selectedIndices.intersect(curItem.nounIndices).size
    val isComplete = foundCount == totalNouns

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (levelFinished) {
                Text(
                    text = "Level 1 Finished! 🎉",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = VioletPrimary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "⭐⭐⭐⭐⭐⭐",
                    fontSize = 32.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You found all the naming words in 6 sentences!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDark,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        itemIndex = 0
                        selectedIndices = emptySet()
                        nudgeMessage = null
                        isShowingSolution = false
                        levelFinished = false
                    },
                    modifier = Modifier.height(52.dp),
                ) {
                    Text(text = "↺ Play Level 1 Again", fontSize = 16.sp)
                }
            } else {
                // Header
                Text(
                    text = "Sentence Lab • Level 1: Naming Words (Nouns)",
                    style = MaterialTheme.typography.titleSmall,
                    color = VioletPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sentence ${itemIndex + 1} of ${items.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isComplete) "All $totalNouns naming words found! Great job!" else "Tap all $totalNouns naming words (nouns). ($foundCount of $totalNouns)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isComplete) VioletPrimary else TextDark,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Words FlowRow
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center,
                ) {
                    curItem.words.forEachIndexed { idx, word ->
                        val isNoun = curItem.nounIndices.contains(idx)
                        val isSelected = selectedIndices.contains(idx) || (isShowingSolution && isNoun)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected && isNoun) NounHighlightBg else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected && isNoun) BorderStroke(2.dp, NounHighlightBorder) else null,
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isNoun) {
                                        selectedIndices = selectedIndices + idx
                                        nudgeMessage = null
                                        if (selectedIndices.intersect(curItem.nounIndices).size == totalNouns) {
                                            showReward = true
                                        }
                                    } else {
                                        nudgeMessage = "Almost! \"${word.trimEnd('.', '!', '?', ',')}\" is not a naming word. Try again!"
                                    }
                                }
                                .semantics {
                                    contentDescription = "$word" + if (isSelected && isNoun) ", identified as noun" else ""
                                },
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = word,
                                    fontSize = 24.sp,
                                    fontWeight = if (isSelected && isNoun) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected && isNoun) NounHighlightText else TextDark,
                                )
                                if (isSelected && isNoun) {
                                    Text(
                                        text = "🏷️ noun",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NounHighlightBorder,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nudge message if incorrect tap
                if (nudgeMessage != null) {
                    Text(
                        text = nudgeMessage ?: "",
                        color = WarmAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = { onSpeakSentence(curItem.text) },
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text(text = "🔊 Hear it", fontSize = 15.sp)
                    }

                    if (!isComplete) {
                        OutlinedButton(
                            onClick = {
                                isShowingSolution = true
                                selectedIndices = selectedIndices + curItem.nounIndices
                                showReward = true
                            },
                            modifier = Modifier.height(48.dp),
                        ) {
                            Text(text = "👀 Show me", fontSize = 15.sp)
                        }
                    }

                    if (isComplete) {
                        Button(
                            onClick = {
                                if (itemIndex < items.size - 1) {
                                    itemIndex++
                                    selectedIndices = emptySet()
                                    nudgeMessage = null
                                    isShowingSolution = false
                                    showReward = false
                                } else {
                                    levelFinished = true
                                    onLevelCompleted()
                                }
                            },
                            modifier = Modifier.height(48.dp),
                        ) {
                            Text(
                                text = if (itemIndex < items.size - 1) "Next sentence ▸" else "Finish Level 1 🎉",
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showReward,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
        ) {
            Text(
                text = "⭐",
                fontSize = 64.sp,
                modifier = Modifier.semantics {
                    contentDescription = "Great job finding naming words!"
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

@Preview(name = "Sentence Lab Level 1", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewSentenceLabScreen() {
    EarlyReaderTheme {
        SentenceLabScreen()
    }
}
