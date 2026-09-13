package com.openpod.app.data.search

import com.openpod.app.data.Http
import org.json.JSONObject
import java.net.URLEncoder

data class SearchHit(
    val title: String,
    val author: String,
    val feedUrl: String,
    val imageUrl: String,
    val genre: String
)

object ItunesSearch {
    fun search(query: String): List<SearchHit> {
        if (query.isBlank()) return emptyList()
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        val body = Http.get("https://itunes.apple.com/search?media=podcast&limit=30&term=$encoded")
        val results = JSONObject(body).optJSONArray("results") ?: return emptyList()
        val hits = mutableListOf<SearchHit>()
        for (i in 0 until results.length()) {
            val obj = results.optJSONObject(i) ?: continue
            val feed = obj.optString("feedUrl")
            if (feed.isBlank()) continue
            hits += SearchHit(
                title = obj.optString("collectionName"),
                author = obj.optString("artistName"),
                feedUrl = feed,
                imageUrl = obj.optString("artworkUrl600").ifBlank { obj.optString("artworkUrl100") },
                genre = obj.optString("primaryGenreName")
            )
        }
        return hits
    }
}
