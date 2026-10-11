package com.openpod.app.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackResumeTest {
    @Test
    fun idleServiceWithRestoredEpisodeReloads() {
        // Sticky-restarted service, or restoreSession filled the UI only.
        assertEquals(
            PlaybackResume.Command.RELOAD,
            PlaybackResume.command(
                serviceConnected = true,
                playerInitialized = true,
                mediaItemCount = 0,
                playbackState = PlaybackResume.STATE_IDLE,
                hasPlayerError = false,
                isPlaying = false,
                loadedEpisodeId = 0L,
                requestedEpisodeId = 42L
            )
        )
    }

    @Test
    fun missingServiceReloads() {
        assertEquals(
            PlaybackResume.Command.RELOAD,
            PlaybackResume.command(
                serviceConnected = false,
                playerInitialized = false,
                mediaItemCount = 0,
                playbackState = PlaybackResume.STATE_IDLE,
                hasPlayerError = false,
                isPlaying = false,
                loadedEpisodeId = 0L,
                requestedEpisodeId = 42L
            )
        )
    }

    @Test
    fun idleOrErroredPlayerReloadsEvenWithAMediaItem() {
        assertEquals(
            PlaybackResume.Command.RELOAD,
            PlaybackResume.command(
                serviceConnected = true,
                playerInitialized = true,
                mediaItemCount = 1,
                playbackState = PlaybackResume.STATE_IDLE,
                hasPlayerError = false,
                isPlaying = false,
                loadedEpisodeId = 42L,
                requestedEpisodeId = 42L
            )
        )
        assertEquals(
            PlaybackResume.Command.RELOAD,
            PlaybackResume.command(
                serviceConnected = true,
                playerInitialized = true,
                mediaItemCount = 1,
                playbackState = PlaybackResume.STATE_IDLE,
                hasPlayerError = true,
                isPlaying = false,
                loadedEpisodeId = 42L,
                requestedEpisodeId = 42L
            )
        )
    }

    @Test
    fun endedPlayerReloadsFromTheSavedEpisode() {
        assertEquals(
            PlaybackResume.Command.RELOAD,
            PlaybackResume.command(
                serviceConnected = true,
                playerInitialized = true,
                mediaItemCount = 1,
                playbackState = PlaybackResume.STATE_ENDED,
                hasPlayerError = false,
                isPlaying = false,
                loadedEpisodeId = 42L,
                requestedEpisodeId = 42L
            )
        )
    }

    @Test
    fun pausedReadyPlayerResumesWithoutReloading() {
        assertEquals(
            PlaybackResume.Command.RESUME,
            PlaybackResume.command(
                serviceConnected = true,
                playerInitialized = true,
                mediaItemCount = 1,
                playbackState = PlaybackResume.STATE_READY,
                hasPlayerError = false,
                isPlaying = false,
                loadedEpisodeId = 42L,
                requestedEpisodeId = 42L
            )
        )
        assertEquals(
            PlaybackResume.Command.RESUME,
            PlaybackResume.command(
                serviceConnected = true,
                playerInitialized = true,
                mediaItemCount = 1,
                playbackState = PlaybackResume.STATE_BUFFERING,
                hasPlayerError = false,
                isPlaying = false,
                loadedEpisodeId = 42L,
                requestedEpisodeId = 42L
            )
        )
    }

    @Test
    fun playingPlayerPauses() {
        assertEquals(
            PlaybackResume.Command.PAUSE,
            PlaybackResume.command(
                serviceConnected = true,
                playerInitialized = true,
                mediaItemCount = 1,
                playbackState = PlaybackResume.STATE_READY,
                hasPlayerError = false,
                isPlaying = true,
                loadedEpisodeId = 42L,
                requestedEpisodeId = 42L
            )
        )
    }

    @Test
    fun differentEpisodeAlwaysReloads() {
        assertEquals(
            PlaybackResume.Command.RELOAD,
            PlaybackResume.command(
                serviceConnected = true,
                playerInitialized = true,
                mediaItemCount = 1,
                playbackState = PlaybackResume.STATE_READY,
                hasPlayerError = false,
                isPlaying = true,
                loadedEpisodeId = 7L,
                requestedEpisodeId = 42L
            )
        )
    }
}
