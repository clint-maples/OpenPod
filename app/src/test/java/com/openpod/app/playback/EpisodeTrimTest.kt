package com.openpod.app.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeTrimTest {
    @Test
    fun sanitizeAllowsZeroAndCapsLength() {
        assertEquals(0, EpisodeTrim.sanitize(-3))
        assertEquals(0, EpisodeTrim.sanitize(0))
        assertEquals(90, EpisodeTrim.sanitize(90))
        assertEquals(1_800, EpisodeTrim.sanitize(1_800))
        assertEquals(1_800, EpisodeTrim.sanitize(9_000))
    }

    @Test
    fun freshStartAppliesIntro() {
        assertEquals(
            90_000L,
            EpisodeTrim.startPositionMs(
                requestedMs = 0L,
                skipIntroSec = 90,
                skipOutroSec = 30,
                durationMs = 3_600_000L
            )
        )
        assertEquals(
            90_000L,
            EpisodeTrim.startPositionMs(
                requestedMs = -1L,
                skipIntroSec = 90,
                skipOutroSec = 0,
                durationMs = 0L
            )
        )
    }

    @Test
    fun resumeKeepsMidEpisodePosition() {
        assertEquals(
            240_000L,
            EpisodeTrim.startPositionMs(
                requestedMs = 240_000L,
                skipIntroSec = 90,
                skipOutroSec = 30,
                durationMs = 3_600_000L
            )
        )
    }

    @Test
    fun effectiveEndSubtractsOutro() {
        assertEquals(
            3_570_000L,
            EpisodeTrim.effectiveEndMs(3_600_000L, skipOutroSec = 30, skipIntroSec = 90)
        )
        assertEquals(0L, EpisodeTrim.effectiveEndMs(0L, skipOutroSec = 30))
    }

    @Test
    fun overlappingTrimsLeaveASecondOfAudio() {
        assertEquals(
            91_000L,
            EpisodeTrim.effectiveEndMs(100_000L, skipOutroSec = 80, skipIntroSec = 90)
        )
    }

    @Test
    fun resumePastOutroClampsToEffectiveEnd() {
        assertEquals(
            3_570_000L,
            EpisodeTrim.startPositionMs(
                requestedMs = 3_590_000L,
                skipIntroSec = 90,
                skipOutroSec = 30,
                durationMs = 3_600_000L
            )
        )
    }

    @Test
    fun outroStopsPlaybackAtTrimmedEnd() {
        assertTrue(EpisodeTrim.shouldStopForOutro(3_570_000L, 3_600_000L, 30, 90))
        assertFalse(EpisodeTrim.shouldStopForOutro(3_569_000L, 3_600_000L, 30, 90))
        assertFalse(EpisodeTrim.shouldStopForOutro(3_599_000L, 3_600_000L, 0, 90))
    }

    @Test
    fun completedUsesFiveSecondSlackOnlyWithoutOutro() {
        assertTrue(EpisodeTrim.isCompleted(3_595_001L, 3_600_000L, skipOutroSec = 0))
        assertFalse(EpisodeTrim.isCompleted(3_594_000L, 3_600_000L, skipOutroSec = 0))
        assertTrue(EpisodeTrim.isCompleted(3_570_000L, 3_600_000L, skipOutroSec = 30, skipIntroSec = 90))
        assertFalse(EpisodeTrim.isCompleted(3_569_000L, 3_600_000L, skipOutroSec = 30, skipIntroSec = 90))
    }
}
