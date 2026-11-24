package com.openpod.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.geometry.Offset
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
import kotlin.math.abs
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h80dp-xxhdpi")
class NowPlayingSeekBarCenteringTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val proofDir = File("/workspace/openpod-scope/thumb-center")

    @Before
    fun createProofDir() {
        proofDir.mkdirs()
    }

    @Test
    fun thumbCenterMatchesTrackCenterAtZeroFiftyAndOneHundredPercent() {
        val duration = 3_600_000L
        var position by mutableStateOf(0L)
        var density = 3f
        rule.setContent {
            density = LocalDensity.current.density
            OpenPodTheme {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(NowPlayingChrome.PROGRESS_BAND_HEIGHT_DP.dp)
                        .background(Color.Black)
                ) {
                    NowPlayingProgressRow(
                        positionMs = position,
                        durationMs = duration,
                        onSeekMs = { position = it }
                    )
                }
            }
        }

        val offsets = linkedMapOf<String, Float>()
        val cases = listOf(
            "0" to 0L,
            "50" to duration / 2,
            "100" to duration
        )
        for ((label, ms) in cases) {
            position = ms
            rule.waitForIdle()
            rule.onNodeWithText(formatMs(ms)).assertExists()
            rule.onNodeWithText(formatRemainingMs(remainingMs(duration, ms))).assertExists()

            val bitmap = capture("proof-158-$label.png")
            val row = rowBand(bitmap, density)
            val measured = measureCenters(row)
            val offsetDp = (measured.thumbCenterY - measured.trackCenterY) / density
            offsets[label] = offsetDp
            save(zoomAroundThumb(row, measured, scale = 8), "proof-158-$label-zoom.png")

            val message = buildString {
                append("progress $label%: thumbCenterY=${measured.thumbCenterY}px, ")
                append("trackCenterY=${measured.trackCenterY}px, ")
                append("offset=${"%.3f".format(offsetDp)}dp (limit 0.5dp). ")
                append("thumbY=${measured.thumbTop}..${measured.thumbBottom}, ")
                append("trackY=${measured.trackTop}..${measured.trackBottom}")
            }
            println(message)
            File(proofDir, "measure-158-$label.txt").writeText(message + "\n")
        }
        val summary = offsets.entries.joinToString("\n") { (k, v) ->
            "$k: ${"%.3f".format(v)} dp"
        } + "\n"
        File(proofDir, "measure-158-summary.txt").writeText(summary)
        println(summary)
        for ((label, offsetDp) in offsets) {
            assertTrue(
                "progress $label%: offset=${"%.3f".format(offsetDp)}dp (limit 0.5dp)",
                abs(offsetDp) <= 0.5f
            )
        }
    }

    @Test
    fun thumbStaysCenteredWhileScrubbing() {
        val duration = 3_600_000L
        var position by mutableStateOf(0L)
        var density = 3f
        rule.setContent {
            density = LocalDensity.current.density
            OpenPodTheme {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(NowPlayingChrome.PROGRESS_BAND_HEIGHT_DP.dp)
                        .background(Color.Black)
                ) {
                    NowPlayingProgressRow(
                        positionMs = position,
                        durationMs = duration,
                        onSeekMs = { position = it }
                    )
                }
            }
        }
        rule.waitForIdle()
        val seekNode = rule.onNodeWithTag(NowPlayingChrome.SEEK_TAG)
        seekNode.performTouchInput {
            val start = Offset(0f, height / 2f)
            val end = Offset(width * 0.6f, height / 2f)
            swipe(start, end, durationMillis = 200)
        }
        rule.waitForIdle()
        assertTrue("Scrub should move position off 0", position > 0L)
        rule.onNodeWithText(formatRemainingMs(remainingMs(duration, position))).assertExists()

        val bitmap = capture("proof-158-scrub.png")
        val row = rowBand(bitmap, density)
        val measured = measureCenters(row)
        val offsetDp = (measured.thumbCenterY - measured.trackCenterY) / density
        val zoom = zoomAroundThumb(row, measured, scale = 8)
        save(zoom, "proof-158-scrub-zoom.png")
        val message = "scrub: thumbCenterY=${measured.thumbCenterY}px, " +
            "trackCenterY=${measured.trackCenterY}px, " +
            "offset=${"%.3f".format(offsetDp)}dp, positionMs=$position"
        println(message)
        File(proofDir, "measure-158-scrub.txt").writeText(message + "\n")
        assertTrue(message, abs(offsetDp) <= 0.5f)
    }

    private data class Centers(
        val thumbCenterY: Float,
        val trackCenterY: Float,
        val thumbTop: Int,
        val thumbBottom: Int,
        val trackTop: Int,
        val trackBottom: Int,
        val thumbCenterX: Int
    )

    private fun measureCenters(row: Bitmap): Centers {
        val bg = AndroidColor.BLACK
        val width = row.width
        val height = row.height
        val spans = Array(width) { x ->
            var top = -1
            var bottom = -1
            for (y in 0 until height) {
                if (row.getPixel(x, y) != bg && AndroidColor.alpha(row.getPixel(x, y)) > 32) {
                    if (top < 0) top = y
                    bottom = y
                }
            }
            top to bottom
        }
        val lengths = spans.map { (t, b) -> if (t < 0) 0 else b - t + 1 }
        val maxLen = lengths.maxOrNull() ?: 0
        require(maxLen > 0) { "No non-black pixels in progress row (w=$width h=$height)" }

        // The thumb is the tallest vertical span (the 12 dp circle). The track is a
        // thinner run of pixels in columns away from the thumb.
        val thumbXs = lengths.mapIndexedNotNull { x, len ->
            if (len >= maxLen - 2 && len > 0) x else null
        }
        val thumbCenterX = thumbXs[thumbXs.size / 2]
        val (thumbTop, thumbBottom) = spans[thumbCenterX]
        val thumbCenterY = (thumbTop + thumbBottom) / 2f

        val trackXs = lengths.mapIndexedNotNull { x, len ->
            if (len <= 0) return@mapIndexedNotNull null
            // Ignore thumb columns and obvious label glyphs; the track is a short
            // horizontal band a few dp tall.
            if (abs(x - thumbCenterX) < maxLen) return@mapIndexedNotNull null
            if (len in 1 until maxLen / 2 + 1) x else null
        }
        require(trackXs.isNotEmpty()) {
            "Could not find track columns away from the thumb (maxLen=$maxLen, thumbX=$thumbCenterX)"
        }
        var trackTopSum = 0
        var trackBottomSum = 0
        for (x in trackXs) {
            trackTopSum += spans[x].first
            trackBottomSum += spans[x].second
        }
        val trackTop = (trackTopSum.toFloat() / trackXs.size).roundToInt()
        val trackBottom = (trackBottomSum.toFloat() / trackXs.size).roundToInt()
        val trackCenterY = (trackTop + trackBottom) / 2f
        return Centers(
            thumbCenterY = thumbCenterY,
            trackCenterY = trackCenterY,
            thumbTop = thumbTop,
            thumbBottom = thumbBottom,
            trackTop = trackTop,
            trackBottom = trackBottom,
            thumbCenterX = thumbCenterX
        )
    }

    private fun rowBand(bitmap: Bitmap, density: Float): Bitmap {
        val rowPx = (NowPlayingChrome.PROGRESS_BAND_HEIGHT_DP * density).roundToInt()
        val h = minOf(rowPx, bitmap.height)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, h)
    }

    private fun zoomAroundThumb(row: Bitmap, measured: Centers, scale: Int): Bitmap {
        val pad = (measured.thumbBottom - measured.thumbTop + 1).coerceAtLeast(12)
        val left = (measured.thumbCenterX - pad * 2).coerceAtLeast(0)
        val top = (measured.thumbTop - pad).coerceAtLeast(0)
        val right = (measured.thumbCenterX + pad * 2).coerceAtMost(row.width)
        val bottom = (measured.thumbBottom + pad).coerceAtMost(row.height)
        val crop = Bitmap.createBitmap(row, left, top, right - left, bottom - top)
        return Bitmap.createScaledBitmap(crop, crop.width * scale, crop.height * scale, false)
    }

    private fun capture(filename: String): Bitmap {
        val content = rule.activity.findViewById<ViewGroup>(android.R.id.content)
        forceSoftwareLayer(content)
        val width = content.width.coerceAtLeast(1)
        val height = content.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        content.draw(AndroidCanvas(bitmap))
        save(bitmap, filename)
        return bitmap
    }

    private fun save(bitmap: Bitmap, filename: String) {
        FileOutputStream(File(proofDir, filename)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
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
