package com.openpod.app.download

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DownloadProgress(
    val episodeId: Long,
    val bytesRead: Long,
    val totalBytes: Long
) {
    val knownTotal: Boolean get() = totalBytes > 0L
    val fraction: Float
        get() = if (knownTotal) (bytesRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

object DownloadTracker {
    private val _progress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    val progress: StateFlow<Map<Long, DownloadProgress>> = _progress.asStateFlow()

    fun start(episodeId: Long, totalBytes: Long = -1L) {
        _progress.update { it + (episodeId to DownloadProgress(episodeId, 0L, totalBytes)) }
    }

    fun update(episodeId: Long, bytesRead: Long, totalBytes: Long) {
        _progress.update { it + (episodeId to DownloadProgress(episodeId, bytesRead, totalBytes)) }
    }

    fun clear(episodeId: Long) {
        _progress.update { it - episodeId }
    }
}
