package com.example.earlyreader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * A centred column that scrolls when its content is taller than the screen (a long page, a small phone, big text).
 * When the content fits it is centred exactly as before. The bars inside consume their own drags, so a horizontal
 * scrub is never turned into a scroll.
 *
 * [resetKey] is whatever identifies the page, word or attempt being shown: when it changes the column jumps back to
 * the top, so the next page never opens scrolled down to where the last one ended. When [revealEnd] turns true (the
 * page is finished and its Next / Again buttons have just appeared) the column scrolls down to show them.
 */
@Composable
internal fun CenteredScrollColumn(
    contentPadding: PaddingValues,
    resetKey: Any? = null,
    revealEnd: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewportHeight: Dp = maxHeight
        val scrollState = rememberScrollState()

        LaunchedEffect(resetKey) {
            scrollState.scrollTo(0)
        }
        LaunchedEffect(resetKey, revealEnd) {
            if (revealEnd) {
                // Wait for the new buttons to be measured, so the scroll range already includes them.
                withFrameNanos { }
                withFrameNanos { }
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .heightIn(min = viewportHeight)
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content,
        )
    }
}
