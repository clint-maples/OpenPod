package com.openpod.app.ui

import com.openpod.app.data.db.SettingsEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ThemeTokensTest {
    @Test
    fun darkSoftTealMatchesLegacyLook() {
        val colors = openPodColors(isDark = true, trueBlack = false, accent = OpenPodAccent.TEAL)
        assertRgb(0xFF7FB7C9, colors.scheme.primary, "primary")
        assertRgb(0xFF00344A, colors.scheme.onPrimary, "onPrimary")
        assertRgb(0xFFB5C9D3, colors.scheme.secondary, "secondary")
        assertRgb(0xFF121418, colors.scheme.background, "background")
        assertRgb(0xFF121418, colors.scheme.surface, "surface")
        assertRgb(0xFF1C2026, colors.scheme.surfaceVariant, "surfaceVariant")
        assertRgb(0xFF262B33, colors.scheme.surfaceContainerHigh, "surfaceContainerHigh")
        assertRgb(0xFFE8F1F5, colors.scheme.onBackground, "onBackground")
        assertRgb(0xFFE8F1F5, colors.scheme.onSurface, "onSurface")
        assertRgb(0xFF000000, colors.progressTransport, "progressTransport")
        assertRgb(0xFF262B33, colors.chromeBar, "chromeBar")
        assertRgb(0xFF8A939C, colors.inactiveSeekTrack, "inactiveSeek")
        assertFalse(colors.useTonalButtons)
    }

    @Test
    fun trueBlackKeepsBlackBandsAndActionsBar() {
        val colors = openPodColors(isDark = true, trueBlack = true, accent = OpenPodAccent.TEAL)
        assertRgb(0xFF000000, colors.scheme.background, "background")
        assertRgb(0xFF000000, colors.scheme.surface, "surface")
        assertRgb(0xFF121418, colors.scheme.surfaceVariant, "surfaceVariant")
        assertRgb(0xFF1C2026, colors.scheme.surfaceContainerHigh, "surfaceContainerHigh")
        assertRgb(0xFF000000, colors.progressTransport, "progressTransport")
        assertRgb(0xFF262B33, colors.chromeBar, "chromeBar")
        assertTrue(colors.isTrueBlack)
    }

    @Test
    fun lightAccentWashAndHalfTintMatchLockedHexes() {
        val expected = mapOf(
            OpenPodAccent.TEAL to listOf(0xFFE2ECEF, 0xFFECF2F4, 0xFFF3F7F8, 0xFFD9E6E9),
            OpenPodAccent.AMBER to listOf(0xFFF8F4EF, 0xFFFAF8F4, 0xFFFCFAF8, 0xFFEEE6DA),
            OpenPodAccent.CORAL to listOf(0xFFFAF2F2, 0xFFFCF6F6, 0xFFFDFAF9, 0xFFF4E2E0),
            OpenPodAccent.GREEN to listOf(0xFFE3EDE8, 0xFFECF3EF, 0xFFF3F7F5, 0xFFDAE7E0),
            OpenPodAccent.PURPLE to listOf(0xFFEDE6F4, 0xFFF3EEF8, 0xFFF7F5FA, 0xFFE7DFF0),
            OpenPodAccent.GRAPHITE to listOf(0xFFE8E9EA, 0xFFF0F0F1, 0xFFF5F6F6, 0xFFE0E2E3)
        )
        expected.forEach { (accent, hexes) ->
            val colors = openPodColors(isDark = false, trueBlack = false, accent = accent)
            assertRgb(hexes[0], colors.page, "${accent.label} page")
            assertRgb(hexes[1], colors.card, "${accent.label} card")
            assertRgb(hexes[2], colors.band, "${accent.label} band")
            assertRgb(hexes[3], colors.controlWell, "${accent.label} well")
            assertTrue(colors.useTonalButtons)
            assertRgb(0xFF121418, colors.scheme.onSurface, "${accent.label} onSurface")
            assertRgb(0xFF3E5560, colors.scheme.secondary, "${accent.label} secondary")
            assertRgb(0xFF5C656C, colors.muted, "${accent.label} muted")
            assertRgb(0xFF7A858E, colors.inactiveSeekTrack, "${accent.label} seek")
            assertEquals(accent.light, colors.scheme.primary)
        }
        assertTrue(OpenPodAccent.AMBER.halfTint && OpenPodAccent.CORAL.halfTint)
        assertFalse(OpenPodAccent.TEAL.halfTint)
    }

    @Test
    fun accentPairsMeetAaContrast() {
        OpenPodAccent.all.forEach { accent ->
            val darkSoft = openPodColors(true, false, accent)
            assertTrue(
                "${accent.label} dark vs soft ${OpenPodPalette.contrastRatio(accent.dark, darkSoft.page)}",
                OpenPodPalette.contrastRatio(accent.dark, darkSoft.page) >= 4.5
            )
            assertTrue(
                "${accent.label} dark vs black",
                OpenPodPalette.contrastRatio(accent.dark, OpenPodPalette.TrueBlack) >= 4.5
            )
            assertTrue(
                "${accent.label} onDark vs dark accent",
                OpenPodPalette.contrastRatio(accent.onDark, accent.dark) >= 4.5
            )
            val light = openPodColors(false, false, accent)
            assertTrue(
                "${accent.label} light vs page ${OpenPodPalette.contrastRatio(accent.light, light.page)}",
                OpenPodPalette.contrastRatio(accent.light, light.page) >= 4.5
            )
            assertTrue(
                "${accent.label} onLight vs light accent",
                OpenPodPalette.contrastRatio(accent.onLight, accent.light) >= 4.5
            )
            assertTrue(
                "${accent.label} body vs page",
                OpenPodPalette.contrastRatio(light.scheme.onSurface, light.page) >= 7.0
            )
            assertTrue(
                "${accent.label} muted vs page",
                OpenPodPalette.contrastRatio(light.muted, light.page) >= 4.5
            )
        }
        assertTrue(
            OpenPodPalette.contrastRatio(OpenPodPalette.DarkInactiveSeek, OpenPodPalette.TrueBlack) >= 4.5
        )
        val tealLight = openPodColors(false, false, OpenPodAccent.TEAL)
        assertTrue(
            OpenPodPalette.contrastRatio(OpenPodPalette.LightInactiveSeek, tealLight.band) >= 3.0
        )
    }

    @Test
    fun resolveAndSanitizeDefaults() {
        assertTrue(resolveOpenPodDark(SettingsEntity.THEME_DARK, false))
        assertFalse(resolveOpenPodDark(SettingsEntity.THEME_LIGHT, true))
        assertTrue(resolveOpenPodDark(SettingsEntity.THEME_SYSTEM, true))
        assertFalse(resolveOpenPodDark(SettingsEntity.THEME_SYSTEM, false))
        assertEquals(SettingsEntity.THEME_DARK, SettingsEntity.sanitizeThemeMode("nope"))
        assertEquals(SettingsEntity.BACKGROUND_SOFT, SettingsEntity.sanitizeDarkBackground("nope"))
        assertEquals(SettingsEntity.ACCENT_TEAL, SettingsEntity.sanitizeAccent("nope"))
        val defaults = SettingsEntity()
        assertEquals(SettingsEntity.THEME_DARK, defaults.themeMode)
        assertEquals(SettingsEntity.BACKGROUND_SOFT, defaults.darkBackground)
        assertEquals(SettingsEntity.ACCENT_TEAL, defaults.accent)
    }

    private fun assertRgb(expected: Long, actual: androidx.compose.ui.graphics.Color, name: String) {
        val e = expected.toInt()
        val a = OpenPodPalette.rgb(actual)
        fun ch(value: Int, shift: Int) = (value shr shift) and 0xFF
        val ok = abs(ch(e, 16) - ch(a, 16)) <= 1 &&
            abs(ch(e, 8) - ch(a, 8)) <= 1 &&
            abs(ch(e, 0) - ch(a, 0)) <= 1
        assertTrue("$name expected #${e.toUInt().toString(16)} got #${a.toUInt().toString(16)}", ok)
    }
}
