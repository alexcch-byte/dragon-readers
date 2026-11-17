package com.example.earlyreader.ui.scrub

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.earlyreader.ui.theme.EarlyReaderTheme
import com.example.earlyreader.ui.theme.SegmentEmpty
import com.example.earlyreader.ui.theme.TextDark
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

// Same value as DONE in docs/index.html: a real finger lifts a little short of
// the physical edge, so requiring ~100% made the word nearly impossible to
// complete, with no feedback as to why.
internal const val DONE_THRESHOLD = 0.92f

// A touch inside the first segment, or within this reach of where the last drag
// left off, picks the bar up exactly where the finger landed. The reach is the
// larger of this fraction of the bar and START_REACH_DP, so it stays generous on
// wide bars. A touch outside it is not ignored (a 4-year-old tapping the middle
// of the bar would see nothing happen): it snaps to the nearest allowed point
// (the start, or the current progress) and a "start here" thumb pulses there.
// After such a snapped start the drag does not follow the finger at all until the
// finger comes within reach of the thumb (or into the first segment), so pressing
// and jittering at the far end of the bar can't creep the word to completion.
private const val MIN_START_REACH = 0.35f
private const val START_REACH_DP = 100

// Keeps a stray tap near the far end from completing the word in one jump: while
// dragging, progress can advance at most this far per input event...
private const val MAX_STEP = 0.4f

// ...and at most this many bar-lengths per second (the same cap as MAX_SPEED in docs/index.html). A finger
// that rests on the bar without moving sends no events, so a frame loop (below) keeps the thumb moving
// toward it at this speed, exactly like the catch-up loop in docs/index.html.
private const val MAX_SPEED = 3.0f

// Fraction of the bar below which a drag counts as "back at the start" (word can be scrubbed again).
internal const val RESCRUB_RESET = 0.03f

// Keeps the bar's start clear of the system back-gesture edge (plan section 10).
private val BarHorizontalPadding = 32.dp

internal val ThumbWidth = 56.dp
internal val ThumbHeight = 48.dp
private val ThumbSize = 44.dp
private val MaxLetterSize = 88.dp

// Rough advance width of a bold letter as a multiple of its font size; used to
// shrink the letters of a narrow segment so they never spill into the next one.
private const val LETTER_WIDTH_EM = 0.7f

/** Each segment's width on the bar is proportional to its letter count (plan section 2). */
internal fun segmentWeights(segments: List<String>): List<Int> = segments.map { it.length.coerceAtLeast(1) }

/** The cumulative end of each segment as a fraction of the bar (the last one is 1). */
internal fun segmentBoundaries(segments: List<String>): List<Float> {
    val weights = segmentWeights(segments)
    val total = weights.sum().toFloat()
    var running = 0
    return weights.map { running += it; running / total }
}

/**
 * Custom pointed arrow peak shape for the slider thumb handle.
 * Features rounded bottom corners and an upward-pointing peak at top center.
 */
val PointedThumbShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val r = h * 0.25f // proportional corner radius
    val peakWidth = w * 0.40f // peak width
    val peakHeight = h * 0.22f // peak height
    val halfW = w / 2f

    moveTo(r, peakHeight)
    lineTo(halfW - peakWidth / 2f, peakHeight)
    lineTo(halfW, 0f)
    lineTo(halfW + peakWidth / 2f, peakHeight)
    lineTo(w - r, peakHeight)
    quadraticBezierTo(w, peakHeight, w, peakHeight + r)
    lineTo(w, h - r)
    quadraticBezierTo(w, h, w - r, h)
    lineTo(r, h)
    quadraticBezierTo(0f, h, 0f, h - r)
    lineTo(0f, peakHeight + r)
    quadraticBezierTo(0f, peakHeight, r, peakHeight)
    close()
}

/**
 * Tactile pointed thumb handle matching the reference mockup.
 * Features a soft upward-pointing peak pointing directly up into the active word / phoneme
 * segment above it, a navy border, and 3 vertical grip ridges (|||) for draggable affordance.
 */
