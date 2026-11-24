package com.openpod.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenPodScrollbarTest {
    private val endInset = 4f
    private val minThumb = 36f

    @Test
    fun hiddenWhenContentFits() {
        assertFalse(OpenPodScrollbar.shouldShow(canScrollForward = false, canScrollBackward = false))
        assertNull(
            OpenPodScrollbar.layout(
                viewportPx = 800f,
                contentPx = 800f,
                scrollPx = 0f,
                trackLengthPx = 792f,
                minThumbPx = minThumb
            )
        )
        assertNull(
            OpenPodScrollbar.layout(
                viewportPx = 800f,
                contentPx = 400f,
                scrollPx = 0f,
                trackLengthPx = 792f,
                minThumbPx = minThumb
            )
        )
        assertNull(
            OpenPodScrollbar.fromScrollValues(
                viewportPx = 800f,
                maxScrollPx = 0f,
                scrollPx = 0f,
                endInsetPx = endInset,
                minThumbPx = minThumb
            )
        )
    }

    @Test
    fun shownWhenContentOverflows() {
        assertTrue(OpenPodScrollbar.shouldShow(canScrollForward = true, canScrollBackward = false))
        assertTrue(OpenPodScrollbar.shouldShow(canScrollForward = false, canScrollBackward = true))
        val metrics = OpenPodScrollbar.layout(
            viewportPx = 800f,
            contentPx = 1600f,
            scrollPx = 0f,
            trackLengthPx = 792f,
            minThumbPx = minThumb
        )
        assertNotNull(metrics)
    }

    @Test
    fun thumbLengthIsProportionalToVisibleOverTotal() {
        val viewport = 800f
        val content = 1600f
        val track = 792f
        val metrics = OpenPodScrollbar.layout(viewport, content, 0f, track, minThumb)!!
        assertEquals(track * (viewport / content), metrics.thumbLength, 0.01f)
        assertEquals(track, metrics.trackLength, 0f)
        assertEquals(0f, metrics.thumbOffset, 0f)
        assertTrue(metrics.isVisible)
        assertTrue(metrics.thumbLength >= minThumb)
    }

    @Test
    fun thumbUsesMinimumWhenContentIsVeryLong() {
        val viewport = 800f
        val content = 40_000f
        val track = 792f
        val raw = track * (viewport / content)
        assertTrue(raw < minThumb)
        val metrics = OpenPodScrollbar.layout(viewport, content, 0f, track, minThumb)!!
        assertEquals(minThumb, metrics.thumbLength, 0f)
    }

    @Test
    fun thumbTravelsFromTopToBottomWithScroll() {
        val viewport = 800f
        val content = 1600f
        val track = 792f
        val atTop = OpenPodScrollbar.layout(viewport, content, 0f, track, minThumb)!!
        val atMid = OpenPodScrollbar.layout(viewport, content, 400f, track, minThumb)!!
        val atBottom = OpenPodScrollbar.layout(viewport, content, 800f, track, minThumb)!!
        val thumb = atTop.thumbLength
        assertEquals(0f, atTop.thumbOffset, 0.01f)
        assertEquals((track - thumb) / 2f, atMid.thumbOffset, 0.01f)
        assertEquals(track - thumb, atBottom.thumbOffset, 0.01f)
        assertEquals(thumb, atMid.thumbLength, 0f)
        assertEquals(thumb, atBottom.thumbLength, 0f)
    }

    @Test
    fun scrollStateGeometryMatchesViewportPlusMaxScroll() {
        val metrics = OpenPodScrollbar.fromScrollValues(
            viewportPx = 1000f,
            maxScrollPx = 1000f,
            scrollPx = 250f,
            endInsetPx = endInset,
            minThumbPx = minThumb
        )!!
        // track = 1000 - 8 = 992; thumb = 992 * 1000/2000 = 496; travel = 496; offset = 124
        assertEquals(992f, metrics.trackLength, 0f)
        assertEquals(496f, metrics.thumbLength, 0.01f)
        assertEquals(124f, metrics.thumbOffset, 0.01f)
    }

    @Test
    fun lazyListHidesWhenItCannotScroll() {
        val visible = List(3) { i ->
            OpenPodScrollbar.LazyItemMeasure(index = i, offset = i * 100, size = 100)
        }
        assertNull(
            OpenPodScrollbar.fromLazyMeasure(
                viewportStart = 0,
                viewportEnd = 800,
                totalItems = 3,
                beforeContentPadding = 0,
                afterContentPadding = 0,
                visible = visible,
                canScroll = false,
                columns = 1,
                endInsetPx = endInset,
                minThumbPx = minThumb
            )
        )
    }

    @Test
    fun lazyListThumbMatchesEstimatedContent() {
        val visible = List(8) { i ->
            OpenPodScrollbar.LazyItemMeasure(index = i, offset = i * 100, size = 100)
        }
        val metrics = OpenPodScrollbar.fromLazyMeasure(
            viewportStart = 0,
            viewportEnd = 800,
            totalItems = 20,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            visible = visible,
            canScroll = true,
            columns = 1,
            endInsetPx = endInset,
            minThumbPx = minThumb
        )!!
        // content = 20 * 100 = 2000; track = 792; thumb = 792 * 800/2000 = 316.8
        assertEquals(792f, metrics.trackLength, 0f)
        assertEquals(792f * (800f / 2000f), metrics.thumbLength, 0.01f)
        assertEquals(0f, metrics.thumbOffset, 0.01f)
    }

    @Test
    fun lazyListThumbMovesWithFirstItemOffset() {
        val visible = List(8) { i ->
            OpenPodScrollbar.LazyItemMeasure(index = i + 2, offset = (i * 100) - 40, size = 100)
        }
        val metrics = OpenPodScrollbar.fromLazyMeasure(
            viewportStart = 0,
            viewportEnd = 800,
            totalItems = 20,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            visible = visible,
            canScroll = true,
            columns = 1,
            endInsetPx = endInset,
            minThumbPx = minThumb
        )!!
        // scroll = 2 * 100 - (-40) = 240; maxScroll = 1200; travel = 792 - 316.8
        val thumb = 792f * (800f / 2000f)
        val expectedOffset = (792f - thumb) * (240f / 1200f)
        assertEquals(expectedOffset, metrics.thumbOffset, 0.01f)
    }

    @Test
    fun lazyGridUsesRowsNotItemsForThumbLength() {
        // 3-column grid, 12 items = 4 rows of 100 px, viewport shows 2 rows.
        val visible = listOf(
            OpenPodScrollbar.LazyItemMeasure(0, 0, 100),
            OpenPodScrollbar.LazyItemMeasure(1, 0, 100),
            OpenPodScrollbar.LazyItemMeasure(2, 0, 100),
            OpenPodScrollbar.LazyItemMeasure(3, 100, 100),
            OpenPodScrollbar.LazyItemMeasure(4, 100, 100),
            OpenPodScrollbar.LazyItemMeasure(5, 100, 100)
        )
        val metrics = OpenPodScrollbar.fromLazyMeasure(
            viewportStart = 0,
            viewportEnd = 200,
            totalItems = 12,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            visible = visible,
            canScroll = true,
            columns = 3,
            endInsetPx = endInset,
            minThumbPx = minThumb
        )!!
        // 4 rows * 100 = 400 content; thumb = 192 * 200/400 = 96
        assertEquals(192f, metrics.trackLength, 0f)
        assertEquals(192f * (200f / 400f), metrics.thumbLength, 0.01f)
    }

    @Test
    fun constantsMatchSbV1() {
        assertEquals(4, OpenPodScrollbar.THUMB_WIDTH_DP)
        assertEquals(2, OpenPodScrollbar.TRACK_WIDTH_DP)
        assertEquals(36, OpenPodScrollbar.MIN_THUMB_DP)
        assertEquals(4, OpenPodScrollbar.END_INSET_DP)
        assertEquals(4, OpenPodScrollbar.TRACK_RIGHT_INSET_DP)
        assertEquals(0.22f, OpenPodScrollbar.TRACK_ALPHA, 0f)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF7FB7C9), OpenPodScrollbar.THUMB_COLOR)
        assertTrue(OpenPodScrollbar.TRACK_COLOR.alpha > 0.2f)
        // Track starts 6 dp from the right edge: 4 dp inset + 2 dp track width.
        assertEquals(6, OpenPodScrollbar.TRACK_RIGHT_INSET_DP + OpenPodScrollbar.TRACK_WIDTH_DP)
    }
}
