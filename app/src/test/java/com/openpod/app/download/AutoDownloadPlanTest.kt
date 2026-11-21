package com.openpod.app.download

import com.openpod.app.data.db.EpisodeEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoDownloadPlanTest {
    @Test
    fun queuedEpisodeIsEnqueuedWhenNothingIsRunning() {
        assertTrue(
            AutoDownloadPlan.shouldEnqueue(
                episode(EpisodeEntity.STATUS_QUEUED),
                podcastAutoDownload = false,
                hasActiveWork = false,
                progressFresh = false
            )
        )
    }

    @Test
    fun freshActiveDownloadIsLeftAlone() {
        assertFalse(
            AutoDownloadPlan.shouldEnqueue(
                episode(EpisodeEntity.STATUS_DOWNLOADING),
                podcastAutoDownload = true,
                hasActiveWork = true,
                progressFresh = true
            )
        )
        assertFalse(AutoDownloadPlan.replaceExisting(hasActiveWork = true, progressFresh = true))
    }

    @Test
    fun staleInProgressDownloadIsReplaced() {
        assertTrue(
            AutoDownloadPlan.shouldEnqueue(
                episode(EpisodeEntity.STATUS_DOWNLOADING),
                podcastAutoDownload = true,
                hasActiveWork = true,
                progressFresh = false
            )
        )
        assertTrue(AutoDownloadPlan.replaceExisting(hasActiveWork = true, progressFresh = false))
    }

    @Test
    fun autoDownloadRetriesErrorsButLeavesTheBackCatalog() {
        assertTrue(
            AutoDownloadPlan.shouldEnqueue(
                episode(EpisodeEntity.STATUS_ERROR),
                podcastAutoDownload = true,
                hasActiveWork = false,
                progressFresh = false
            )
        )
        assertFalse(
            AutoDownloadPlan.shouldEnqueue(
                episode(EpisodeEntity.STATUS_ERROR),
                podcastAutoDownload = false,
                hasActiveWork = false,
                progressFresh = false
            )
        )
        assertFalse(
            AutoDownloadPlan.shouldEnqueue(
                episode(EpisodeEntity.STATUS_NONE),
                podcastAutoDownload = true,
                hasActiveWork = false,
                progressFresh = false
            )
        )
    }

    @Test
    fun savedEpisodeIsNotDownloadedAgain() {
        assertFalse(
            AutoDownloadPlan.shouldEnqueue(
                episode(EpisodeEntity.STATUS_DONE, localUri = "content://saved"),
                podcastAutoDownload = true,
                hasActiveWork = false,
                progressFresh = false
            )
        )
    }

    private fun episode(status: String, localUri: String = "") = EpisodeEntity(
        id = 4,
        podcastId = 1,
        guid = "g",
        title = "Episode",
        audioUrl = "https://cdn.example/a.mp3",
        localUri = localUri,
        downloadStatus = status
    )
}
