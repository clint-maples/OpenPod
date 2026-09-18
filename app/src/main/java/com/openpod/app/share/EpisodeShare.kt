package com.openpod.app.share

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.download.episodeHasLocalDownload

/**
 * Interoperable episode share: Android Sharesheet with podcast title, episode
 * title, public enclosure URL, and permalink when we have one. Not an OpenPod
 * deep link.
 */
object EpisodeShare {
    fun looksLikeHttpUrl(raw: String): Boolean {
        val text = raw.trim()
        return text.startsWith("http://", ignoreCase = true) ||
            text.startsWith("https://", ignoreCase = true)
    }

    fun permalink(episode: EpisodeEntity): String? {
        val stored = episode.permalink.trim()
        if (looksLikeHttpUrl(stored) && !sameUrl(stored, episode.audioUrl)) return stored
        val guid = episode.guid.trim()
        if (looksLikeHttpUrl(guid) && !sameUrl(guid, episode.audioUrl)) return guid
        return null
    }

    fun plainText(podcastTitle: String, episode: EpisodeEntity): String {
        return buildList {
            add(podcastTitle.trim())
            add(episode.title.trim())
            permalink(episode)?.let { add(it) }
            add(episode.audioUrl.trim())
        }.filter { it.isNotBlank() }.joinToString("\n")
    }

    fun htmlText(podcastTitle: String, episode: EpisodeEntity): String {
        val podcast = escapeHtml(podcastTitle.trim())
        val title = escapeHtml(episode.title.trim())
        val audio = episode.audioUrl.trim()
        val link = permalink(episode)
        return buildString {
            append("<p><strong>$podcast</strong><br>$title</p>")
            if (link != null) {
                val href = escapeHtml(link)
                append("<p><a href=\"$href\">$href</a></p>")
            }
            if (audio.isNotBlank()) {
                val href = escapeHtml(audio)
                append("<p><a href=\"$href\">$href</a></p>")
            }
        }
    }

    fun subject(podcastTitle: String, episode: EpisodeEntity): String {
        val podcast = podcastTitle.trim()
        val title = episode.title.trim()
        return when {
            podcast.isNotBlank() && title.isNotBlank() -> "$podcast — $title"
            title.isNotBlank() -> title
            else -> podcast
        }
    }

    fun sendIntent(podcastTitle: String, episode: EpisodeEntity): Intent {
        val text = plainText(podcastTitle, episode)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_HTML_TEXT, htmlText(podcastTitle, episode))
            val heading = subject(podcastTitle, episode)
            if (heading.isNotBlank()) putExtra(Intent.EXTRA_SUBJECT, heading)
        }
        val local = episode.localUri.trim()
        if (episodeHasLocalDownload(episode) && local.startsWith("content:", ignoreCase = true)) {
            val uri = Uri.parse(local)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.clipData = ClipData.newRawUri(episode.title.ifBlank { "episode" }, uri)
        }
        return intent
    }

    fun chooserIntent(podcastTitle: String, episode: EpisodeEntity): Intent {
        return Intent.createChooser(sendIntent(podcastTitle, episode), "Share episode").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun sameUrl(a: String, b: String): Boolean =
        a.trim().equals(b.trim(), ignoreCase = true)

    private fun escapeHtml(raw: String): String {
        return raw
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
}
