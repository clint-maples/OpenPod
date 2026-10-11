package com.openpod.app.data

/**
 * Enclosure URLs on many hosts are signed and rotate. A feed refresh should
 * replace a stored URL when the feed has a new one, and keep the stored URL
 * when the feed item has no enclosure.
 */
object EpisodeFeedMerge {
    fun mergedAudioUrl(stored: String, fresh: String): String {
        val next = fresh.trim()
        if (next.isBlank()) return stored
        return next
    }
}
