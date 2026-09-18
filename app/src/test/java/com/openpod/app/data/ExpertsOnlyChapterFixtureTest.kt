package com.openpod.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpertsOnlyChapterFixtureTest {
    private fun resource(name: String): String {
        val stream = checkNotNull(javaClass.classLoader!!.getResourceAsStream(name)) { "missing $name" }
        return stream.bufferedReader().use { it.readText() }
    }

    @Test
    fun expertsOnly063TrailingClocksParse() {
        // Official Experts Only Radio RSS has no podcast:chapters URL.
        // Show notes are "Artist - Track HH:MM:SS" at the end of the line.
        val chapters = ChapterNav.parseOutlineChapters(resource("experts_only_ep063_description.html"))
        assertEquals(17, chapters.size)
        assertEquals(6_000L, chapters[0].startMs)
        assertEquals("Adam Ten & Rafael - Toys", chapters[0].title)
        assertEquals(247_000L, chapters[1].startMs)
        assertEquals("Will Clarke & DJ Funk - Booty Percolatin", chapters[1].title)
        assertEquals(3_146_000L, chapters[14].startMs)
        assertEquals("rSUN - 6am", chapters[14].title)
        assertEquals(3_459_000L, chapters.last().startMs)
        assertEquals("GRiZ & Subtronics - Griztronics", chapters.last().title)
        assertEquals(247_000L, ChapterNav.seekTargetMs(chapters, 10_000L, forward = true))
    }

    @Test
    fun numberedTrailingClockLinesParse() {
        val html = """
            1. Walker & Royce, FENIK - UTOPIA 00:00:07
            2. FENIK ft. Modern Lover - Voices 00:05:53
            Tune in every other Friday
        """.trimIndent()
        val chapters = ChapterNav.parseOutlineChapters(html)
        assertEquals(2, chapters.size)
        assertEquals(7_000L, chapters[0].startMs)
        assertTrue(chapters[0].title.contains("UTOPIA"))
        assertEquals(353_000L, chapters[1].startMs)
    }

    @Test
    fun singleTrailingClockLineIsNotAnOutline() {
        assertEquals(
            emptyList<Chapter>(),
            ChapterNav.parseOutlineChapters("Adam Ten & Rafael - Toys 00:00:06")
        )
    }
}
