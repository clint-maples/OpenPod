package com.openpod.app.data

import android.content.Context
import android.net.Uri
import com.openpod.app.data.db.AppDatabase
import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.data.db.PlaylistEntity
import com.openpod.app.data.db.PlaylistItemEntity
import com.openpod.app.data.db.PodcastEntity
import com.openpod.app.data.db.SettingsEntity
import com.openpod.app.data.rss.RssParser
import com.openpod.app.data.search.ItunesSearch
import com.openpod.app.download.DownloadTracker
import com.openpod.app.download.EpisodeDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class Chapter(val startMs: Long, val title: String)
data class TranscriptLine(val startMs: Long?, val text: String)

class PodRepository(context: Context) {
    private val app = context.applicationContext
    val db = AppDatabase.get(app)
    private val downloader = EpisodeDownloader(app)

    fun podcasts() = db.podcasts().observeAll()
    fun episodes(podcastId: Long) = db.episodes().observeForPodcast(podcastId)

    fun episodes(podcastId: Long, showArchived: Boolean) =
        db.episodes().observeForPodcastFiltered(podcastId, showArchived)
    fun episode(id: Long) = db.episodes().observeById(id)
    fun playlists() = db.playlists().observeAll()
    fun playlistItems(id: Long) = db.playlists().observeItems(id)
    fun settings(): Flow<SettingsEntity> = db.settings().observe().map { it ?: SettingsEntity() }
    fun downloaded() = db.episodes().observeDownloaded()

    suspend fun settingsOnce(): SettingsEntity {
        val existing = db.settings().get()
        if (existing != null) return existing
        val created = SettingsEntity()
        db.settings().upsert(created)
        return created
    }

    suspend fun updateSettings(block: (SettingsEntity) -> SettingsEntity) {
        db.settings().upsert(block(settingsOnce()))
    }

    suspend fun search(query: String) = withContext(Dispatchers.IO) { ItunesSearch.search(query) }

    suspend fun subscribe(feedUrl: String): PodcastEntity = withContext(Dispatchers.IO) {
        val existing = db.podcasts().byFeed(feedUrl)
        if (existing != null) {
            refreshPodcast(existing.id)
            return@withContext db.podcasts().byId(existing.id)!!
        }
        val parsed = RssParser.parse(Http.get(feedUrl))
        val defaults = settingsOnce()
        val id = db.podcasts().insert(
            PodcastEntity(
                title = parsed.title,
                feedUrl = feedUrl,
                siteUrl = parsed.siteUrl,
                author = parsed.author,
                description = parsed.description,
                imageUrl = parsed.imageUrl,
                autoDownload = defaults.autoDownloadDefault,
                autoDelete = defaults.autoDeleteDefault,
                folderName = parsed.title
            )
        )
        upsertEpisodes(id, parsed)
        if (defaults.autoDownloadDefault) {
            db.episodes().forPodcast(id).firstOrNull()?.let { downloader.download(it.id) }
        }
        db.podcasts().byId(id)!!
    }

    suspend fun refreshPodcast(podcastId: Long) = withContext(Dispatchers.IO) {
        val podcast = db.podcasts().byId(podcastId) ?: return@withContext
        val parsed = RssParser.parse(Http.get(podcast.feedUrl))
        db.podcasts().update(
            podcast.copy(
                title = parsed.title.ifBlank { podcast.title },
                author = parsed.author.ifBlank { podcast.author },
                description = parsed.description.ifBlank { podcast.description },
                imageUrl = parsed.imageUrl.ifBlank { podcast.imageUrl },
                siteUrl = parsed.siteUrl.ifBlank { podcast.siteUrl }
            )
        )
        val before = db.episodes().forPodcast(podcastId).map { it.guid }.toSet()
        upsertEpisodes(podcastId, parsed)
        val refreshed = db.podcasts().byId(podcastId) ?: return@withContext
        if (refreshed.autoDownload) {
            db.episodes().forPodcast(podcastId)
                .filter { it.guid !in before && it.downloadStatus == EpisodeEntity.STATUS_NONE }
                .forEach { downloader.download(it.id) }
        }
    }

    suspend fun refreshAll() = withContext(Dispatchers.IO) {
        db.podcasts().all().forEach { refreshPodcast(it.id) }
        autoDeleteCompleted()
    }

