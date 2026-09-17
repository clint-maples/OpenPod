package com.openpod.app.data

import org.json.JSONArray
import org.json.JSONObject

object ChapterJson {
    fun parse(body: String, durationMs: Long = 0L): List<Chapter> {
        val trimmed = body.trim().removePrefix("\uFEFF")
        if (trimmed.isEmpty()) return emptyList()
        val arr = if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            JSONObject(trimmed).optJSONArray("chapters") ?: return emptyList()
        }
        val raw = ArrayList<Pair<Double, String>>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val start = readChapterStart(obj) ?: continue
            val title = obj.optString("title").ifBlank { "Chapter ${i + 1}" }
            raw += start to title
        }
        if (raw.isEmpty()) return emptyList()
        val starts = ChapterNav.normalizeStartMs(raw.map { it.first }, durationMs)
        return raw.zip(starts) { pair, ms -> Chapter(ms, pair.second) }.sortedBy { it.startMs }
    }

    private fun readChapterStart(obj: JSONObject): Double? {
        for (key in listOf("startTime", "start", "start_time")) {
            if (!obj.has(key)) continue
            val numeric = obj.optDouble(key, Double.NaN)
            if (!numeric.isNaN() && numeric >= 0.0) return numeric
            ChapterNav.parseClockToMs(obj.optString(key))?.let { return it / 1000.0 }
        }
        return null
    }
}
