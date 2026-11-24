package com.openpod.app.ui

import android.net.Uri
import com.openpod.app.data.db.SettingsEntity
import com.openpod.app.download.DownloadScanResult
import com.openpod.app.playback.SkipIntervals

/** Routes, labels, and one-line summaries for the Settings category hub. */
object SettingsRoutes {
    const val HUB = "settings"
    const val PLAYBACK = "settings/playback"
    const val DOWNLOADS = "settings/downloads"
    const val NOTIFICATIONS = "settings/notifications"
    const val APPEARANCE = "settings/appearance"
    const val DATA = "settings/data"
    const val ABOUT = "settings/about"

    fun isSettings(route: String?): Boolean {
        val value = route.orEmpty()
        return value == HUB || value.startsWith("settings/")
    }
}

object SettingsCopy {
    val SKIP_PRESETS: List<Int> = listOf(10, 15, 30, 60)

    const val HUB_TITLE = "Settings"
    const val PLAYBACK_TITLE = "Playback"
    const val DOWNLOADS_TITLE = "Downloads"
    const val NOTIFICATIONS_TITLE = "Notifications"
    const val APPEARANCE_TITLE = "Appearance"
    const val DATA_TITLE = "Data"
    const val ABOUT_TITLE = "About"

    const val SKIP_BACK = "Skip back"
    const val SKIP_FORWARD = "Skip forward"
    const val CUSTOM = "Custom"
    const val SPEED = "Speed"
    const val TRIM_SILENCE = "Trim silence"
    const val TRIM_SILENCE_SUBTITLE = "Cuts dead air"
    const val HEADSET_BUTTONS = "Headset buttons"
    const val HEADSET_SUMMARY = "Play, 2× fwd, 3× back"

    const val DOWNLOAD_FOLDER = "Download folder"
    const val FOLDER_NOT_SET = "Not set"
    const val AUTO_DOWNLOAD = "Auto-download"
    const val AUTO_DOWNLOAD_SUBTITLE = "New shows"
    const val AUTO_DELETE = "Auto-delete"
    const val AUTO_DELETE_SUBTITLE = "After you finish"
    const val CHECK_EPISODES = "Check for new episodes"
    const val FILE_NAMING = "File naming"
    const val FILE_NAMING_TOKENS = "Tokens: {podcast} {title} {episode} {season} {date} {guid}"
    const val ADVANCED = "Advanced"

    const val LIBRARY_LAYOUT = "Library layout"
    const val LAYOUT_LIST = "List"
    const val LAYOUT_GRID = "Grid"

    const val EXPORT_SUBSCRIPTIONS = "Export subscriptions"
    const val IMPORT_SUBSCRIPTIONS = "Import subscriptions"
    const val RESCAN_DOWNLOADS = "Rescan downloads"
    const val RESCAN_SUBTITLE = "Match files already saved"

    const val OPEN_POD = "OpenPod"

    fun isSkipPreset(seconds: Int): Boolean = seconds in SKIP_PRESETS

    fun skipLabel(seconds: Int): String = "${SkipIntervals.sanitize(seconds)} s"

    fun skipSummary(backSec: Int, forwardSec: Int): String =
        "${skipLabel(backSec)} back · ${skipLabel(forwardSec)} forward"

    fun speedLabel(speed: Float): String = "${formatSpeed(speed)}x"

    fun playbackSummary(backSec: Int, forwardSec: Int, speed: Float): String =
        "${skipSummary(backSec, forwardSec)} · ${speedLabel(speed)}"

    fun folderLabel(downloadTreeUri: String): String {
        if (downloadTreeUri.isBlank()) return FOLDER_NOT_SET
        return runCatching {
            Uri.parse(downloadTreeUri).lastPathSegment?.takeIf { it.isNotBlank() }
        }.getOrNull() ?: downloadTreeUri
    }

    fun onOff(on: Boolean): String = if (on) "On" else "Off"

    fun downloadsSummary(downloadTreeUri: String, autoDownload: Boolean): String {
        val auto = if (autoDownload) "Auto-download on" else "Auto-download off"
        return "${folderLabel(downloadTreeUri)} · $auto"
    }

    fun notificationsSummary(enabled: Boolean): String = onOff(enabled)

    fun layoutLabel(layout: String): String =
        if (layout == SettingsEntity.LAYOUT_GRID) LAYOUT_GRID else LAYOUT_LIST

    fun appearanceSummary(layout: String): String = "${layoutLabel(layout)} layout"

    fun dataSummary(): String = "Export, import, rescan"

    fun aboutSummary(versionName: String): String = "OpenPod $versionName"

    fun formatClock(hour: Int, minute: Int): String {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        return "$h:${m.toString().padStart(2, '0')}"
    }

    fun syncSummary(frequency: String, hour: Int, minute: Int): String {
        val time = formatClock(hour, minute)
        return when (frequency) {
            SettingsEntity.SYNC_HOURLY -> "Hourly"
            SettingsEntity.SYNC_DAILY -> "Daily at $time"
            SettingsEntity.SYNC_WEEKLY -> "Weekly at $time"
            else -> "Every 30 minutes"
        }
    }

    fun scanAllMessage(result: DownloadScanResult): String = when {
        result.folderUnset -> "Choose a download folder in Settings"
        result.linked == 0 && result.alreadyLinked > 0 ->
            "Already linked ${result.alreadyLinked} downloaded episode(s)"
        result.linked == 0 -> "No matching downloads found"
        else -> "Linked ${result.linked} episode(s)"
    }
}