    private suspend fun upsertEpisodes(podcastId: Long, parsed: com.openpod.app.data.rss.ParsedPodcast) {
        parsed.episodes.forEach { item ->
            db.episodes().insert(
                EpisodeEntity(
                    podcastId = podcastId,
                    guid = item.guid,
                    title = item.title,
                    description = item.description,
                    audioUrl = item.audioUrl,
                    mimeType = item.mimeType,
                    pubDate = item.pubDate,
                    durationMs = item.durationMs,
                    episodeNumber = item.episodeNumber,
                    seasonNumber = item.seasonNumber,
                    imageUrl = item.imageUrl,
                    transcriptUrl = item.transcriptUrl,
                    transcriptType = item.transcriptType,
                    chaptersUrl = item.chaptersUrl
                )
            )
        }
    }

    suspend fun setPodcastFlags(id: Long, autoDownload: Boolean? = null, autoDelete: Boolean? = null) {
        val podcast = db.podcasts().byId(id) ?: return
        db.podcasts().update(
            podcast.copy(
                autoDownload = autoDownload ?: podcast.autoDownload,
                autoDelete = autoDelete ?: podcast.autoDelete
            )
        )
    }

    suspend fun unsubscribe(id: Long) {
        val podcast = db.podcasts().byId(id) ?: return
        db.episodes().forPodcast(id).forEach { downloader.deleteLocal(it.id) }
        db.podcasts().delete(podcast)
    }

    suspend fun download(episodeId: Long) = downloader.download(episodeId)
    suspend fun deleteDownload(episodeId: Long) = downloader.deleteLocal(episodeId)
    val downloadProgress = DownloadTracker.progress

    suspend fun markProgress(episodeId: Long, positionMs: Long, durationMs: Long) {
        val episode = db.episodes().byId(episodeId) ?: return
        val completed = durationMs > 0 && positionMs >= durationMs - 5000
        db.episodes().updateProgress(episodeId, positionMs, completed)
        if (completed) {
            val podcast = db.podcasts().byId(episode.podcastId)
            if (podcast?.autoDelete == true) downloader.deleteLocal(episodeId)
        }
    }

    suspend fun setLastEpisode(episodeId: Long) {
        updateSettings { it.copy(lastEpisodeId = episodeId) }
    }

    suspend fun autoDeleteCompleted() {
        db.podcasts().all().filter { it.autoDelete }.forEach { podcast ->
            db.episodes().forPodcast(podcast.id)
                .filter { it.completed && it.localUri.isNotBlank() }
                .forEach { downloader.deleteLocal(it.id) }
        }
    }

    suspend fun createPlaylist(
        name: String,
        isSmart: Boolean = false,
        playFilter: String = PlaylistEntity.PLAY_ALL,
        downloadFilter: String = PlaylistEntity.DOWNLOAD_ALL
    ): Long = db.playlists().insert(
        PlaylistEntity(
            name = name,
            isSmart = isSmart,
            playFilter = playFilter,
            downloadFilter = downloadFilter
        )
    )

    suspend fun renamePlaylist(id: Long, name: String) {
        val existing = db.playlists().byId(id) ?: return
        db.playlists().update(existing.copy(name = name))
    }

    suspend fun deletePlaylist(entity: PlaylistEntity) = db.playlists().delete(entity)

    suspend fun addToPlaylist(playlistId: Long, episodeId: Long) {
        val order = db.playlists().items(playlistId).size
        db.playlists().insertItem(PlaylistItemEntity(playlistId, episodeId, order))
    }

    suspend fun addManyToPlaylist(playlistId: Long, episodeIds: List<Long>) {
        episodeIds.forEach { addToPlaylist(playlistId, it) }
    }

    suspend fun removeFromPlaylist(playlistId: Long, episodeId: Long) =
        db.playlists().removeItem(playlistId, episodeId)

    suspend fun setArchived(ids: List<Long>, archived: Boolean) {
        if (ids.isEmpty()) return
        db.episodes().setArchived(ids, archived)
    }

    suspend fun downloadMany(ids: List<Long>) {
        ids.forEach { id ->
            val episode = db.episodes().byId(id) ?: return@forEach
            if (episode.downloadStatus != EpisodeEntity.STATUS_DOWNLOADING) {
                db.episodes().setDownloadStatus(id, EpisodeEntity.STATUS_QUEUED)
            }
        }
        ids.forEach { id ->
            runCatching { downloader.download(id) }
        }
    }

