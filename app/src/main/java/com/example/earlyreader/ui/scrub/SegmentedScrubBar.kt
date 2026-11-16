package com.example.earlyreader.ui.scrub

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.earlyreader.ui.theme.SegmentEmpty
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent
import kotlin.math.abs

// Same value as DONE in docs/index.html: a real finger lifts a little short of
// the physical edge, so requiring ~100% made the word nearly impossible to
// complete, with no feedback as to why.
private const val DONE_THRESHOLD = 0.92f

// A touch may pick the bar up again only this close (as a fraction of the bar)
// to where the last drag left off, or inside the first segment. This stops a
// stray tap near the far end from completing the word in one jump.
private const val RESUME_REACH = 0.15f

// Keeps the bar's start clear of the system back-gesture edge (plan section 10).
private val BarHorizontalPadding = 32.dp

/**
 * Milestone 1: drag-to-reveal segmented scrub bar.
 * Segment widths are proportional to letter count (section 2); vertical drift
 * is ignored since only the x position of the drag is read, and only the first
 * pointer is tracked (palm / second-finger rejection).
 */
@Composable
fun SegmentedScrubBar(
    segments: List<String>,
    modifier: Modifier = Modifier,
    onWordComplete: () -> Unit = {},
) {
    var progress by rememberSaveable(segments) { mutableStateOf(0f) }
    var completedFired by rememberSaveable(segments) { mutableStateOf(false) }

    // Segment boxes are weighted by letter count, so "filled" must be measured
    // against the cumulative weighted boundaries, not against equal slices.
    val weights = remember(segments) { segments.map { it.length.coerceAtLeast(1) } }
    val boundaries = remember(segments) {
        val total = weights.sum().toFloat()
        var running = 0
        weights.map { running += it; running / total }
    }

    val isComplete = progress >= DONE_THRESHOLD
    val displayProgress = if (isComplete) 1f else progress
    val filledSegments = if (isComplete) {
        segments.size
    } else {
        boundaries.count { it <= progress }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Each letter is centred inside its weighted segment so it sits above its own segment.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BarHorizontalPadding)
                .padding(bottom = 16.dp),
        ) {
            segments.forEachIndexed { index, letters ->
                Box(
                    modifier = Modifier.weight(weights[index].toFloat()),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = letters,
                        style = MaterialTheme.typography.displayLarge,
                        color = if (index < filledSegments) WarmAccent else MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }

        val fillFraction by animateFloatAsState(targetValue = displayProgress, label = "scrubFill")
        val wordText = segments.joinToString("")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BarHorizontalPadding)
                .height(72.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(SegmentEmpty)
                .semantics {
                    contentDescription = "Sound out the word $wordText"
                    stateDescription = "$filledSegments of ${segments.size} sounds"
                    progressBarRangeInfo = ProgressBarRangeInfo(displayProgress, 0f..1f)
                    setProgress(action = { target ->
                        progress = target.coerceIn(0f, 1f)
                        true
                    })
                }
                .pointerInput(segments, boundaries) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val width = size.width.toFloat()
                        if (width <= 0f) return@awaitEachGesture

                        val startFraction = (down.position.x / width).coerceIn(0f, 1f)
                        val insideFirstSegment = startFraction <= (boundaries.firstOrNull() ?: 1f)
                        val nearCurrent = abs(startFraction - progress) <= RESUME_REACH
                        if (!insideFirstSegment && !nearCurrent) return@awaitEachGesture

                        // Track only this pointer; any other finger or palm is ignored.
                        val pointerId = down.id
                        down.consume()
                        progress = startFraction
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) break
                            progress = (change.position.x / width).coerceIn(0f, 1f)
                            change.consume()
                        }
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fillFraction)
                    .clip(RoundedCornerShape(36.dp))
                    .background(VioletPrimary),
            )

            Row(modifier = Modifier.fillMaxSize()) {
                weights.forEach { weight ->
                    Box(
                        modifier = Modifier
                            .weight(weight.toFloat())
                            .fillMaxHeight()
                            .border(width = 2.dp, color = Color.White.copy(alpha = 0.5f)),
                    )
                }
            }
        }

        LaunchedEffect(isComplete) {
            if (isComplete) {
                if (!completedFired) {
                    completedFired = true
                    onWordComplete()
                }
            } else {
                completedFired = false
            }
        }
    }
}
