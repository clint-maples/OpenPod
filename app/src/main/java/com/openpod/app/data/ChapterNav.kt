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
        val maxRaw = rawValues.maxOrNull() ?: 0.0
        // Podcast Index startTime is seconds. Only treat as milliseconds when
        // seconds would overshoot a known duration, or values exceed 24h in seconds.
        // Do NOT use a 10_000 cutoff when duration is missing: a 3h Lex episode
        // has chapter starts around 12_000 seconds, which that rule collapsed
        // into ~12s seeks (looks like a no-op).
        val useMs = when {
            durationMs > 0L &&
                asSec.any { it > durationMs + 5_000L } &&
                asMs.all { it <= durationMs + 5_000L } -> true
            durationMs <= 0L && maxRaw > 86_400.0 -> true
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

    fun parseOutlineChapters(html: String): List<Chapter> {
        if (html.isBlank()) return emptyList()
        val text = html
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>"), "\n")
        // Dash after ) is optional: Lex uses "(00:00) – Title", All-In uses "(0:00) Title".
        val leadingClockRe = Regex(
            """^\s*\((\d{1,2}:\d{2}(?::\d{2})?)\)\s*(?:[-–—]\s*)?(.+?)\s*$"""
        )
        // Experts Only Radio: "Artist - Track 00:04:07" (clock at end of the line).
        val trailingClockRe = Regex(
            """^\s*(.+?)\s+(\d{1,2}:\d{2}(?::\d{2})?)\s*$"""
        )
        val out = ArrayList<Chapter>()
        for (raw in text.lineSequence()) {
            val line = decodeBasicEntities(raw.replace(Regex("<[^>]+>"), "")).trim()
            if (line.isEmpty()) continue
            val chapter = parseOutlineLine(line, leadingClockRe, trailingClockRe) ?: continue
            out += chapter
        }
        val chapters = out.distinctBy { it.startMs }.sortedBy { it.startMs }
        return if (chapters.size >= 2) chapters else emptyList()
    }

    private fun parseOutlineLine(
        line: String,
        leadingClockRe: Regex,
        trailingClockRe: Regex
    ): Chapter? {
        leadingClockRe.matchEntire(line)?.let { match ->
            val startMs = parseClockToMs(match.groupValues[1]) ?: return null
            val title = match.groupValues[2].trim()
            return title.takeIf { it.isNotEmpty() }?.let { Chapter(startMs, it) }
        }
        trailingClockRe.matchEntire(line)?.let { match ->
            val title = match.groupValues[1].trim()
            val startMs = parseClockToMs(match.groupValues[2]) ?: return null
            return title.takeIf { it.isNotEmpty() }?.let { Chapter(startMs, it) }
        }
        return null
    }

    private fun decodeBasicEntities(value: String): String {
        var text = numericEntity.decode(value)
        text = text.replace("&ndash;", "–")
            .replace("&mdash;", "—")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
        return text
    }

    private val numericEntity = Regex("&#(\\d+);")

    private fun Regex.decode(value: String): String {
        return replace(value) { match ->
            match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: match.value
        }
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
