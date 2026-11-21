package com.openpod.app.data.search

import com.openpod.app.data.Http
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class ChartShow(
    val appleId: String,
    val name: String,
    val artistName: String,
    val artworkUrl: String
)

data class PodcastGenre(val id: String, val name: String)

/** Apple's current top-level podcast genres. All 19 returned 100 entries on 2026-11-21. */
object TopPodcastGenres {
    val all: List<PodcastGenre> = listOf(
        PodcastGenre("1301", "Arts"),
        PodcastGenre("1321", "Business"),
        PodcastGenre("1303", "Comedy"),
        PodcastGenre("1304", "Education"),
        PodcastGenre("1483", "Fiction"),
        PodcastGenre("1511", "Government"),
        PodcastGenre("1512", "Health & Fitness"),
        PodcastGenre("1487", "History"),
        PodcastGenre("1305", "Kids & Family"),
        PodcastGenre("1502", "Leisure"),
        PodcastGenre("1310", "Music"),
        PodcastGenre("1489", "News"),
        PodcastGenre("1314", "Religion & Spirituality"),
        PodcastGenre("1533", "Science"),
        PodcastGenre("1324", "Society & Culture"),
        PodcastGenre("1545", "Sports"),
        PodcastGenre("1318", "Technology"),
        PodcastGenre("1488", "True Crime"),
        PodcastGenre("1309", "TV & Film")
    ).sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
}

object TopPodcastFeeds {
    const val TOP_US = "https://rss.marketingtools.apple.com/api/v2/us/podcasts/top/100/podcasts.json"

    fun genreUrl(genreId: String): String =
        "https://itunes.apple.com/us/rss/toppodcasts/limit=100/genre=$genreId/json"

    fun cacheKey(genreId: String?): String = genreId?.ifBlank { null } ?: "all"
}

object TopPodcastParser {
    fun parseMarketingFeed(body: String): List<ChartShow> {
        val results = JSONObject(body).optJSONObject("feed")?.optJSONArray("results") ?: return emptyList()
        val shows = mutableListOf<ChartShow>()
        for (i in 0 until results.length()) {
            val obj = results.optJSONObject(i) ?: continue
            val id = obj.optString("id").trim()
            val name = obj.optString("name").trim()
            if (id.isEmpty() || name.isEmpty()) continue
            shows += ChartShow(
                appleId = id,
                name = name,
                artistName = obj.optString("artistName").trim(),
                artworkUrl = obj.optString("artworkUrl100").trim()
            )
        }
        return shows.distinctBy { it.appleId }
    }

    fun parseLegacyGenreFeed(body: String): List<ChartShow> {
        val entry = JSONObject(body).optJSONObject("feed")?.opt("entry") ?: return emptyList()
        val array = when (entry) {
            is JSONArray -> entry
            is JSONObject -> JSONArray().put(entry)
            else -> return emptyList()
        }
        val shows = mutableListOf<ChartShow>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val id = obj.optJSONObject("id")?.optJSONObject("attributes")?.optString("im:id").orEmpty().trim()
            val name = obj.optJSONObject("im:name")?.optString("label").orEmpty().trim()
            if (id.isEmpty() || name.isEmpty()) continue
            shows += ChartShow(
                appleId = id,
                name = name,
                artistName = obj.optJSONObject("im:artist")?.optString("label").orEmpty().trim(),
                artworkUrl = largestImage(obj.optJSONArray("im:image"))
            )
        }
        return shows.distinctBy { it.appleId }
    }

    private fun largestImage(images: JSONArray?): String {
        if (images == null || images.length() == 0) return ""
        var bestUrl = ""
        var bestHeight = -1
        for (i in 0 until images.length()) {
            val image = images.optJSONObject(i) ?: continue
            val url = image.optString("label").trim()
            val height = image.optJSONObject("attributes")?.optString("height")?.toIntOrNull() ?: i
            if (url.isNotEmpty() && height >= bestHeight) {
                bestHeight = height
                bestUrl = url
            }
        }
        return bestUrl
    }
}

