package com.openpod.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openpod.app.data.Chapter
import com.openpod.app.data.ChapterNav
import com.openpod.app.data.PodRepository
import com.openpod.app.data.TranscriptLine
import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.data.db.PlaylistEntity
import com.openpod.app.data.db.PodcastEntity
import com.openpod.app.data.search.SearchHit
import com.openpod.app.download.DownloadTracker
import com.openpod.app.playback.PlayerService
import com.openpod.app.playback.SkipIntervals
import com.openpod.app.work.PodcastSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    val repo = PodRepository(app)
    val player = PlayerService.state
    val podcasts = repo.podcasts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val playlists = repo.playlists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val queuedIds = repo.queuedEpisodeIds().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())
    val upcomingQueue = repo.upcomingQueue().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings = repo.settings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.openpod.app.data.db.SettingsEntity())
    val downloadProgress = DownloadTracker.progress

    init {
        viewModelScope.launch { restoreLastEpisode() }
    }

    private val _searchResults = MutableStateFlow<List<SearchHit>>(emptyList())
    val searchResults = _searchResults.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters = _chapters.asStateFlow()
    private val _transcript = MutableStateFlow<List<TranscriptLine>>(emptyList())
    val transcript = _transcript.asStateFlow()

    fun episodes(podcastId: Long) = repo.episodes(podcastId)
    fun episodes(podcastId: Long, showArchived: Boolean) = repo.episodes(podcastId, showArchived)
    fun episode(id: Long) = repo.episode(id)
    fun playlistItems(id: Long) = repo.playlistItems(id)

    fun search(query: String) {
        viewModelScope.launch {
            _busy.value = true
            _searchResults.value = runCatching { repo.search(query) }.getOrDefault(emptyList())
            _busy.value = false
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    fun subscribe(feedUrl: String) {
        viewModelScope.launch {
            _busy.value = true
            runCatching { repo.subscribe(feedUrl) }
                .onSuccess { _message.value = "Subscribed to ${it.title}" }
                .onFailure { _message.value = it.message ?: "Could not add podcast" }
            _busy.value = false
        }
    }

    fun refresh(podcastId: Long) {
        viewModelScope.launch {
            _busy.value = true
            runCatching { repo.refreshPodcast(podcastId) }
                .onFailure { _message.value = it.message }
            _busy.value = false
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _busy.value = true
            runCatching { repo.refreshAll() }
            _busy.value = false
        }
    }

    fun setFlags(
        id: Long,
        autoDownload: Boolean? = null,
        autoDelete: Boolean? = null,
        skipIntroSec: Int? = null,
        skipOutroSec: Int? = null,
        autoArchive: Boolean? = null
    ) {
        viewModelScope.launch {
            repo.setPodcastFlags(id, autoDownload, autoDelete, skipIntroSec, skipOutroSec, autoArchive)
            if (skipIntroSec != null || skipOutroSec != null) {
                val podcast = repo.db.podcasts().byId(id) ?: return@launch
                if (PlayerService.state.value.podcastId == id) {
                    PlayerService.setEpisodeTrim(podcast.skipIntroSec, podcast.skipOutroSec)
                }
            }
        }
    }

    fun unsubscribe(id: Long) {
        viewModelScope.launch {
            val playingId = PlayerService.state.value.episodeId
            if (playingId != 0L) {
                val playing = repo.db.episodes().byId(playingId)
                if (playing?.podcastId == id) {
                    PlayerService.stopAndClear()
                    repo.setLastEpisode(0L)
                }
            }
            repo.unsubscribe(id)
            _message.value = "Removed from library"
        }
    }

    fun download(id: Long) {
        viewModelScope.launch {
            runCatching { repo.download(id) }
                .onSuccess { result ->
                    result.onSuccess { _message.value = "Saved episode" }
                        .onFailure { _message.value = it.message ?: "Download failed" }
                }
                .onFailure { _message.value = it.message ?: "Download failed" }
        }
    }

    fun deleteDownload(id: Long) {
        viewModelScope.launch { repo.deleteDownload(id) }
    }

    fun play(episode: EpisodeEntity, podcast: PodcastEntity) {
        viewModelScope.launch {
            val fresh = repo.db.episodes().byId(episode.id) ?: episode
            PlayerService.play(getApplication(), fresh, podcast.title, podcast.imageUrl)
        }
    }

    fun play(episode: EpisodeEntity, podcastTitle: String, artwork: String) {
        viewModelScope.launch {
            val fresh = repo.db.episodes().byId(episode.id) ?: episode
            val podcast = repo.db.podcasts().byId(fresh.podcastId)
            PlayerService.play(
                getApplication(),
                fresh,
                podcastTitle.ifBlank { podcast?.title.orEmpty() },
                artwork.ifBlank { podcast?.imageUrl ?: fresh.imageUrl }
            )
        }
    }

    fun playOrToggle(episode: EpisodeEntity, podcast: PodcastEntity) {
        if (player.value.episodeId == episode.id) {
            togglePlay()
            return
        }
        play(episode, podcast)
    }

    fun playOrToggle(episode: EpisodeEntity, podcastTitle: String = "", artwork: String = "") {
        if (player.value.episodeId == episode.id) {
            togglePlay()
            return
        }
        play(episode, podcastTitle, artwork)
    }

    fun playNext(episode: EpisodeEntity) {
        viewModelScope.launch {
            val nowPlaying = PlayerService.state.value.episodeId
            if (nowPlaying == 0L) {
                play(episode, "", "")
                return@launch
            }
            if (nowPlaying == episode.id) return@launch
            repo.queuePlayNext(episode.id, nowPlaying)
            _message.value = "Playing next"
        }
    }

    fun addToQueue(episode: EpisodeEntity) {
        viewModelScope.launch {
            val nowPlaying = PlayerService.state.value.episodeId
            if (nowPlaying == episode.id) return@launch
            repo.queueAdd(episode.id, nowPlaying)
            _message.value = "Added to Up Next"
        }
    }

    fun moveQueueItem(episodeId: Long, delta: Int) {
        viewModelScope.launch { repo.queueMove(episodeId, delta) }
    }

    fun removeFromQueue(episodeId: Long) {
        viewModelScope.launch { repo.removeFromQueue(episodeId) }
    }

    fun clearQueue() {
        viewModelScope.launch {
            repo.clearQueue()
            _message.value = "Queue cleared"
        }
    }

    fun togglePlay() {
        val svc = PlayerService.instance
        if (svc != null && PlayerService.state.value.episodeId != 0L) {
            svc.togglePlay()
            return
        }
        viewModelScope.launch { resumeLastEpisode() }
    }

    fun skip(forward: Boolean) = PlayerService.instance?.skip(forward)

    fun skipChapter(forward: Boolean) {
        val target = ChapterNav.seekTargetMs(
            chapters = _chapters.value,
            positionMs = PlayerService.instance?.currentPositionMs() ?: player.value.positionMs,
            forward = forward
        ) ?: return
        seek(target)
        if (PlayerService.instance == null) {
            viewModelScope.launch { resumeLastEpisode() }
        }
    }

    fun seek(ms: Long) = PlayerService.seekOrQueue(ms)
    fun setSpeed(speed: Float) = PlayerService.setSpeed(clampSpeed(speed))

    private suspend fun restoreLastEpisode() {
        if (PlayerService.state.value.episodeId != 0L) return
        val lastId = repo.settingsOnce().lastEpisodeId
        if (lastId == 0L) return
        val episode = repo.db.episodes().byId(lastId) ?: return
        val podcast = repo.db.podcasts().byId(episode.podcastId)
        PlayerService.restoreSession(
            episode,
            podcast?.title.orEmpty(),
            podcast?.imageUrl ?: episode.imageUrl
        )
    }

    private suspend fun resumeLastEpisode() {
        val id = PlayerService.state.value.episodeId.takeIf { it != 0L }
            ?: repo.settingsOnce().lastEpisodeId
        if (id == 0L) return
        val episode = repo.db.episodes().byId(id) ?: return
        val podcast = repo.db.podcasts().byId(episode.podcastId)
        PlayerService.play(
            getApplication(),
            episode,
            podcast?.title.orEmpty(),
            podcast?.imageUrl ?: episode.imageUrl
        )
    }

    fun loadExtras(episode: EpisodeEntity) {
        viewModelScope.launch {
            _chapters.value = repo.loadChapters(episode)
            _transcript.value = repo.loadTranscript(episode)
        }
    }

    fun setDownloadFolder(uri: Uri) {
        viewModelScope.launch {
            runCatching { repo.persistTreeUri(uri) }
            repo.updateSettings { it.copy(downloadTreeUri = uri.toString()) }
        }
    }

    fun setNaming(pattern: String) {
        viewModelScope.launch { repo.updateSettings { it.copy(namingPattern = pattern) } }
    }

    fun setSkip(forward: Int, back: Int) {
        val fwd = SkipIntervals.sanitize(forward)
        val backSec = SkipIntervals.sanitize(back)
        PlayerService.setSkipSeconds(fwd, backSec)
        viewModelScope.launch {
            repo.updateSettings { it.copy(skipForwardSec = fwd, skipBackSec = backSec) }
        }
    }

    fun setSleepMinutes(minutes: Int) {
        PlayerService.setSleepMinutes(minutes)
    }

    fun setSleepEndOfEpisode(enabled: Boolean) {
        PlayerService.setSleepEndOfEpisode(enabled)
    }

    fun cancelSleepTimer() {
        PlayerService.cancelSleepTimer()
    }

    fun setDefaults(autoDownload: Boolean, autoDelete: Boolean) {
        viewModelScope.launch {
            repo.updateSettings { it.copy(autoDownloadDefault = autoDownload, autoDeleteDefault = autoDelete) }
        }
    }

    fun setEq(enabled: Boolean) {
        viewModelScope.launch {
            val controller = PlayerService.instance?.equalizer()
            controller?.setEnabled(enabled)
            repo.updateSettings {
                it.copy(eqEnabled = enabled, eqBandsJson = controller?.snapshotJson() ?: it.eqBandsJson)
            }
        }
    }

    fun setEqBand(index: Short, level: Short) {
        val controller = PlayerService.instance?.equalizer() ?: return
        controller.setBand(index, level)
        viewModelScope.launch {
            repo.updateSettings { it.copy(eqBandsJson = controller.snapshotJson(), eqEnabled = true) }
        }
    }

    fun previewVolumeBoost(value: Float) {
        PlayerService.setVolumeBoost(value)
    }

    fun setVolumeBoost(value: Float) {
        val clamped = value.coerceIn(-1f, 1f)
        PlayerService.setVolumeBoost(clamped)
        viewModelScope.launch {
            repo.updateSettings { it.copy(volumeBoost = clamped) }
        }
    }

    fun resetEq() {
        PlayerService.instance?.equalizer()?.reset()
        PlayerService.setVolumeBoost(0f)
        viewModelScope.launch {
            val json = PlayerService.instance?.equalizer()?.snapshotJson().orEmpty()
            repo.updateSettings { it.copy(eqBandsJson = json, volumeBoost = 0f) }
        }
    }

    fun setShowArchived(show: Boolean) {
        viewModelScope.launch { repo.updateSettings { it.copy(showArchived = show) } }
    }

    fun setLibraryLayout(layout: String) {
        viewModelScope.launch { repo.updateSettings { it.copy(libraryLayout = layout) } }
    }

    fun setSyncSchedule(frequency: String, hour: Int, minute: Int) {
        viewModelScope.launch {
            repo.updateSettings {
                it.copy(syncFrequency = frequency, syncHour = hour, syncMinute = minute)
            }
            PodcastSyncWorker.enqueue(getApplication(), frequency, hour, minute)
            _message.value = "Sync schedule saved"
        }
    }

    fun archiveMany(ids: List<Long>, archived: Boolean) {
        viewModelScope.launch {
            repo.setArchived(ids, archived)
            _message.value = if (archived) "Archived ${ids.size}" else "Unarchived ${ids.size}"
        }
    }

    fun archivePodcast(id: Long) {
        viewModelScope.launch {
            val count = repo.archivePodcast(id)
            _message.value = "Archived $count"
        }
    }

    fun downloadMany(ids: List<Long>) {
        viewModelScope.launch {
            val count = repo.downloadMany(ids)
            _message.value = when {
                count == 0 -> "Already downloaded"
                count < ids.size -> "Download finished for $count episode(s); skipped ${ids.size - count} already saved"
                else -> "Download finished for $count episode(s)"
            }
        }
    }

    fun exportSubscriptions(uri: Uri) {
        viewModelScope.launch {
            _busy.value = true
            runCatching {
                val xml = repo.exportOpml()
                val out = getApplication<Application>().contentResolver.openOutputStream(uri)
                    ?: error("Could not write the file")
                out.use { it.write(xml.toByteArray(Charsets.UTF_8)) }
                repo.db.podcasts().all().size
            }.onSuccess { count ->
                _message.value = "Exported $count subscription(s)"
            }.onFailure {
                _message.value = it.message ?: "Export failed"
            }
            _busy.value = false
        }
    }

    fun importSubscriptions(uri: Uri, replace: Boolean) {
        viewModelScope.launch {
            _busy.value = true
            runCatching {
                val xml = getApplication<Application>().contentResolver.openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    ?: error("Could not read the file")
                repo.importSubscriptions(xml, replace)
            }.onSuccess { result ->
                _message.value = buildString {
                    append("Added ${result.added}")
                    if (result.existing > 0) append(", ${result.existing} already subscribed")
                    if (result.removed > 0) append(", removed ${result.removed}")
                    if (result.failed > 0) append(", ${result.failed} failed")
                }
            }.onFailure {
                _message.value = it.message ?: "Import failed"
            }
            _busy.value = false
        }
    }

    fun scanDownloads(podcastId: Long) {
        viewModelScope.launch {
            _busy.value = true
            runCatching { repo.scanPodcastDownloads(podcastId) }
                .onSuccess { result ->
                    _message.value = when {
                        result.folderUnset -> "Choose a download folder in Settings"
                        result.folderMissing -> "No download folder found for this podcast"
                        result.linked == 0 && result.alreadyLinked > 0 ->
                            "Already linked ${result.alreadyLinked} downloaded episode(s)"
                        result.linked == 0 -> "No matching downloads found"
                        else -> "Linked ${result.linked} episode(s)"
                    }
                }
                .onFailure {
                    _message.value = it.message ?: "Scan failed"
                }
            _busy.value = false
        }
    }

    fun deleteDownloadsMany(ids: List<Long>) {
        viewModelScope.launch {
            repo.deleteDownloads(ids)
            _message.value = "Removed files for ${ids.size} episode(s)"
        }
    }

    fun addManyToPlaylist(playlistId: Long, ids: List<Long>) {
        viewModelScope.launch {
            repo.addManyToPlaylist(playlistId, ids)
            _message.value = "Added ${ids.size} to playlist"
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { repo.createPlaylist(name) }
    }

    fun createSmartPlaylist(name: String, playFilter: String, downloadFilter: String) {
        viewModelScope.launch {
            repo.createPlaylist(name, isSmart = true, playFilter = playFilter, downloadFilter = downloadFilter)
        }
    }

    suspend fun smartEpisodes(playFilter: String, downloadFilter: String) =
        repo.matchingEpisodes(playFilter, downloadFilter)

    fun renamePlaylist(id: Long, name: String) {
        viewModelScope.launch { repo.renamePlaylist(id, name) }
    }

    fun deletePlaylist(entity: PlaylistEntity) {
        viewModelScope.launch { repo.deletePlaylist(entity) }
    }

    fun addToPlaylist(playlistId: Long, episodeId: Long) {
        viewModelScope.launch { repo.addToPlaylist(playlistId, episodeId) }
    }

    fun removeFromPlaylist(playlistId: Long, episodeId: Long) {
        viewModelScope.launch { repo.removeFromPlaylist(playlistId, episodeId) }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
