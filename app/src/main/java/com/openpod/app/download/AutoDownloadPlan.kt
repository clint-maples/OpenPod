package com.openpod.app.download

import com.openpod.app.data.db.EpisodeEntity

/**
 * Which episodes an auto-download sweep should enqueue.
 *
 * New episodes are stored as [EpisodeEntity.STATUS_QUEUED] when the show
 * auto-downloads. Historical episodes left at [EpisodeEntity.STATUS_NONE]
 * are not pulled in later. A download that is already running and still
 * reporting bytes is left alone. One that is marked in progress with no
 * fresh bytes, or that failed on an auto-download show, is started again.
 */
object AutoDownloadPlan {
    const val STALE_PROGRESS_MS = 10L * 60L * 1000L

    fun shouldEnqueue(
        episode: EpisodeEntity,
        podcastAutoDownload: Boolean,
        hasActiveWork: Boolean,
        progressFresh: Boolean
    ): Boolean {
        if (isSaved(episode)) return false
        if (hasActiveWork && progressFresh) return false
        return when (episode.downloadStatus) {
            EpisodeEntity.STATUS_QUEUED,
            EpisodeEntity.STATUS_DOWNLOADING -> true
            EpisodeEntity.STATUS_ERROR -> podcastAutoDownload
            else -> false
        }
    }

    /** Cancel a worker that is still marked active but has stopped reporting bytes. */
    fun replaceExisting(hasActiveWork: Boolean, progressFresh: Boolean): Boolean =
        hasActiveWork && !progressFresh

    fun isSaved(episode: EpisodeEntity): Boolean =
        episode.downloadStatus == EpisodeEntity.STATUS_DONE && episode.localUri.isNotBlank()
}
