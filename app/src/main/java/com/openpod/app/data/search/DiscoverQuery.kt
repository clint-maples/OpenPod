package com.openpod.app.data.search

/**
 * Pocket Casts-style Discover input: a search term, or a pasted RSS / podcast URL.
 */
object DiscoverQuery {
    private val schemePrefix = Regex("^(https?|feed)://", RegexOption.IGNORE_CASE)
    private val schemelessHost = Regex(
        """^([\w-]+\.)+[A-Za-z]{2,}(:\d+)?(/.*)?$"""
    )

    fun looksLikeFeedUrl(raw: String): Boolean {
        val candidate = unwrap(raw) ?: return false
        return parse(candidate) != null
    }

    fun normalizeFeedUrl(raw: String): String {
        val candidate = unwrap(raw) ?: return raw.trim()
        return parse(candidate) ?: candidate
    }

    private fun unwrap(raw: String): String? {
        var text = raw.trim()
        if (text.isEmpty() || text.any { it.isWhitespace() }) return null
        if (text.startsWith("<") && text.endsWith(">")) {
            text = text.substring(1, text.lastIndex).trim()
        }
        return text.takeIf { it.isNotEmpty() && !it.any { ch -> ch.isWhitespace() } }
    }

    private fun parse(raw: String): String? {
        val withScheme = when {
            schemePrefix.containsMatchIn(raw) -> raw
            schemelessHost.matches(raw) -> "https://$raw"
            else -> return null
        }
        val asHttps = if (withScheme.startsWith("feed://", ignoreCase = true)) {
            "https://${withScheme.substring(7)}"
        } else {
            withScheme
        }
        val scheme = asHttps.substringBefore("://", missingDelimiterValue = "").lowercase()
        if (scheme != "http" && scheme != "https") return null
        val rest = asHttps.substringAfter("://")
        val host = rest.substringBefore('/').substringBefore('?').substringBefore('#')
        if (host.isBlank() || !host.contains('.')) return null
        return asHttps
    }
}
