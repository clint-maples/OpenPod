package com.openpod.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertNotNull
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
@Config(sdk = [33], application = Application::class, qualifiers = "w360dp-h780dp-xxhdpi")
class OpenPodScrollbarUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val proofDir = File("/workspace/openpod-scope/scrollbar")

    @Before
    fun createProofDir() {
        proofDir.mkdirs()
    }

    @Test
    fun overflowMathYieldsFixedThumbWhenContentExceedsViewport() {
        val state = LazyListState()
        rule.setContent { ProofPodcastScreen(state) }
        rule.waitForIdle()
        assertTrue(
            "List should be able to scroll after layout when the same state is passed to LazyColumn",
            state.canScrollForward || state.canScrollBackward
        )
        val density = rule.activity.resources.displayMetrics.density
        val inset = OpenPodScrollbar.END_INSET_DP * density
        val thumb = OpenPodScrollbar.THUMB_HEIGHT_DP * density
        val resolved = OpenPodScrollbar.fromLazyList(state, inset, thumb)
        assertNotNull("Overflow math should produce a thumb when content exceeds the viewport", resolved)
        assertTrue(resolved!!.isVisible)
        assertEqualsWithin("Fixed 48 dp thumb at rest, not a content-estimated length", thumb, resolved.thumbLength, 0.5f)
        assertTrue(resolved.thumbLength <= resolved.trackLength)
        assertEqualsWithin("Fraction at rest should be near the top", 0f, resolved.fraction, 0.05f)
    }

    @Test
    fun podcastScreenPaintsFixedThumbAtRestAndAfterDrag() {
        val podcastState = LazyListState()
        rule.setContent { ProofPodcastScreen(podcastState) }
        rule.waitForIdle()
        val atRest = capture("proof154-podcast-rest.png")
        val restPixels = thumbPixels(atRest)
        assertTrue(
            "Podcast screen at rest must have #7FB7C9 thumb pixels at the right edge (found ${restPixels.size})",
            restPixels.size >= 20
        )
        val restHeight = thumbHeight(restPixels)
        val restTop = restPixels.minOf { it.second }
        assertFixedThumbHeight("podcast rest", restHeight, atRest)

        dragRailTowardBottom()
        rule.waitForIdle()
        val dragged = capture("proof154-podcast-drag.png")
        val draggedPixels = thumbPixels(dragged)
        assertTrue(
            "Podcast screen after dragging the rail must still show the thumb (found ${draggedPixels.size})",
            draggedPixels.size >= 20
        )
        val draggedHeight = thumbHeight(draggedPixels)
        val draggedTop = draggedPixels.minOf { it.second }
        assertFixedThumbHeight("podcast after drag", draggedHeight, dragged)
        assertTrue(
            "Thumb must move down after a downward drag (rest top=$restTop, drag top=$draggedTop)",
            draggedTop > restTop + 40
        )
        assertTrue(
            "List scroll position must have advanced after dragging the rail",
            podcastState.firstVisibleItemIndex > 0 || podcastState.firstVisibleItemScrollOffset > 0
        )
    }

    @Test
    fun libraryScreenPaintsFixedThumbAtRestAndAfterDrag() {
        val libraryState = LazyListState()
        rule.setContent { ProofLibraryScreen(libraryState) }
        rule.waitForIdle()
        val libraryRest = capture("proof154-library-rest.png")
        val libraryRestPixels = thumbPixels(libraryRest)
        assertTrue(
            "Library screen at rest must have #7FB7C9 thumb pixels at the right edge (found ${libraryRestPixels.size})",
            libraryRestPixels.size >= 20
        )
        val libraryRestHeight = thumbHeight(libraryRestPixels)
        val libraryRestTop = libraryRestPixels.minOf { it.second }
        assertFixedThumbHeight("library rest", libraryRestHeight, libraryRest)

        dragRailTowardBottom()
        rule.waitForIdle()
        val libraryDragged = capture("proof154-library-drag.png")
        val libraryDraggedPixels = thumbPixels(libraryDragged)
        assertTrue(
            "Library screen after dragging the rail must still show the thumb (found ${libraryDraggedPixels.size})",
            libraryDraggedPixels.size >= 20
        )
        val libraryDraggedHeight = thumbHeight(libraryDraggedPixels)
        val libraryDraggedTop = libraryDraggedPixels.minOf { it.second }
        assertFixedThumbHeight("library after drag", libraryDraggedHeight, libraryDragged)
        assertTrue(
            "Thumb must move down after a downward drag (rest top=$libraryRestTop, drag top=$libraryDraggedTop)",
            libraryDraggedTop > libraryRestTop + 40
        )
        assertTrue(
            "List scroll position must have advanced after dragging the rail",
            libraryState.firstVisibleItemIndex > 0 || libraryState.firstVisibleItemScrollOffset > 0
        )
    }

    private fun dragRailTowardBottom() {
        rule.onNodeWithTag(OpenPodScrollbar.RAIL_TAG).assertExists()
        rule.onNodeWithTag(OpenPodScrollbar.RAIL_TAG).performTouchInput {
            val startY = (OpenPodScrollbar.END_INSET_DP + OpenPodScrollbar.THUMB_HEIGHT_DP / 2f).dp.toPx()
            val endY = visibleSize.height * 0.72f
            val x = visibleSize.width / 2f
            down(Offset(x, startY))
            moveTo(Offset(x, endY))
            up()
        }
    }

    private fun assertFixedThumbHeight(label: String, measuredPx: Int, bitmap: Bitmap) {
        val density = bitmap.width / 360f
        val expected = (OpenPodScrollbar.THUMB_HEIGHT_DP * density).toInt()
        assertTrue(
            "$label thumb height must stay a fixed 48 dp ($expected px), was $measuredPx",
            abs(measuredPx - expected) <= 16
        )
    }

    private fun assertEqualsWithin(message: String, expected: Float, actual: Float, delta: Float) {
        assertTrue("$message expected=$expected actual=$actual delta=$delta", abs(expected - actual) <= delta)
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

    private fun thumbPixels(bitmap: Bitmap): List<Pair<Int, Int>> {
        val density = bitmap.width / 360f
        val stripStart = (bitmap.width - 16f * density).toInt().coerceAtLeast(0)
        val found = ArrayList<Pair<Int, Int>>()
        var y = 0
        while (y < bitmap.height) {
            var x = stripStart
            while (x < bitmap.width) {
                val color = bitmap.getPixel(x, y)
                val a = android.graphics.Color.alpha(color)
                val r = android.graphics.Color.red(color)
                val g = android.graphics.Color.green(color)
                val b = android.graphics.Color.blue(color)
                val dist = kotlin.math.abs(r - 0x7F) + kotlin.math.abs(g - 0xB7) + kotlin.math.abs(b - 0xC9)
                if (a > 180 && dist <= 36) {
                    found.add(x to y)
                }
                x++
            }
            y++
        }
        return found
    }

    private fun thumbHeight(pixels: List<Pair<Int, Int>>): Int {
        return pixels.maxOf { it.second } - pixels.minOf { it.second } + 1
    }

    companion object {
        const val PROOF_WIDTH = 1080
        const val PROOF_HEIGHT = 2340
    }
}

@Composable
private fun ProofPodcastScreen(state: LazyListState) {
    OpenPodTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Text(
                "Moonshots",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            )
            OpenPodScrollbarBox(state, Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
                    item {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF262B33))
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Moonshots", style = MaterialTheme.typography.titleMedium)
                                Text("Peter Diamandis", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    items(40) { index ->
                        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text("Episode ${214 - index}", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Nov ${16 - (index % 12)}, 2025",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProofLibraryScreen(state: LazyListState) {
    OpenPodTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Text(
                "Library",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            )
            OpenPodScrollbarBox(state, Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
                    items(30) { index ->
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF262B33))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Show ${index + 1}", maxLines = 1)
                                Text("Author ${index + 1}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
