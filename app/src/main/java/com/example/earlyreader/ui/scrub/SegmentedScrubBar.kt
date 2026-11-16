package com.example.earlyreader.ui.scrub

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.example.earlyreader.ui.theme.SegmentEmpty
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent
import kotlin.math.floor

/**
 * Milestone 1: drag-to-reveal segmented scrub bar.
 * Segment widths are proportional to letter count (section 2); vertical drift
 * is ignored since only the x position of the drag is read.
 */
@Composable
fun SegmentedScrubBar(
    segments: List<String>,
    modifier: Modifier = Modifier,
    onWordComplete: () -> Unit = {},
) {
    var progress by remember(segments) { mutableFloatStateOf(0f) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    var completedFired by remember(segments) { mutableStateOf(false) }

    val filledSegments = if (progress >= 0.999f) {
        segments.size
    } else {
        floor(progress * segments.size).toInt().coerceIn(0, segments.size)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            segments.forEachIndexed { index, letters ->
                Text(
                    text = letters,
                    style = MaterialTheme.typography.displayLarge,
                    color = if (index < filledSegments) WarmAccent else MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        val fillFraction by animateFloatAsState(targetValue = progress, label = "scrubFill")

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(72.dp)
                .onGloballyPositioned { widthPx = it.size.width.toFloat() }
                .clip(RoundedCornerShape(36.dp))
                .background(SegmentEmpty)
                .pointerInput(segments) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (widthPx > 0f) {
                                progress = (offset.x / widthPx).coerceIn(0f, 1f)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (widthPx > 0f) {
                                progress = (change.position.x / widthPx).coerceIn(0f, 1f)
                            }
                        },
                    )
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
                segments.forEach { letters ->
                    Box(
                        modifier = Modifier
                            .weight(letters.length.toFloat().coerceAtLeast(1f))
                            .fillMaxHeight()
                            .border(width = 2.dp, color = Color.White.copy(alpha = 0.5f)),
                    )
                }
            }
        }

        LaunchedEffect(filledSegments, progress) {
            if (filledSegments == segments.size && progress >= 0.999f) {
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
