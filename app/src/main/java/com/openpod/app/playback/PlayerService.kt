package com.openpod.app.playback

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.core.content.ContextCompat
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import android.os.Bundle
import androidx.core.graphics.drawable.IconCompat
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.openpod.app.MainActivity
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
    val podcastId: Long = 0L,
    val title: String = "",
    val podcastTitle: String = "",
    val artwork: String = "",
    val playing: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val speed: Float = 1f,
    val skipForwardSec: Int = SkipIntervals.DEFAULT_FORWARD,
    val skipBackSec: Int = SkipIntervals.DEFAULT_BACK,
    val volumeBoost: Float = 0f,
    val sleepDeadlineMs: Long = 0L,
    val sleepEndOfEpisode: Boolean = false,
    val sleepRemainingMs: Long = 0L,
    val sleepMinutes: Int = 0
) {
    val sleepActive: Boolean get() = sleepDeadlineMs > 0L || sleepEndOfEpisode
}

@UnstableApi
class PlayerService : MediaSessionService() {
    private lateinit var exo: ExoPlayer
    private lateinit var player: Player
    private var session: MediaSession? = null
    private lateinit var repo: PodRepository
    private val eq = EqController()
    private val gainProcessor = GainAudioProcessor()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var progressJob: Job? = null
    private var currentEpisode: EpisodeEntity? = null
    private var lastAudioSessionId = 0


