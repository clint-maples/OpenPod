package com.openpod.app.share

import com.openpod.app.data.db.EpisodeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeShareTest {
    @Test
    fun textIncludesPodcastEpisodeAndEnclosure() {
        val episode = episode(
            title = "E123: Markets",
            audioUrl = "https://cdn.example.com/e123.mp3",
            guid = "guid-e123"
        )
        val text = EpisodeShare.plainText("All-In", episode)
        assertEquals(
            "All-In\nE123: Markets\nhttps://cdn.example.com/e123.mp3",
            text
        )
        assertFalse(text.contains("openpod://"))
        assertFalse(text.contains("com.openpod"))
    }

    @Test
    fun permalinkFromStoredFieldWhenDistinctFromAudio() {
        val episode = episode(
            permalink = "https://example.com/e123",
            audioUrl = "https://cdn.example.com/e123.mp3",
            guid = "https://cdn.example.com/e123.mp3"
        )
        assertEquals("https://example.com/e123", EpisodeShare.permalink(episode))
        val text = EpisodeShare.plainText("Show", episode)
        assertTrue(text.contains("https://example.com/e123"))
        assertTrue(text.contains("https://cdn.example.com/e123.mp3"))
    }

    @Test
    fun permalinkFallsBackToHttpGuid() {
        val episode = episode(
            guid = "https://example.com/posts/e123",
            audioUrl = "https://cdn.example.com/e123.mp3"
        )
        assertEquals("https://example.com/posts/e123", EpisodeShare.permalink(episode))
    }

    @Test
    fun enclosureGuidIsNotRepeatedAsPermalink() {
        val episode = episode(
            guid = "https://cdn.example.com/e123.mp3",
            audioUrl = "https://cdn.example.com/e123.mp3"
        )
        assertNull(EpisodeShare.permalink(episode))
        assertEquals(
            "Show\nTitle\nhttps://cdn.example.com/e123.mp3",
            EpisodeShare.plainText("Show", episode)
        )
    }

    @Test
    fun htmlEscapesTitlesAndIncludesLinks() {
        val episode = episode(
            title = "A <B> & C",
            permalink = "https://example.com/a",
            audioUrl = "https://cdn.example.com/a.mp3"
        )
        val html = EpisodeShare.htmlText("Pod & Co", episode)
        assertTrue(html.contains("Pod &amp; Co"))
        assertTrue(html.contains("A &lt;B&gt; &amp; C"))
        assertTrue(html.contains("href=\"https://example.com/a\""))
        assertTrue(html.contains("href=\"https://cdn.example.com/a.mp3\""))
    }

    @Test
    fun subjectJoinsPodcastAndEpisode() {
        assertEquals(
            "All-In — E123: Markets",
            EpisodeShare.subject("All-In", episode(title = "E123: Markets"))
        )
    }

    private fun episode(
        title: String = "Title",
        audioUrl: String = "https://cdn.example.com/e.mp3",
        guid: String = "guid",
        permalink: String = "",
        localUri: String = ""
    ) = EpisodeEntity(
        id = 1,
        podcastId = 1,
        guid = guid,
        title = title,
        audioUrl = audioUrl,
        permalink = permalink,
        localUri = localUri
    )
}
