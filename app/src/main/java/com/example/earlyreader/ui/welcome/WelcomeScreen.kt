package com.example.earlyreader.ui.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.earlyreader.ui.CenteredScrollColumn
import com.example.earlyreader.ui.theme.EarlyReaderTheme
import com.example.earlyreader.ui.theme.TextDark
import com.example.earlyreader.ui.theme.VioletPrimary
import com.example.earlyreader.ui.theme.WarmAccent

/** Every tile is at least this tall: big, simple targets for a 4-year-old. */
internal val WelcomeTileMinHeight = 112.dp

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
 * The Welcome page: what the Home button opens, and what the app shows when it starts. It speaks only when the
 * child taps the dragon ([onHearHello]); nothing plays by itself.
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
) {
    CenteredScrollColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        // The dragon is the app's mascot (the launcher icon is a dragon reading a book); tapping it says hello.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .clickable(role = Role.Button, onClick = onHearHello)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .semantics(mergeDescendants = true) { contentDescription = "Hear hello" },
        ) {
            Text(text = "🐉", fontSize = 72.sp, modifier = Modifier.clearAndSetSemantics { })
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

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Early Reader",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = VioletPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Hi! What shall we do today?",
            fontSize = 20.sp,
            color = TextDark,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))

        val tiles = listOf(
            WelcomeTile("👆", "Read", readTile.caption, readTile.description, ReadStyle, onRead),
            WelcomeTile("🔤", "Sounds", "Drag to sound out words", "Sounds, drag to sound out words", SoundsStyle, onSounds),
            WelcomeTile("📖", "Stories", "Read word by word", "Stories, read word by word", StoriesStyle, onStories),
            WelcomeTile("✏️", "Sentence Lab", "for ages 6+", "Sentence Lab, for ages 6 and up", LabStyle, onSentenceLab),
        )
        BoxWithConstraints(modifier = Modifier.widthIn(max = 880.dp).fillMaxWidth()) {
            val perRow = if (maxWidth >= WideLayoutMinWidth) 4 else 2
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tiles.chunked(perRow).forEach { rowTiles ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                    ) {
                        rowTiles.forEach { tile -> WelcomeTileButton(tile) }
                    }
                }
            }
        }

        if (progressLine != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = progressLine,
                fontSize = 16.sp,
                color = TextDark,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RowScope.WelcomeTileButton(tile: WelcomeTile) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .heightIn(min = WelcomeTileMinHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(tile.style.background)
            .border(BorderStroke(2.dp, tile.style.border), RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = tile.onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) { contentDescription = tile.description },
    ) {
        // The picture and words are described once, by the tile's own description.
        Text(text = tile.emoji, fontSize = 40.sp, modifier = Modifier.clearAndSetSemantics { })
        Text(
            text = tile.label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics { },
        )
        if (tile.caption.isNotEmpty()) {
            Text(
                text = tile.caption,
                fontSize = 14.sp,
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
