package com.openpod.app.ui

import com.openpod.app.data.db.EpisodeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeRowChromeTest {
    @Test
    fun remainingTimeForInProgress() {
        assertEquals("1:00 left", EpisodeRowChrome.playbackLabel(120_000, 60_000, completed = false))
        assertEquals("In progress", EpisodeRowChrome.playbackLabel(0, 15_000, completed = false))
    }

    @Test
    fun playedListingsUseMutedText() {
        assertTrue(EpisodeRowChrome.usesMutedText(completed = true))
        assertFalse(EpisodeRowChrome.usesMutedText(completed = false))
    }

    @Test
    fun durationForUnplayedAndPlayed() {
        assertEquals("45:00", EpisodeRowChrome.playbackLabel(45 * 60_000L, 0, completed = false))
        assertEquals("Unplayed", EpisodeRowChrome.playbackLabel(0, 0, completed = false))
        assertEquals("Played", EpisodeRowChrome.playbackLabel(45 * 60_000L, 45 * 60_000L, completed = true))
    }

    @Test
    fun downloadedUsesLocalFileOrDoneStatus() {
        assertTrue(
            EpisodeRowChrome.isDownloaded(
                episode(downloadStatus = EpisodeEntity.STATUS_DONE, localUri = "content://1")
            )
        )
        assertTrue(
            EpisodeRowChrome.isDownloaded(
                episode(downloadStatus = EpisodeEntity.STATUS_NONE, localUri = "content://2")
            )
        )
        assertFalse(
            EpisodeRowChrome.isDownloaded(
                episode(downloadStatus = EpisodeEntity.STATUS_NONE)
            )
        )
    }

    @Test
    fun favoriteMarkUsesTheEpisodeFlag() {
        assertTrue(EpisodeRowChrome.isFavorite(episode(downloadStatus = EpisodeEntity.STATUS_NONE, favorite = true)))
        assertFalse(EpisodeRowChrome.isFavorite(episode(downloadStatus = EpisodeEntity.STATUS_DONE, localUri = "content://1")))
        assertEquals("Favorite", EpisodeRowChrome.favoriteMarkContentDescription(true))
        assertEquals("Not a favorite", EpisodeRowChrome.favoriteMarkContentDescription(false))
        assertEquals(0xFFE91E63.toInt(), EpisodeRowChrome.FAVORITE_HEART_ARGB)
        val downloadCheck = 0xFF7FB7C9.toInt()
        assertEquals(downloadCheck, EpisodeRowChrome.favoriteHeartArgb(isFavorite = false, downloadCheckArgb = downloadCheck))
        assertEquals(EpisodeRowChrome.FAVORITE_HEART_ARGB, EpisodeRowChrome.favoriteHeartArgb(isFavorite = true, downloadCheckArgb = downloadCheck))
    }

    @Test
    fun favoritesChoiceSelectsOnlyFavoritedEpisodesInTheCurrentList() {
        val episodes = listOf(
            episode(downloadStatus = EpisodeEntity.STATUS_NONE, favorite = true).copy(id = 1),
            episode(downloadStatus = EpisodeEntity.STATUS_DONE, localUri = "content://1", favorite = false).copy(id = 2),
            episode(downloadStatus = EpisodeEntity.STATUS_DONE, localUri = "content://2", favorite = true).copy(id = 3)
        )
        assertEquals(setOf(1L, 3L), EpisodeRowChrome.favoriteEpisodeIds(episodes))
    }

    @Test
    fun metaIncludesQueueAndRemaining() {
        val line = EpisodeRowChrome.metaParts(
            dateLabel = "Sep 18, 2026",
            episodeNumber = 12,
            playbackLabel = "10:00 left",
            downloadLabel = "Saved",
            queued = true,
            archived = false
        )
        assertEquals("Sep 18, 2026 · Ep 12 · 10:00 left · Saved · In queue", line)
    }

    private fun episode(
        downloadStatus: String,
        localUri: String = "",
        favorite: Boolean = false
    ) = EpisodeEntity(
        id = 1,
        podcastId = 1,
        guid = "guid-1",
        title = "Episode",
        audioUrl = "https://cdn.example.com/1.mp3",
        localUri = localUri,
        downloadStatus = downloadStatus,
        favorite = favorite
    )
}
