package com.openpod.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicReference

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
    fun overflowMathYieldsVisibleThumbWhenContentExceedsViewport() {
        val state = LazyListState()
        rule.setContent { ProofPodcastScreen(state) }
        rule.waitForIdle()
        assertTrue(
            "List should be able to scroll after layout when the same state is passed to LazyColumn",
            state.canScrollForward || state.canScrollBackward
        )
        val density = rule.activity.resources.displayMetrics.density
        val inset = OpenPodScrollbar.END_INSET_DP * density
        val minThumbPx = OpenPodScrollbar.MIN_THUMB_DP * density
        val resolved = OpenPodScrollbar.fromLazyList(state, inset, minThumbPx)
        assertNotNull("Overflow math should produce a thumb when content exceeds the viewport", resolved)
        assertTrue(resolved!!.isVisible)
        assertTrue(resolved.thumbLength >= minThumbPx - 0.5f)
        assertTrue(resolved.thumbLength <= resolved.trackLength)
    }

    @Test
    fun podcastAndLibraryScreensPaintThumbAtRightEdgeAtRestAndAfterScroll() {
        val podcastState = LazyListState()
        rule.setContent { ProofPodcastScreen(podcastState) }
        rule.waitForIdle()
        val atRest = capture("proof-podcast-rest.png")
        val restPixels = thumbPixels(atRest)
        assertTrue(
            "Podcast screen at rest must have #7FB7C9 thumb pixels at the right edge (found ${restPixels.size})",
            restPixels.size >= 20
        )
        val restTop = restPixels.minOf { it.second }

        runBlocking { podcastState.scrollToItem(18) }
        rule.waitForIdle()
        val scrolled = capture("proof-podcast-scrolled.png")
        val scrolledPixels = thumbPixels(scrolled)
        assertTrue(
            "Podcast screen after scrolling must still show the thumb (found ${scrolledPixels.size})",
            scrolledPixels.size >= 20
        )
        val scrolledTop = scrolledPixels.minOf { it.second }
        assertTrue(
            "Thumb must move down after scrolling (rest top=$restTop, scrolled top=$scrolledTop)",
            scrolledTop > restTop + 8
        )
    }

    @Test
    fun libraryScreenPaintsThumbAtRightEdgeAtRestAndAfterScroll() {
        val libraryState = LazyListState()
        rule.setContent { ProofLibraryScreen(libraryState) }
        rule.waitForIdle()
        val libraryRest = capture("proof-library-rest.png")
        val libraryRestPixels = thumbPixels(libraryRest)
        assertTrue(
            "Library screen at rest must have #7FB7C9 thumb pixels at the right edge (found ${libraryRestPixels.size})",
            libraryRestPixels.size >= 20
        )
        val libraryRestTop = libraryRestPixels.minOf { it.second }

        runBlocking { libraryState.scrollToItem(16) }
        rule.waitForIdle()
        val libraryScrolled = capture("proof-library-scrolled.png")
        val libraryScrolledPixels = thumbPixels(libraryScrolled)
        assertTrue(
            "Library screen after scrolling must still show the thumb (found ${libraryScrolledPixels.size})",
            libraryScrolledPixels.size >= 20
        )
        val libraryScrolledTop = libraryScrolledPixels.minOf { it.second }
        assertTrue(
            "Thumb must move down after scrolling (rest top=$libraryRestTop, scrolled top=$libraryScrolledTop)",
            libraryScrolledTop > libraryRestTop + 8
        )
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
        val stripStart = (bitmap.width - 12f * density).toInt().coerceAtLeast(0)
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

    companion object {
        // 360 x 780 dp at xxhdpi (3x) matches the OpenPod mockup density.
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
