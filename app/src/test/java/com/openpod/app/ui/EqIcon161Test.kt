package com.openpod.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.unit.dp
import com.openpod.app.data.db.SettingsEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h120dp-xxhdpi")
class EqIcon161Test {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val proofDir = File("/workspace/openpod-scope/eq-label")

    @Before
    fun createProofDir() {
        proofDir.mkdirs()
    }

    @Test
    fun equalizerIsIconOnlyAndCenteredInDark() {
        setBar(dark = true)
        assertNoEqLabel()
        val measurements = measureAlignment()
        capture("actions-dark.png")
        writeMeasurements("measurements-dark.txt", measurements)
        assertCentered(measurements)
    }

    @Test
    fun equalizerIsIconOnlyAndCenteredInLight() {
        setBar(dark = false)
        assertNoEqLabel()
        val measurements = measureAlignment()
        capture("actions-light.png")
        writeMeasurements("measurements-light.txt", measurements)
        assertCentered(measurements)
    }

    private fun setBar(dark: Boolean) {
        val theme = if (dark) SettingsEntity.THEME_DARK else SettingsEntity.THEME_LIGHT
        rule.setContent {
            OpenPodTheme(themeMode = theme, accentId = SettingsEntity.ACCENT_TEAL) {
                ActionsBarPreview()
            }
        }
        rule.waitForIdle()
    }

    private fun assertNoEqLabel() {
        rule.onAllNodesWithText("EQ").assertCountEquals(0)
        rule.onNodeWithTag(NowPlayingChrome.EQUALIZER_TAG).assertIsDisplayed()
        rule.onNodeWithContentDescription("Equalizer").assertIsDisplayed()
    }

    private fun measureAlignment(): Map<String, Float> {
        val density = rule.density.density
        val bar = rule.onNodeWithTag(NowPlayingChrome.ACTIONS_BAR_TAG).getUnclippedBoundsInRoot()
        val eq = rule.onNodeWithContentDescription("Equalizer", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val sleep = rule.onNodeWithContentDescription("Sleep timer", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val share = rule.onNodeWithContentDescription("Share episode", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val favorite = rule.onNodeWithContentDescription("Favorite", useUnmergedTree = true).getUnclippedBoundsInRoot()
        fun midY(b: androidx.compose.ui.unit.DpRect) = (b.top.value + b.bottom.value) / 2f
        val barMid = midY(bar)
        val barHeight = bar.bottom.value - bar.top.value
        return mapOf(
            "density" to density,
            "barTopDp" to bar.top.value,
            "barBottomDp" to bar.bottom.value,
            "barHeightDp" to barHeight,
            "eqTopDp" to eq.top.value,
            "eqBottomDp" to eq.bottom.value,
            "eqMidYDp" to midY(eq),
            "sleepMidYDp" to midY(sleep),
            "shareMidYDp" to midY(share),
            "favoriteMidYDp" to midY(favorite),
            "barMidYDp" to barMid,
            "eqTopMarginDp" to (eq.top.value - bar.top.value),
            "eqBottomMarginDp" to (bar.bottom.value - eq.bottom.value)
        )
    }

    private fun assertCentered(m: Map<String, Float>) {
        assertEquals(NowPlayingChrome.ACTIONS_BAND_HEIGHT_DP.toFloat(), m.getValue("barHeightDp"), 0.5f)
        assertTrue(
            "EQ icon top ${m["eqTopDp"]} is above the bar ${m["barTopDp"]}",
            m.getValue("eqTopDp") >= m.getValue("barTopDp") - 0.5f
        )
        assertTrue(
            "EQ icon bottom ${m["eqBottomDp"]} extends past the bar ${m["barBottomDp"]}",
            m.getValue("eqBottomDp") <= m.getValue("barBottomDp") + 0.5f
        )
        assertTrue(
            "EQ icon not vertically centered in the bar: mid ${m["eqMidYDp"]} vs bar ${m["barMidYDp"]}",
            abs(m.getValue("eqMidYDp") - m.getValue("barMidYDp")) <= 1.0f
        )
        listOf("sleepMidYDp", "shareMidYDp", "favoriteMidYDp").forEach { key ->
            assertTrue(
                "EQ icon mid ${m["eqMidYDp"]} does not line up with $key ${m[key]}",
                abs(m.getValue("eqMidYDp") - m.getValue(key)) <= 1.0f
            )
        }
        assertTrue("expected a little margin above the icon", m.getValue("eqTopMarginDp") >= 2f)
        assertTrue("expected a little margin below the icon", m.getValue("eqBottomMarginDp") >= 2f)
    }

    private fun writeMeasurements(filename: String, m: Map<String, Float>) {
        File(proofDir, filename).writeText(
            buildString {
                appendLine("OpenPod 1.61 equalizer icon measurements (dp)")
                m.forEach { (k, v) -> appendLine("$k=$v") }
            }
        )
    }

    private fun capture(filename: String) {
        val content = rule.activity.findViewById<ViewGroup>(android.R.id.content)
        forceSoftwareLayer(content)
        val width = content.width.coerceAtLeast(1)
        val height = content.height.coerceAtLeast(1)
        val full = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        content.draw(AndroidCanvas(full))
        val barPx = (NowPlayingChrome.ACTIONS_BAND_HEIGHT_DP * rule.density.density).toInt()
            .coerceIn(1, full.height)
        val bitmap = Bitmap.createBitmap(full, 0, 0, full.width, barPx)
        FileOutputStream(File(proofDir, filename)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("wrote ${File(proofDir, filename)} ${bitmap.width}x${bitmap.height}")
    }

    private fun forceSoftwareLayer(view: View) {
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) forceSoftwareLayer(view.getChildAt(i))
        }
    }
}

@Composable
private fun ActionsBarPreview() {
    val chrome = LocalOpenPodColors.current
    val tint = openPodControlTint()
    val icon = NowPlayingChrome.scaledDp(NowPlayingChrome.STANDARD_ICON_DP).dp
    Surface(color = chrome.chromeBar, tonalElevation = 0.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(NowPlayingChrome.ACTIONS_BAND_HEIGHT_DP.dp)
                .padding(horizontal = 4.dp)
                .testTag(NowPlayingChrome.ACTIONS_BAR_TAG),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                OpenPodControlWell(
                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("1.0x", color = tint)
                }
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                NowPlayingEqualizerButton(onClick = {})
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(onClick = {}, modifier = Modifier.size(NowPlayingChrome.MIN_TAP_TARGET_DP.dp)) {
                    OpenPodControlWell(Modifier.size(32.dp)) {
                        Icon(Icons.Default.Bedtime, "Sleep timer", modifier = Modifier.size(icon), tint = tint)
                    }
                }
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(onClick = {}, modifier = Modifier.size(NowPlayingChrome.MIN_TAP_TARGET_DP.dp)) {
                    OpenPodControlWell(Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, "Share episode", modifier = Modifier.size(icon), tint = tint)
                    }
                }
            }
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = {}, modifier = Modifier.size(NowPlayingChrome.MIN_TAP_TARGET_DP.dp)) {
                    OpenPodControlWell(Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Favorite,
                            "Favorite",
                            modifier = Modifier.size(icon),
                            tint = chrome.favoriteHeart
                        )
                    }
                }
            }
        }
    }
}
