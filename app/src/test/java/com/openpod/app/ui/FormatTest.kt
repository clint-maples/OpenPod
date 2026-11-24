package com.openpod.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun formatMsUsesMinutesOrHours() {
        assertEquals("0:00", formatMs(0))
        assertEquals("0:00", formatMs(-5))
        assertEquals("0:01", formatMs(1_000))
        assertEquals("42:10", formatMs((42 * 60 + 10) * 1_000L))
        assertEquals("1:02:10", formatMs((1 * 3600 + 2 * 60 + 10) * 1_000L))
    }

    @Test
    fun remainingMsIsDurationMinusPositionClampedAtZero() {
        assertEquals(40_000L, remainingMs(60_000, 20_000))
        assertEquals(60_000L, remainingMs(60_000, 0))
        assertEquals(0L, remainingMs(60_000, 60_000))
        assertEquals(0L, remainingMs(60_000, 80_000))
        assertEquals(0L, remainingMs(0, 10_000))
        assertEquals(0L, remainingMs(-1, 10_000))
        assertEquals(60_000L, remainingMs(60_000, -5_000))
    }

    @Test
    fun formatRemainingMsAddsALeadingMinus() {
        assertEquals("-0:00", formatRemainingMs(0))
        assertEquals("-0:00", formatRemainingMs(-1))
        assertEquals("-0:01", formatRemainingMs(1_000))
        assertEquals("-42:10", formatRemainingMs((42 * 60 + 10) * 1_000L))
        assertEquals("-1:02:10", formatRemainingMs((1 * 3600 + 2 * 60 + 10) * 1_000L))
        assertEquals(
            "-42:10",
            formatRemainingMs(remainingMs(3_600_000, 3_600_000 - (42 * 60 + 10) * 1_000L))
        )
    }
}
