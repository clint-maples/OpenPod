package com.openpod.app.playback

/**
 * What a Play tap should do.
 *
 * After a long idle the screen can still show the last episode while ExoPlayer
 * was never prepared: the service was sticky-restarted empty, the process died
 * and [PlayerService.restoreSession] only filled the UI, or the player dropped
 * to idle/error. [androidx.media3.common.Player.play] does not leave STATE_IDLE,
 * so those taps must reload the episode instead of calling play().
 */
object PlaybackResume {
    const val STATE_IDLE = 1
    const val STATE_BUFFERING = 2
    const val STATE_READY = 3
    const val STATE_ENDED = 4

    enum class Command { PAUSE, RESUME, RELOAD }

    fun command(
        serviceConnected: Boolean,
        playerInitialized: Boolean,
        mediaItemCount: Int,
        playbackState: Int,
        hasPlayerError: Boolean,
        isPlaying: Boolean,
        loadedEpisodeId: Long,
        requestedEpisodeId: Long
    ): Command {
        val differentEpisode = requestedEpisodeId != 0L && loadedEpisodeId != requestedEpisodeId
        val needsReload = !serviceConnected ||
            !playerInitialized ||
            mediaItemCount <= 0 ||
            hasPlayerError ||
            playbackState == STATE_IDLE ||
            playbackState == STATE_ENDED ||
            differentEpisode
        if (needsReload) return Command.RELOAD
        return if (isPlaying) Command.PAUSE else Command.RESUME
    }
}
