package com.example.earlyreader.ui.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.earlyreader.audio.SpeechStatus
import com.example.earlyreader.ui.CenteredScrollColumn
import com.example.earlyreader.ui.theme.EarlyReaderTheme
import com.example.earlyreader.ui.theme.TextDark
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent

/** Every tile is at least this tall: big, simple targets for a 4-year-old. */
internal val WelcomeTileMinHeight = 112.dp

/** The least a tile may be on a short screen (see the compact layout of [WelcomeScreen]). */
internal val WelcomeTileMinHeightCompact = 96.dp

/** From this width up (a tablet in landscape) the four tiles sit in one row; below it they are a 2x2 grid. */
private val WideLayoutMinWidth = 720.dp

private data class TileStyle(val background: Color, val border: Color)

// Pale tints with dark text (contrast well above 4.5:1); each tile also has its own picture and label, so colour is
// never the only way to tell them apart.
private val ReadStyle = TileStyle(Color(0xFFEDE9FE), VioletPrimary)
private val SoundsStyle = TileStyle(Color(0xFFFFEDD5), WarmAccent)
private val StoriesStyle = TileStyle(Color(0xFFDCFCE7), Color(0xFF15803D))
private val LabStyle = TileStyle(Color(0xFFDBEAFE), Color(0xFF1D4ED8))

private data class WelcomeTile(
    val emoji: String,
    val label: String,
    val caption: String,
    val description: String,
    val style: TileStyle,
    val onClick: () -> Unit,
)

/**
 * Below this much height for the page (the area under the top bar) the page switches to its compact layout, so it can
 * still be seen whole on a small phone. Above it the roomy layout needs about 590dp.
 */
private val CompactHeightMax = 640.dp

/**
 * The Welcome page: what the Home button opens, and what the app shows when it starts. It speaks only when the
 * child taps the dragon ([onHearHello]); nothing plays by itself. [speechNote] is a small calm line shown when
 * speech cannot work on this device (null when it can).
 *
 * Height budget of the compact layout on the smallest phone it is meant for, 320x568dp with a status bar (24dp), a
 * three-button navigation bar (48dp) and the two-row top bar (116dp): 380dp are left. The page needs 16 (padding) +
 * 52 (dragon and "Hear hello" side by side) + 4 + 31 (title, 26sp) + 20 (subtitle, 16sp) + 8 + 218 (two tile rows of
 * about 105: picture 32sp, label 18sp, a two-line 12sp caption, 8dp padding; 96dp is the least) = 349dp for a new
 * child, and about 390dp with a two-line progress line: at most a line or two of scrolling in the very worst case
 * (which is what [CenteredScrollColumn] is for), and with gesture navigation (no 48dp bar) it all fits.
 */
@Composable
fun WelcomeScreen(
    readTile: ReadTileInfo,
    progressLine: String?,
    onRead: () -> Unit,
    onSounds: () -> Unit,
    onStories: () -> Unit,
    onSentenceLab: () -> Unit,
    onHearHello: () -> Unit,
    speechNote: String? = null,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compact = maxHeight < CompactHeightMax
        CenteredScrollColumn(
            contentPadding = if (compact) {
                PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            } else {
                PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            },
        ) {
            WelcomeContent(
                compact = compact,
                readTile = readTile,
                progressLine = progressLine,
                speechNote = speechNote,
                onRead = onRead,
                onSounds = onSounds,
                onStories = onStories,
                onSentenceLab = onSentenceLab,
                onHearHello = onHearHello,
            )
        }
    }
}

