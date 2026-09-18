package com.openpod.app.playback

object SleepTimerLogic {
    const val FADE_WINDOW_MS = 20_000L
    val MINUTE_PRESETS = listOf(5, 15, 30, 45, 60)

    fun remainingMs(
        nowMs: Long,
        deadlineMs: Long,
        endOfEpisode: Boolean,
        positionMs: Long,
        durationMs: Long
    ): Long? {
        var remaining: Long? = null
        if (deadlineMs > 0L) {
            remaining = deadlineMs - nowMs
        }
        if (endOfEpisode && durationMs > 0L) {
            val episodeLeft = durationMs - positionMs
            remaining = if (remaining == null) episodeLeft else minOf(remaining, episodeLeft)
        }
        return remaining?.coerceAtLeast(0L)
    }

    fun fadeVolume(remainingMs: Long, fadeWindowMs: Long = FADE_WINDOW_MS): Float {
        if (remainingMs >= fadeWindowMs) return 1f
        if (remainingMs <= 0L) return 0f
        return remainingMs.toFloat() / fadeWindowMs.toFloat()
    }

    fun formatRemaining(remainingMs: Long): String {
        val totalSec = (remainingMs / 1000L).coerceAtLeast(0L)
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }
}
