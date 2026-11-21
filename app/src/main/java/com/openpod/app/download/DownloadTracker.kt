package com.openpod.app.download

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DownloadProgress(
    val episodeId: Long,
    val bytesRead: Long,
    val totalBytes: Long,
    val updatedAtElapsed: Long = 0L
) {
    val knownTotal: Boolean get() = totalBytes > 0L
    val fraction: Float
        get() = if (knownTotal) (bytesRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

object DownloadTracker {
    private val _progress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    val progress: StateFlow<Map<Long, DownloadProgress>> = _progress.asStateFlow()

    fun start(episodeId: Long, totalBytes: Long = -1L) {
        _progress.update {
            it + (episodeId to DownloadProgress(episodeId, 0L, totalBytes, SystemClock.elapsedRealtime()))
        }
    }

    fun update(episodeId: Long, bytesRead: Long, totalBytes: Long) {
        _progress.update {
            it + (episodeId to DownloadProgress(episodeId, bytesRead, totalBytes, SystemClock.elapsedRealtime()))
        }
    }

    fun clear(episodeId: Long) {
        _progress.update { it - episodeId }
    }

    /** True when this process has seen bytes for the episode inside [AutoDownloadPlan.STALE_PROGRESS_MS]. */
    fun isProgressFresh(episodeId: Long, nowElapsed: Long = SystemClock.elapsedRealtime()): Boolean {
        val updatedAt = _progress.value[episodeId]?.updatedAtElapsed ?: return false
        if (updatedAt <= 0L) return false
        return nowElapsed - updatedAt < AutoDownloadPlan.STALE_PROGRESS_MS
    }
}
