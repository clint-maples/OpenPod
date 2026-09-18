package com.openpod.app.download

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.data.db.PodcastEntity
import com.openpod.app.data.db.SettingsEntity

data class DownloadScanResult(
    val linked: Int,
    val alreadyLinked: Int = 0,
    val folderMissing: Boolean = false,
    val folderUnset: Boolean = false
)

class DownloadFolderScanner(private val context: Context) {
    fun scan(
        podcast: PodcastEntity,
        episodes: List<EpisodeEntity>,
        settings: SettingsEntity
    ): Pair<DownloadScanResult, List<DownloadMatcher.Link>> {
        if (settings.downloadTreeUri.isBlank()) {
            return DownloadScanResult(linked = 0, folderUnset = true) to emptyList()
        }
        val root = DocumentFile.fromTreeUri(context, Uri.parse(settings.downloadTreeUri))
            ?: return DownloadScanResult(linked = 0, folderMissing = true) to emptyList()
        val folder = findPodcastFolder(root, podcast)
            ?: return DownloadScanResult(linked = 0, folderMissing = true) to emptyList()

        val files = folder.listFiles().mapNotNull { doc ->
            val name = doc.name ?: return@mapNotNull null
            if (!doc.isFile || !DownloadMatcher.isAudioFile(name)) return@mapNotNull null
            val length = doc.length()
            if (length in 1 until MIN_AUDIO_BYTES) return@mapNotNull null
            DownloadMatcher.LocalFile(name, doc.uri.toString())
        }

        val already = episodes.filter { episode ->
            episode.downloadStatus == EpisodeEntity.STATUS_DONE &&
                episode.localUri.isNotBlank() &&
                files.any { file ->
                    file.uri == episode.localUri ||
                        (episode.fileName.isNotBlank() && file.name.equals(episode.fileName, ignoreCase = true))
                }
        }
        val claimedUris = already.map { it.localUri }.toSet()
        val claimedNames = already.map { it.fileName }.filter { it.isNotBlank() }.map { it.lowercase() }.toSet()
        val available = files.filter { file ->
            file.uri !in claimedUris && file.name.lowercase() !in claimedNames
        }
        val toMatch = episodes.filter { episode -> already.none { it.id == episode.id } }
        val links = DownloadMatcher.findLinks(podcast, toMatch, available, settings.namingPattern)
        return DownloadScanResult(linked = links.size, alreadyLinked = already.size) to links
    }

    private fun findPodcastFolder(root: DocumentFile, podcast: PodcastEntity): DocumentFile? {
        val wanted = listOf(
            FileNamer.podcastFolder(podcast),
            FileNamer.sanitize(podcast.folderName),
            FileNamer.sanitize(podcast.title)
        ).map { it.trim() }.filter { it.isNotBlank() }.distinct()

        for (name in wanted) {
            root.findFile(name)?.takeIf { it.isDirectory }?.let { return it }
        }
        val dirs = root.listFiles().filter { it.isDirectory && !it.name.isNullOrBlank() }
        for (name in wanted) {
            dirs.find { it.name.equals(name, ignoreCase = true) }?.let { return it }
        }
        return null
    }

    companion object {
        const val MIN_AUDIO_BYTES = 16_384L
    }
}
