package com.openpod.app.data

/**
 * Chapter start times and prev/next targets. Hosts mix seconds and milliseconds
 * in JSON; navigation must seek by time, not by a clamped index that no-ops
 * on the first/last chapter.
 */
object ChapterNav {
    fun normalizeStartMs(raw: Double, durationMs: Long = 0L): Long? {
        if (raw.isNaN() || raw < 0.0) return null
        return normalizeStartMs(listOf(raw), durationMs).firstOrNull()
    }

    fun normalizeStartMs(rawValues: List<Double>, durationMs: Long = 0L): List<Long> {
        if (rawValues.isEmpty()) return emptyList()
        val asSec = rawValues.map { (it * 1000.0).toLong() }
        val asMs = rawValues.map { it.toLong() }
        val useMs = when {
            durationMs > 0L &&
                asSec.any { it > durationMs + 5_000L } &&
                asMs.all { it <= durationMs + 5_000L } -> true
            durationMs <= 0L && (rawValues.maxOrNull() ?: 0.0) >= 10_000.0 -> true
            else -> false
        }
        return if (useMs) asMs else asSec
    }

    fun parseClockToMs(raw: String): Long? {
        val text = raw.trim()
        if (text.isEmpty() || text.toDoubleOrNull() != null) return null
        val parts = text.split(":")
        if (parts.size !in 2..3) return null
        val nums = parts.map { it.toDoubleOrNull() ?: return null }
        val seconds = when (nums.size) {
            3 -> nums[0] * 3600 + nums[1] * 60 + nums[2]
            else -> nums[0] * 60 + nums[1]
        }
        if (seconds < 0) return null
        return (seconds * 1000.0).toLong()
    }

    /**
     * @return start time to seek to, or null when there is no chapter in that direction
     */
    fun seekTargetMs(chapters: List<Chapter>, positionMs: Long, forward: Boolean): Long? {
        val starts = chapters.map { it.startMs }.filter { it >= 0 }.distinct().sorted()
        if (starts.isEmpty()) return null
        return if (forward) {
            starts.firstOrNull { it > positionMs + 400L }
        } else {
            val current = starts.indexOfLast { it <= positionMs }
            if (current < 0) return starts.first()
            val intoChapter = positionMs - starts[current]
            if (intoChapter > 2_000L) starts[current]
            else starts.getOrNull(current - 1) ?: starts[current]
        }
    }
}
