package com.example.earlyreader.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Rounded sans (Andika/Lexend per section 8) can be swapped in later via a FontFamily;
// using the default system font for the Milestone 1 prototype.
val EarlyReaderTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 88.sp,
    ),
)
