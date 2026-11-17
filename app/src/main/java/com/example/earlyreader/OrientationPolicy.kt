package com.example.earlyreader

import android.content.pm.ActivityInfo

/** Screens at least this wide (smallest side, in dp) count as tablets and may rotate to landscape. */
internal const val TABLET_MIN_WIDTH_DP = 600

/**
 * The screen orientation the activity asks for. Phones stay portrait; tablets (smallest width 600dp or more, such as a
 * Fire HD) may rotate to landscape. In split-screen or another multi-window mode the app does not force anything: the
 * window is not the whole screen, so a forced portrait there is clumsy and the system decides. The rule is evaluated
 * again whenever the configuration changes (a folded or unfolded foldable, entering or leaving multi-window), so it
 * never stays stuck on whatever the screen was at start-up.
 */
internal fun orientationFor(smallestWidthDp: Int, inMultiWindow: Boolean): Int = when {
    inMultiWindow -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    smallestWidthDp >= TABLET_MIN_WIDTH_DP -> ActivityInfo.SCREEN_ORIENTATION_FULL_USER
    else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}
