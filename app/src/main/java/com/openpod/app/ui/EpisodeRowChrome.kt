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

    /** Played rows in the podcast episode list use muted text. Pass false while that episode is playing. */
    fun usesMutedText(completed: Boolean): Boolean = completed

    fun remainingMs(durationMs: Long, positionMs: Long): Long? {
        if (durationMs <= 0L) return null
        return (durationMs - positionMs.coerceAtLeast(0L)).coerceAtLeast(0L)
    }

    fun isDownloaded(episode: EpisodeEntity): Boolean = episodeHasLocalDownload(episode)

    /** Same per-episode flag as the Now Playing heart. Unfavorited rows draw nothing in this spot. */
    fun showsFavoriteStar(episode: EpisodeEntity): Boolean = episode.favorite

    /** Filled-star tint: gold, as an Android color int. */
    const val FAVORITE_STAR_ARGB: Int = 0xFFFFD700.toInt()

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