@Composable
fun FingerSliderThumb(
    modifier: Modifier = Modifier,
    isPulsing: Boolean = false,
) {
    val scale = if (isPulsing) 1.25f else 1f
    val animatedScale by animateFloatAsState(targetValue = scale, label = "thumbScale")

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            },
        contentAlignment = Alignment.Center,
    ) {
        if (isPulsing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding((-6).dp)
                    .clip(PointedThumbShape)
                    .background(VioletPrimary.copy(alpha = 0.35f)),
            )
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = PointedThumbShape,
            color = Color.White,
            border = BorderStroke(2.5.dp, TextDark),
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Tactile grip lines: 3 vertical grip ridges (|||) indicating draggable affordance
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(Color(0xFFC4B5FD)),
                        )
                    }
                }
            }
        }
    }
}

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
    onDragStart: () -> Unit = {},
    // (segment, startsNewScrub): startsNewScrub is true for the first sound of a scrub from the start of the
    // word, which is the only time the speech queue is flushed; every other sound waits behind the last one.
    onSegmentSound: (String, Boolean) -> Unit = { _, _ -> },
    onWordComplete: () -> Unit = {},
) {
    var progress by rememberSaveable(segments) { mutableStateOf(0f) }
    var completedFired by rememberSaveable(segments) { mutableStateOf(false) }
    var dragStartedFired by rememberSaveable(segments) { mutableStateOf(false) }
    // Once the word is complete the bar is finished: no more dragging, no re-firing the reward.
    var locked by rememberSaveable(segments) { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    // Where the held finger is along the bar (0..1) and whether this drag may move the thumb (false after a
    // snapped start, until the finger reaches the thumb); both are read by the frame loop below.
    var fingerFrac by remember { mutableFloatStateOf(0f) }
    var following by remember { mutableStateOf(false) }
    var lastAdvanceAt by remember { mutableLongStateOf(0L) }

    // "Start here" pulse shown when a touch was snapped to an allowed start point.
    var startHint by remember { mutableStateOf(false) }
    var barWidthPx by remember { mutableIntStateOf(0) }

    // Segment boxes are weighted by letter count, so "filled" must be measured
    // against the cumulative weighted boundaries, not against equal slices.
    val weights = remember(segments) { segmentWeights(segments) }
    val boundaries = remember(segments) { segmentBoundaries(segments) }

    val isComplete = locked || progress >= DONE_THRESHOLD
    val displayProgress = if (isComplete) 1f else progress
    val filledSegments = if (isComplete) {
        segments.size
    } else {
        boundaries.count { it <= progress }
    }

    var lastSoundedSegment by rememberSaveable(segments) { mutableIntStateOf(0) }

    LaunchedEffect(filledSegments) {
        if (filledSegments > lastSoundedSegment && filledSegments <= segments.size) {
            for (idx in lastSoundedSegment until filledSegments) {
                onSegmentSound(segments[idx], idx == 0)
            }
            lastSoundedSegment = filledSegments
        } else if (progress <= RESCRUB_RESET) {
            lastSoundedSegment = 0
        }
    }

    // Moves the thumb toward the finger: never more than MAX_STEP in one go, nor faster than MAX_SPEED
    // bar-lengths per second since the last time it was allowed to move (shared by touch events and the frame loop,
    // so the two together can never move it faster than that).
    fun advanceToFinger() {
        val now = SystemClock.uptimeMillis()
        val dt = (now - lastAdvanceAt).coerceAtLeast(0L) / 1000f
        lastAdvanceAt = now
        progress = if (fingerFrac <= RESCRUB_RESET) {
            0f
        } else {
            fingerFrac.coerceAtMost(progress + minOf(MAX_STEP, dt * MAX_SPEED))
        }
    }

    // A finger that rests on the bar (or has moved on faster than the thumb may follow) sends no new events, so the
    // thumb would stall. While a drag is held and "following", every frame moves it on toward the finger at MAX_SPEED.
    // A drag that was snapped to the start and has not reached the thumb is not following, so a finger resting or
    // jittering at the far end of the bar still does nothing.
    LaunchedEffect(isDragging) {
        if (!isDragging) return@LaunchedEffect
        while (true) {
            withFrameNanos { }
            if (locked) break
            if (following && fingerFrac > progress) {
                advanceToFinger()
                if (progress >= DONE_THRESHOLD) {
                    progress = 1f
                    locked = true
                    break
                }
            } else {
                lastAdvanceAt = SystemClock.uptimeMillis()
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Each letter is centred inside its weighted segment so it sits above its own segment.
        // The size is in dp (so a large system font scale can't blow it up) and shrinks to fit
        // narrow segments, e.g. the single-letter segments of a 4-letter word.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BarHorizontalPadding)
                .padding(bottom = 8.dp),
        ) {
            segments.forEachIndexed { index, letters ->
                BoxWithConstraints(
                    modifier = Modifier.weight(weights[index].toFloat()),
                    contentAlignment = Alignment.Center,
                ) {
                    val fitted = (maxWidth / (letters.length.coerceAtLeast(1) * LETTER_WIDTH_EM))
                        .coerceAtMost(MaxLetterSize)
                    val fontSize = with(LocalDensity.current) { fitted.toSp() }
                    Text(
                        text = letters,
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = fontSize,
                        color = if (index < filledSegments) WarmAccent else MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }

        val animatedProgress by animateFloatAsState(
            targetValue = displayProgress,
            animationSpec = if (isDragging) snap() else spring(),
            label = "scrubFill",
        )
        val fillFraction = if (isDragging) displayProgress else animatedProgress
        val hintAlpha by animateFloatAsState(targetValue = if (startHint) 1f else 0f, label = "startHint")
        val wordText = segments.joinToString("")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BarHorizontalPadding)
                .height(72.dp)
                .onSizeChanged { barWidthPx = it.width }
                .clip(RoundedCornerShape(36.dp))
                .background(SegmentEmpty)
                // The pale empty track alone is ~1.7:1 against the background, so it gets a violet outline
                // (the same 2dp outline as the web bar). The violet fill drawn on top hides it as it fills.
                .border(width = 2.dp, color = VioletPrimary, shape = RoundedCornerShape(36.dp))
                .semantics {
                    contentDescription = "Sound out the word $wordText"
                    stateDescription = "$filledSegments of ${segments.size} sounds"
                    // N segments give N - 1 steps between the ends (0 and 1 are not steps).
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        displayProgress,
                        0f..1f,
                        steps = (segments.size - 1).coerceAtLeast(0),
                    )
                    setProgress(action = { target ->
                        if (locked) {
                            false
                        } else {
                            // Accessibility services move the bar one segment at a time: forward to
                            // the next segment boundary, back to the previous one. This also keeps
                            // any single step under ~0.34 of the bar for equal-width segments.
                            val next = if (target > progress) {
                                boundaries.firstOrNull { it > progress + 0.001f } ?: 1f
                            } else if (target < progress) {
                                boundaries.lastOrNull { it < progress - 0.001f } ?: 0f
                            } else {
                                progress
                            }
                            progress = next.coerceIn(0f, 1f)
                            true
                        }
                    })
                }
                .pointerInput(segments, boundaries) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (locked) return@awaitEachGesture
                        val width = size.width.toFloat()
                        if (width <= 0f) return@awaitEachGesture

                        val reach = maxOf(MIN_START_REACH, START_REACH_DP.dp.toPx() / width)
                        val firstBoundary = boundaries.firstOrNull() ?: 1f
                        val touch = (down.position.x / width).coerceIn(0f, 1f)
                        val insideFirstSegment = touch <= firstBoundary
                        val nearCurrent = abs(touch - progress) <= reach
                        // False after a snapped start: moves are ignored until the finger reaches the thumb.
                        following = insideFirstSegment || nearCurrent
                        fingerFrac = touch
                        lastAdvanceAt = SystemClock.uptimeMillis()
                        if (!dragStartedFired) {
                            dragStartedFired = true
                            onDragStart()
                        }
                        if (following) {
                            progress = if (touch <= RESCRUB_RESET) 0f else touch.coerceAtMost(progress + MAX_STEP)
                        } else {
                            // Too far from anywhere a drag may begin: start from the nearest
                            // allowed point (the start or the current progress) and show where.
                            val snapTo = if (touch <= progress / 2f) 0f else progress
                            startHint = true
                            progress = snapTo
                        }

                        // Track only this pointer; any other finger or palm is ignored.
                        val pointerId = down.id
                        down.consume()
                        try {
                            isDragging = true
                            while (true) {
                                if (progress >= DONE_THRESHOLD) { locked = true; break }
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                if (!change.pressed) {
                                    if (following && fingerFrac > progress) {
                                        progress = fingerFrac
                                        if (fingerFrac >= DONE_THRESHOLD) {
                                            progress = 1f
                                            locked = true
                                        }
                                    }
                                    break
                                }
                                val x = (change.position.x / width).coerceIn(0f, 1f)
                                fingerFrac = x
                                if (!following && (x <= firstBoundary || abs(x - progress) <= reach)) {
                                    following = true
                                    lastAdvanceAt = SystemClock.uptimeMillis()
                                }
                                if (following) advanceToFinger()
                                change.consume()
                            }
                        } finally {
                            isDragging = false
                            following = false
                        }
                        if (progress >= DONE_THRESHOLD) locked = true
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
                weights.forEachIndexed { index, weight ->
                    Spacer(modifier = Modifier.weight(weight.toFloat()).fillMaxHeight())
                    if (index < weights.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(Color.White.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            // The finger-shaped thumb rests at the fill edge pointing directly up at the active sound segment.
            val thumbWidthPx = with(LocalDensity.current) { ThumbWidth.roundToPx() }
            val thumbX = (fillFraction * barWidthPx - thumbWidthPx / 2f)
                .coerceIn(0f, (barWidthPx - thumbWidthPx).coerceAtLeast(0).toFloat())
            FingerSliderThumb(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset { IntOffset(thumbX.roundToInt(), 0) }
                    .size(width = ThumbWidth, height = ThumbHeight),
                isPulsing = hintAlpha > 0.01f,
            )
        }

        // Clear the pulse shortly after it starts.
        LaunchedEffect(startHint) {
            if (startHint) {
                delay(700)
                startHint = false
            }
        }

        LaunchedEffect(isComplete) {
            if (isComplete && !completedFired) {
                completedFired = true
                locked = true
                onWordComplete()
            }
        }
    }
}

@Preview(name = "Single Letter Word (cat)", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewSegmentedScrubBarCat() {
    EarlyReaderTheme {
        SegmentedScrubBar(segments = listOf("c", "a", "t"))
    }
}

@Preview(name = "Digraph Word (ship)", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewSegmentedScrubBarShip() {
    EarlyReaderTheme {
        SegmentedScrubBar(segments = listOf("sh", "i", "p"))
    }
}
