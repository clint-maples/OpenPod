package com.openpod.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class NowPlayingSeekBarGeometryTest {
    @Test
    fun fractionAndThumbCenterAreInversesAtZeroFiftyAndOneHundred() {
        val width = 300f
        val thumb = 36f // 12 dp at xxhdpi
        val cases = listOf(0f, 0.5f, 1f)
        for (fraction in cases) {
            val x = NowPlayingSeekBarGeometry.thumbCenterX(width, fraction, thumb)
            val back = NowPlayingSeekBarGeometry.fractionFromX(x, width, thumb)
            assertEquals("fraction $fraction should round-trip through x=$x", fraction, back, 0.0001f)
        }
    }

    @Test
    fun fractionFromXClampsAndAccountsForThumbInset() {
        val width = 300f
        val thumb = 36f
        assertEquals(0f, NowPlayingSeekBarGeometry.fractionFromX(0f, width, thumb), 0f)
        assertEquals(0f, NowPlayingSeekBarGeometry.fractionFromX(thumb / 2f, width, thumb), 0f)
        assertEquals(1f, NowPlayingSeekBarGeometry.fractionFromX(width, width, thumb), 0f)
        assertEquals(1f, NowPlayingSeekBarGeometry.fractionFromX(width - thumb / 2f, width, thumb), 0f)
        assertEquals(0.25f, NowPlayingSeekBarGeometry.fractionFromX(thumb / 2f + 0.25f * (width - thumb), width, thumb), 0.0001f)
    }

    @Test
    fun centerYIsTheVerticalMidpoint() {
        // 32 dp row at xxhdpi is 96 px; the 12 dp thumb and 3 dp track must share this y.
        assertEquals(48f, NowPlayingSeekBarGeometry.centerY(96f), 0f)
        assertEquals(16f, NowPlayingSeekBarGeometry.centerY(32f), 0f)
    }
}
