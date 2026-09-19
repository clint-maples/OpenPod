package com.openpod.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MiniPlayerVisibilityTest {
    @Test
    fun hiddenWhenNothingIsLoaded() {
        assertFalse(MiniPlayerVisibility.shouldShow("library", 0L))
        assertFalse(MiniPlayerVisibility.shouldShow("player", 0L))
    }

    @Test
    fun hiddenOnNowPlayingOnly() {
        assertFalse(MiniPlayerVisibility.shouldShow("player", 9L))
    }

    @Test
    fun shownOnEveryOtherTabAndDetailRoute() {
        assertTrue(MiniPlayerVisibility.shouldShow("library", 9L))
        assertTrue(MiniPlayerVisibility.shouldShow("discover", 9L))
        assertTrue(MiniPlayerVisibility.shouldShow("playlists", 9L))
        assertTrue(MiniPlayerVisibility.shouldShow("playlist/{id}", 9L))
        assertTrue(MiniPlayerVisibility.shouldShow("podcast/{id}", 9L))
        assertTrue(MiniPlayerVisibility.shouldShow("podcast/{id}/settings", 9L))
        assertTrue(MiniPlayerVisibility.shouldShow("episode/{id}", 9L))
        assertTrue(MiniPlayerVisibility.shouldShow("settings", 9L))
        assertTrue(MiniPlayerVisibility.shouldShow("eq", 9L))
        assertTrue(MiniPlayerVisibility.shouldShow("upnext", 9L))
    }
}
