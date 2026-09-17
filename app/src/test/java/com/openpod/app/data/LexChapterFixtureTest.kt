package com.openpod.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LexChapterFixtureTest {
    private fun resource(name: String): String {
        val stream = checkNotNull(javaClass.classLoader!!.getResourceAsStream(name)) { "missing $name" }
        return stream.bufferedReader().use { it.readText() }
    }

    @Test
    fun lexJsonWithMissingDurationStaysOnHourScale() {
        // Official #502 RSS has no itunes:duration (durationMs = 0) and no
        // podcast:chapters URL. This fixture is the outline converted to
        // Podcast Index JSON (startTime in seconds). The 1.16 heuristic
        // treated max 12798 as milliseconds (~12.8s). Next/prev then sought
        // a few milliseconds and looked like a no-op.
        val chapters = ChapterJson.parse(resource("lex_fridman_ep502_chapters.json"), durationMs = 0L)
        assertEquals(15, chapters.size)
        assertEquals(0L, chapters.first().startMs)
        assertEquals("Introduction", chapters.first().title)
        assertEquals(12_798_000L, chapters.last().startMs)
        assertEquals("Future of Psychiatry", chapters.last().title)
        assertTrue(chapters[6].startMs >= 60_000L)
        assertTrue(chapters.all { it.startMs == 0L || it.startMs >= 60_000L || it.startMs == 67_000L })
        val next = ChapterNav.seekTargetMs(chapters, 10_000L, forward = true)
        assertEquals(67_000L, next)
        val later = ChapterNav.seekTargetMs(chapters, 5_000_000L, forward = true)
        assertTrue(later != null && later >= 5_374_000L)
    }

    @Test
    fun lexDescriptionOutlineParsesClockTimes() {
        val chapters = ChapterNav.parseOutlineChapters(resource("lex_fridman_ep502_description.html"))
        assertEquals(15, chapters.size)
        assertEquals(0L, chapters.first().startMs)
        assertEquals(67_000L, chapters[1].startMs)
        assertEquals(3_806_000L, chapters[6].startMs)
        assertEquals(12_798_000L, chapters.last().startMs)
        assertTrue(chapters.last().startMs > 3 * 60 * 60 * 1000L)
    }
}
