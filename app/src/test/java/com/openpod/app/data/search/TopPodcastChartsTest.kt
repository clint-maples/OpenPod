package com.openpod.app.data.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TopPodcastChartsTest {
    @Test
    fun marketingFeedReadsAppleIds() {
        val body = """
            {"feed":{"results":[
              {"id":"1200361736","name":"The Daily","artistName":"The New York Times","artworkUrl100":"https://example/100.png"},
              {"id":"","name":"Missing","artistName":"Nobody","artworkUrl100":""}
            ]}}
        """.trimIndent()
        val shows = TopPodcastParser.parseMarketingFeed(body)
        assertEquals(1, shows.size)
        assertEquals("1200361736", shows[0].appleId)
        assertEquals("The Daily", shows[0].name)
        assertEquals("The New York Times", shows[0].artistName)
    }

    @Test
    fun legacyGenreFeedReadsImIdAndLargestArt() {
        val body = """
            {"feed":{"entry":{
              "im:name":{"label":"The Joe Rogan Experience"},
              "im:artist":{"label":"Joe Rogan"},
              "im:image":[
                {"label":"https://example/55.png","attributes":{"height":"55"}},
                {"label":"https://example/170.png","attributes":{"height":"170"}}
              ],
              "id":{"attributes":{"im:id":"360084272"}}
            }}}
        """.trimIndent()
        val shows = TopPodcastParser.parseLegacyGenreFeed(body)
        assertEquals("360084272", shows.single().appleId)
        assertEquals("https://example/170.png", shows.single().artworkUrl)
        assertEquals("Joe Rogan", shows.single().artistName)
    }

    @Test
    fun lookupBodyReturnsFeedUrl() {
        val body = """{"resultCount":1,"results":[{"feedUrl":"https://feeds.example/daily"}]}"""
        assertEquals("https://feeds.example/daily", ItunesSearch.parseLookupFeedUrl(body))
        assertEquals("", ItunesSearch.parseLookupFeedUrl("""{"results":[]}"""))
    }

    @Test
    fun cacheServesFreshThenExpiresAndKeepsStale() {
        val dir = File.createTempFile("charts", "dir")
        dir.delete()
        dir.mkdirs()
        val cache = ChartCache(dir, ttlMs = 1_000L)
        val shows = listOf(ChartShow("1", "Show", "Host", "https://example/a.png"))
        cache.write("all", shows, nowMs = 5_000L)
        assertEquals(shows, cache.fresh("all", nowMs = 5_500L))
        assertNull(cache.fresh("all", nowMs = 7_000L))
        assertEquals(shows, cache.stale("all"))
        val reloaded = ChartCache(dir, ttlMs = 1_000L)
        assertEquals(shows, reloaded.stale("all"))
    }

    @Test
    fun genreMissWithoutCacheFallsBackAndOfflineKeepsStale() {
        assertEquals(ChartMiss.HideGenresAndShowAll, ChartLoadPolicy.onMiss(genreSelected = true, hasStaleCache = false))
        assertEquals(ChartMiss.ServeStale, ChartLoadPolicy.onMiss(genreSelected = true, hasStaleCache = true))
        assertEquals(ChartMiss.Retry, ChartLoadPolicy.onMiss(genreSelected = false, hasStaleCache = false))
    }

    @Test
    fun loaderUsesCacheInsideTtlAndStaleAfterFailure() {
        val dir = File.createTempFile("charts2", "dir")
        dir.delete()
        dir.mkdirs()
        var calls = 0
        val charts = TopPodcastCharts(ChartCache(dir, ttlMs = 1_000L)) {
            calls++
            if (calls == 1) {
                """{"feed":{"results":[{"id":"9","name":"One","artistName":"A","artworkUrl100":""}]}}"""
            } else {
                error("offline")
            }
        }
        assertEquals("One", charts.load(null, nowMs = 0L)!!.single().name)
        assertEquals(1, calls)
        assertEquals("One", charts.load(null, nowMs = 100L)!!.single().name)
        assertEquals(1, calls)
        assertEquals("One", charts.load(null, nowMs = 5_000L)!!.single().name)
        assertEquals(2, calls)
        assertTrue(charts.hasStale(null))
    }
}
