package com.openpod.app.download

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.openpod.app.data.db.AppDatabase
import com.openpod.app.data.db.EpisodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class EpisodeDownloader(private val context: Context) {
    private val db = AppDatabase.get(context)

    suspend fun download(episodeId: Long): Result<String> = withContext(Dispatchers.IO) {
        val episode = db.episodes().byId(episodeId) ?: return@withContext Result.failure(IllegalStateException("Missing episode"))
        val podcast = db.podcasts().byId(episode.podcastId) ?: return@withContext Result.failure(IllegalStateException("Missing podcast"))
        val settings = db.settings().get()
        val tree = settings?.downloadTreeUri.orEmpty()
        if (tree.isBlank()) return@withContext Result.failure(IllegalStateException("Choose a download folder in Settings"))

        db.episodes().update(episode.copy(downloadStatus = EpisodeEntity.STATUS_DOWNLOADING))
        try {
            val root = DocumentFile.fromTreeUri(context, Uri.parse(tree))
                ?: return@withContext Result.failure(IllegalStateException("Download folder is not available"))
            val folderName = FileNamer.podcastFolder(podcast)
            val folder = root.findFile(folderName)?.takeIf { it.isDirectory }
                ?: root.createDirectory(folderName)
                ?: return@withContext Result.failure(IllegalStateException("Could not create podcast folder"))
            val name = FileNamer.fileName(settings?.namingPattern ?: "{podcast} - {episode}", podcast, episode)
            folder.findFile(name)?.delete()
            val mime = episode.mimeType.ifBlank { "audio/mpeg" }
            val dest = folder.createFile(mime, name.substringBeforeLast('.'))
                ?: return@withContext Result.failure(IllegalStateException("Could not create episode file"))
            val conn = URL(episode.audioUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 20000
            conn.readTimeout = 60000
            conn.setRequestProperty("User-Agent", "OpenPod/1.0")
            conn.inputStream.use { input ->
                context.contentResolver.openOutputStream(dest.uri)?.use { output ->
                    input.copyTo(output)
                } ?: return@withContext Result.failure(IllegalStateException("Could not write episode file"))
            }
            val uri = dest.uri.toString()
            db.episodes().update(
                episode.copy(
                    localUri = uri,
                    fileName = dest.name ?: name,
                    downloadStatus = EpisodeEntity.STATUS_DONE
                )
            )
            Result.success(uri)
        } catch (e: Exception) {
            db.episodes().update(episode.copy(downloadStatus = EpisodeEntity.STATUS_ERROR))
            Result.failure(e)
        }
    }

    suspend fun deleteLocal(episodeId: Long) = withContext(Dispatchers.IO) {
        val episode = db.episodes().byId(episodeId) ?: return@withContext
        if (episode.localUri.isNotBlank()) {
            try {
                DocumentFile.fromSingleUri(context, Uri.parse(episode.localUri))?.delete()
            } catch (_: Exception) {
            }
        }
        db.episodes().update(
            episode.copy(localUri = "", fileName = "", downloadStatus = EpisodeEntity.STATUS_NONE)
        )
    }
}
