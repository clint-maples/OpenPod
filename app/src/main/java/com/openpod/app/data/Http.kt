package com.openpod.app.data

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

object Http {
    const val MAX_FEED_BYTES = 8L * 1024 * 1024

    fun get(url: String): String {
        var current = url.trim()
        repeat(8) {
            val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 20000
                readTimeout = 30000
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, */*")
                setRequestProperty("Accept-Encoding", "identity")
            }
            val code = conn.responseCode
            val location = conn.getHeaderField("Location")
            if (code in 300..399 && !location.isNullOrBlank()) {
                current = URL(URL(current), location).toString()
                conn.disconnect()
                return@repeat
            }
            if (code !in 200..299) {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                conn.disconnect()
                throw IOException("Could not load feed (HTTP $code) from $current ${err.take(120)}")
            }
            val charset = charsetFrom(conn.contentType)
            val declared = conn.contentLengthLong
            if (declared > MAX_FEED_BYTES) {
                conn.disconnect()
                throw IOException("Feed too large")
            }
            val body = conn.inputStream.use { input ->
                val out = ByteArrayOutputStream()
                val buf = ByteArray(16 * 1024)
                var total = 0L
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    total += n
                    if (total > MAX_FEED_BYTES) {
                        conn.disconnect()
                        throw IOException("Feed too large")
                    }
                    out.write(buf, 0, n)
                }
                out.toString(charset.name())
            }
            conn.disconnect()
            if (body.isBlank()) throw IOException("Feed was empty")
            return body
        }
        throw IOException("Too many redirects for $url")
    }

    private fun charsetFrom(contentType: String?): Charset {
        val raw = contentType.orEmpty()
        val match = Regex("charset=([^;]+)", RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)
        return try {
            Charset.forName(match?.trim() ?: "UTF-8")
        } catch (_: Exception) {
            Charsets.UTF_8
        }
    }

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) OpenPod/1.0"
}
