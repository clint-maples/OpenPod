package com.openpod.app.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.openpod.app.data.db.EpisodeEntity

/**
 * Overnight/QA: adb shell am broadcast -a com.openpod.app.DEBUG_PLAY \
 *   -n com.openpod.app/.playback.DebugPlayReceiver \
 *   --es url "https://..." --es title "Test" --es podcast "OpenPod QA"
 *
 * Kept for automated emulator notification repros (works in release builds too).
 */
class DebugPlayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        val url = intent.getStringExtra(EXTRA_URL)?.takeIf { it.isNotBlank() } ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Debug episode"
        val podcast = intent.getStringExtra(EXTRA_PODCAST) ?: "OpenPod QA"
        val episode = EpisodeEntity(
            id = -1L,
            podcastId = -1L,
            guid = "debug-${System.currentTimeMillis()}",
            title = title,
            audioUrl = url,
            durationMs = 0L
        )
        PlayerService.play(context, episode, podcast, "")
    }

    companion object {
        const val ACTION = "com.openpod.app.DEBUG_PLAY"
        const val EXTRA_URL = "url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_PODCAST = "podcast"
    }
}