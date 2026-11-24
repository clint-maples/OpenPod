package com.openpod.app.ui

import android.app.Application
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicReference

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class OpenPodScrollbarUiTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun overflowMathYieldsVisibleThumbWhenContentExceedsViewport() {
        val endInset = AtomicReference(0f)
        val minThumb = AtomicReference(0f)
        val state = LazyListState()
        rule.setContent {
            val density = LocalDensity.current
            endInset.set(with(density) { OpenPodScrollbar.END_INSET_DP.dp.toPx() })
            minThumb.set(with(density) { OpenPodScrollbar.MIN_THUMB_DP.dp.toPx() })
            OpenPodTheme {
                LazyColumn(
                    state = state,
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                ) {
                    items(40) { index ->
                        Text("Item $index", Modifier.height(48.dp).fillMaxWidth())
                    }
                }
            }
        }
        rule.waitForIdle()
        assertTrue(
            "List should be able to scroll after layout",
            state.canScrollForward || state.canScrollBackward
        )
        val metrics = OpenPodScrollbar.fromLazyList(state, endInset.get(), minThumb.get())
        assertNotNull("Overflow math should produce a thumb when content exceeds the viewport", metrics)
        assertTrue(metrics!!.isVisible)
        assertTrue(metrics.thumbLength >= minThumb.get() - 0.5f)
        assertTrue(metrics.thumbLength <= metrics.trackLength)
    }
}
