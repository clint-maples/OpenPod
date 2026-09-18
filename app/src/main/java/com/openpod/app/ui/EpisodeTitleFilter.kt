package com.openpod.app.ui

import com.openpod.app.data.db.EpisodeEntity

object EpisodeTitleFilter {
    fun filter(episodes: List<EpisodeEntity>, query: String): List<EpisodeEntity> {
        val needle = query.trim()
        if (needle.isEmpty()) return episodes
        return episodes.filter { it.title.contains(needle, ignoreCase = true) }
    }
}
