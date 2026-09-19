package com.openpod.app.playback

object EpisodeTrim {
    const val MIN_SEC = 0
    const val MAX_SEC = 1800

    fun sanitize(seconds: Int): Int = seconds.coerceIn(MIN_SEC, MAX_SEC)

    fun parse(text: String, fallback: Int = 0): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return sanitize(fallback)
        val parsed = trimmed.toIntOrNull() ?: return sanitize(fallback)
        return sanitize(parsed)
    }

    fun introMs(skipIntroSec: Int): Long = sanitize(skipIntroSec) * 1000L

    fun outroMs(skipOutroSec: Int): Long = sanitize(skipOutroSec) * 1000L

    /**
     * Playable end of the episode: duration minus outro, never before the intro
     * start, and never empty when the file has a duration.
     */
    fun effectiveEndMs(durationMs: Long, skipOutroSec: Int, skipIntroSec: Int = 0): Long {
        if (durationMs <= 0L) return 0L
        val intro = introMs(skipIntroSec).coerceAtMost(durationMs)
        val rawEnd = durationMs - outroMs(skipOutroSec)
        val minPlayable = 1_000L
        val floor = if (intro + minPlayable <= durationMs) intro + minPlayable else durationMs
        return rawEnd.coerceIn(floor, durationMs)
    }

    fun startPositionMs(
        requestedMs: Long,
        skipIntroSec: Int,
        skipOutroSec: Int,
        durationMs: Long
    ): Long {
        val intro = introMs(skipIntroSec)
        val end = effectiveEndMs(durationMs, skipOutroSec, skipIntroSec)
        if (requestedMs <= 0L) {
            return if (end > 0L) intro.coerceAtMost(end) else intro
        }
        if (end > 0L && requestedMs >= end) return end
        return requestedMs
    }

    fun shouldStopForOutro(
        positionMs: Long,
        durationMs: Long,
        skipOutroSec: Int,
        skipIntroSec: Int = 0
    ): Boolean {
        if (sanitize(skipOutroSec) <= 0 || durationMs <= 0L) return false
        return positionMs >= effectiveEndMs(durationMs, skipOutroSec, skipIntroSec)
    }

    fun isCompleted(
        positionMs: Long,
        durationMs: Long,
        skipOutroSec: Int,
        skipIntroSec: Int = 0
    ): Boolean {
        if (durationMs <= 0L) return false
        val end = effectiveEndMs(durationMs, skipOutroSec, skipIntroSec)
        val slack = if (sanitize(skipOutroSec) > 0) 0L else 5_000L
        return positionMs >= (end - slack).coerceAtLeast(0L)
    }
}
