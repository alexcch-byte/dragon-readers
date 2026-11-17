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
    BUILD_SENTENCE,
    SORT_ROLES,
}

data class SentenceCheckItem(
    val text: String,
    val isSentence: Boolean,
    val why: String,
    val hint: String,
)

data class SentenceBuildItem(
    val kind: String, // "telling", "asking", "exciting"
    val text: String,
) {
    val endMark: String = text.takeLast(1)
    val rawWords: List<String> = text.dropLast(1).split(" ")
    val scrambledWords: List<String> = rawWords.shuffled(java.util.Random(text.hashCode().toLong()))
        .let { if (it == rawWords && it.size > 1) it.reversed() else it }
}

data class SentenceRoleChunk(
    val text: String,
    val role: String, // "subject", "verb", "object"
)

data class SentenceRoleItem(
    val chunks: List<SentenceRoleChunk>,
) {
    val fullSentence: String = chunks.joinToString(" ") { it.text }
}

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
    val buildItems: List<SentenceBuildItem> = emptyList(),
    val roleItems: List<SentenceRoleItem> = emptyList(),
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

    val level5 = SentenceLabLevel(
        id = "build",
        title = "Level 5: Build a Sentence",
        shortName = "5. Build",
        type = SentenceLabType.BUILD_SENTENCE,
        targetName = "sentence",
        prompt = "Tap the words in order, then add the ending mark.",
        buildItems = listOf(
            SentenceBuildItem(kind = "telling", text = "The cat is very soft."),
            SentenceBuildItem(kind = "asking", text = "Do you like soup?"),
            SentenceBuildItem(kind = "telling", text = "We planted seeds in the garden."),
            SentenceBuildItem(kind = "asking", text = "Where did the dog go?"),
            SentenceBuildItem(kind = "exciting", text = "I see a big rainbow!"),
            SentenceBuildItem(kind = "telling", text = "The bus stops at our school."),
        ),
    )

    val level6 = SentenceLabLevel(
        id = "roles",
        title = "Level 6: Who and What (Roles)",
        shortName = "6. Roles",
        type = SentenceLabType.SORT_ROLES,
        targetName = "sentence parts",
        prompt = "Tap a part of the sentence, then tap the box where it belongs.",
        roleItems = listOf(
            SentenceRoleItem(
                listOf(
                    SentenceRoleChunk("The baby", "subject"),
                    SentenceRoleChunk("ate", "verb"),
                    SentenceRoleChunk("a banana.", "object"),
                ),
            ),
            SentenceRoleItem(
                listOf(
                    SentenceRoleChunk("Dad", "subject"),
                    SentenceRoleChunk("washed", "verb"),
                    SentenceRoleChunk("the car.", "object"),
                ),
            ),
            SentenceRoleItem(
                listOf(
                    SentenceRoleChunk("Our teacher", "subject"),
                    SentenceRoleChunk("told", "verb"),
                    SentenceRoleChunk("a story.", "object"),
                ),
            ),
            SentenceRoleItem(
                listOf(
                    SentenceRoleChunk("The children", "subject"),
                    SentenceRoleChunk("planted", "verb"),
                    SentenceRoleChunk("some seeds.", "object"),
                ),
            ),
            SentenceRoleItem(
                listOf(
                    SentenceRoleChunk("The wind", "subject"),
                    SentenceRoleChunk("shook", "verb"),
                    SentenceRoleChunk("the trees.", "object"),
                ),
            ),
            SentenceRoleItem(
                listOf(
                    SentenceRoleChunk("A girl", "subject"),
                    SentenceRoleChunk("drew", "verb"),
                    SentenceRoleChunk("a rainbow.", "object"),
                ),
            ),
        ),
    )

    val allLevels = listOf(level1, level2, level3, level4, level5, level6)
    val level1Items: List<SentenceLabItem> get() = level1.items
    val level2Items: List<SentenceLabItem> get() = level2.items
    val level3Items: List<SentenceLabItem> get() = level3.items
    val level4Items: List<SentenceCheckItem> get() = level4.checkItems
    val level5Items: List<SentenceBuildItem> get() = level5.buildItems
    val level6Items: List<SentenceRoleItem> get() = level6.roleItems
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceLabScreen(
    levels: List<SentenceLabLevel> = SentenceLabData.allLevels,
    initialLevelIndex: Int = 0,
    completedLevels: Set<String> = emptySet(),
    onSpeakSentence: (String) -> Unit = {},
    onLevelSelected: (Int) -> Unit = {},
    onLevelCompleted: (String) -> Unit = {},
) {
    var levelIndex by rememberSaveable { mutableIntStateOf(initialLevelIndex.coerceIn(0, levels.size - 1)) }
    var itemIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedIndices by rememberSaveable { mutableStateOf(setOf<Int>()) }
    var nudgeMessage by remember { mutableStateOf<String?>(null) }
    var isShowingSolution by rememberSaveable { mutableStateOf(false) }
    var showReward by rememberSaveable { mutableStateOf(false) }
    var levelFinished by rememberSaveable { mutableStateOf(false) }
    var localCompletedLevels by rememberSaveable { mutableStateOf(completedLevels) }

    var judgeAnswer by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var judgeFeedback by rememberSaveable { mutableStateOf<String?>(null) }
    var judgeCorrect by rememberSaveable { mutableStateOf<Boolean?>(null) }

    var placedTokens by rememberSaveable { mutableStateOf(listOf<String>()) }
    var buildFeedback by rememberSaveable { mutableStateOf<String?>(null) }
    var buildSuccess by rememberSaveable { mutableStateOf(false) }

    var selectedRoleChunkIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var assignedRoles by rememberSaveable { mutableStateOf(mapOf<String, Int>()) }
    var roleFeedback by rememberSaveable { mutableStateOf<String?>(null) }
    var roleSuccess by rememberSaveable { mutableStateOf(false) }

    val currentLevel = levels[levelIndex % levels.size]
    val isJudgeLevel = currentLevel.type == SentenceLabType.JUDGE_SENTENCE
    val isBuildLevel = currentLevel.type == SentenceLabType.BUILD_SENTENCE
    val isRoleLevel = currentLevel.type == SentenceLabType.SORT_ROLES
    val totalItems = when (currentLevel.type) {
        SentenceLabType.JUDGE_SENTENCE -> currentLevel.checkItems.size
        SentenceLabType.BUILD_SENTENCE -> currentLevel.buildItems.size
        SentenceLabType.SORT_ROLES -> currentLevel.roleItems.size
        else -> currentLevel.items.size
    }

    val curTapItem = if (currentLevel.type == SentenceLabType.TAP_WORDS && currentLevel.items.isNotEmpty()) {
        currentLevel.items[itemIndex % currentLevel.items.size]
    } else null
    val curCheckItem = if (isJudgeLevel && currentLevel.checkItems.isNotEmpty()) {
        currentLevel.checkItems[itemIndex % currentLevel.checkItems.size]
    } else null
    val curBuildItem = if (isBuildLevel && currentLevel.buildItems.isNotEmpty()) {
        currentLevel.buildItems[itemIndex % currentLevel.buildItems.size]
    } else null
    val curRoleItem = if (isRoleLevel && currentLevel.roleItems.isNotEmpty()) {
        currentLevel.roleItems[itemIndex % currentLevel.roleItems.size]
    } else null

    val targetIndices = curTapItem?.targetIndices(currentLevel.targetTag) ?: emptySet()
    val totalTargets = targetIndices.size
    val foundCount = selectedIndices.intersect(targetIndices).size
    val isComplete = when (currentLevel.type) {
        SentenceLabType.JUDGE_SENTENCE -> judgeCorrect == true
        SentenceLabType.BUILD_SENTENCE -> buildSuccess
        SentenceLabType.SORT_ROLES -> roleSuccess
        else -> foundCount == totalTargets
    }

    val switchLevel: (Int) -> Unit = { newIdx ->
        levelIndex = newIdx
        itemIndex = 0
        selectedIndices = emptySet()
        nudgeMessage = null
        isShowingSolution = false
        judgeAnswer = null
        judgeFeedback = null
        judgeCorrect = null
        placedTokens = emptyList()
        buildFeedback = null
        buildSuccess = false
        selectedRoleChunkIndex = null
        assignedRoles = emptyMap()
        roleFeedback = null
        roleSuccess = false
        levelFinished = false
        onLevelSelected(newIdx)
    }

    val resetCurrentLevel: () -> Unit = {
        itemIndex = 0
        selectedIndices = emptySet()
        nudgeMessage = null
        isShowingSolution = false
        judgeAnswer = null
        judgeFeedback = null
        judgeCorrect = null
        placedTokens = emptyList()
        buildFeedback = null
        buildSuccess = false
        selectedRoleChunkIndex = null
        assignedRoles = emptyMap()
        roleFeedback = null
        roleSuccess = false
        levelFinished = false
    }

    val checkBuildSentence: () -> Unit = {
        if (curBuildItem != null) {
            val marks = listOf(".", "?", "!")
            val placedMarks = placedTokens.filter { it in marks }
            val placedWords = placedTokens.filter { it !in marks }
            val targetWords = curBuildItem.rawWords

            if (placedTokens.isEmpty()) {
                buildFeedback = "Tap some words first to build your sentence."
                buildSuccess = false
            } else if (placedMarks.size > 1) {
                buildFeedback = "Almost! A sentence needs just one ending mark."
                buildSuccess = false
            } else if (placedWords.size < targetWords.size) {
                buildFeedback = "Nice start! You used ${placedWords.size} of ${targetWords.size} words. Use them all, then finish with an ending mark."
                buildSuccess = false
            } else if (placedTokens.first() != targetWords.first()) {
                buildFeedback = "Almost! Which word starts with a capital letter? A sentence starts with one."
                buildSuccess = false
            } else if (placedMarks.isEmpty()) {
                buildFeedback = "Almost! Every sentence ends with a mark: a period, a question mark, or an exclamation mark."
                buildSuccess = false
            } else if (placedTokens.last() !in marks) {
                buildFeedback = "Almost! The ending mark goes at the very end."
                buildSuccess = false
            } else if (placedTokens.last() != curBuildItem.endMark) {
                val markHint = when (curBuildItem.kind) {
                    "asking" -> "This is an asking sentence (question), so it needs a question mark (?)."
                    "exciting" -> "This is an exciting sentence with big feeling, so it needs an exclamation mark (!)."
                    else -> "This is a telling sentence, so it needs a period (.)."
                }
                buildFeedback = "Almost! $markHint"
                buildSuccess = false
            } else if (placedWords != targetWords) {
                buildFeedback = "Nearly! Read your sentence aloud: does it sound right? Tap a word to take it out and try again."
                buildSuccess = false
            } else {
                buildFeedback = "Yes! Wonderful sentence building! 🎉"
                buildSuccess = true
                showReward = true
            }
        }
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
                    val isCompleted = localCompletedLevels.contains(lvl.id)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) VioletPrimary else Color.Transparent)
                            .clickable { switchLevel(idx) }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .semantics {
                                contentDescription = "${lvl.title}" +
                                    if (isSelected) ", selected" else "" +
                                    if (isCompleted) ", completed" else ""
                            },
                    ) {
                        Text(
                            text = lvl.shortName + if (isCompleted) "⭐" else "",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (levelFinished) {
                val allLevelsFinished = levels.all { localCompletedLevels.contains(it.id) }

                if (allLevelsFinished) {
                    // --- ALL 6 LEVELS COMPLETE MASTER CELEBRATION ---
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(2.dp, Color(0xFFD97706)),
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(text = "🏆", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sentence Master!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐",
                                fontSize = 20.sp,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "All 6 Levels Finished! You are a sentence master!\nYou explored naming words, action words, describing words, complete thoughts, sentence building, and who/what roles!",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF78350F),
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = {
                                    levelIndex = 0
                                    resetCurrentLevel()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            ) {
                                Text(text = "↺ Play Sentence Lab Again", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Standard level complete screen
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
                        text = when {
                            isJudgeLevel -> "You checked all 6 complete thoughts vs fragments!"
                            isBuildLevel -> "You built all 6 sentences with capital letters and ending marks!"
                            isRoleLevel -> "You sorted all 6 sentences into who, action, and what!"
                            else -> "You found all the ${currentLevel.targetName}s in 6 sentences!"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextDark,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { resetCurrentLevel() },
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
                                        localCompletedLevels = localCompletedLevels + currentLevel.id
                                        onLevelCompleted(currentLevel.id)
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
                } else if (isBuildLevel && curBuildItem != null) {
                    // --- BUILD SENTENCE UI (Level 5) ---
                    val buildPrompt = when (curBuildItem.kind) {
                        "asking" -> "Build an asking sentence (a question). Tap the words in order, then add the ending mark."
                        "exciting" -> "Build an exciting sentence (it shows a big feeling). Tap the words in order, then add the ending mark."
                        else -> "Build a telling sentence. Tap the words in order, then add the ending mark."
                    }
                    Text(
                        text = buildPrompt,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VioletPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sentence Tray
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(2.dp, if (buildSuccess) Color(0xFF15803D) else VioletPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (placedTokens.isEmpty()) {
                                Text(
                                    text = "Tap words below to place them here...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 15.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    placedTokens.forEachIndexed { idx, token ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (token in listOf(".", "?", "!")) Color(0xFFE0E7FF) else Color.White,
                                            border = BorderStroke(1.5.dp, VioletPrimary),
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable(enabled = !buildSuccess) {
                                                    placedTokens = placedTokens.toMutableList().also { it.removeAt(idx) }
                                                    buildFeedback = null
                                                }
                                                .semantics {
                                                    contentDescription = "$token, placed. Tap to remove"
                                                },
                                        ) {
                                            Text(
                                                text = token,
                                                fontSize = if (token in listOf(".", "?", "!")) 24.sp else 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VioletPrimary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Word Bank
                    Text(
                        text = "Word Bank",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        curBuildItem.scrambledWords.forEach { word ->
                            val occurrencesInRaw = curBuildItem.rawWords.count { it == word }
                            val occurrencesPlaced = placedTokens.count { it == word }
                            val isUsed = occurrencesPlaced >= occurrencesInRaw

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isUsed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (!isUsed) BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.3f)) else null,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isUsed && !buildSuccess) {
                                        placedTokens = placedTokens + word
                                        buildFeedback = null
                                    }
                                    .semantics {
                                        contentDescription = if (isUsed) "$word, used" else "$word, tap to add"
                                    },
                            ) {
                                Text(
                                    text = word,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else TextDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Punctuation marks
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        listOf(".", "?", "!").forEach { mark ->
                            val isUsed = placedTokens.contains(mark)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isUsed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color(0xFFEDE9FE),
                                border = BorderStroke(1.5.dp, if (isUsed) Color.Transparent else VioletPrimary),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = !isUsed && !buildSuccess) {
                                        placedTokens = placedTokens + mark
                                        buildFeedback = null
                                    }
                                    .semantics {
                                        contentDescription = "Ending mark $mark"
                                    },
                            ) {
                                Text(
                                    text = mark,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else VioletPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feedback banner
                    if (buildFeedback != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (buildSuccess) Color(0xFFDCFCE7) else Color(0xFFFFF7ED),
                            border = BorderStroke(1.dp, if (buildSuccess) Color(0xFF15803D) else Color(0xFFC2410C)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .semantics { liveRegion = LiveRegionMode.Polite },
                        ) {
                            Text(
                                text = buildFeedback ?: "",
                                color = if (buildSuccess) Color(0xFF14532D) else Color(0xFF7C2D12),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Controls for Build Level
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = {
                                val textToSpeak = if (placedTokens.isNotEmpty()) placedTokens.joinToString(" ") else curBuildItem.text
                                onSpeakSentence(textToSpeak)
                            },
                            modifier = Modifier.height(46.dp),
                        ) {
                            Text(text = "🔊 Hear it", fontSize = 14.sp)
                        }

                        if (!buildSuccess) {
                            OutlinedButton(
                                onClick = {
                                    placedTokens = emptyList()
                                    buildFeedback = null
                                },
                                modifier = Modifier.height(46.dp),
                            ) {
                                Text(text = "↺ Clear", fontSize = 14.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    placedTokens = curBuildItem.rawWords + curBuildItem.endMark
                                    buildFeedback = "Here is the sentence: ${curBuildItem.text}"
                                    buildSuccess = true
                                    showReward = true
                                },
                                modifier = Modifier.height(46.dp),
                            ) {
                                Text(text = "👀 Show me", fontSize = 14.sp)
                            }

                            Button(
                                onClick = { checkBuildSentence() },
                                modifier = Modifier.height(46.dp),
                            ) {
                                Text(text = "✔ Check", fontSize = 14.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (itemIndex < totalItems - 1) {
                                        itemIndex++
                                        placedTokens = emptyList()
                                        buildFeedback = null
                                        buildSuccess = false
                                    } else {
                                        levelFinished = true
                                        localCompletedLevels = localCompletedLevels + currentLevel.id
                                        onLevelCompleted(currentLevel.id)
                                    }
                                },
                                modifier = Modifier.height(46.dp),
                            ) {
                                Text(
                                    text = if (itemIndex < totalItems - 1) "Next sentence ▸" else "Finish Level 🎉",
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                } else if (isRoleLevel && curRoleItem != null) {
                    // --- SORT ROLES UI (Level 6: Who and What) ---
                    Text(
                        text = currentLevel.prompt,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VioletPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sentence Chunks at top
                    Text(
                        text = "Sentence Parts (tap to select):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                    ) {
                        curRoleItem.chunks.forEachIndexed { cIdx, chunk ->
                            val isAssigned = assignedRoles.values.contains(cIdx)
                            val isSelected = selectedRoleChunkIndex == cIdx

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAssigned) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                } else if (isSelected) {
                                    VioletPrimary.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = if (isSelected) {
                                    BorderStroke(2.dp, VioletPrimary)
                                } else if (!isAssigned) {
                                    BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.3f))
                                } else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !roleSuccess) {
                                        if (isAssigned) {
                                            assignedRoles = assignedRoles.filterValues { it != cIdx }
                                            roleFeedback = null
                                        } else {
                                            selectedRoleChunkIndex = if (selectedRoleChunkIndex == cIdx) null else cIdx
                                            roleFeedback = null
                                        }
                                    }
                                    .semantics {
                                        contentDescription = "${chunk.text}, " + if (isAssigned) "already placed" else if (isSelected) "selected" else "tap to select"
                                    },
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = chunk.text + if (isAssigned) " ✔" else "",
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected || isAssigned) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isAssigned) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else TextDark,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Three Target Role Boxes
                    val roleBoxes = listOf(
                        Triple("subject", "👤 Who or what (Subject)" to "Who or what is it about?", Triple(Color(0xFFF3E8FF), Color(0xFF7E22CE), Color(0xFF581C87))),
                        Triple("verb", "⚡ Action (Verb)" to "What did they do?", Triple(Color(0xFFFFEDD5), Color(0xFFC2410C), Color(0xFF7C2D12))),
                        Triple("object", "🎯 What? (Object)" to "What did they do it to?", Triple(Color(0xFFCCFBF1), Color(0xFF0F766E), Color(0xFF115E59))),
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                    ) {
                        roleBoxes.forEach { (roleKey, labels, colors) ->
                            val (title, question) = labels
                            val (bgColor, borderColor, textColor) = colors
                            val assignedChunkIdx = assignedRoles[roleKey]
                            val isFilled = assignedChunkIdx != null

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFilled) bgColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(2.dp, if (isFilled) borderColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !roleSuccess) {
                                        if (isFilled) {
                                            assignedRoles = assignedRoles - roleKey
                                            roleFeedback = null
                                        } else if (selectedRoleChunkIndex != null) {
                                            val candidateChunk = curRoleItem.chunks[selectedRoleChunkIndex!!]
                                            if (candidateChunk.role == roleKey) {
                                                val newMap = assignedRoles + (roleKey to selectedRoleChunkIndex!!)
                                                assignedRoles = newMap
                                                selectedRoleChunkIndex = null
                                                roleFeedback = null
                                                if (newMap.size == 3) {
                                                    roleSuccess = true
                                                    showReward = true
                                                }
                                            } else {
                                                val nudge = when (roleKey) {
                                                    "subject" -> "Almost! The subject tells who or what the sentence is about."
                                                    "verb" -> "Almost! The verb is the action (what they did)."
                                                    else -> "Almost! The object is what they did it to."
                                                }
                                                roleFeedback = nudge
                                            }
                                        } else {
                                            roleFeedback = "Tap a sentence part above first, then tap this box!"
                                        }
                                    }
                                    .semantics {
                                        contentDescription = if (isFilled) {
                                            "$title: ${curRoleItem.chunks[assignedChunkIdx!!].text}. Tap to remove"
                                        } else {
                                            "$title box, empty. $question"
                                        }
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFilled) textColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = if (isFilled) {
                                                curRoleItem.chunks[assignedChunkIdx!!].text
                                            } else {
                                                question
                                            },
                                            fontSize = if (isFilled) 18.sp else 12.sp,
                                            fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isFilled) textColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        )
                                    }
                                    if (isFilled) {
                                        Text(
                                            text = "✕",
                                            fontSize = 16.sp,
                                            color = textColor,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Feedback banner
                    if (roleFeedback != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF7ED),
                            border = BorderStroke(1.dp, Color(0xFFC2410C)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .semantics { liveRegion = LiveRegionMode.Polite },
                        ) {
                            Text(
                                text = roleFeedback ?: "",
                                color = Color(0xFF7C2D12),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Controls for Roles Level
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { onSpeakSentence(curRoleItem.fullSentence) },
                            modifier = Modifier.height(46.dp),
                        ) {
                            Text(text = "🔊 Hear it", fontSize = 14.sp)
                        }

                        if (!roleSuccess) {
                            OutlinedButton(
                                onClick = {
                                    assignedRoles = curRoleItem.chunks.mapIndexed { idx, c -> c.role to idx }.toMap()
                                    roleSuccess = true
                                    showReward = true
                                    roleFeedback = null
                                },
                                modifier = Modifier.height(46.dp),
                            ) {
                                Text(text = "👀 Show me", fontSize = 14.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (itemIndex < totalItems - 1) {
                                        itemIndex++
                                        selectedRoleChunkIndex = null
                                        assignedRoles = emptyMap()
                                        roleFeedback = null
                                        roleSuccess = false
                                    } else {
                                        levelFinished = true
                                        localCompletedLevels = localCompletedLevels + currentLevel.id
                                        onLevelCompleted(currentLevel.id)
                                    }
                                },
                                modifier = Modifier.height(46.dp),
                            ) {
                                Text(
                                    text = if (itemIndex < totalItems - 1) "Next sentence ▸" else "Finish Level 🎉",
                                    fontSize = 14.sp,
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
                                        localCompletedLevels = localCompletedLevels + currentLevel.id
                                        onLevelCompleted(currentLevel.id)
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

@Preview(name = "Sentence Lab Level 5 (Build Sentence)", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewSentenceLabLevel5() {
    EarlyReaderTheme {
        SentenceLabScreen(initialLevelIndex = 4)
    }
}

@Preview(name = "Sentence Lab Level 6 (Roles)", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewSentenceLabLevel6() {
    EarlyReaderTheme {
        SentenceLabScreen(initialLevelIndex = 5)
    }
}
