package com.openpod.app.data

import com.openpod.app.data.db.EpisodeEntity

/**
 * Decides when a finished episode should be archived.
 * Played state is [EpisodeEntity.completed]; archive uses the existing archived flag.
 */
object AutoArchive {
    /**
     * Archive once, when playback first crosses the completed threshold, and only
     * if this show's auto-archive setting is on. Later progress ticks and a manual
     * unarchive of an already-played episode do not archive again.
     */
    fun shouldArchiveOnProgress(
        autoArchive: Boolean,
        completed: Boolean,
        wasCompleted: Boolean
    ): Boolean = autoArchive && completed && !wasCompleted

    /** Episodes already played when auto-archive is turned on. */
    fun backlogIds(episodes: List<EpisodeEntity>): List<Long> =
        episodes.filter { it.completed && !it.archived }.map { it.id }
}