    override fun onCreate() {
        super.onCreate()
        instance = this
        repo = PodRepository(this)
        // startForegroundService() requires startForeground() quickly ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â do that first.
        enterForeground(getString(R.string.app_name))
        val renderers = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink {
                return DefaultAudioSink.Builder(context)
                    .setEnableFloatOutput(enableFloatOutput)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .setAudioProcessors(arrayOf(gainProcessor))
                    .build()
            }
        }
        exo = ExoPlayer.Builder(this, renderers)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setSeekBackIncrementMs(_state.value.skipBackSec * 1000L)
            .setSeekForwardIncrementMs(_state.value.skipForwardSec * 1000L)
            .build()
        player = OpenPodPlayer(exo)
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setNotificationId(NOTIFICATION_ID)
            .build()
        notificationProvider.setSmallIcon(R.drawable.ic_notification)
        setMediaNotificationProvider(notificationProvider)
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                publish()
                if (!isPlaying) persistProgress()
                refreshMediaNotification()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                publish()
                refreshMediaNotification()
                if (playbackState == Player.STATE_ENDED) {
                    val ep = currentEpisode ?: return
                    val duration = player.duration.coerceAtLeast(0)
                    ioScope.launch { repo.markProgress(ep.id, duration, duration) }
                    if (_state.value.sleepEndOfEpisode) {
                        finishSleepTimer()
                    }
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                attachEq(audioSessionId)
            }
        })
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        session = MediaSession.Builder(this, player)
            .setId("openpod")
            .setCallback(ButtonCallback())
            .setSessionActivity(sessionActivity)
            .build()
        refreshMediaNotification()
        startProgress()
        ioScope.launch {
            val settings = repo.settingsOnce()
            val fwd = SkipIntervals.nearest(settings.skipForwardSec)
            val back = SkipIntervals.nearest(settings.skipBackSec)
            if (fwd != settings.skipForwardSec || back != settings.skipBackSec) {
                repo.updateSettings { it.copy(skipForwardSec = fwd, skipBackSec = back) }
            }
            scope.launch {
                applyVolumeBoost(settings.volumeBoost)
                setSkipSeconds(fwd, back)
            }
        }
        pending?.let { (episode, title, art) ->
            pending = null
            playEpisode(episode, title, art)
        }
    }

    
        override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        // Do NOT call super: Media3's MediaNotificationManager stopForeground(REMOVE)s
        // while its MediaController timeline is still empty, which cancels id 1001 and
        // never re-posts even after PLAYING. Own the MediaStyle FGS notification instead.
        val title = player.mediaMetadata.displayTitle
            ?: player.mediaMetadata.title
            ?: getString(R.string.app_name)
        val text = player.mediaMetadata.artist
            ?: player.mediaMetadata.albumTitle
            ?: title.toString()
        enterForeground(text.toString().ifBlank { title.toString() })
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
        // exo released via ForwardingPlayer.release()
        instance = null
        super.onDestroy()
    }

    fun playEpisode(episode: EpisodeEntity, podcastTitle: String, artwork: String) {
        currentEpisode = episode
        val art = artwork.ifBlank { episode.imageUrl }
        val meta = MediaMetadata.Builder()
            .setTitle(episode.title)
            .setArtist(podcastTitle)
            .setAlbumTitle(podcastTitle)
            .setDisplayTitle(episode.title)
            .setSubtitle(podcastTitle)
            .apply {
                if (episode.durationMs > 0) setDurationMs(episode.durationMs)
                if (art.isNotBlank()) setArtworkUri(android.net.Uri.parse(art))
            }
            .build()
        val queued = pendingSeekMs
        pendingSeekMs = -1L
        val start = when {
            queued >= 0L -> queued
            !episode.completed && episode.positionMs > 0L -> episode.positionMs
            else -> 0L
        }
        player.setMediaItem(
            MediaItem.Builder()
                .setUri(episode.localUri.ifBlank { episode.audioUrl })
                .setMediaMetadata(meta)
                .build(),
            start
        )
        player.prepare()
        player.setPlaybackSpeed(_state.value.speed.coerceIn(0.5f, 5.0f))
        player.play()
        refreshMediaNotification()
        _state.update {
            it.copy(
                episodeId = episode.id,
                podcastId = episode.podcastId,
                title = episode.title,
                podcastTitle = podcastTitle,
                artwork = art,
                positionMs = start,
                durationMs = episode.durationMs
            )
        }
        publish()
        ioScope.launch { repo.setLastEpisode(episode.id) }
        attachEq(exo.audioSessionId)
    }

    fun togglePlay() {
        if (!::player.isInitialized) return
        if (player.mediaItemCount == 0) return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun currentPositionMs(): Long {
        if (!::player.isInitialized || player.mediaItemCount == 0) return _state.value.positionMs
        return player.currentPosition.coerceAtLeast(0)
    }

    fun skip(forward: Boolean) {
        if (!::player.isInitialized) return
        val amount = if (forward) {
            SkipIntervals.nearest(_state.value.skipForwardSec)
        } else {
            SkipIntervals.nearest(_state.value.skipBackSec)
        }
        val delta = amount * 1000L * if (forward) 1 else -1
        val next = (player.currentPosition + delta).coerceAtLeast(0)
        exo.seekTo(next)
        publish()
        persistProgress()
    }

    fun seek(positionMs: Long) {
        if (!::player.isInitialized || player.mediaItemCount == 0) {
            queueSeek(positionMs)
            return
        }
        val duration = player.duration.takeIf { it > 0 } ?: _state.value.durationMs
        val clamped = if (duration > 0L) positionMs.coerceIn(0L, duration) else positionMs.coerceAtLeast(0)
        player.seekTo(clamped)
        _state.update { it.copy(positionMs = clamped) }
        persistProgress()
    }

    fun setSpeed(speed: Float) {
        if (!::player.isInitialized) return
        player.setPlaybackSpeed(speed)
        _state.update { it.copy(speed = speed) }
    }

    fun setSkipSeconds(forward: Int, back: Int) {
        Companion.setSkipSeconds(forward, back)
    }

    fun setSleepMinutes(minutes: Int) {
        Companion.setSleepMinutes(minutes)
        applySleepVolume()
    }

    fun setSleepEndOfEpisode(enabled: Boolean) {
        Companion.setSleepEndOfEpisode(enabled)
        applySleepVolume()
    }

    fun cancelSleepTimer() {
        Companion.cancelSleepTimer()
        restoreSleepVolume()
    }

    fun applyVolumeBoost(slider: Float) {
        val value = slider.coerceIn(-1f, 1f)
        gainProcessor.gain = volumeBoostToGain(value)
        _state.update { it.copy(volumeBoost = value) }
    }

    fun equalizer(): EqController = eq

    fun stopAndClear() {
        if (::player.isInitialized) {
            persistProgressBlocking()
            runCatching { player.stop() }
            runCatching { player.clearMediaItems() }
        }
        currentEpisode = null
        restoreSleepVolume()
        _state.update {
            PlayerUiState(
                skipForwardSec = it.skipForwardSec,
                skipBackSec = it.skipBackSec,
                volumeBoost = it.volumeBoost,
                speed = it.speed
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
                applyVolumeBoost(settings.volumeBoost)
            }
        }
    }

    private fun startProgress() {
        progressJob = scope.launch {
            while (isActive) {
                publish()
                tickSleepTimer()
                persistProgress()
                delay(1_000)
            }
        }
    }

    private fun tickSleepTimer() {
        if (!::player.isInitialized) return
        val current = _state.value
        val remaining = SleepTimerLogic.remainingMs(
            nowMs = System.currentTimeMillis(),
            deadlineMs = current.sleepDeadlineMs,
            endOfEpisode = current.sleepEndOfEpisode,
            positionMs = player.currentPosition.coerceAtLeast(0),
            durationMs = player.duration.takeIf { it > 0 } ?: current.durationMs
        )
        _state.update { it.copy(sleepRemainingMs = remaining ?: 0L) }
        if (remaining == null) {
            restoreSleepVolume()
            return
        }
        if (remaining <= 0L) {
            if (player.isPlaying) player.pause()
            finishSleepTimer()
            return
        }
        applySleepVolume(remaining)
    }

    private fun applySleepVolume(remainingMs: Long? = null) {
        if (!::exo.isInitialized) return
        val remaining = remainingMs ?: SleepTimerLogic.remainingMs(
            nowMs = System.currentTimeMillis(),
            deadlineMs = _state.value.sleepDeadlineMs,
            endOfEpisode = _state.value.sleepEndOfEpisode,
            positionMs = if (::player.isInitialized) player.currentPosition.coerceAtLeast(0) else _state.value.positionMs,
            durationMs = if (::player.isInitialized) {
                player.duration.takeIf { it > 0 } ?: _state.value.durationMs
            } else {
                _state.value.durationMs
            }
        )
        if (remaining == null) {
            restoreSleepVolume()
            return
        }
        exo.volume = SleepTimerLogic.fadeVolume(remaining)
    }

    private fun restoreSleepVolume() {
        if (::exo.isInitialized) exo.volume = 1f
    }

    private fun finishSleepTimer() {
        restoreSleepVolume()
        _state.update {
            it.copy(
                sleepDeadlineMs = 0L,
                sleepEndOfEpisode = false,
                sleepRemainingMs = 0L,
                sleepMinutes = 0
            )
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
        val position = player.currentPosition.coerceAtLeast(0)
        val remaining = SleepTimerLogic.remainingMs(
            nowMs = System.currentTimeMillis(),
            deadlineMs = _state.value.sleepDeadlineMs,
            endOfEpisode = _state.value.sleepEndOfEpisode,
            positionMs = position,
            durationMs = duration.coerceAtLeast(0)
        )
        _state.update {
            it.copy(
                playing = player.isPlaying,
                positionMs = position,
                durationMs = duration.coerceAtLeast(0),
                sleepRemainingMs = remaining ?: 0L
            )
        }
    }

    private fun refreshMediaNotification() {
        val active = session ?: return
        // Media3 posts a MediaStyle notification tied to the session
        // (required for lock screen + system tray player).
        onUpdateNotification(active, true)
    }

    private fun enterForeground(text: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
        val active = session
        if (active != null) {
            builder.setStyle(MediaStyleNotificationHelper.MediaStyle(active))
            builder.setContentIntent(active.sessionActivity)
        }
        val type = if (Build.VERSION.SDK_INT >= 29) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, builder.build(), type)
    }

    private inner class ButtonCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val result = super.onConnect(session, controller)
            val sessionCommands = (
                if (result.isAccepted) result.availableSessionCommands
                else MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
            ).buildUpon()
                .add(SessionCommand(ACTION_REWIND, Bundle.EMPTY))
                .add(SessionCommand(ACTION_FORWARD, Bundle.EMPTY))
                .build()
            val playerCommands = (
                if (result.isAccepted) result.availablePlayerCommands
                else MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
            ).buildUpon()
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_FORWARD)
                .add(Player.COMMAND_SEEK_BACK)
                .build()
            return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_REWIND -> skip(false)
                ACTION_FORWARD -> skip(true)
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }


    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "playback_media_v2"
        const val ACTION_REWIND = "openpod.REWIND"
        const val ACTION_FORWARD = "openpod.FORWARD"
        private val _state = MutableStateFlow(PlayerUiState())
        val state = _state.asStateFlow()
        var instance: PlayerService? = null
            private set
        private var pending: Triple<EpisodeEntity, String, String>? = null
        private var pendingSeekMs: Long = -1L

        fun queueSeek(positionMs: Long) {
            pendingSeekMs = positionMs.coerceAtLeast(0)
            _state.update { it.copy(positionMs = pendingSeekMs) }
        }

        fun seekOrQueue(positionMs: Long) {
            val svc = instance
            if (svc != null) svc.seek(positionMs) else queueSeek(positionMs)
        }

        fun setSkipSeconds(forward: Int, back: Int) {
            val fwd = SkipIntervals.nearest(forward)
            val backSec = SkipIntervals.nearest(back)
            _state.update { it.copy(skipForwardSec = fwd, skipBackSec = backSec) }
        }

        fun setSleepMinutes(minutes: Int) {
            val duration = minutes.coerceAtLeast(1)
            val end = System.currentTimeMillis() + duration * 60_000L
            _state.update {
                it.copy(
                    sleepDeadlineMs = end,
                    sleepRemainingMs = duration * 60_000L,
                    sleepMinutes = duration
                )
            }
            instance?.applySleepVolume()
        }

        fun setSleepEndOfEpisode(enabled: Boolean) {
            _state.update { it.copy(sleepEndOfEpisode = enabled) }
            instance?.applySleepVolume()
        }

        fun cancelSleepTimer() {
            _state.update {
                it.copy(
                    sleepDeadlineMs = 0L,
                    sleepEndOfEpisode = false,
                    sleepRemainingMs = 0L,
                    sleepMinutes = 0
                )
            }
            instance?.restoreSleepVolume()
        }

        fun setSpeed(speed: Float) {
            val clamped = speed.coerceIn(0.5f, 5.0f)
            _state.update { it.copy(speed = clamped) }
            instance?.setSpeed(clamped)
        }

        fun setVolumeBoost(slider: Float) {
            val value = slider.coerceIn(-1f, 1f)
            _state.update { it.copy(volumeBoost = value) }
            instance?.applyVolumeBoost(value)
        }

        fun play(context: android.content.Context, episode: EpisodeEntity, podcastTitle: String, artwork: String) {
            val existing = instance
            if (existing != null) {
                existing.playEpisode(episode, podcastTitle, artwork)
                return
            }
            pending = Triple(episode, podcastTitle, artwork)
            val intent = Intent(context, PlayerService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun restoreSession(episode: EpisodeEntity, podcastTitle: String, artwork: String) {
            if (_state.value.episodeId != 0L && instance != null) return
            _state.update {
                it.copy(
                    episodeId = episode.id,
                    podcastId = episode.podcastId,
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
            pendingSeekMs = -1L
            instance?.stopAndClear()
            if (instance == null) {
                val keep = _state.value
                _state.value = PlayerUiState(
                    skipForwardSec = keep.skipForwardSec,
                    skipBackSec = keep.skipBackSec,
                    volumeBoost = keep.volumeBoost,
                    speed = keep.speed
                )
            }
        }
    }
}

