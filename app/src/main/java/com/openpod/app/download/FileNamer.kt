package com.openpod.app.download

import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.data.db.PodcastEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileNamer {
    fun fileName(pattern: String, podcast: PodcastEntity, episode: EpisodeEntity): String {
        val episodeToken = episode.episodeNumber?.toString()
            ?: episode.title.take(40)
        val date = if (episode.pubDate > 0) {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(episode.pubDate))
        } else ""
        val raw = pattern
            .replace("{podcast}", podcast.title)
            .replace("{title}", episode.title)
            .replace("{episode}", episodeToken)
            .replace("{season}", episode.seasonNumber?.toString().orEmpty())
            .replace("{date}", date)
            .replace("{guid}", episode.guid.take(12))
        val base = sanitize(raw).ifBlank { sanitize(episode.title).ifBlank { "episode" } }
        val ext = extensionFor(episode.mimeType, episode.audioUrl)
        return "$base.$ext"
    }

    fun podcastFolder(podcast: PodcastEntity): String {
        return sanitize(podcast.folderName.ifBlank { podcast.title }).ifBlank { "Podcast" }
    }

    private fun extensionFor(mime: String, url: String): String {
        val fromUrl = url.substringAfterLast('.', "").substringBefore('?').lowercase()
        if (fromUrl in setOf("mp3", "m4a", "aac", "ogg", "opus", "wav", "mp4")) return fromUrl
        return when {
            mime.contains("mp4") || mime.contains("m4a") || mime.contains("aac") -> "m4a"
            mime.contains("ogg") -> "ogg"
            mime.contains("opus") -> "opus"
            else -> "mp3"
        }
    }

    fun sanitize(input: String): String {
        return input.replace(Regex("[\\\\/:*?\"<>|]"), "-")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(120)
    }
}
