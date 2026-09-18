package com.openpod.app.playback

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

/**
 * Single-episode podcast playback still needs SKIP_TO_NEXT/PREV in PlaybackState so
 * System UI MediaControlProfile accepts the MediaStyle notification (API 36+).
 * Map next/prev to seek forward/back instead of playlist navigation.
 */
@UnstableApi
class OpenPodPlayer(
    private val delegate: Player
) : ForwardingPlayer(delegate) {
    override fun getAvailableCommands(): Player.Commands {
        return super.getAvailableCommands()
            .buildUpon()
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_FORWARD)
            .add(Player.COMMAND_SEEK_BACK)
            .build()
    }

    override fun isCommandAvailable(command: Int): Boolean {
        return when (command) {
            Player.COMMAND_SEEK_TO_NEXT,
            Player.COMMAND_SEEK_TO_PREVIOUS,
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
            Player.COMMAND_SEEK_FORWARD,
            Player.COMMAND_SEEK_BACK -> true
            else -> super.isCommandAvailable(command)
        }
    }

    override fun getSeekBackIncrement(): Long {
        return PlayerService.state.value.skipBackSec * 1000L
    }

    override fun getSeekForwardIncrement(): Long {
        return PlayerService.state.value.skipForwardSec * 1000L
    }

    override fun seekBack() {
        PlayerService.instance?.skip(false) ?: delegate.seekBack()
    }

    override fun seekForward() {
        PlayerService.instance?.skip(true) ?: delegate.seekForward()
    }

    override fun seekToNext() {
        seekForward()
    }

    override fun seekToPrevious() {
        seekBack()
    }

    override fun seekToNextMediaItem() {
        seekForward()
    }

    override fun seekToPreviousMediaItem() {
        seekBack()
    }

    override fun hasNextMediaItem(): Boolean = true

    override fun hasPreviousMediaItem(): Boolean = true
}
