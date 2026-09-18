package com.openpod.app.ui

import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.download.episodeHasLocalDownload

object EpisodeRowChrome {
    fun playbackLabel(
        durationMs: Long,
        positionMs: Long,
        completed: Boolean
    ): String {
        if (completed) return "Played"
        val remaining = remainingMs(durationMs, positionMs)
        return when {
            positionMs > 0L && remaining != null -> "${formatMs(remaining)} left"
            positionMs > 0L -> "In progress"
            remaining != null -> formatMs(remaining)
            else -> "Unplayed"
        }
    }

    fun remainingMs(durationMs: Long, positionMs: Long): Long? {
        if (durationMs <= 0L) return null
        return (durationMs - positionMs.coerceAtLeast(0L)).coerceAtLeast(0L)
    }

    fun isDownloaded(episode: EpisodeEntity): Boolean = episodeHasLocalDownload(episode)

    fun metaParts(
        dateLabel: String,
        episodeNumber: Int?,
        playbackLabel: String,
        downloadLabel: String?,
        queued: Boolean,
        archived: Boolean
    ): String {
        return listOfNotNull(
            dateLabel.ifBlank { null },
            episodeNumber?.let { "Ep $it" },
            playbackLabel,
            downloadLabel,
            if (queued) "In queue" else null,
            if (archived) "Archived" else null
        ).joinToString(" · ")
    }
}
