package com.openpod.app.ui

import com.openpod.app.data.db.SettingsEntity
import com.openpod.app.download.DownloadScanResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsCopyTest {
    @Test
    fun skipPresetsAndCustomDetection() {
        for (preset in listOf(10, 15, 30, 60)) {
            assertTrue(SettingsCopy.isSkipPreset(preset))
        }
        assertFalse(SettingsCopy.isSkipPreset(20))
        assertFalse(SettingsCopy.isSkipPreset(1))
        assertEquals("15 s", SettingsCopy.skipLabel(15))
        assertEquals("15 s back · 30 s forward", SettingsCopy.skipSummary(15, 30))
    }

    @Test
    fun playbackAndDownloadsSummaries() {
        assertEquals(
            "15 s back · 30 s forward · 1.0x",
            SettingsCopy.playbackSummary(15, 30, 1f)
        )
        assertEquals("Not set", SettingsCopy.folderLabel(""))
        assertEquals(
            "Not set · Auto-download off",
            SettingsCopy.downloadsSummary("", false)
        )
        assertEquals("On", SettingsCopy.notificationsSummary(true))
        assertEquals("Off", SettingsCopy.notificationsSummary(false))
        assertEquals(
            "Dark · Teal · List",
            SettingsCopy.appearanceSummary(
                SettingsEntity.THEME_DARK,
                SettingsEntity.ACCENT_TEAL,
                SettingsEntity.LAYOUT_LIST
            )
        )
        assertEquals(
            "Light · Coral · Grid",
            SettingsCopy.appearanceSummary(
                SettingsEntity.THEME_LIGHT,
                SettingsEntity.ACCENT_CORAL,
                SettingsEntity.LAYOUT_GRID
            )
        )
        assertEquals(
            "System · Teal · List",
            SettingsCopy.appearanceSummary(
                SettingsEntity.THEME_SYSTEM,
                "unknown",
                SettingsEntity.LAYOUT_LIST
            )
        )
        assertEquals("Export, import, rescan", SettingsCopy.dataSummary())
        assertEquals("OpenPod 1.59", SettingsCopy.aboutSummary("1.59"))
    }

    @Test
    fun syncSummaryIsCompact() {
        assertEquals("Every 30 minutes", SettingsCopy.syncSummary(SettingsEntity.SYNC_30_MIN, 6, 0))
        assertEquals("Hourly", SettingsCopy.syncSummary(SettingsEntity.SYNC_HOURLY, 6, 0))
        assertEquals("Daily at 6:00", SettingsCopy.syncSummary(SettingsEntity.SYNC_DAILY, 6, 0))
        assertEquals("Weekly at 18:05", SettingsCopy.syncSummary(SettingsEntity.SYNC_WEEKLY, 18, 5))
        assertEquals("6:00", SettingsCopy.formatClock(6, 0))
    }

    @Test
    fun scanAllMessage() {
        assertEquals(
            "Choose a download folder in Settings",
            SettingsCopy.scanAllMessage(DownloadScanResult(linked = 0, folderUnset = true))
        )
        assertEquals(
            "Already linked 3 downloaded episode(s)",
            SettingsCopy.scanAllMessage(DownloadScanResult(linked = 0, alreadyLinked = 3))
        )
        assertEquals(
            "No matching downloads found",
            SettingsCopy.scanAllMessage(DownloadScanResult(linked = 0))
        )
        assertEquals(
            "Linked 4 episode(s)",
            SettingsCopy.scanAllMessage(DownloadScanResult(linked = 4, alreadyLinked = 1))
        )
    }

    @Test
    fun settingsRouteDetection() {
        assertTrue(SettingsRoutes.isSettings("settings"))
        assertTrue(SettingsRoutes.isSettings("settings/playback"))
        assertFalse(SettingsRoutes.isSettings("library"))
        assertFalse(SettingsRoutes.isSettings(null))
    }
}
