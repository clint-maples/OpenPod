package com.openpod.app.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryPodcastMenuTest {
    @Test
    fun subscribedShowAlwaysOffersArchive() {
        assertTrue(LibraryPodcastMenu.showsArchive(subscribed = true))
        assertFalse(LibraryPodcastMenu.showsArchive(subscribed = false))
        assertEquals("Archive", LibraryPodcastMenu.archiveLabel)
        assertTrue(LibraryPodcastMenu.archiveDetail.contains("Subscription stays"))
    }

    @Test
    fun version132GlyphDisappearsOnLightCoversUntilScrimIsApplied() {
        // 1.32 painted the grid overflow with theme onSurface and no backing plate.
        val glyph = Color(0xFFE8F1F5)
        assertTrue(contrastRatio(glyph, Color.White) < 3.0)
        assertTrue(contrastRatio(glyph, Color(0xFFF4F0E6)) < 3.0)

        val covers = listOf(
            Color.White,
            Color.Black,
            Color(0xFFF4F0E6),
            Color(0xFF101010),
            Color(0xFF7FB7C9)
        )
        for (cover in covers) {
            val behind = composite(LibraryPodcastMenu.compactScrim, cover)
            val ratio = contrastRatio(glyph, behind)
            assertTrue("cover=$cover ratio=$ratio behind=$behind", ratio >= 3.0)
        }
    }

    private fun composite(src: Color, dst: Color): Color {
        val outA = src.alpha + dst.alpha * (1f - src.alpha)
        if (outA == 0f) return Color.Transparent
        fun channel(s: Float, d: Float) = (s * src.alpha + d * dst.alpha * (1f - src.alpha)) / outA
        return Color(channel(src.red, dst.red), channel(src.green, dst.green), channel(src.blue, dst.blue), outA)
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val lighter = maxOf(luminance(foreground), luminance(background))
        val darker = minOf(luminance(foreground), luminance(background))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun luminance(color: Color): Double {
        fun channel(value: Float): Double {
            val v = value.toDouble()
            return if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }
}
