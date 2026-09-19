package com.openpod.app.data.backup

import com.openpod.app.data.db.PodcastEntity
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

data class OpmlFeed(
    val title: String,
    val xmlUrl: String,
    val htmlUrl: String = "",
    val autoDownload: Boolean? = null,
    val autoDelete: Boolean? = null,
    val folderName: String? = null,
    val skipIntroSec: Int? = null,
    val skipOutroSec: Int? = null
)

data class SubscriptionImportResult(
    val added: Int,
    val existing: Int,
    val failed: Int,
    val removed: Int = 0,
    val addedTitles: List<String> = emptyList(),
    val failedTitles: List<String> = emptyList()
)

object Opml {
    const val OPENPOD_NS = "https://openpod.app/opml"

    fun export(podcasts: List<PodcastEntity>, nowMs: Long = System.currentTimeMillis()): String {
        val created = rfc822(nowMs)
        val body = podcasts.joinToString("\n") { podcast ->
            val title = podcast.title.ifBlank { podcast.feedUrl }
            val attrs = buildString {
                append(" text=\"").append(escapeAttr(title)).append('"')
                append(" title=\"").append(escapeAttr(title)).append('"')
                append(" type=\"rss\"")
                append(" xmlUrl=\"").append(escapeAttr(podcast.feedUrl)).append('"')
                if (podcast.siteUrl.isNotBlank()) {
                    append(" htmlUrl=\"").append(escapeAttr(podcast.siteUrl)).append('"')
                }
                append(" openpod:autoDownload=\"").append(podcast.autoDownload).append('"')
                append(" openpod:autoDelete=\"").append(podcast.autoDelete).append('"')
                if (podcast.folderName.isNotBlank()) {
                    append(" openpod:folderName=\"").append(escapeAttr(podcast.folderName)).append('"')
                }
                if (podcast.skipIntroSec > 0) {
                    append(" openpod:skipIntroSec=\"").append(podcast.skipIntroSec).append('"')
                }
                if (podcast.skipOutroSec > 0) {
                    append(" openpod:skipOutroSec=\"").append(podcast.skipOutroSec).append('"')
                }
            }
            "    <outline$attrs />"
        }
        return """
            |<?xml version="1.0" encoding="UTF-8"?>
            |<opml version="2.0" xmlns:openpod="$OPENPOD_NS">
            |  <head>
            |    <title>OpenPod subscriptions</title>
            |    <dateCreated>$created</dateCreated>
            |  </head>
            |  <body>
            |$body
            |  </body>
            |</opml>
            |
        """.trimMargin()
    }

    fun parse(xml: String): List<OpmlFeed> {
        val trimmed = xml.trim().removePrefix("\uFEFF")
        if (trimmed.isBlank()) return emptyList()
        val builder = secureBuilder()
        val doc = try {
            builder.parse(InputSource(StringReader(trimmed)))
        } catch (e: Exception) {
            throw IllegalArgumentException("This file is not valid OPML or XML", e)
        }
        val nodes = doc.getElementsByTagName("outline")
        val feeds = ArrayList<OpmlFeed>(nodes.length)
        val seen = HashSet<String>()
        for (i in 0 until nodes.length) {
            val el = nodes.item(i) as? Element ?: continue
            val xmlUrl = canonicalizeFeedUrl(firstAttr(el, "xmlUrl", "xmlURL", "xmlurl"))
                .ifBlank {
                    val type = firstAttr(el, "type")
                    if (type.equals("rss", true) || type.equals("atom", true) || type.equals("podcast", true)) {
                        canonicalizeFeedUrl(firstAttr(el, "url"))
                    } else {
                        ""
                    }
                }
            if (!looksLikeFeedUrl(xmlUrl)) continue
            val key = normalizeFeedUrl(xmlUrl)
            if (!seen.add(key)) continue
            val title = firstAttr(el, "text", "title").ifBlank { xmlUrl }
            feeds += OpmlFeed(
                title = title,
                xmlUrl = xmlUrl,
                htmlUrl = firstAttr(el, "htmlUrl", "htmlURL", "htmlurl"),
                autoDownload = parseBool(nsOrLocal(el, "autoDownload")),
                autoDelete = parseBool(nsOrLocal(el, "autoDelete")),
                folderName = nsOrLocal(el, "folderName")?.ifBlank { null },
                skipIntroSec = parseInt(nsOrLocal(el, "skipIntroSec")),
                skipOutroSec = parseInt(nsOrLocal(el, "skipOutroSec"))
            )
        }
        return feeds
    }

    fun normalizeFeedUrl(url: String): String = canonicalizeFeedUrl(url).trimEnd('/').lowercase(Locale.US)

    fun canonicalizeFeedUrl(url: String): String {
        val trimmed = url.trim()
        return when {
            trimmed.startsWith("feed://", ignoreCase = true) ->
                "https://" + trimmed.substring(7)
            trimmed.startsWith("itpc://", ignoreCase = true) ->
                "https://" + trimmed.substring(7)
            trimmed.startsWith("pcast://", ignoreCase = true) ->
                "https://" + trimmed.substring(8)
            else -> trimmed
        }
    }

    fun looksLikeFeedUrl(url: String): Boolean {
        val value = canonicalizeFeedUrl(url)
        return value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true)
    }

    internal fun escapeAttr(value: String): String = buildString(value.length) {
        for (c in value) {
            when (c) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> {
                    val code = c.code
                    if (code == 0x9 || code == 0xA || code == 0xD ||
                        code in 0x20..0xD7FF || code in 0xE000..0xFFFD
                    ) {
                        append(c)
                    }
                }
            }
        }
    }

    private fun rfc822(nowMs: Long): String {
        val fmt = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date(nowMs))
    }

    private fun secureBuilder() = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
        isExpandEntityReferences = false
        runCatching { setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true) }
        runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
        runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
        runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
    }.newDocumentBuilder()

    private fun firstAttr(el: Element, vararg names: String): String {
        val attrs = el.attributes ?: return ""
        for (name in names) {
            val direct = el.getAttribute(name)
            if (direct.isNotBlank()) return direct
        }
        for (i in 0 until attrs.length) {
            val node = attrs.item(i) ?: continue
            val local = node.localName?.ifBlank { null } ?: node.nodeName.substringAfter(':')
            if (names.any { it.equals(local, true) || it.equals(node.nodeName, true) }) {
                val value = node.nodeValue.orEmpty()
                if (value.isNotBlank()) return value
            }
        }
        return ""
    }

    private fun nsOrLocal(el: Element, localName: String): String? {
        val namespaced = el.getAttributeNS(OPENPOD_NS, localName)
        if (namespaced.isNotBlank()) return namespaced
        val prefixed = el.getAttribute("openpod:$localName")
        if (prefixed.isNotBlank()) return prefixed
        val local = firstAttr(el, localName)
        return local.ifBlank { null }
    }

    private fun parseBool(raw: String?): Boolean? {
        val value = raw?.trim()?.lowercase(Locale.US) ?: return null
        return when (value) {
            "true", "1", "yes" -> true
            "false", "0", "no" -> false
            else -> null
        }
    }

    private fun parseInt(raw: String?): Int? {
        val value = raw?.trim() ?: return null
        return value.toIntOrNull()
    }
}
