package com.openpod.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.openpod.app.playback.PlayerUiState
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
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h160dp-xxhdpi")
class MiniPlayerIconTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val proofDir = File("/workspace/openpod-scope/mini-ff")

    @Before
    fun createProofDir() {
        proofDir.mkdirs()
    }

    @Test
    fun miniPlayerRendersMirroredForwardSkipAndSavesProof() {
        rule.setContent {
            OpenPodTheme {
                MiniPlayer(
                    player = PlayerUiState(
                        episodeId = 4,
                        title = "Episode title",
                        podcastTitle = "Show name",
                        durationMs = 60 * 60 * 1000L,
                        positionMs = 15 * 60 * 1000L
                    ),
                    onOpen = {},
                    onUpNext = {},
                    onTogglePlay = {},
                    onSkipBack = {},
                    onSkipForward = {}
                )
            }
        }
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Back 15s").assertIsDisplayed()
        rule.onNodeWithContentDescription("Forward 30s").assertIsDisplayed()
        val bitmap = capture("proof-mini-ff.png")
        assertTrue("Mini player proof should be a wide render", bitmap.width >= 720)
        assertTrue("Mini player proof should have painted pixels", nonBlackCount(bitmap) >= 20)
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

    private fun nonBlackCount(bitmap: Bitmap): Int {
        var n = 0
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val color = bitmap.getPixel(x, y)
                val r = android.graphics.Color.red(color)
                val g = android.graphics.Color.green(color)
                val b = android.graphics.Color.blue(color)
                val a = android.graphics.Color.alpha(color)
                if (a > 20 && r + g + b > 24) n++
                x++
            }
            y++
        }
        return n
    }
}
