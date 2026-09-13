package com.openpod.app.playback

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null
    private var currentEpisode: EpisodeEntity? = null

    private val headset = HeadsetClickTracker(
        onSingle = { togglePlay() },
        onDouble = { skip(true) },
        onTriple = { skip(false) }
    )

    override fun onCreate() {
        super.onCreate()
        instance = this
        repo = PodRepository(this)
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
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                publish()
                attachEq()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                publish()
                if (playbackState == Player.STATE_ENDED) {
                    currentEpisode?.let { ep ->
                        scope.launch {
                            repo.markProgress(ep.id, player.duration.coerceAtLeast(0), player.duration.coerceAtLeast(0))
                        }
                    }
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                attachEq()
            }
        })
        session = MediaSession.Builder(this, player)
            .setCallback(ButtonCallback())
            .build()
        startProgress()
        scope.launch {
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

    override fun onDestroy() {
        progressJob?.cancel()
        eq.release()
        session?.release()
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
        player.setMediaItem(
            MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(meta)
                .build()
        )
        player.prepare()
        if (episode.positionMs > 0 && !episode.completed) {
            player.seekTo(episode.positionMs)
        }
        player.play()
        _state.update {
            it.copy(
                episodeId = episode.id,
                title = episode.title,
                podcastTitle = podcastTitle,
                artwork = artwork.ifBlank { episode.imageUrl }
            )
        }
        publish()
        attachEq()
    }

    fun togglePlay() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skip(forward: Boolean) {
        val amount = if (forward) _state.value.skipForwardSec else _state.value.skipBackSec
        val delta = amount * 1000L * if (forward) 1 else -1
        val next = (player.currentPosition + delta).coerceAtLeast(0)
        player.seekTo(next)
        publish()
    }

    fun seek(positionMs: Long) {
        player.seekTo(positionMs)
        publish()
    }

    fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        _state.update { it.copy(speed = speed) }
    }

    fun setSkipSeconds(forward: Int, back: Int) {
        _state.update { it.copy(skipForwardSec = forward, skipBackSec = back) }
    }

    fun equalizer(): EqController = eq

    private fun attachEq() {
        scope.launch {
            val settings = repo.settingsOnce()
            eq.attach(player.audioSessionId, settings.eqEnabled, settings.eqBandsJson)
        }
    }

    private fun startProgress() {
        progressJob = scope.launch {
            while (isActive) {
                publish()
                val ep = currentEpisode
                if (ep != null && player.currentPosition > 0) {
                    repo.markProgress(ep.id, player.currentPosition, player.duration.coerceAtLeast(0))
                }
                delay(1000)
            }
        }
    }

    private fun publish() {
        _state.update {
            it.copy(
                playing = player.isPlaying,
                positionMs = player.currentPosition.coerceAtLeast(0),
                durationMs = player.duration.coerceAtLeast(0)
            )
        }
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
        private val _state = MutableStateFlow(PlayerUiState())
        val state = _state.asStateFlow()
        var instance: PlayerService? = null
            private set
        private var pending: Triple<EpisodeEntity, String, String>? = null

        fun play(context: android.content.Context, episode: EpisodeEntity, podcastTitle: String, artwork: String) {
            val existing = instance
            if (existing != null) {
                existing.playEpisode(episode, podcastTitle, artwork)
                return
            }
            pending = Triple(episode, podcastTitle, artwork)
            val intent = Intent(context, PlayerService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }
    }
}
