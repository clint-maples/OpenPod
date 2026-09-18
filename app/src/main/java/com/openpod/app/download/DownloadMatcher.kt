package com.openpod.app.download

import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.data.db.PodcastEntity
import java.util.Locale

object DownloadMatcher {
    val AUDIO_EXTENSIONS = setOf("mp3", "m4a", "aac", "ogg", "opus", "wav", "mp4", "m4b")

    data class LocalFile(val name: String, val uri: String)

    data class Link(
        val episodeId: Long,
        val uri: String,
        val fileName: String
    )

    fun isAudioFile(name: String): Boolean {
        if (name.startsWith(".")) return false
        return fileExtension(name) in AUDIO_EXTENSIONS
    }

    fun fileExtension(name: String): String {
        return name.substringAfterLast('.', "").substringBefore('?').lowercase(Locale.US)
    }

    fun normalizeKey(value: String): String {
        return FileNamer.sanitize(value)
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    fun findLinks(
        podcast: PodcastEntity,
        episodes: List<EpisodeEntity>,
        files: List<LocalFile>,
        namingPattern: String
    ): List<Link> {
        val remainingFiles = files.filter { isAudioFile(it.name) }.distinctBy { it.uri }.toMutableList()
        val remaining = episodes.distinctBy { it.id }.toMutableList()
        val links = ArrayList<Link>()

        fun claim(episode: EpisodeEntity, file: LocalFile) {
            if (remaining.none { it.id == episode.id }) return
            if (remainingFiles.none { it.uri == file.uri }) return
            remaining.removeAll { it.id == episode.id }
            remainingFiles.removeAll { it.uri == file.uri }
            links += Link(episode.id, file.uri, file.name)
        }

        for (episode in remaining.toList()) {
            val expected = FileNamer.fileName(namingPattern, podcast, episode)
            val match = remainingFiles.find { it.name.equals(expected, ignoreCase = true) } ?: continue
            claim(episode, match)
        }

        for (episode in remaining.toList()) {
            val expectedBase = FileNamer.fileName(namingPattern, podcast, episode).substringBeforeLast('.')
            val matches = remainingFiles.filter {
                it.name.substringBeforeLast('.').equals(expectedBase, ignoreCase = true)
            }
            if (matches.size == 1) claim(episode, matches.first())
        }

        val tokensByEpisode = remaining.associateWith { guidTokens(it.guid) }
        val tokenOwners = HashMap<String, MutableList<Long>>()
        tokensByEpisode.forEach { (episode, tokens) ->
            tokens.forEach { token ->
                tokenOwners.getOrPut(token.lowercase(Locale.US)) { mutableListOf() }.add(episode.id)
            }
        }

        for (episode in remaining.toList()) {
            val tokens = tokensByEpisode[episode].orEmpty()
            val uniqueTokens = tokens.filter { token ->
                tokenOwners[token.lowercase(Locale.US)]?.singleOrNull() == episode.id
            }
            if (uniqueTokens.isEmpty()) continue
            val matches = remainingFiles.filter { file ->
                uniqueTokens.any { token -> file.name.contains(token, ignoreCase = true) }
            }
            if (matches.size == 1) claim(episode, matches.first())
        }

        val podcastKey = normalizeKey(podcast.title)
        val keysByEpisode = remaining.associateWith { titleKeys(it) }
        val keyOwners = HashMap<String, MutableList<Long>>()
        keysByEpisode.forEach { (episode, keys) ->
            keys.forEach { key ->
                keyOwners.getOrPut(key) { mutableListOf() }.add(episode.id)
            }
        }

        for (episode in remaining.toList()) {
            val uniqueKeys = keysByEpisode[episode].orEmpty().filter { key ->
                key != podcastKey && keyOwners[key]?.singleOrNull() == episode.id
            }
            if (uniqueKeys.isEmpty()) continue
            val exact = remainingFiles.filter { file ->
                val fileKey = normalizeKey(file.name.substringBeforeLast('.'))
                uniqueKeys.any { it == fileKey }
            }
            if (exact.size == 1) {
                claim(episode, exact.first())
                continue
            }
            val contained = remainingFiles.filter { file ->
                val fileKey = normalizeKey(file.name.substringBeforeLast('.'))
                uniqueKeys.any { key -> key.length >= 12 && fileKey.contains(key) }
            }
            if (contained.size != 1) continue
            val file = contained.first()
            val fileKey = normalizeKey(file.name.substringBeforeLast('.'))
            val collision = remaining.any { other ->
                other.id != episode.id &&
                    keysByEpisode[other].orEmpty().any { key -> key.length >= 12 && fileKey.contains(key) }
            }
            if (!collision) claim(episode, file)
        }

        return links
    }

    internal fun guidTokens(guid: String): List<String> {
        val raw = guid.trim()
        if (raw.isEmpty()) return emptyList()
        val tokens = LinkedHashSet<String>()
        val short = FileNamer.sanitize(raw.take(12))
        if (isDistinctiveToken(short)) tokens += short
        val lastSeg = raw.substringAfterLast('/').substringBefore('?').substringBefore('#')
        val last = FileNamer.sanitize(lastSeg)
        if (isDistinctiveToken(last)) tokens += last
        return tokens.toList()
    }

    private fun titleKeys(episode: EpisodeEntity): Set<String> {
        val keys = LinkedHashSet<String>()
        val full = normalizeKey(episode.title)
        if (full.length >= 8) keys += full
        val short = normalizeKey(episode.title.take(40))
        if (short.length >= 8) keys += short
        return keys
    }

    private fun isDistinctiveToken(token: String): Boolean {
        if (token.length < 8) return false
        val compact = token.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), "")
        if (compact.length < 6) return false
        if (compact == "https" || compact.startsWith("http")) return false
        return true
    }
}
