package com.openpod.app.playback

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Picks the URI ExoPlayer should open. A downloaded file wins over the
 * enclosure URL, which may have expired or redirected while the app was idle.
 */
object PlaybackSource {
    fun choose(existingLocalUri: String?, remoteUrl: String?): String {
        val local = existingLocalUri?.trim().orEmpty()
        if (local.isNotEmpty()) return local
        return remoteUrl?.trim().orEmpty()
    }

    fun existingLocalUri(context: Context, localUri: String): String? {
        val uri = localUri.trim()
        if (uri.isEmpty()) return null
        val exists = try {
            DocumentFile.fromSingleUri(context, Uri.parse(uri))?.exists() == true
        } catch (_: Exception) {
            false
        }
        return if (exists) uri else null
    }
}
