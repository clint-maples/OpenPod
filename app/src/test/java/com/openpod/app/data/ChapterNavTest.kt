package com.openpod.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChapterNavTest {
    private val chapters = listOf(
        Chapter(0L, "Intro"),
        Chapter(60_000L, "Topic"),
        Chapter(180_000L, "Close")
    )

    @Test
    fun nextSeeksToFollowingChapterStart() {
        assertEquals(60_000L, ChapterNav.seekTargetMs(chapters, 10_000L, forward = true))
        assertEquals(180_000L, ChapterNav.seekTargetMs(chapters, 60_000L, forward = true))
    }

    @Test
    fun nextAtLastChapterDoesNothing() {
        assertNull(ChapterNav.seekTargetMs(chapters, 180_000L, forward = true))
        assertNull(ChapterNav.seekTargetMs(chapters, 200_000L, forward = true))
    }

    @Test
    fun previousFromMidChapterRestartsChapter() {
        assertEquals(60_000L, ChapterNav.seekTargetMs(chapters, 90_000L, forward = false))
    }

    @Test
    fun previousNearChapterStartGoesToPriorChapter() {
        assertEquals(0L, ChapterNav.seekTargetMs(chapters, 60_500L, forward = false))
        assertEquals(60_000L, ChapterNav.seekTargetMs(chapters, 181_000L, forward = false))
    }

    @Test
    fun previousAtFirstChapterStaysAtStart() {
        assertEquals(0L, ChapterNav.seekTargetMs(chapters, 0L, forward = false))
        assertEquals(0L, ChapterNav.seekTargetMs(chapters, 1_000L, forward = false))
    }

    @Test
    fun emptyChaptersHaveNoTarget() {
        assertNull(ChapterNav.seekTargetMs(emptyList(), 10_000L, forward = true))
        assertNull(ChapterNav.seekTargetMs(emptyList(), 10_000L, forward = false))
    }

    @Test
    fun treatsSecondsAsMillisecondsWhenAllValuesFitDuration() {
        assertEquals(listOf(0L, 90_000L, 180_000L), ChapterNav.normalizeStartMs(listOf(0.0, 90.0, 180.0), 3_600_000L))
    }

    @Test
    fun treatsRawValuesAsMillisecondsWhenSecondsWouldPassDuration() {
        assertEquals(
            listOf(0L, 90_000L, 180_000L),
            ChapterNav.normalizeStartMs(listOf(0.0, 90_000.0, 180_000.0), 3_600_000L)
        )
    }

    @Test
    fun parsesClockStrings() {
        assertEquals(3_000L, ChapterNav.parseClockToMs("0:03"))
        assertEquals(3_830_000L, ChapterNav.parseClockToMs("1:03:50"))
        assertNull(ChapterNav.parseClockToMs("95"))
    }
}
