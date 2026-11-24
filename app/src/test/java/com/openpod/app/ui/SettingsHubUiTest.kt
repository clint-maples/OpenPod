package com.openpod.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.openpod.app.data.db.SettingsEntity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h1600dp-xxhdpi")
class SettingsHubUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val proofDir = File("/workspace/openpod-scope/settings/built-159")

    @Before
    fun createProofDir() {
        proofDir.mkdirs()
    }

    @Test
    fun hubListsCategoriesWithSummaries() {
        rule.setContent {
            OpenPodTheme {
                SettingsHubScreen(
                    playbackSummary = "15 s back · 30 s forward · 1.0x",
                    downloadsSummary = "OpenPod · Auto-download off",
                    notificationsSummary = "On",
                    appearanceSummary = "List layout",
                    dataSummary = "Export, import, rescan",
                    aboutSummary = "OpenPod 1.59",
                    onOpen = {}
                )
            }
        }
        rule.waitForIdle()
        rule.onNodeWithTag(SettingsTags.HUB).assertIsDisplayed()
        rule.onNodeWithText("Playback").assertIsDisplayed()
        rule.onNodeWithText("15 s back · 30 s forward · 1.0x").assertIsDisplayed()
        rule.onNodeWithText("Downloads").assertIsDisplayed()
        rule.onNodeWithText("OpenPod · Auto-download off").assertIsDisplayed()
        rule.onNodeWithText("Notifications").assertIsDisplayed()
        rule.onNodeWithText("Appearance").assertIsDisplayed()
        rule.onNodeWithText("Data").assertIsDisplayed()
        rule.onNodeWithText("About").assertIsDisplayed()
        rule.onNodeWithText("OpenPod 1.59").assertIsDisplayed()
        capture("settings-hub.png")
    }

    @Test
    fun playbackSubScreenUsesChipsAndTrimCopy() {
        rule.setContent {
            OpenPodTheme {
                PlaybackSettingsScreen(
                    skipBackSec = 15,
                    skipForwardSec = 30,
                    speed = 1f,
                    trimSilence = false,
                    onSkipBack = {},
                    onSkipForward = {},
                    onSpeed = {},
                    onTrimSilence = {},
                    onBack = {}
                )
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("Skip back").assertIsDisplayed()
        rule.onNodeWithText("Skip forward").assertIsDisplayed()
        rule.onAllNodesWithText("Custom").assertCountEquals(2)
        rule.onNodeWithText("Trim silence").assertIsDisplayed()
        rule.onNodeWithText("Cuts dead air").assertIsDisplayed()
        rule.onNodeWithText("Headset buttons").assertIsDisplayed()
        rule.onNodeWithText("Play, 2× fwd, 3× back").assertIsDisplayed()
        capture("settings-playback.png")
    }

    @Test
    fun downloadsSubScreenAndOtherPagesRender() {
        rule.setContent {
            OpenPodTheme {
                DownloadsSettingsScreen(
                    folderLabel = "OpenPod",
                    autoDownload = false,
                    autoDelete = false,
                    syncFrequency = SettingsEntity.SYNC_30_MIN,
                    syncHour = 6,
                    syncMinute = 0,
                    namingPattern = "{podcast} - {episode}",
                    onChooseFolder = {},
                    onAutoDownload = {},
                    onAutoDelete = {},
                    onSync = { _, _, _ -> },
                    onNaming = {},
                    onBack = {}
                )
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("Download folder").assertIsDisplayed()
        rule.onNodeWithText("Auto-download").assertIsDisplayed()
        rule.onNodeWithText("New shows").assertIsDisplayed()
        rule.onNodeWithText("CHECK FOR NEW EPISODES").assertIsDisplayed()
        rule.onNodeWithText("File naming").assertIsDisplayed()
        capture("settings-downloads.png")
    }

    @Test
    fun notificationsSubScreenRenders() {
        rule.setContent {
            OpenPodTheme {
                NotificationsSettingsScreen(enabled = true, onToggle = {}, onBack = {})
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("On").assertIsDisplayed()
        capture("settings-notifications.png")
    }

    @Test
    fun appearanceSubScreenRenders() {
        rule.setContent {
            OpenPodTheme {
                AppearanceSettingsScreen(
                    layout = SettingsEntity.LAYOUT_LIST,
                    onLayout = {},
                    onBack = {}
                )
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("Library layout").assertIsDisplayed()
        rule.onNodeWithText("List").assertIsDisplayed()
        rule.onNodeWithText("Grid").assertIsDisplayed()
        capture("settings-appearance.png")
    }

    @Test
    fun dataSubScreenRenders() {
        rule.setContent {
            OpenPodTheme {
                DataSettingsScreen(onExport = {}, onImport = {}, onRescan = {}, onBack = {})
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("Export subscriptions").assertIsDisplayed()
        rule.onNodeWithText("Import subscriptions").assertIsDisplayed()
        rule.onNodeWithText("Rescan downloads").assertIsDisplayed()
        rule.onNodeWithText("Match files already saved").assertIsDisplayed()
        capture("settings-data.png")
    }

    @Test
    fun aboutSubScreenRenders() {
        rule.setContent {
            OpenPodTheme {
                AboutSettingsScreen(versionName = "1.59", versionCode = 60, onBack = {})
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("OpenPod").assertIsDisplayed()
        rule.onNodeWithText("1.59").assertIsDisplayed()
        rule.onNodeWithText("Build 60").assertIsDisplayed()
        capture("settings-about.png")
    }

    private fun capture(filename: String) {
        val content = rule.activity.findViewById<ViewGroup>(android.R.id.content)
        forceSoftwareLayer(content)
        val width = content.width.coerceAtLeast(1)
        val height = content.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        content.draw(AndroidCanvas(bitmap))
        val cropped = cropToContent(bitmap)
        FileOutputStream(File(proofDir, filename)).use { out ->
            cropped.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("wrote ${File(proofDir, filename)} ${cropped.width}x${cropped.height}")
    }

    private fun cropToContent(src: Bitmap): Bitmap {
        val bg = src.getPixel(0, 0)
        var last = src.height - 1
        scan@ while (last > 0) {
            var x = 0
            while (x < src.width) {
                if (src.getPixel(x, last) != bg) break@scan
                x += 3
            }
            last--
        }
        val bottom = (last + 48).coerceAtMost(src.height).coerceAtLeast(1)
        return Bitmap.createBitmap(src, 0, 0, src.width, bottom)
    }

    private fun forceSoftwareLayer(view: View) {
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) forceSoftwareLayer(view.getChildAt(i))
        }
    }
}
