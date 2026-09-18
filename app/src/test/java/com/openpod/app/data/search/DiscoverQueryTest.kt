package com.openpod.app.data.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoverQueryTest {
    @Test
    fun searchTermsAreNotUrls() {
        assertFalse(DiscoverQuery.looksLikeFeedUrl("All-In"))
        assertFalse(DiscoverQuery.looksLikeFeedUrl("Lex Fridman"))
        assertFalse(DiscoverQuery.looksLikeFeedUrl("experts only radio"))
        assertFalse(DiscoverQuery.looksLikeFeedUrl(""))
        assertFalse(DiscoverQuery.looksLikeFeedUrl("   "))
    }

    @Test
    fun httpsFeedIsAUrl() {
        val url = "https://feeds.megaphone.fm/all-in"
        assertTrue(DiscoverQuery.looksLikeFeedUrl(url))
        assertEquals(url, DiscoverQuery.normalizeFeedUrl("  $url  "))
    }

    @Test
    fun httpAndFeedSchemesNormalize() {
        assertTrue(DiscoverQuery.looksLikeFeedUrl("http://example.com/rss.xml"))
        assertTrue(DiscoverQuery.looksLikeFeedUrl("feed://example.com/podcast.xml"))
        assertEquals(
            "https://example.com/podcast.xml",
            DiscoverQuery.normalizeFeedUrl("feed://example.com/podcast.xml")
        )
    }

    @Test
    fun schemelessHostWithPathIsAUrl() {
        assertTrue(DiscoverQuery.looksLikeFeedUrl("feeds.libsyn.com/all-in/rss"))
        assertEquals(
            "https://feeds.libsyn.com/all-in/rss",
            DiscoverQuery.normalizeFeedUrl("feeds.libsyn.com/all-in/rss")
        )
    }

    @Test
    fun angleBracketPasteIsAccepted() {
        val url = "https://example.com/feed.xml"
        assertTrue(DiscoverQuery.looksLikeFeedUrl("<$url>"))
        assertEquals(url, DiscoverQuery.normalizeFeedUrl("<$url>"))
    }
}
