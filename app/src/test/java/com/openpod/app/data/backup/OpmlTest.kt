package com.openpod.app.data.backup

import com.openpod.app.data.db.PodcastEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpmlTest {
    private val shows = listOf(
        PodcastEntity(
            id = 1,
            title = "All-In & Friends",
            feedUrl = "https://feeds.example.com/all-in.xml",
            siteUrl = "https://allin.com",
            autoDownload = true,
            autoDelete = false,
            autoArchive = true,
            folderName = "All-In",
            skipIntroSec = 90,
            skipOutroSec = 30
        ),
        PodcastEntity(
            id = 2,
            title = "Lex <Fridman>",
            feedUrl = "https://lex.example.com/rss",
            siteUrl = "",
            autoDownload = false,
            autoDelete = true,
            folderName = "Lex"
        )
    )

    @Test
    fun exportRoundTripsTitleUrlAndOpenPodFlags() {
        val xml = Opml.export(shows, nowMs = 1_779_206_400_000L)
        assertTrue(xml.contains("xmlns:openpod=\"https://openpod.app/opml\""))
        assertTrue(xml.contains("type=\"rss\""))
        assertTrue(xml.contains("xmlUrl=\"https://feeds.example.com/all-in.xml\""))
        assertTrue(xml.contains("openpod:autoDownload=\"true\""))
        assertTrue(xml.contains("openpod:autoDelete=\"true\""))
        assertTrue(xml.contains("openpod:autoArchive=\"true\""))
        assertTrue(xml.contains("openpod:autoArchive=\"false\""))
        assertTrue(xml.contains("openpod:folderName=\"All-In\""))
        assertTrue(xml.contains("openpod:skipIntroSec=\"90\""))
        assertTrue(xml.contains("openpod:skipOutroSec=\"30\""))
        assertTrue(xml.contains("Lex &lt;Fridman&gt;"))

        val parsed = Opml.parse(xml)
        assertEquals(2, parsed.size)
        assertEquals("All-In & Friends", parsed[0].title)
        assertEquals("https://feeds.example.com/all-in.xml", parsed[0].xmlUrl)
        assertEquals("https://allin.com", parsed[0].htmlUrl)
        assertEquals(true, parsed[0].autoDownload)
        assertEquals(false, parsed[0].autoDelete)
        assertEquals(true, parsed[0].autoArchive)
        assertEquals("All-In", parsed[0].folderName)
        assertEquals(90, parsed[0].skipIntroSec)
        assertEquals(30, parsed[0].skipOutroSec)
        assertEquals("Lex <Fridman>", parsed[1].title)
        assertEquals(true, parsed[1].autoDelete)
        assertEquals(false, parsed[1].autoArchive)
        assertNull(parsed[1].skipIntroSec)
        assertNull(parsed[1].skipOutroSec)
    }

    @Test
    fun parseStandardOpmlFromOtherApps() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <body>
                <outline text="News">
                  <outline type="rss" text="Up First" xmlUrl="https://feeds.npr.org/510318/podcast.xml" htmlUrl="https://npr.org" />
                </outline>
                <outline type="rss" title="The Daily" xmlUrl="HTTP://feeds.example.com/daily/" />
                <outline type="link" text="Not a feed" url="https://example.com" />
              </body>
            </opml>
        """.trimIndent()
        val parsed = Opml.parse(xml)
        assertEquals(2, parsed.size)
        assertEquals("Up First", parsed[0].title)
        assertEquals("https://feeds.npr.org/510318/podcast.xml", parsed[0].xmlUrl)
        assertEquals("The Daily", parsed[1].title)
        assertEquals("HTTP://feeds.example.com/daily/", parsed[1].xmlUrl)
        assertNull(parsed[0].autoDownload)
        assertNull(parsed[0].autoArchive)
    }

    @Test
    fun canonicalizeFeedSchemesAndDedupes() {
        val xml = """
            <opml version="2.0"><body>
              <outline text="A" xmlUrl="feed://feeds.example.com/show.xml" />
              <outline text="A again" xmlURL="https://feeds.example.com/show.xml/" />
              <outline text="B" xmlurl="itpc://feeds.example.com/other.xml" />
            </body></opml>
        """.trimIndent()
        val parsed = Opml.parse(xml)
        assertEquals(2, parsed.size)
        assertEquals("https://feeds.example.com/show.xml", parsed[0].xmlUrl)
        assertEquals("https://feeds.example.com/other.xml", parsed[1].xmlUrl)
    }

    @Test
    fun emptyOrInvalidInput() {
        assertTrue(Opml.parse("").isEmpty())
        assertTrue(Opml.parse("<opml version=\"2.0\"><body></body></opml>").isEmpty())
        try {
            Opml.parse("<not-xml")
            throw AssertionError("expected failure")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("not valid OPML"))
        }
    }

    @Test
    fun normalizeFeedUrlIsCaseAndSlashInsensitive() {
        assertEquals(
            "https://feeds.example.com/show.xml",
            Opml.normalizeFeedUrl("HTTPS://Feeds.Example.com/show.xml/")
        )
    }
}
