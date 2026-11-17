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
import androidx.compose.runtime.key
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
    val nounIndices: Set<Int> get() = targetIndices("noun")
    val verbIndices: Set<Int> get() = targetIndices("verb")
    val adjectiveIndices: Set<Int> get() = targetIndices("adj")

    fun targetIndices(targetTag: String): Set<Int> =
        tags.mapIndexedNotNull { idx, tag -> if (tag == targetTag) idx else null }.toSet()
}

data class SentenceLabLevel(
    val id: String,
    val title: String,
    val shortName: String,
    val targetTag: String,
    val targetName: String,
    val badgeLabel: String,
    val prompt: String,
    val nudgeText: String,
    val highlightBg: Color,
    val highlightBorder: Color,
    val highlightText: Color,
    val items: List<SentenceLabItem>,
)

object SentenceLabData {
    val level1 = SentenceLabLevel(
        id = "nouns",
        title = "Level 1: Naming Words (Nouns)",
        shortName = "1. Nouns",
        targetTag = "noun",
        targetName = "naming word",
        badgeLabel = "🏷️ noun",
        prompt = "Tap all {n} naming words (nouns).",
        nudgeText = "Almost! Is that a word that names something? Try again!",
        highlightBg = Color(0xFFDBEAFE),
        highlightBorder = Color(0xFF2563EB),
        highlightText = Color(0xFF1E40AF),
        items = listOf(
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
        ),
    )

    val level2 = SentenceLabLevel(
        id = "verbs",
        title = "Level 2: Action Words (Verbs)",
        shortName = "2. Verbs",
        targetTag = "verb",
        targetName = "action word",
        badgeLabel = "⚡ verb",
        prompt = "Tap the action word (verb).",
        nudgeText = "Almost! Does that word tell what someone or something is doing? Try again!",
        highlightBg = Color(0xFFFFEDD5),
        highlightBorder = Color(0xFFC2410C),
        highlightText = Color(0xFF7C2D12),
        items = listOf(
            SentenceLabItem(
                text = "The children laughed at the funny clown.",
                tags = listOf("det", "noun", "verb", "prep", "det", "adj", "noun"),
            ),
            SentenceLabItem(
                text = "Mom baked bread for dinner.",
                tags = listOf("noun", "verb", "noun", "prep", "noun"),
            ),
            SentenceLabItem(
                text = "The kitten climbed up the stairs.",
                tags = listOf("det", "noun", "verb", "prep", "det", "noun"),
            ),
            SentenceLabItem(
                text = "Our class painted a picture.",
                tags = listOf("det", "noun", "verb", "det", "noun"),
            ),
            SentenceLabItem(
                text = "The bus stopped at the school.",
                tags = listOf("det", "noun", "verb", "prep", "det", "noun"),
            ),
            SentenceLabItem(
                text = "A boy splashed in the puddle.",
                tags = listOf("det", "noun", "verb", "prep", "det", "noun"),
            ),
        ),
    )

    val level3 = SentenceLabLevel(
        id = "adjectives",
        title = "Level 3: Describing Words (Adjectives)",
        shortName = "3. Adjectives",
        targetTag = "adj",
        targetName = "describing word",
        badgeLabel = "🎨 adj",
        prompt = "Tap all {n} describing words (adjectives).",
        nudgeText = "Almost! Does that word tell us what something is like? Try again!",
        highlightBg = Color(0xFFDCFCE7),
        highlightBorder = Color(0xFF15803D),
        highlightText = Color(0xFF14532D),
        items = listOf(
            SentenceLabItem(
                text = "The soft kitten drank warm milk.",
                tags = listOf("det", "adj", "noun", "verb", "adj", "noun"),
            ),
            SentenceLabItem(
                text = "A yellow bus drove down the long road.",
                tags = listOf("det", "adj", "noun", "verb", "prep", "det", "adj", "noun"),
            ),
            SentenceLabItem(
                text = "The tired boy slept in a cozy bed.",
                tags = listOf("det", "adj", "noun", "verb", "prep", "det", "adj", "noun"),
            ),
            SentenceLabItem(
                text = "Grandma wore a long blue scarf.",
                tags = listOf("noun", "verb", "det", "adj", "adj", "noun"),
            ),
            SentenceLabItem(
                text = "The cold wind shook the tiny leaves.",
                tags = listOf("det", "adj", "noun", "verb", "det", "adj", "noun"),
            ),
            SentenceLabItem(
                text = "We ate crunchy apples on a sunny day.",
                tags = listOf("pron", "verb", "adj", "noun", "prep", "det", "adj", "noun"),
            ),
        ),
    )

