package com.openpod.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeOverflowMenuTest {
    @Test
    fun availableEpisodeOffersArchiveEpisode() {
        assertEquals("Archive episode", EpisodeOverflowMenu.actionLabel(archived = false))
        assertTrue(EpisodeOverflowMenu.archivesEpisode(archived = false))
        assertEquals("Play next", EpisodeOverflowMenu.playNextLabel)
        assertEquals("Add to Up Next", EpisodeOverflowMenu.upNextLabel)
        assertEquals("Share", EpisodeOverflowMenu.shareLabel)
        assertEquals("Add to favorites", EpisodeOverflowMenu.favoriteActionLabel(favorite = false))
    }

    @Test
    fun favoriteEpisodeOffersRemoveFromFavorites() {
        assertEquals("Remove from favorites", EpisodeOverflowMenu.favoriteActionLabel(favorite = true))
    }

    @Test
    fun archivedEpisodeOffersUnarchive() {
        assertEquals("Unarchive episode", EpisodeOverflowMenu.actionLabel(archived = true))
        assertFalse(EpisodeOverflowMenu.archivesEpisode(archived = true))
    }
}