    suspend fun deleteDownloads(ids: List<Long>) {
        ids.forEach { downloader.deleteLocal(it) }
    }

    suspend fun matchingEpisodes(playFilter: String, downloadFilter: String): List<EpisodeEntity> {
        return db.episodes().allActive().filter { episode ->
            val playOk = when (playFilter) {
                PlaylistEntity.PLAY_UNPLAYED -> !episode.completed && episode.positionMs <= 0
                PlaylistEntity.PLAY_IN_PROGRESS -> !episode.completed && episode.positionMs > 0
                PlaylistEntity.PLAY_PLAYED -> episode.completed
                else -> true
            }
            val downloadOk = when (downloadFilter) {
                PlaylistEntity.DOWNLOAD_DOWNLOADED -> episode.downloadStatus == EpisodeEntity.STATUS_DONE
                PlaylistEntity.DOWNLOAD_NOT_DOWNLOADED -> episode.downloadStatus != EpisodeEntity.STATUS_DONE
                else -> true
            }
            playOk && downloadOk
        }
    }

    suspend fun loadChapters(episode: EpisodeEntity): List<Chapter> = withContext(Dispatchers.IO) {
        if (episode.chaptersUrl.isBlank()) return@withContext emptyList()
        try {
            parseChapterJson(Http.get(episode.chaptersUrl), episode.durationMs)
        } catch (_: Exception) {
            emptyList()
        }
    }

    internal fun parseChapterJson(body: String, durationMs: Long): List<Chapter> {
        val trimmed = body.trim().removePrefix("\uFEFF")
        if (trimmed.isEmpty()) return emptyList()
        val arr = if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            JSONObject(trimmed).optJSONArray("chapters") ?: return emptyList()
        }
        val raw = ArrayList<Pair<Double, String>>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val start = readChapterStart(obj) ?: continue
            val title = obj.optString("title").ifBlank { "Chapter ${i + 1}" }
            raw += start to title
        }
        if (raw.isEmpty()) return emptyList()
        val starts = ChapterNav.normalizeStartMs(raw.map { it.first }, durationMs)
        return raw.zip(starts) { pair, ms -> Chapter(ms, pair.second) }.sortedBy { it.startMs }
    }

    private fun readChapterStart(obj: JSONObject): Double? {
        for (key in listOf("startTime", "start", "start_time")) {
            if (!obj.has(key)) continue
            val numeric = obj.optDouble(key, Double.NaN)
            if (!numeric.isNaN() && numeric >= 0.0) return numeric
            ChapterNav.parseClockToMs(obj.optString(key))?.let { return it / 1000.0 }
        }
        return null
    }

    suspend fun loadTranscript(episode: EpisodeEntity): List<TranscriptLine> = withContext(Dispatchers.IO) {
        if (episode.transcriptUrl.isBlank()) return@withContext emptyList()
        try {
            val body = Http.get(episode.transcriptUrl)
            val type = episode.transcriptType.lowercase()
            if (type.contains("json") || body.trim().startsWith("{")) {
                parseJsonTranscript(body)
            } else {
                body.lines()
                    .filter { it.isNotBlank() && !it.startsWith("WEBVTT") && !it.matches(Regex("\\d+")) }
                    .filter { !it.contains("-->") }
                    .map { TranscriptLine(null, it.trim()) }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseJsonTranscript(body: String): List<TranscriptLine> {
        val root = JSONObject(body)
        val segments = root.optJSONArray("segments") ?: root.optJSONArray("transcripts")
        if (segments != null) {
            return (0 until segments.length()).mapNotNull { i ->
                val obj = segments.optJSONObject(i) ?: return@mapNotNull null
                val text = obj.optString("body").ifBlank { obj.optString("text") }
                if (text.isBlank()) return@mapNotNull null
                val start = obj.optDouble("startTime", obj.optDouble("start", -1.0))
                TranscriptLine(if (start >= 0) (start * 1000).toLong() else null, text)
            }
        }
        return listOf(TranscriptLine(null, body))
    }

    fun persistTreeUri(uri: Uri) {
        app.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }
}
