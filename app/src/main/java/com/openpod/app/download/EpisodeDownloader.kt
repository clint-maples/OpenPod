package com.openpod.app.download

import android.content.Context
import android.net.Uri
import android.os.SystemClock
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
        val episode = db.episodes().byId(episodeId)
            ?: return@withContext Result.failure(IllegalStateException("Missing episode"))
        val podcast = db.podcasts().byId(episode.podcastId)
            ?: return@withContext Result.failure(IllegalStateException("Missing podcast"))
        val settings = db.settings().get()
        val tree = settings?.downloadTreeUri.orEmpty()
        if (tree.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Choose a download folder in Settings"))
        }

        db.episodes().setDownloadStatus(episodeId, EpisodeEntity.STATUS_DOWNLOADING)
        DownloadTracker.start(episodeId)
        var dest: DocumentFile? = null
        var conn: HttpURLConnection? = null
        try {
            val root = DocumentFile.fromTreeUri(context, Uri.parse(tree))
                ?: return@withContext fail(episodeId, dest, IllegalStateException("Download folder is not available"))
            val folderName = FileNamer.podcastFolder(podcast)
            val folder = root.findFile(folderName)?.takeIf { it.isDirectory }
                ?: root.createDirectory(folderName)
                ?: return@withContext fail(episodeId, dest, IllegalStateException("Could not create podcast folder"))
            val name = FileNamer.fileName(settings?.namingPattern ?: "{podcast} - {episode}", podcast, episode)
            folder.findFile(name)?.delete()
            val mime = episode.mimeType.ifBlank { "audio/mpeg" }
            dest = folder.createFile(mime, name.substringBeforeLast('.'))
                ?: return@withContext fail(episodeId, null, IllegalStateException("Could not create episode file"))
            val http = (URL(episode.audioUrl).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 20000
                readTimeout = 60000
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) OpenPod/1.0"
                )
                setRequestProperty("Accept-Encoding", "identity")
            }
            conn = http
            val code = http.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("Download failed (HTTP $code)")
            }
            val total = http.contentLengthLong
            if (total > MAX_DOWNLOAD_BYTES) {
                throw IllegalStateException("Episode file is too large to download")
            }
            DownloadTracker.update(episodeId, 0L, total)
            val output = context.contentResolver.openOutputStream(dest.uri)
                ?: return@withContext fail(episodeId, dest, IllegalStateException("Could not write episode file"))
            val buffer = ByteArray(64 * 1024)
            var read = 0L
            var lastEmit = 0L
            output.use { out ->
                http.inputStream.use { input ->
                    while (true) {
                        val n = input.read(buffer)
                        if (n < 0) break
                        out.write(buffer, 0, n)
                        read += n
                        if (read > MAX_DOWNLOAD_BYTES) {
                            throw IllegalStateException("Episode file is too large to download")
                        }
                        val now = SystemClock.elapsedRealtime()
                        if (now - lastEmit >= 150L) {
                            lastEmit = now
                            DownloadTracker.update(episodeId, read, total)
                        }
                    }
                    out.flush()
                }
            }
            DownloadTracker.update(episodeId, read, if (total > 0L) total else read)
            val uri = dest.uri.toString()
            db.episodes().setDownloadResult(
                episodeId,
                uri,
                dest.name ?: name,
                EpisodeEntity.STATUS_DONE
            )
            Result.success(uri)
        } catch (e: Exception) {
            dest?.delete()
            db.episodes().setDownloadStatus(episodeId, EpisodeEntity.STATUS_ERROR)
            Result.failure(e)
        } finally {
            runCatching { conn?.disconnect() }
            DownloadTracker.clear(episodeId)
        }
    }

    private suspend fun fail(
        episodeId: Long,
        dest: DocumentFile?,
        error: Exception
    ): Result<String> {
        dest?.delete()
        db.episodes().setDownloadStatus(episodeId, EpisodeEntity.STATUS_ERROR)
        DownloadTracker.clear(episodeId)
        return Result.failure(error)
    }

    suspend fun deleteLocal(episodeId: Long) = withContext(Dispatchers.IO) {
        DownloadTracker.clear(episodeId)
        val episode = db.episodes().byId(episodeId) ?: return@withContext
        if (episode.localUri.isNotBlank()) {
            try {
                DocumentFile.fromSingleUri(context, Uri.parse(episode.localUri))?.delete()
            } catch (_: Exception) {
            }
        }
        db.episodes().setDownloadResult(episodeId, "", "", EpisodeEntity.STATUS_NONE)
    }

    companion object {
        const val MAX_DOWNLOAD_BYTES = 2L * 1024 * 1024 * 1024
    }
}
