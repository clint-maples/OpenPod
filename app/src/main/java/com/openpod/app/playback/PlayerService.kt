package com.openpod.app.playback

import android.app.Notification
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.openpod.app.R
import com.openpod.app.data.PodRepository
import com.openpod.app.data.db.EpisodeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class PlayerUiState(
    val episodeId: Long = 0L,
    val title: String = "",
    val podcastTitle: String = "",
    val artwork: String = "",
    val playing: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val speed: Float = 1f,
    val skipForwardSec: Int = 30,
    val skipBackSec: Int = 15
)

class PlayerService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private var session: MediaSession? = null
    private lateinit var repo: PodRepository
    private val eq = EqController()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var progressJob: Job? = null
    private var currentEpisode: EpisodeEntity? = null
    private var lastAudioSessionId = 0

    private val headset = HeadsetClickTracker(
        onSingle = { togglePlay() },
        onDouble = { skip(true) },
        onTriple = { skip(false) }
    )

    override fun onCreate() {
        super.onCreate()
        instance = this
        repo = PodRepository(this)
        enterForeground("Starting playback")
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelId("playback")
                .setChannelName(R.string.app_name)
                .setNotificationId(NOTIFICATION_ID)
                .build()
        )
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                publish()
                if (!isPlaying) persistProgress()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                publish()
                if (playbackState == Player.STATE_ENDED) {
                    val ep = currentEpisode ?: return
                    val duration = player.duration.coerceAtLeast(0)
                    ioScope.launch { repo.markProgress(ep.id, duration, duration) }
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                attachEq(audioSessionId)
            }
        })
        session = MediaSession.Builder(this, player)
            .setCallback(ButtonCallback())
            .build()
        startProgress()
        ioScope.launch {
            val settings = repo.settingsOnce()
            _state.update {
                it.copy(skipForwardSec = settings.skipForwardSec, skipBackSec = settings.skipBackSec)
            }
        }
        pending?.let { (episode, title, art) ->
            pending = null
            playEpisode(episode, title, art)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        progressJob?.cancel()
        persistProgressBlocking()
        eq.release()
        session?.release()
        session = null
        player.release()
        instance = null
        super.onDestroy()
    }

    fun playEpisode(episode: EpisodeEntity, podcastTitle: String, artwork: String) {
        currentEpisode = episode
        val uri = episode.localUri.ifBlank { episode.audioUrl }
        val meta = MediaMetadata.Builder()
            .setTitle(episode.title)
            .setArtist(podcastTitle)
            .build()
        val start = if (!episode.completed && episode.positionMs > 0L) episode.positionMs else 0L
        player.setMediaItem(
            MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(meta)
                .build(),
            start
        )
        player.prepare()
        player.setPlaybackSpeed(_state.value.speed.coerceIn(0.5f, 5.0f))
        player.play()
        _state.update {
            it.copy(
                episodeId = episode.id,
                title = episode.title,
                podcastTitle = podcastTitle,
                artwork = artwork.ifBlank { episode.imageUrl },
                positionMs = start,
                durationMs = episode.durationMs
            )
        }
        publish()
        ioScope.launch { repo.setLastEpisode(episode.id) }
        attachEq(player.audioSessionId)
    }

    fun togglePlay() {
        if (!::player.isInitialized) return
        if (player.mediaItemCount == 0) return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skip(forward: Boolean) {
        if (!::player.isInitialized) return
        val amount = if (forward) _state.value.skipForwardSec else _state.value.skipBackSec
        val delta = amount * 1000L * if (forward) 1 else -1
        val next = (player.currentPosition + delta).coerceAtLeast(0)
        player.seekTo(next)
        publish()
        persistProgress()
    }

    fun seek(positionMs: Long) {
        if (!::player.isInitialized) return
        player.seekTo(positionMs)
        publish()
        persistProgress()
    }

    fun setSpeed(speed: Float) {
        if (!::player.isInitialized) return
        player.setPlaybackSpeed(speed)
        _state.update { it.copy(speed = speed) }
    }

    fun setSkipSeconds(forward: Int, back: Int) {
        _state.update { it.copy(skipForwardSec = forward, skipBackSec = back) }
    }

    fun equalizer(): EqController = eq

    fun stopAndClear() {
        if (::player.isInitialized) {
            persistProgressBlocking()
            runCatching { player.stop() }
            runCatching { player.clearMediaItems() }
        }
        currentEpisode = null
        _state.update {
            PlayerUiState(
                skipForwardSec = it.skipForwardSec,
                skipBackSec = it.skipBackSec
            )
        }
    }

    private fun attachEq(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == lastAudioSessionId) return
        lastAudioSessionId = audioSessionId
        ioScope.launch {
            val settings = repo.settingsOnce()
            scope.launch {
                eq.attach(audioSessionId, settings.eqEnabled, settings.eqBandsJson)
            }
        }
    }

    private fun startProgress() {
        progressJob = scope.launch {
            while (isActive) {
                publish()
                persistProgress()
                delay(2_000)
            }
        }
    }

    private fun persistProgress() {
        val ep = currentEpisode ?: return
        if (!::player.isInitialized) return
        val position = player.currentPosition.coerceAtLeast(0)
        val duration = player.duration.takeIf { it > 0 } ?: ep.durationMs
        if (position <= 0L) return
        ioScope.launch { repo.markProgress(ep.id, position, duration) }
    }

    private fun persistProgressBlocking() {
        val ep = currentEpisode ?: return
        if (!::player.isInitialized) return
        val position = player.currentPosition.coerceAtLeast(0)
        val duration = player.duration.takeIf { it > 0 } ?: ep.durationMs
        if (position <= 0L) return
        runBlocking(Dispatchers.IO) { repo.markProgress(ep.id, position, duration) }
    }

    private fun publish() {
        if (!::player.isInitialized) return
        val duration = player.duration.takeIf { it > 0 } ?: _state.value.durationMs
        _state.update {
            it.copy(
                playing = player.isPlaying,
                positionMs = player.currentPosition.coerceAtLeast(0),
                durationMs = duration.coerceAtLeast(0)
            )
        }
    }

    private fun enterForeground(text: String) {
        val notification: Notification = NotificationCompat.Builder(this, "playback")
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setSilent(true)
            .build()
        val type = if (Build.VERSION.SDK_INT >= 29) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
    }

    private inner class ButtonCallback : MediaSession.Callback {
        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent
        ): Boolean {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return false
            if (event.action != KeyEvent.ACTION_UP) return true
            return when (event.keyCode) {
                KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    headset.onClick()
                    true
                }
                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    skip(true)
                    true
                }
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    skip(false)
                    true
                }
                else -> false
            }
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        private val _state = MutableStateFlow(PlayerUiState())
        val state = _state.asStateFlow()
        var instance: PlayerService? = null
            private set
        private var pending: Triple<EpisodeEntity, String, String>? = null

        fun setSpeed(speed: Float) {
            val clamped = speed.coerceIn(0.5f, 5.0f)
            _state.update { it.copy(speed = clamped) }
            instance?.setSpeed(clamped)
        }

        fun play(context: android.content.Context, episode: EpisodeEntity, podcastTitle: String, artwork: String) {
            val existing = instance
            if (existing != null) {
                existing.playEpisode(episode, podcastTitle, artwork)
                return
            }
            pending = Triple(episode, podcastTitle, artwork)
            val intent = Intent(context, PlayerService::class.java)
            context.startService(intent)
        }

        fun restoreSession(episode: EpisodeEntity, podcastTitle: String, artwork: String) {
            if (_state.value.episodeId != 0L && instance != null) return
            _state.update {
                it.copy(
                    episodeId = episode.id,
                    title = episode.title,
                    podcastTitle = podcastTitle,
                    artwork = artwork.ifBlank { episode.imageUrl },
                    playing = false,
                    positionMs = if (episode.completed) 0L else episode.positionMs,
                    durationMs = episode.durationMs
                )
            }
        }

        fun stopAndClear() {
            pending = null
            instance?.stopAndClear()
            if (instance == null) {
                _state.value = PlayerUiState()
            }
        }
    }
}
