package com.openpod.app.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackSourceTest {
    @Test
    fun downloadedFileWinsOverEnclosure() {
        assertEquals(
            "content://downloads/ep.mp3",
            PlaybackSource.choose("content://downloads/ep.mp3", "https://cdn.example/old.mp3")
        )
    }

    @Test
    fun missingDownloadUsesRemoteUrl() {
        assertEquals(
            "https://cdn.example/fresh.mp3",
            PlaybackSource.choose(null, "https://cdn.example/fresh.mp3")
        )
        assertEquals(
            "https://cdn.example/fresh.mp3",
            PlaybackSource.choose("  ", "  https://cdn.example/fresh.mp3  ")
        )
    }

    @Test
    fun blankSourceStaysBlank() {
        assertEquals("", PlaybackSource.choose(null, " "))
        assertEquals("", PlaybackSource.choose("", null))
    }
}
