package com.openpod.app.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SleepTimerLogicTest {
    @Test
    fun inactiveWhenNeitherModeSet() {
        assertNull(
            SleepTimerLogic.remainingMs(
                nowMs = 1_000L,
                deadlineMs = 0L,
                endOfEpisode = false,
                positionMs = 10_000L,
                durationMs = 60_000L
            )
        )
    }

    @Test
    fun timedDeadlineCountsDown() {
        assertEquals(
            60_000L,
            SleepTimerLogic.remainingMs(
                nowMs = 1_000L,
                deadlineMs = 61_000L,
                endOfEpisode = false,
                positionMs = 0L,
                durationMs = 3_600_000L
            )
        )
    }

    @Test
    fun endOfEpisodeUsesRemainingDuration() {
        assertEquals(
            20_000L,
            SleepTimerLogic.remainingMs(
                nowMs = 0L,
                deadlineMs = 0L,
                endOfEpisode = true,
                positionMs = 40_000L,
                durationMs = 60_000L
            )
        )
    }

    @Test
    fun combinedUsesSoonerOfTimeOrEpisodeEnd() {
        assertEquals(
            5_000L,
            SleepTimerLogic.remainingMs(
                nowMs = 10_000L,
                deadlineMs = 40_000L,
                endOfEpisode = true,
                positionMs = 55_000L,
                durationMs = 60_000L
            )
        )
        assertEquals(
            8_000L,
            SleepTimerLogic.remainingMs(
                nowMs = 10_000L,
                deadlineMs = 18_000L,
                endOfEpisode = true,
                positionMs = 10_000L,
                durationMs = 60_000L
            )
        )
    }

    @Test
    fun fadeStartsInsideWindow() {
        assertEquals(1f, SleepTimerLogic.fadeVolume(20_000L), 0.001f)
        assertEquals(0.5f, SleepTimerLogic.fadeVolume(10_000L), 0.001f)
        assertEquals(0f, SleepTimerLogic.fadeVolume(0L), 0.001f)
        assertEquals(1f, SleepTimerLogic.fadeVolume(45_000L), 0.001f)
    }

    @Test
    fun formatsCountdown() {
        assertEquals("0:05", SleepTimerLogic.formatRemaining(5_000L))
        assertEquals("12:34", SleepTimerLogic.formatRemaining(754_000L))
        assertEquals("1:02:03", SleepTimerLogic.formatRemaining(3_723_000L))
    }
}
