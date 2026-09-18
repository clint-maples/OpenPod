package com.openpod.app.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class SkipIntervalsTest {
    @Test
    fun nearestSnapsToAllowedPresets() {
        assertEquals(10, SkipIntervals.nearest(5))
        assertEquals(10, SkipIntervals.nearest(10))
        assertEquals(10, SkipIntervals.nearest(12))
        assertEquals(15, SkipIntervals.nearest(13))
        assertEquals(15, SkipIntervals.nearest(18))
        assertEquals(30, SkipIntervals.nearest(28))
        assertEquals(45, SkipIntervals.nearest(50))
        assertEquals(60, SkipIntervals.nearest(90))
    }

    @Test
    fun labelsIncludeDirection() {
        assertEquals("−15", SkipIntervals.compactLabel(15, forward = false))
        assertEquals("+30", SkipIntervals.compactLabel(30, forward = true))
        assertEquals("Back 15s", SkipIntervals.contentDescription(16, forward = false))
        assertEquals("Forward 30s", SkipIntervals.contentDescription(28, forward = true))
    }
}
