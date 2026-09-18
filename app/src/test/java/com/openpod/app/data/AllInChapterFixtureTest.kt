package com.openpod.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AllInChapterFixtureTest {
    private fun resource(name: String): String {
        val stream = checkNotNull(javaClass.classLoader!!.getResourceAsStream(name)) { "missing $name" }
        return stream.bufferedReader().use { it.readText() }
    }

    @Test
    fun allInDescriptionOutlineWithoutDashParses() {
        // Official All-In Libsyn item has no podcast:chapters URL. Outline
        // lines are <p>(0:00) Title</p> with no dash after the clock.
        val chapters = ChapterNav.parseOutlineChapters(resource("all_in_latest_description.html"))
        assertEquals(6, chapters.size)
        assertEquals(0L, chapters[0].startMs)
        assertEquals("Dina Powell McCormick joins The Besties!", chapters[0].title)
        assertEquals(112_000L, chapters[1].startMs)
        assertTrue(chapters[1].title.startsWith("Impact of Meta's data center"))
        assertEquals(712_000L, chapters[2].startMs)
        assertTrue(chapters[2].title.startsWith("How Meta is alleviating data center concerns"))
        assertEquals(1_197_000L, chapters[3].startMs)
        assertEquals(1_618_000L, chapters[4].startMs)
        assertEquals(2_125_000L, chapters[5].startMs)
        assertEquals(112_000L, ChapterNav.seekTargetMs(chapters, 10_000L, forward = true))
    }

    @Test
    fun singleTimestampLineIsNotAnOutline() {
        val html = "<p>(0:00) We start here.</p>"
        assertEquals(emptyList<Chapter>(), ChapterNav.parseOutlineChapters(html))
    }
}
