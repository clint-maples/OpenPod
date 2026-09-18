package com.openpod.app.data.rss

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class ParsedPodcast(
    val title: String,
    val author: String,
    val description: String,
    val imageUrl: String,
    val siteUrl: String,
    val episodes: List<ParsedEpisode>
)

data class ParsedEpisode(
    val guid: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val mimeType: String,
    val pubDate: Long,
    val durationMs: Long,
    val episodeNumber: Int?,
    val seasonNumber: Int?,
    val imageUrl: String,
    val transcriptUrl: String,
    val transcriptType: String,
    val chaptersUrl: String
)

object RssParser {
    private val dateFormats = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm:ss z",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssZ"
    ).map {
        SimpleDateFormat(it, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    }

    fun parse(xml: String): ParsedPodcast {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(StringReader(xml.trim().removePrefix("\uFEFF")))
        var event = parser.eventType
        var title = ""
        var author = ""
        var description = ""
        var imageUrl = ""
        var siteUrl = ""
        val episodes = mutableListOf<ParsedEpisode>()
        var inItem = false
        var inImage = false
        var inOwner = false
        var item = ItemScratch()

        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                val name = parser.name.orEmpty()
                val href = attr(parser, "href")
                val urlAttr = attr(parser, "url")
                when {
                    name.equals("item", true) -> {
                        inItem = true
                        item = ItemScratch()
                    }
                    name.equals("owner", true) && !inItem -> inOwner = true
                    name.equals("image", true) && !inItem -> {
                        inImage = true
                        if (href.isNotBlank()) imageUrl = href
                    }
                    !inItem && name.equals("title", true) && !inImage && !inOwner ->
                        title = parser.readInnerText()
                    !inItem && name.equals("author", true) && !inOwner ->
                        if (author.isBlank()) author = parser.readInnerText()
                    !inItem && name.equals("name", true) && inOwner ->
                        if (author.isBlank()) author = parser.readInnerText()
                    !inItem && (name.equals("description", true) || name.equals("summary", true)) ->
                        if (description.isBlank()) description = parser.readInnerText()
                    !inItem && name.equals("link", true) -> {
                        if (href.isNotBlank() && siteUrl.isBlank()) siteUrl = href
                        else {
                            val text = parser.readInnerText()
                            if (siteUrl.isBlank() && text.startsWith("http")) siteUrl = text
                        }
                    }
                    !inItem && name.equals("url", true) && inImage ->
                        imageUrl = parser.readInnerText()
                    inItem && name.equals("title", true) ->
                        if (item.title.isBlank()) item.title = parser.readInnerText()
                    inItem && name.equals("guid", true) -> item.guid = parser.readInnerText()
                    inItem && (name.equals("description", true) || name.equals("summary", true) || name.equals("encoded", true)) -> {
                        val text = parser.readInnerText()
                        if (text.length > item.description.length) item.description = text
                    }
                    inItem && name.equals("pubDate", true) ->
                        item.pubDate = parseDate(parser.readInnerText())
                    inItem && name.equals("duration", true) ->
                        item.durationMs = parseDuration(parser.readInnerText())
                    inItem && name.equals("episode", true) ->
                        item.episodeNumber = parser.readInnerText().toIntOrNull() ?: item.episodeNumber
                    inItem && name.equals("season", true) ->
                        item.seasonNumber = parser.readInnerText().toIntOrNull() ?: item.seasonNumber
                    inItem && name.equals("enclosure", true) -> {
                        item.audioUrl = urlAttr.ifBlank { href }
                        item.mimeType = attr(parser, "type").ifBlank { "audio/mpeg" }
                    }
                    inItem && name.equals("transcript", true) -> {
                        if (urlAttr.isNotBlank()) {
                            item.transcriptUrl = urlAttr
                            item.transcriptType = attr(parser, "type")
                        }
                    }
                    inItem && name.equals("chapters", true) -> {
                        val chapterUrl = urlAttr.ifBlank { href }
                        if (chapterUrl.isNotBlank()) item.chaptersUrl = chapterUrl
                    }
                    inItem && name.equals("image", true) -> {
                        if (href.isNotBlank()) item.imageUrl = href
                        else if (urlAttr.isNotBlank()) item.imageUrl = urlAttr
                    }
                }
            } else if (event == XmlPullParser.END_TAG) {
                val name = parser.name.orEmpty()
                if (name.equals("image", true)) inImage = false
                if (name.equals("owner", true)) inOwner = false
                if (name.equals("item", true) && inItem) {
                    if (item.audioUrl.isNotBlank()) {
                        if (item.guid.isBlank()) item.guid = item.audioUrl
                        if (item.title.isBlank()) item.title = "Episode"
                        episodes += item.toParsed()
                    }
                    inItem = false
                }
            }
            event = parser.next()
        }
        if (title.isBlank()) title = "Podcast"
        return ParsedPodcast(title, author, description, imageUrl, siteUrl, episodes)
    }

    private fun attr(parser: XmlPullParser, name: String): String {
        for (i in 0 until parser.attributeCount) {
            if (parser.getAttributeName(i).equals(name, true)) {
                return parser.getAttributeValue(i).orEmpty()
            }
        }
        return parser.getAttributeValue(null, name).orEmpty()
    }

    private fun XmlPullParser.readInnerText(): String {
        return try {
            if (isEmptyElementTag) return ""
            val sb = StringBuilder()
            var depth = 1
            while (depth > 0) {
                when (next()) {
                    XmlPullParser.START_TAG -> depth++
                    XmlPullParser.END_TAG -> depth--
                    XmlPullParser.TEXT, XmlPullParser.CDSECT -> if (depth == 1) sb.append(text)
                }
            }
            sb.toString().trim()
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseDate(raw: String): Long {
        if (raw.isBlank()) return 0L
        dateFormats.forEach { fmt ->
            try {
                return fmt.parse(raw)?.time ?: 0L
            } catch (_: Exception) {
            }
        }
        return 0L
    }

    private fun parseDuration(raw: String): Long {
        val parts = raw.trim().split(":").mapNotNull { it.toIntOrNull() }
        return when (parts.size) {
            3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000L
            2 -> (parts[0] * 60 + parts[1]) * 1000L
            1 -> parts[0] * 1000L
            else -> raw.toLongOrNull()?.let { if (it < 10000) it * 1000 else it } ?: 0L
        }
    }

    private data class ItemScratch(
        var guid: String = "",
        var title: String = "",
        var description: String = "",
        var audioUrl: String = "",
        var mimeType: String = "audio/mpeg",
        var pubDate: Long = 0L,
        var durationMs: Long = 0L,
        var episodeNumber: Int? = null,
        var seasonNumber: Int? = null,
        var imageUrl: String = "",
        var transcriptUrl: String = "",
        var transcriptType: String = "",
        var chaptersUrl: String = ""
    ) {
        fun toParsed() = ParsedEpisode(
            guid, title, description, audioUrl, mimeType, pubDate, durationMs,
            episodeNumber, seasonNumber, imageUrl, transcriptUrl, transcriptType, chaptersUrl
        )
    }
}
