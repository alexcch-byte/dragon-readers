package com.example.earlyreader

import android.content.pm.ActivityInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class OrientationPolicyTest {

    @Test
    fun phonesStayPortrait() {
        for (width in listOf(320, 360, 411, 599)) {
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, orientationFor(width, inMultiWindow = false))
        }
    }

    @Test
    fun tabletsMayRotate() {
        for (width in listOf(600, 720, 800, 962)) {
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_FULL_USER, orientationFor(width, inMultiWindow = false))
        }
    }

    @Test
    fun multiWindowIsNeverForced() {
        for (width in listOf(320, 599, 600, 900)) {
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, orientationFor(width, inMultiWindow = true))
        }
    }

    @Test
    fun aFoldableThatUnfoldsAndFoldsFollowsTheCurrentWidth() {
        // Folded (phone), unfolded (tablet-sized), folded again: each evaluation depends only on the current width.
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, orientationFor(360, false))
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_FULL_USER, orientationFor(673, false))
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, orientationFor(360, false))
    }
}
