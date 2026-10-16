package com.openpod.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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

    /** Same per-episode flag as the Now Playing heart. */
    fun isFavorite(episode: EpisodeEntity): Boolean = episode.favorite

    /** Pink for the filled heart only. Outlined hearts use [downloadedCheckColor]. */
    const val FAVORITE_HEART_ARGB: Int = 0xFFE91E63.toInt()

    /** Same color as the downloaded check mark. Outlined hearts use this, not pink. */
    @Composable
    fun downloadedCheckColor(): Color = MaterialTheme.colorScheme.primary

    fun favoriteHeartArgb(isFavorite: Boolean, downloadCheckArgb: Int): Int =
        if (isFavorite) FAVORITE_HEART_ARGB else downloadCheckArgb

    @Composable
    fun favoriteHeartColor(isFavorite: Boolean): Color =
        Color(favoriteHeartArgb(isFavorite, downloadedCheckColor().toArgb()))

    /** Status mark beside the download check: filled heart when favorite, outline otherwise. */
    fun favoriteMarkContentDescription(isFavorite: Boolean): String =
        if (isFavorite) "Favorite" else "Not a favorite"

    /** Episode ids to select for the Favorites choice in the current list. */
    fun favoriteEpisodeIds(episodes: List<EpisodeEntity>): Set<Long> =
        episodes.filter { isFavorite(it) }.map { it.id }.toSet()

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
