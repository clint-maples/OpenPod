package com.openpod.app.playback

object SkipIntervals {
    const val DEFAULT_BACK = 15
    const val DEFAULT_FORWARD = 30
    const val MIN_SEC = 1
    const val MAX_SEC = 600

    fun sanitize(seconds: Int): Int = seconds.coerceIn(MIN_SEC, MAX_SEC)

    fun parse(text: String, fallback: Int): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return sanitize(fallback)
        val parsed = trimmed.toIntOrNull() ?: return sanitize(fallback)
        return sanitize(parsed)
    }

    fun compactLabel(seconds: Int, forward: Boolean): String {
        // Visible skip counts on Now Playing are unsigned ("15" / "60").
        // The unused `forward` flag is kept so existing call sites stay the same.
        return sanitize(seconds).toString()
    }

    fun contentDescription(seconds: Int, forward: Boolean): String {
        val amount = sanitize(seconds)
        return if (forward) "Forward ${amount}s" else "Back ${amount}s"
    }
}
