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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.earlyreader.ui.theme.EarlyReaderTheme
import com.example.earlyreader.ui.theme.TextDark
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent
import kotlinx.coroutines.delay

enum class SentenceLabType {
    TAP_WORDS,
    JUDGE_SENTENCE,
}

data class SentenceCheckItem(
    val text: String,
    val isSentence: Boolean,
    val why: String,
    val hint: String,
)

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
    val type: SentenceLabType = SentenceLabType.TAP_WORDS,
    val targetTag: String = "",
    val targetName: String = "",
    val badgeLabel: String = "",
    val prompt: String = "",
    val nudgeText: String = "",
    val highlightBg: Color = Color.Transparent,
    val highlightBorder: Color = Color.Transparent,
    val highlightText: Color = Color.Black,
    val items: List<SentenceLabItem> = emptyList(),
    val checkItems: List<SentenceCheckItem> = emptyList(),
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

    val level4 = SentenceLabLevel(
        id = "sentence-check",
        title = "Level 4: Is it a Sentence?",
        shortName = "4. Sentence?",
        type = SentenceLabType.JUDGE_SENTENCE,
        targetName = "complete thought",
        prompt = "Read it aloud. Is it a whole sentence?",
        checkItems = listOf(
            SentenceCheckItem(
                text = "The big dog.",
                isSentence = false,
                why = "“The big dog” tells who it is about, but not what happens. Add a word like “ran” and it becomes a sentence.",
                hint = "Who is it about? A big dog. But what happens? It does not say yet.",
            ),
            SentenceCheckItem(
                text = "We played in the park.",
                isSentence = true,
                why = "“We” is who it is about and “played” is what happens. That is a whole thought.",
                hint = "Who is it about? We. What happens? We played. Does the thought feel finished?",
            ),
            SentenceCheckItem(
                text = "The baby sleeps.",
                isSentence = true,
                why = "“The baby” is who it is about and “sleeps” is what happens. That is a whole thought.",
                hint = "Who is it about? The baby. What happens? The baby sleeps.",
            ),
            SentenceCheckItem(
                text = "Ran down the hill.",
                isSentence = false,
                why = "It tells what happened, but not who ran. Add “We” at the start and it becomes a sentence.",
                hint = "What happens? Someone ran down the hill. But who or what is it about?",
            ),
            SentenceCheckItem(
                text = "In the park.",
                isSentence = false,
                why = "It tells where, but not who or what it is about, and not what happens.",
                hint = "Who or what is it about? It does not say. What happens? It does not say either.",
            ),
            SentenceCheckItem(
                text = "My sister reads a book.",
                isSentence = true,
                why = "“My sister” is who it is about and “reads a book” is what she does. That is a whole thought.",
                hint = "Who is it about? My sister. What does she do? She reads a book.",
            ),
        ),
    )

    val allLevels = listOf(level1, level2, level3, level4)
    val level1Items: List<SentenceLabItem> get() = level1.items
    val level2Items: List<SentenceLabItem> get() = level2.items
    val level3Items: List<SentenceLabItem> get() = level3.items
    val level4Items: List<SentenceCheckItem> get() = level4.checkItems
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

    var judgeAnswer by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var judgeFeedback by rememberSaveable { mutableStateOf<String?>(null) }
    var judgeCorrect by rememberSaveable { mutableStateOf<Boolean?>(null) }

    val currentLevel = levels[levelIndex % levels.size]
    val isJudgeLevel = currentLevel.type == SentenceLabType.JUDGE_SENTENCE
    val totalItems = if (isJudgeLevel) currentLevel.checkItems.size else currentLevel.items.size

    val curTapItem = if (!isJudgeLevel && currentLevel.items.isNotEmpty()) {
        currentLevel.items[itemIndex % currentLevel.items.size]
    } else null
    val curCheckItem = if (isJudgeLevel && currentLevel.checkItems.isNotEmpty()) {
        currentLevel.checkItems[itemIndex % currentLevel.checkItems.size]
    } else null

    val targetIndices = curTapItem?.targetIndices(currentLevel.targetTag) ?: emptySet()
    val totalTargets = targetIndices.size
    val foundCount = selectedIndices.intersect(targetIndices).size
    val isComplete = if (isJudgeLevel) judgeCorrect == true else foundCount == totalTargets

    val switchLevel: (Int) -> Unit = { newIdx ->
        levelIndex = newIdx
        itemIndex = 0
        selectedIndices = emptySet()
        nudgeMessage = null
        isShowingSolution = false
        judgeAnswer = null
        judgeFeedback = null
        judgeCorrect = null
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
                    text = if (isJudgeLevel) {
                        "You checked all 6 complete thoughts vs fragments!"
                    } else {
                        "You found all the ${currentLevel.targetName}s in 6 sentences!"
                    },
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
                            judgeAnswer = null
                            judgeFeedback = null
                            judgeCorrect = null
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
                    text = "Sentence ${itemIndex + 1} of $totalItems",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isJudgeLevel && curCheckItem != null) {
                    // --- JUDGE SENTENCE UI ---
                    Text(
                        text = currentLevel.prompt,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VioletPrimary,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quotation card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "“${curCheckItem.text}”",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Two choice buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        val greenColor = Color(0xFF15803D)
                        val redColor = Color(0xFFC2410C)

                        Button(
                            onClick = {
                                val isCorrect = curCheckItem.isSentence
                                judgeAnswer = true
                                judgeCorrect = isCorrect
                                judgeFeedback = if (isCorrect) curCheckItem.why else curCheckItem.hint
                                if (isCorrect) showReward = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (judgeAnswer == true) {
                                    if (judgeCorrect == true) greenColor else redColor
                                } else VioletPrimary,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .semantics {
                                    contentDescription = "Complete sentence"
                                },
                        ) {
                            Text(
                                text = "✅ Complete sentence",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                        }

                        Button(
                            onClick = {
                                val isCorrect = !curCheckItem.isSentence
                                judgeAnswer = false
                                judgeCorrect = isCorrect
                                judgeFeedback = if (isCorrect) curCheckItem.why else curCheckItem.hint
                                if (isCorrect) showReward = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (judgeAnswer == false) {
                                    if (judgeCorrect == true) greenColor else redColor
                                } else VioletPrimary,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .semantics {
                                    contentDescription = "Only a piece"
                                },
                        ) {
                            Text(
                                text = "🧩 Only a piece",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Feedback explanation or hint
                    if (judgeFeedback != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (judgeCorrect == true) Color(0xFFDCFCE7) else Color(0xFFFFF7ED),
                            border = BorderStroke(1.dp, if (judgeCorrect == true) Color(0xFF15803D) else Color(0xFFC2410C)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .semantics { liveRegion = LiveRegionMode.Polite },
                        ) {
                            Text(
                                text = judgeFeedback ?: "",
                                color = if (judgeCorrect == true) Color(0xFF14532D) else Color(0xFF7C2D12),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Controls for Judge Level
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { onSpeakSentence(curCheckItem.text) },
                            modifier = Modifier.height(48.dp),
                        ) {
                            Text(text = "🔊 Hear it", fontSize = 15.sp)
                        }

                        if (judgeCorrect == true) {
                            Button(
                                onClick = {
                                    if (itemIndex < totalItems - 1) {
                                        itemIndex++
                                        judgeAnswer = null
                                        judgeFeedback = null
                                        judgeCorrect = null
                                    } else {
                                        levelFinished = true
                                        onLevelCompleted()
                                    }
                                },
                                modifier = Modifier.height(48.dp),
                            ) {
                                Text(
                                    text = if (itemIndex < totalItems - 1) "Next sentence ▸" else "Finish Level 🎉",
                                    fontSize = 15.sp,
                                )
                            }
                        }
                    }
                } else if (curTapItem != null) {
                    // --- TAP WORDS UI (Levels 1-3) ---
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
                        curTapItem.words.forEachIndexed { idx, word ->
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

                    // Controls for Tap Level
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { onSpeakSentence(curTapItem.text) },
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
                                    if (itemIndex < totalItems - 1) {
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
                                    text = if (itemIndex < totalItems - 1) "Next sentence ▸" else "Finish Level 🎉",
                                    fontSize = 15.sp,
                                )
                            }
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

@Preview(name = "Sentence Lab Level 4 (Sentence Check)", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewSentenceLabLevel4() {
    EarlyReaderTheme {
        SentenceLabScreen(initialLevelIndex = 3)
    }
}