    val allLevels = listOf(level1, level2, level3)
    val level1Items: List<SentenceLabItem> get() = level1.items
    val level2Items: List<SentenceLabItem> get() = level2.items
    val level3Items: List<SentenceLabItem> get() = level3.items
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceLabScreen(
    levels: List<SentenceLabLevel> = SentenceLabData.allLevels,
    initialLevelIndex: Int = 0,
    onSpeakSentence: (String) -> Unit = {},
    onLevelSelected: (Int) -> Unit = {},
    onLevelCompleted: () -> Unit = {},
) {
    var levelIndex by rememberSaveable { mutableIntStateOf(initialLevelIndex.coerceIn(0, levels.size - 1)) }
    var itemIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedIndices by rememberSaveable { mutableStateOf(setOf<Int>()) }
    var nudgeMessage by remember { mutableStateOf<String?>(null) }
    var isShowingSolution by rememberSaveable { mutableStateOf(false) }
    var showReward by rememberSaveable { mutableStateOf(false) }
    var levelFinished by rememberSaveable { mutableStateOf(false) }

    val currentLevel = levels[levelIndex % levels.size]
    val items = currentLevel.items
    val curItem = items[itemIndex % items.size]
    val targetIndices = curItem.targetIndices(currentLevel.targetTag)
    val totalTargets = targetIndices.size
    val foundCount = selectedIndices.intersect(targetIndices).size
    val isComplete = foundCount == totalTargets

    val switchLevel: (Int) -> Unit = { newIdx ->
        levelIndex = newIdx
        itemIndex = 0
        selectedIndices = emptySet()
        nudgeMessage = null
        isShowingSolution = false
        levelFinished = false
        onLevelSelected(newIdx)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Level Selector Pills
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                levels.forEachIndexed { idx, lvl ->
                    val isSelected = idx == levelIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) VioletPrimary else Color.Transparent)
                            .clickable { switchLevel(idx) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .semantics {
                                contentDescription = "${lvl.title}" + if (isSelected) ", selected" else ""
                            },
                    ) {
                        Text(
                            text = lvl.shortName,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (levelFinished) {
                Text(
                    text = "${currentLevel.title} Finished! 🎉",
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
                    text = "You found all the ${currentLevel.targetName}s in 6 sentences!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDark,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            itemIndex = 0
                            selectedIndices = emptySet()
                            nudgeMessage = null
                            isShowingSolution = false
                            levelFinished = false
                        },
                        modifier = Modifier.height(52.dp),
                    ) {
                        Text(text = "↺ Play Again", fontSize = 16.sp)
                    }

                    if (levelIndex < levels.size - 1) {
                        Button(
                            onClick = { switchLevel(levelIndex + 1) },
                            modifier = Modifier.height(52.dp),
                        ) {
                            Text(text = "Next Level ▸", fontSize = 16.sp)
                        }
                    }
                }
            } else {
                Text(
                    text = "Sentence ${itemIndex + 1} of ${items.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                val promptText = if (isComplete) {
                    "All $totalTargets ${currentLevel.targetName}s found! Great job!"
                } else {
                    val basePrompt = currentLevel.prompt.replace("{n}", totalTargets.toString())
                    if (totalTargets > 1) "$basePrompt ($foundCount of $totalTargets)" else basePrompt
                }
                Text(
                    text = promptText,
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
                        val isTarget = targetIndices.contains(idx)
                        val isSelected = selectedIndices.contains(idx) || (isShowingSolution && isTarget)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected && isTarget) currentLevel.highlightBg else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected && isTarget) BorderStroke(2.dp, currentLevel.highlightBorder) else null,
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isTarget) {
                                        selectedIndices = selectedIndices + idx
                                        nudgeMessage = null
                                        if (selectedIndices.intersect(targetIndices).size == totalTargets) {
                                            showReward = true
                                        }
                                    } else {
                                        nudgeMessage = currentLevel.nudgeText
                                    }
                                }
                                .semantics {
                                    contentDescription = if (isSelected && isTarget) {
                                        "$word, ${currentLevel.targetName}, found"
                                    } else {
                                        "$word, tap to choose"
                                    }
                                },
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = word,
                                    fontSize = 24.sp,
                                    fontWeight = if (isSelected && isTarget) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected && isTarget) currentLevel.highlightText else TextDark,
                                )
                                if (isSelected && isTarget) {
                                    Text(
                                        text = currentLevel.badgeLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentLevel.highlightBorder,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feedback/nudge banner with TalkBack live region
                if (nudgeMessage != null) {
                    Text(
                        text = nudgeMessage ?: "",
                        color = WarmAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite },
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
                                selectedIndices = selectedIndices + targetIndices
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
                                text = if (itemIndex < items.size - 1) "Next sentence ▸" else "Finish Level 🎉",
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
                    contentDescription = "Great job finding ${currentLevel.targetName}s!"
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

@Preview(name = "Sentence Lab Level 1 (Nouns)", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewSentenceLabLevel1() {
    EarlyReaderTheme {
        SentenceLabScreen(initialLevelIndex = 0)
    }
}

@Preview(name = "Sentence Lab Level 2 (Verbs)", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewSentenceLabLevel2() {
    EarlyReaderTheme {
        SentenceLabScreen(initialLevelIndex = 1)
    }
}

@Preview(name = "Sentence Lab Level 3 (Adjectives)", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewSentenceLabLevel3() {
    EarlyReaderTheme {
        SentenceLabScreen(initialLevelIndex = 2)
    }
}