@Composable
private fun ColumnScope.WelcomeContent(
    compact: Boolean,
    readTile: ReadTileInfo,
    progressLine: String?,
    speechNote: String?,
    onRead: () -> Unit,
    onSounds: () -> Unit,
    onStories: () -> Unit,
    onSentenceLab: () -> Unit,
    onHearHello: () -> Unit,
) {
    // The dragon is the app's mascot (the launcher icon is a dragon reading a book); tapping it says hello. In the
    // compact layout the dragon and the "Hear hello" button sit side by side to save height.
    val helloModifier = Modifier
        .clip(RoundedCornerShape(24.dp))
        .clickable(role = Role.Button, onClick = onHearHello)
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .semantics(mergeDescendants = true) { contentDescription = "Hear hello" }
    if (compact) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = helloModifier,
        ) {
            Text(text = "🐉", fontSize = 40.sp, modifier = Modifier.clearAndSetSemantics { })
            HelloPill()
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = helloModifier) {
            Text(text = "🐉", fontSize = 72.sp, modifier = Modifier.clearAndSetSemantics { })
            HelloPill()
        }
    }

    Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
    Text(
        text = "Early Reader",
        fontSize = if (compact) 26.sp else 40.sp,
        fontWeight = FontWeight.Bold,
        color = VioletPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() },
    )
    if (!compact) Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Hi! What shall we do today?",
        fontSize = if (compact) 16.sp else 20.sp,
        color = TextDark,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(if (compact) 8.dp else 20.dp))

    val tiles = listOf(
        WelcomeTile("👆", "Read", readTile.caption, readTile.description, ReadStyle, onRead),
        WelcomeTile("🔤", "Sounds", "Drag to sound out words", "Sounds, drag to sound out words", SoundsStyle, onSounds),
        WelcomeTile("📖", "Stories", "Read word by word", "Stories, read word by word", StoriesStyle, onStories),
        WelcomeTile("✏️", "Sentence Lab", "for ages 6+", "Sentence Lab, for ages 6 and up", LabStyle, onSentenceLab),
    )
    val gap = if (compact) 8.dp else 12.dp
    BoxWithConstraints(modifier = Modifier.widthIn(max = 880.dp).fillMaxWidth()) {
        val perRow = if (maxWidth >= WideLayoutMinWidth) 4 else 2
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            tiles.chunked(perRow).forEach { rowTiles ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                ) {
                    rowTiles.forEach { tile -> WelcomeTileButton(tile, compact) }
                }
            }
        }
    }

    if (progressLine != null) {
        Spacer(modifier = Modifier.height(if (compact) 8.dp else 20.dp))
        Text(
            text = progressLine,
            fontSize = if (compact) 14.sp else 16.sp,
            color = TextDark,
            textAlign = TextAlign.Center,
        )
    }

    if (speechNote != null) {
        Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))
        Text(
            text = speechNote,
            fontSize = if (compact) 13.sp else 14.sp,
            color = TextDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

/** The "Hear hello" button shape under (or beside) the dragon; the whole dragon block is the one tap target. */
@Composable
private fun HelloPill() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "🔊 Hear hello",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = VioletPrimary,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

@Composable
private fun RowScope.WelcomeTileButton(tile: WelcomeTile, compact: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .heightIn(min = if (compact) WelcomeTileMinHeightCompact else WelcomeTileMinHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(tile.style.background)
            .border(BorderStroke(2.dp, tile.style.border), RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = tile.onClick)
            .padding(horizontal = 8.dp, vertical = if (compact) 8.dp else 12.dp)
            .semantics(mergeDescendants = true) { contentDescription = tile.description },
    ) {
        // The picture and words are described once, by the tile's own description.
        Text(text = tile.emoji, fontSize = if (compact) 32.sp else 40.sp, modifier = Modifier.clearAndSetSemantics { })
        Text(
            text = tile.label,
            fontSize = if (compact) 18.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics { },
        )
        if (tile.caption.isNotEmpty()) {
            Text(
                text = tile.caption,
                fontSize = if (compact) 12.sp else 14.sp,
                color = TextDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
    }
}

@Preview(name = "Welcome - small phone", widthDp = 320, heightDp = 568, showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewWelcomePhone() {
    EarlyReaderTheme {
        WelcomeScreen(
            readTile = ReadTileInfo(3, "The Cat on the Mat", continuing = true),
            progressLine = welcomeProgressLine(3, 14, 12),
            onRead = {},
            onSounds = {},
            onStories = {},
            onSentenceLab = {},
            onHearHello = {},
        )
    }
}

// 320x568dp phone with a status bar, a three-button navigation bar and the two-row top bar: 568 - 24 - 48 - 116 = 380dp
// are left for the page. The worst case: a Continue caption, a progress line and the speech note.
@Preview(name = "Welcome - small phone, bars and top bar", widthDp = 320, heightDp = 380, showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
fun PreviewWelcomeSmallPhoneTight() {
    EarlyReaderTheme {
        WelcomeScreen(
            readTile = ReadTileInfo(3, "The Cat on the Mat", continuing = true),
            progressLine = welcomeProgressLine(3, 14, 12),
            onRead = {},
            onSounds = {},
            onStories = {},
            onSentenceLab = {},
            onHearHello = {},
            speechNote = speechNote(SpeechStatus.NO_LANGUAGE),
        )
    }
}

@Preview(
    name = "Welcome - tablet landscape",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    showBackground = true,
    backgroundColor = 0xFFFAF7F2,
)
@Composable
fun PreviewWelcomeTablet() {
    EarlyReaderTheme {
        WelcomeScreen(
            readTile = ReadTileInfo(0, "The Red Hen", continuing = false),
            progressLine = null,
            onRead = {},
            onSounds = {},
            onStories = {},
            onSentenceLab = {},
            onHearHello = {},
        )
    }
}
