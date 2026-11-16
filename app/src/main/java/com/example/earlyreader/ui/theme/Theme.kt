package com.example.earlyreader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EarlyReaderColorScheme = lightColorScheme(
    primary = VioletPrimary,
    background = OffWhiteBackground,
    surface = OffWhiteBackground,
    secondary = WarmAccent,
    onBackground = TextDark,
    onSurface = TextDark,
)

@Composable
fun EarlyReaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EarlyReaderColorScheme,
        typography = EarlyReaderTypography,
        content = content,
    )
}
