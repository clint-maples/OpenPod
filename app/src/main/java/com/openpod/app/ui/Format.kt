package com.openpod.app.ui

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

fun stripHtml(raw: String): String {
    return raw.replace(Regex("<[^>]+>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace(Regex("\\s+"), " ")
        .trim()
}
