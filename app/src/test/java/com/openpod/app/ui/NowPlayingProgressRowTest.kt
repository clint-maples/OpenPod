package com.openpod.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
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

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h80dp-xxhdpi")
class NowPlayingProgressRowTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val proofDir = File("/workspace/openpod-scope/remaining")

    @Before
    fun createProofDir() {
        proofDir.mkdirs()
    }

    @Test
    fun rightLabelShowsRemainingWithMinusAndKeepsWidthStable() {
        val hour = 3_600_000L
        var position by mutableStateOf(hour - (42 * 60 + 10) * 1_000L)
        rule.setContent {
            OpenPodTheme {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(Color.Black)
                ) {
                    NowPlayingProgressRow(
                        positionMs = position,
                        durationMs = hour,
                        onSeekMs = { position = it }
                    )
                }
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("17:50").assertIsDisplayed()
        rule.onNodeWithText("-42:10").assertIsDisplayed()
        val midWidth = remainingWidth()
        capture("proof-remaining-4210.png")

        position = hour - (59 * 60 + 59) * 1_000L
        rule.waitForIdle()
        rule.onNodeWithText("0:01").assertIsDisplayed()
        rule.onNodeWithText("-59:59").assertIsDisplayed()
        assertEquals(
            "Remaining label width must stay stable when the countdown drops under an hour",
            midWidth,
            remainingWidth()
        )

        position = 0L
        rule.waitForIdle()
        rule.onNodeWithText("0:00").assertIsDisplayed()
        rule.onNodeWithText("-1:00:00").assertIsDisplayed()
        assertEquals(
            "Remaining label width must stay stable at the full-duration countdown",
            midWidth,
            remainingWidth()
        )
        capture("proof-remaining-hour.png")
    }

    @Test
    fun remainingUpdatesWhileScrubbingAndOverAnHour() {
        val twoHours = 2 * 3_600_000L
        val remaining = (1 * 3600 + 2 * 60 + 10) * 1_000L
        val position = twoHours - remaining
        rule.setContent {
            OpenPodTheme {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(Color.Black)
                ) {
                    NowPlayingProgressRow(
                        positionMs = position,
                        durationMs = twoHours,
                        onSeekMs = {}
                    )
                }
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("57:50").assertIsDisplayed()
        rule.onNodeWithText("-1:02:10").assertIsDisplayed()
        capture("proof-remaining-over-hour.png")
    }

    private fun remainingWidth(): Int {
        return rule.onNodeWithTag(NowPlayingChrome.REMAINING_TAG)
            .fetchSemanticsNode()
            .size
            .width
    }

    private fun capture(filename: String): Bitmap {
        val content = rule.activity.findViewById<ViewGroup>(android.R.id.content)
        forceSoftwareLayer(content)
        val width = content.width.coerceAtLeast(1)
        val height = content.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        content.draw(AndroidCanvas(bitmap))
        FileOutputStream(File(proofDir, filename)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return bitmap
    }

    private fun forceSoftwareLayer(view: View) {
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                forceSoftwareLayer(view.getChildAt(i))
            }
        }
    }
}
