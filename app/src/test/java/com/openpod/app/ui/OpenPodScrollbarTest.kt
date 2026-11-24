package com.openpod.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenPodScrollbarTest {
    private val endInset = 4f
    private val thumb = 48f

    @Test
    fun hiddenWhenContentFits() {
        assertFalse(OpenPodScrollbar.shouldShow(canScrollForward = false, canScrollBackward = false))
        assertNull(
            OpenPodScrollbar.layout(
                canScroll = false,
                fraction = 0f,
                trackLengthPx = 792f,
                thumbLengthPx = thumb
            )
        )
        assertNull(
            OpenPodScrollbar.fromScrollValues(
                viewportPx = 800f,
                maxScrollPx = 0f,
                scrollPx = 0f,
                endInsetPx = endInset,
                thumbLengthPx = thumb
            )
        )
    }

    @Test
    fun shownWhenContentOverflows() {
        assertTrue(OpenPodScrollbar.shouldShow(canScrollForward = true, canScrollBackward = false))
        assertTrue(OpenPodScrollbar.shouldShow(canScrollForward = false, canScrollBackward = true))
        val metrics = OpenPodScrollbar.layout(
            canScroll = true,
            fraction = 0f,
            trackLengthPx = 792f,
            thumbLengthPx = thumb
        )
        assertNotNull(metrics)
        assertTrue(metrics!!.isVisible)
    }

    @Test
    fun thumbLengthIsFixedRegardlessOfContentLength() {
        val shortList = OpenPodScrollbar.layout(true, 0f, 792f, thumb)!!
        val longList = OpenPodScrollbar.fromLazyMeasure(
            viewportStart = 0,
            viewportEnd = 800,
            totalItems = 4,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            visible = List(2) { i -> OpenPodScrollbar.LazyItemMeasure(i, i * 400, 400) },
            canScroll = true,
            columns = 1,
            endInsetPx = endInset,
            thumbLengthPx = thumb
        )!!
        val veryLong = OpenPodScrollbar.fromLazyMeasure(
            viewportStart = 0,
            viewportEnd = 800,
            totalItems = 200,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            visible = List(8) { i -> OpenPodScrollbar.LazyItemMeasure(i, i * 100, 100) },
            canScroll = true,
            columns = 1,
            endInsetPx = endInset,
            thumbLengthPx = thumb
        )!!
        assertEquals(thumb, shortList.thumbLength, 0f)
        assertEquals(thumb, longList.thumbLength, 0f)
        assertEquals(thumb, veryLong.thumbLength, 0f)
        assertEquals(longList.thumbLength, veryLong.thumbLength, 0f)
    }

    @Test
    fun thumbPositionTracksFractionFromZeroToOne() {
        val track = 792f
        val atTop = OpenPodScrollbar.layout(true, 0f, track, thumb)!!
        val atMid = OpenPodScrollbar.layout(true, 0.5f, track, thumb)!!
        val atBottom = OpenPodScrollbar.layout(true, 1f, track, thumb)!!
        val travel = track - thumb
        assertEquals(0f, atTop.thumbOffset, 0.01f)
        assertEquals(travel / 2f, atMid.thumbOffset, 0.01f)
        assertEquals(travel, atBottom.thumbOffset, 0.01f)
        assertEquals(thumb, atTop.thumbLength, 0f)
        assertEquals(thumb, atMid.thumbLength, 0f)
        assertEquals(thumb, atBottom.thumbLength, 0f)
        assertEquals(0f, atTop.fraction, 0f)
        assertEquals(0.5f, atMid.fraction, 0f)
        assertEquals(1f, atBottom.fraction, 0f)
    }

    @Test
    fun scrollFractionAndPxAreInverses() {
        assertEquals(0f, OpenPodScrollbar.scrollFraction(0f, 1000f), 0f)
        assertEquals(0.25f, OpenPodScrollbar.scrollFraction(250f, 1000f), 0f)
        assertEquals(1f, OpenPodScrollbar.scrollFraction(1000f, 1000f), 0f)
        assertEquals(0f, OpenPodScrollbar.scrollFraction(10f, 0f), 0f)
        assertEquals(0, OpenPodScrollbar.scrollPxFromFraction(0f, 1000f))
        assertEquals(250, OpenPodScrollbar.scrollPxFromFraction(0.25f, 1000f))
        assertEquals(1000, OpenPodScrollbar.scrollPxFromFraction(1f, 1000f))
        assertEquals(0, OpenPodScrollbar.scrollPxFromFraction(0.5f, 0f))
        assertEquals(1f, OpenPodScrollbar.scrollFraction(10_000f, 1000f), 0f)
    }

    @Test
    fun fractionFromPointerMapsTravelAndInvertsLayoutOffset() {
        val trackTop = 4f
        val track = 792f
        val grab = thumb / 2f
        assertEquals(
            0f,
            OpenPodScrollbar.fractionFromPointer(trackTop + grab, trackTop, track, thumb, grab),
            0.01f
        )
        assertEquals(
            0.5f,
            OpenPodScrollbar.fractionFromPointer(trackTop + (track - thumb) / 2f + grab, trackTop, track, thumb, grab),
            0.01f
        )
        assertEquals(
            1f,
            OpenPodScrollbar.fractionFromPointer(trackTop + (track - thumb) + grab, trackTop, track, thumb, grab),
            0.01f
        )
        val mid = OpenPodScrollbar.layout(true, 0.5f, track, thumb)!!
        val recovered = OpenPodScrollbar.fractionFromPointer(
            pointerY = trackTop + mid.thumbOffset + grab,
            trackTop = trackTop,
            trackLength = track,
            thumbLength = thumb,
            grabOffsetInThumb = grab
        )
        assertEquals(0.5f, recovered, 0.01f)
    }

    @Test
    fun dragMappingUsesGrabOffsetInsideTheThumb() {
        val trackTop = 4f
        val track = 792f
        val atTop = OpenPodScrollbar.layout(true, 0f, track, thumb)!!
        val grabNearBottom = thumb - 8f
        // Pointer starts on the thumb near its bottom; moving it so the same
        // grab point sits at the mid travel should yield 0.5, not a jump to 1.
        val midPointer = trackTop + (track - thumb) / 2f + grabNearBottom
        val fraction = OpenPodScrollbar.fractionFromPointer(
            pointerY = midPointer,
            trackTop = trackTop,
            trackLength = track,
            thumbLength = atTop.thumbLength,
            grabOffsetInThumb = grabNearBottom
        )
        assertEquals(0.5f, fraction, 0.01f)
        assertTrue(
            OpenPodScrollbar.pointerIsOnThumb(
                pointerY = trackTop + 10f,
                trackTop = trackTop,
                thumbOffset = 0f,
                thumbLength = thumb
            )
        )
        assertFalse(
            OpenPodScrollbar.pointerIsOnThumb(
                pointerY = trackTop + thumb + 20f,
                trackTop = trackTop,
                thumbOffset = 0f,
                thumbLength = thumb
            )
        )
    }

    @Test
    fun lazyScrollTargetMapsFractionToIndexAndOffset() {
        val atStart = OpenPodScrollbar.lazyScrollTarget(
            fraction = 0f,
            totalItems = 20,
            columns = 1,
            averageRowPx = 100f,
            viewportPx = 800f,
            beforeContentPadding = 0,
            afterContentPadding = 0
        )
        assertEquals(0, atStart.index)
        assertEquals(0, atStart.scrollOffset)

        val atMid = OpenPodScrollbar.lazyScrollTarget(
            fraction = 0.5f,
            totalItems = 20,
            columns = 1,
            averageRowPx = 100f,
            viewportPx = 800f,
            beforeContentPadding = 0,
            afterContentPadding = 0
        )
        // content = 2000, maxScroll = 1200, scroll = 600 -> row 6, offset 0
        assertEquals(6, atMid.index)
        assertEquals(0, atMid.scrollOffset)

        val withOffset = OpenPodScrollbar.lazyScrollTarget(
            fraction = 0.5f,
            totalItems = 20,
            columns = 1,
            averageRowPx = 100f,
            viewportPx = 850f,
            beforeContentPadding = 0,
            afterContentPadding = 0
        )
        // maxScroll = 1150, scroll = 575 -> row 5, offset 75
        assertEquals(5, withOffset.index)
        assertEquals(75, withOffset.scrollOffset)

        val atEnd = OpenPodScrollbar.lazyScrollTarget(
            fraction = 1f,
            totalItems = 20,
            columns = 1,
            averageRowPx = 100f,
            viewportPx = 800f,
            beforeContentPadding = 0,
            afterContentPadding = 0
        )
        // scroll = 1200 -> row 12, offset 0
        assertEquals(12, atEnd.index)
        assertEquals(0, atEnd.scrollOffset)
    }

    @Test
    fun lazyGridTargetUsesRowsNotItems() {
        val mid = OpenPodScrollbar.lazyScrollTarget(
            fraction = 0.5f,
            totalItems = 12,
            columns = 3,
            averageRowPx = 100f,
            viewportPx = 200f,
            beforeContentPadding = 0,
            afterContentPadding = 0
        )
        // 4 rows, content 400, maxScroll 200, scroll 100 -> row 1, index 3
        assertEquals(3, mid.index)
        assertEquals(0, mid.scrollOffset)
    }

    @Test
    fun fromScrollValuesUsesFixedThumbAndScrollFraction() {
        val metrics = OpenPodScrollbar.fromScrollValues(
            viewportPx = 1000f,
            maxScrollPx = 1000f,
            scrollPx = 250f,
            endInsetPx = endInset,
            thumbLengthPx = thumb
        )!!
        assertEquals(992f, metrics.trackLength, 0f)
        assertEquals(thumb, metrics.thumbLength, 0f)
        assertEquals(0.25f, metrics.fraction, 0.01f)
        assertEquals((992f - thumb) * 0.25f, metrics.thumbOffset, 0.01f)
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
                thumbLengthPx = thumb
            )
        )
    }

    @Test
    fun lazyListFractionUsesEstimatedScrollButKeepsFixedThumb() {
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
            thumbLengthPx = thumb
        )!!
        // scroll = 240, maxScroll = 1200, fraction = 0.2
        assertEquals(thumb, metrics.thumbLength, 0f)
        assertEquals(0.2f, metrics.fraction, 0.01f)
        assertEquals((792f - thumb) * 0.2f, metrics.thumbOffset, 0.01f)
    }

    @Test
    fun constantsMatchFixedThumbSpec() {
        assertEquals(4, OpenPodScrollbar.THUMB_WIDTH_DP)
        assertEquals(8, OpenPodScrollbar.THUMB_DRAGGING_WIDTH_DP)
        assertEquals(48, OpenPodScrollbar.THUMB_HEIGHT_DP)
        assertEquals(2, OpenPodScrollbar.TRACK_WIDTH_DP)
        assertEquals(4, OpenPodScrollbar.END_INSET_DP)
        assertEquals(4, OpenPodScrollbar.TRACK_RIGHT_INSET_DP)
        assertEquals(28, OpenPodScrollbar.TOUCH_WIDTH_DP)
        assertEquals(OpenPodScrollbar.TOUCH_WIDTH_DP, OpenPodScrollbar.RAIL_WIDTH_DP)
        assertEquals(0.22f, OpenPodScrollbar.TRACK_ALPHA, 0f)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF7FB7C9), OpenPodScrollbar.THUMB_COLOR)
        assertTrue(OpenPodScrollbar.TRACK_COLOR.alpha > 0.2f)
        assertTrue(OpenPodScrollbar.TOUCH_WIDTH_DP in 24..32)
        assertTrue(OpenPodScrollbar.THUMB_DRAGGING_WIDTH_DP in 6..8)
    }
}
