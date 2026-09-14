package com.openpod.app.ui

import android.text.Spanned
import androidx.core.text.HtmlCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}

fun formatDate(ms: Long): String {
    if (ms <= 0) return ""
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(ms))
}

const val SPEED_MIN = 0.5f
const val SPEED_MAX = 5.0f
const val SPEED_STEP = 0.1f

fun clampSpeed(value: Float): Float {
    val rounded = kotlin.math.round(value * 10f) / 10f
    return rounded.coerceIn(SPEED_MIN, SPEED_MAX)
}

fun formatSpeed(speed: Float): String = String.format(Locale.US, "%.1f", clampSpeed(speed))

fun formatBytes(bytes: Long): String {
    if (bytes < 0L) return "—"
    if (bytes < 1024L) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024.0) return String.format(Locale.US, "%.1f MB", mb)
    return String.format(Locale.US, "%.2f GB", mb / 1024.0)
}

fun publisherHtml(raw: String): String {
    val text = raw.trim()
    if (text.isEmpty()) return ""
    val hasBlock = Regex("(?i)<(p|br|div|li|h[1-6]|ul|ol|blockquote)\\b").containsMatchIn(text)
    return if (hasBlock) text else text.replace("\r\n", "\n").replace("\n", "<br>")
}

fun htmlToSpanned(raw: String): Spanned {
    return HtmlCompat.fromHtml(publisherHtml(raw), HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun stripHtml(raw: String): String {
    return htmlToSpanned(raw).toString()
        .replace(Regex("[ \\t\\x0B\\f\\r]+"), " ")
        .replace(Regex(" *\\n *"), "\n")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}