/** What to do when a chart request cannot be refreshed. */
enum class ChartMiss {
    ServeStale,
    HideGenresAndShowAll,
    Retry
}

object ChartLoadPolicy {
    fun onMiss(genreSelected: Boolean, hasStaleCache: Boolean): ChartMiss = when {
        hasStaleCache -> ChartMiss.ServeStale
        genreSelected -> ChartMiss.HideGenresAndShowAll
        else -> ChartMiss.Retry
    }
}

class ChartCache(
    private val directory: File,
    private val ttlMs: Long = 60L * 60L * 1000L
) {
    private val memory = LinkedHashMap<String, CachedChart>()

    data class CachedChart(val fetchedAtMs: Long, val shows: List<ChartShow>)

    @Synchronized
    fun read(key: String): CachedChart? {
        memory[key]?.let { return it }
        val file = fileFor(key)
        if (!file.isFile) return null
        val parsed = runCatching { decode(file.readText()) }.getOrNull() ?: return null
        memory[key] = parsed
        return parsed
    }

    fun fresh(key: String, nowMs: Long): List<ChartShow>? {
        val cached = read(key) ?: return null
        if (nowMs - cached.fetchedAtMs > ttlMs) return null
        return cached.shows
    }

    fun stale(key: String): List<ChartShow>? = read(key)?.shows

    @Synchronized
    fun write(key: String, shows: List<ChartShow>, nowMs: Long) {
        val cached = CachedChart(nowMs, shows)
        memory[key] = cached
        val file = fileFor(key)
        file.parentFile?.mkdirs()
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(encode(cached))
        if (!tmp.renameTo(file)) {
            file.writeText(encode(cached))
            tmp.delete()
        }
    }

    private fun fileFor(key: String): File {
        val safe = key.filter { it.isLetterOrDigit() }.ifBlank { "all" }
        return File(directory, "$safe.json")
    }

    private fun encode(cached: CachedChart): String {
        val shows = JSONArray()
        cached.shows.forEach { show ->
            shows.put(
                JSONObject()
                    .put("appleId", show.appleId)
                    .put("name", show.name)
                    .put("artistName", show.artistName)
                    .put("artworkUrl", show.artworkUrl)
            )
        }
        return JSONObject().put("fetchedAtMs", cached.fetchedAtMs).put("shows", shows).toString()
    }

    private fun decode(body: String): CachedChart {
        val obj = JSONObject(body)
        val showsJson = obj.optJSONArray("shows") ?: JSONArray()
        val shows = mutableListOf<ChartShow>()
        for (i in 0 until showsJson.length()) {
            val show = showsJson.optJSONObject(i) ?: continue
            val id = show.optString("appleId")
            val name = show.optString("name")
            if (id.isBlank() || name.isBlank()) continue
            shows += ChartShow(id, name, show.optString("artistName"), show.optString("artworkUrl"))
        }
        return CachedChart(obj.optLong("fetchedAtMs"), shows)
    }
}

class TopPodcastCharts(
    private val cache: ChartCache,
    private val fetch: (String) -> String = Http::get
) {
    fun load(genreId: String?, nowMs: Long): List<ChartShow>? {
        val key = TopPodcastFeeds.cacheKey(genreId)
        cache.fresh(key, nowMs)?.let { return it }
        val downloaded = runCatching {
            val body = if (genreId.isNullOrBlank()) {
                fetch(TopPodcastFeeds.TOP_US)
            } else {
                fetch(TopPodcastFeeds.genreUrl(genreId))
            }
            val shows = if (genreId.isNullOrBlank()) {
                TopPodcastParser.parseMarketingFeed(body)
            } else {
                TopPodcastParser.parseLegacyGenreFeed(body)
            }
            if (shows.isEmpty()) error("empty chart")
            shows
        }
        downloaded.onSuccess { cache.write(key, it, nowMs) }
        return downloaded.getOrNull() ?: cache.stale(key)
    }

    fun hasStale(genreId: String?): Boolean = cache.stale(TopPodcastFeeds.cacheKey(genreId)) != null
}
