package com.openpod.app.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class SkipIntervalsTest {
    @Test
    fun sanitizeClampsToPositiveRange() {
        assertEquals(1, SkipIntervals.sanitize(0))
        assertEquals(1, SkipIntervals.sanitize(-12))
        assertEquals(10, SkipIntervals.sanitize(10))
        assertEquals(90, SkipIntervals.sanitize(90))
        assertEquals(600, SkipIntervals.sanitize(600))
        assertEquals(600, SkipIntervals.sanitize(9_999))
    }

    @Test
    fun parseReadsIntegersAndFallsBack() {
        assertEquals(15, SkipIntervals.parse("", 15))
        assertEquals(15, SkipIntervals.parse("abc", 15))
        assertEquals(1, SkipIntervals.parse("0", 15))
        assertEquals(45, SkipIntervals.parse(" 45 ", 15))
        assertEquals(600, SkipIntervals.parse("800", 15))
    }

    @Test
    fun labelsUseExactSanitizedSeconds() {
        assertEquals("12", SkipIntervals.compactLabel(12, forward = false))
        assertEquals("90", SkipIntervals.compactLabel(90, forward = true))
        assertEquals("Back 12s", SkipIntervals.contentDescription(12, forward = false))
        assertEquals("Forward 90s", SkipIntervals.contentDescription(90, forward = true))
    }
}
