package com.openpod.app.download

import com.openpod.app.data.db.EpisodeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeDownloadActionTest {
    @Test
    fun notDownloadedShowsDownload() {
        val episode = episode(downloadStatus = EpisodeEntity.STATUS_NONE)
        assertFalse(episodeHasLocalDownload(episode))
        assertEquals(EpisodeDownloadAction.DOWNLOAD, episodeDownloadAction(episode, downloading = false))
    }

    @Test
    fun doneStatusShowsDelete() {
        val episode = episode(downloadStatus = EpisodeEntity.STATUS_DONE, localUri = "content://file/1")
        assertTrue(episodeHasLocalDownload(episode))
        assertEquals(EpisodeDownloadAction.DELETE, episodeDownloadAction(episode, downloading = false))
    }

    @Test
    fun localUriAloneShowsDelete() {
        val episode = episode(downloadStatus = EpisodeEntity.STATUS_NONE, localUri = "content://file/2")
        assertTrue(episodeHasLocalDownload(episode))
        assertEquals(EpisodeDownloadAction.DELETE, episodeDownloadAction(episode, downloading = false))
    }

    @Test
    fun failedDownloadShowsDownload() {
        val episode = episode(downloadStatus = EpisodeEntity.STATUS_ERROR)
        assertEquals(EpisodeDownloadAction.DOWNLOAD, episodeDownloadAction(episode, downloading = false))
    }

    @Test
    fun inProgressKeepsProgressControl() {
        val episode = episode(downloadStatus = EpisodeEntity.STATUS_DOWNLOADING)
        assertEquals(EpisodeDownloadAction.IN_PROGRESS, episodeDownloadAction(episode, downloading = true))
    }

    private fun episode(
        downloadStatus: String,
        localUri: String = ""
    ) = EpisodeEntity(
        id = 1,
        podcastId = 1,
        guid = "guid-1",
        title = "Episode",
        audioUrl = "https://cdn.example.com/1.mp3",
        localUri = localUri,
        downloadStatus = downloadStatus
    )
}
