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
    fun averageForUnmeasuredIgnoresTallHeaderOutlier() {
        val sizes = mapOf(0 to 800, 1 to 80, 2 to 80, 3 to 80, 4 to 80)
        assertEquals(80f, OpenPodScrollbar.averageForUnmeasured(sizes), 0.01f)
        assertEquals(800f, OpenPodScrollbar.averageForUnmeasured(mapOf(0 to 800)), 0.01f)
        assertEquals(100f, OpenPodScrollbar.averageForUnmeasured(mapOf(0 to 100, 1 to 100, 2 to 100)), 0.01f)
    }

    @Test
    fun variableHeightsDoNotUseVisibleAverageForUnmeasuredItems() {
        // Now Playing-like: a tall first item, then many short rows. After a
        // few short rows are measured, the remaining list must not be
        // estimated at the first item's height (the 1.54 bug: thumb crawls
        // then jumps).
        val sizes = (listOf(0 to 800) + (1..8).map { it to 80 }).toMap()
        val visible = listOf(
            OpenPodScrollbar.LazyItemMeasure(index = 6, offset = -20, size = 80),
            OpenPodScrollbar.LazyItemMeasure(index = 7, offset = 60, size = 80),
            OpenPodScrollbar.LazyItemMeasure(index = 8, offset = 140, size = 80)
        )
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
            thumbLengthPx = thumb,
            measuredSizes = sizes
        )!!
        // content = 800 + 19*80 = 2320, maxScroll = 1520
        // first visible index 6: before = 800 + 5*80 = 1200, intoRow = 20, scroll = 1220
        assertEquals(thumb, metrics.thumbLength, 0f)
        assertEquals(1220f / 1520f, metrics.fraction, 0.01f)

        val visibleOnly = OpenPodScrollbar.fromLazyMeasure(
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
        // Without the stored map, only the short visible rows are known, so
        // the unmeasured first item is also estimated at 80. That is the
        // "after the rest loaded" estimate; it must not be ~1.0 the way the
        // old visible-average * totalItems math was (scroll 500 / max 800).
        assertTrue(
            "Visible-only short rows must not place the thumb at the end",
            visibleOnly.fraction < 0.7f
        )
    }

    @Test
    fun variableHeightFractionIsExactWhenEveryItemIsMeasured() {
        val sizes = (listOf(0 to 800) + (1..19).map { it to 80 }).toMap()
        val atTop = OpenPodScrollbar.fromLazyMeasure(
            viewportStart = 0,
            viewportEnd = 800,
            totalItems = 20,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            visible = listOf(OpenPodScrollbar.LazyItemMeasure(0, 0, 800)),
            canScroll = true,
            columns = 1,
            endInsetPx = endInset,
            thumbLengthPx = thumb,
            measuredSizes = sizes
        )!!
        assertEquals(0f, atTop.fraction, 0.01f)

        // Scrolled exactly the height of the tall first item: first visible
        // is item 1 at offset 0.
        val afterHeader = OpenPodScrollbar.fromLazyMeasure(
            viewportStart = 0,
            viewportEnd = 800,
            totalItems = 20,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            visible = List(10) { i ->
                OpenPodScrollbar.LazyItemMeasure(index = i + 1, offset = i * 80, size = 80)
            },
            canScroll = true,
            columns = 1,
            endInsetPx = endInset,
            thumbLengthPx = thumb,
            measuredSizes = sizes
        )!!
        // content 2320, max 1520, scroll 800
        assertEquals(800f / 1520f, afterHeader.fraction, 0.01f)

        val atEnd = OpenPodScrollbar.fromLazyMeasure(
            viewportStart = 0,
            viewportEnd = 800,
            totalItems = 20,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            visible = List(10) { i ->
                OpenPodScrollbar.LazyItemMeasure(index = i + 10, offset = i * 80, size = 80)
            },
            canScroll = true,
            columns = 1,
            endInsetPx = endInset,
            thumbLengthPx = thumb,
            measuredSizes = sizes
        )!!
        // first index 10, before = 800+9*80=1520, intoRow=0, scroll=1520, fraction=1
        assertEquals(1f, atEnd.fraction, 0.01f)
        assertEquals(thumb, atTop.thumbLength, 0f)
        assertEquals(thumb, afterHeader.thumbLength, 0f)
        assertEquals(thumb, atEnd.thumbLength, 0f)
    }

    @Test
    fun dragMappingWithVariableHeightsUsesTheSameSizeMath() {
        val sizes = (listOf(0 to 800) + (1..19).map { it to 80 }).toMap()
        val avg = OpenPodScrollbar.averageForUnmeasured(sizes)
        assertEquals(80f, avg, 0.01f)
        val afterHeader = OpenPodScrollbar.lazyScrollTargetFromSizes(
            fraction = 800f / 1520f,
            totalItems = 20,
            columns = 1,
            viewportPx = 800f,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            measuredSizes = sizes,
            unmeasuredAvg = avg
        )
        assertEquals(1, afterHeader.index)
        assertEquals(0, afterHeader.scrollOffset)

        val atStart = OpenPodScrollbar.lazyScrollTargetFromSizes(
            fraction = 0f,
            totalItems = 20,
            columns = 1,
            viewportPx = 800f,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            measuredSizes = sizes,
            unmeasuredAvg = avg
        )
        assertEquals(0, atStart.index)
        assertEquals(0, atStart.scrollOffset)

        val atEnd = OpenPodScrollbar.lazyScrollTargetFromSizes(
            fraction = 1f,
            totalItems = 20,
            columns = 1,
            viewportPx = 800f,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            measuredSizes = sizes,
            unmeasuredAvg = avg
        )
        assertEquals(10, atEnd.index)
        assertEquals(0, atEnd.scrollOffset)

        // The old visible-average target would treat every row as 80 and land
        // on index 5 at mid-list (0.5 * 800 / 80), skipping the tall header.
        val midOld = OpenPodScrollbar.lazyScrollTarget(
            fraction = 0.5f,
            totalItems = 20,
            columns = 1,
            averageRowPx = 80f,
            viewportPx = 800f,
            beforeContentPadding = 0,
            afterContentPadding = 0
        )
        val midNew = OpenPodScrollbar.lazyScrollTargetFromSizes(
            fraction = 0.5f,
            totalItems = 20,
            columns = 1,
            viewportPx = 800f,
            beforeContentPadding = 0,
            afterContentPadding = 0,
            measuredSizes = sizes,
            unmeasuredAvg = avg
        )
        assertEquals(5, midOld.index)
        // 0.5 * 1520 = 760, which is still inside the 800 px first item
        assertEquals(0, midNew.index)
        assertEquals(760, midNew.scrollOffset)
    }

    @Test
    fun indexBasedFractionIsStableForUniformRows() {
        assertEquals(
            0f,
            OpenPodScrollbar.lazyFractionFromIndex(
                firstIndex = 0,
                firstOffset = 0,
                firstSize = 100,
                lastIndex = 7,
                lastOffset = 700,
                lastSize = 100,
                totalItems = 20,
                viewportStart = 0,
                viewportEnd = 800
            ),
            0.01f
        )
        val mid = OpenPodScrollbar.lazyFractionFromIndex(
            firstIndex = 6,
            firstOffset = 0,
            firstSize = 100,
            lastIndex = 13,
            lastOffset = 700,
            lastSize = 100,
            totalItems = 20,
            viewportStart = 0,
            viewportEnd = 800
        )
        // rowsBefore=6, rowsAfter=(20-1-13)=6, fraction=0.5
        assertEquals(0.5f, mid, 0.01f)
        assertEquals(
            1f,
            OpenPodScrollbar.lazyFractionFromIndex(
                firstIndex = 12,
                firstOffset = 0,
                firstSize = 100,
                lastIndex = 19,
                lastOffset = 700,
                lastSize = 100,
                totalItems = 20,
                viewportStart = 0,
                viewportEnd = 800
            ),
            0.01f
        )
        val smoothed = OpenPodScrollbar.lazyFractionFromIndex(
            firstIndex = 6,
            firstOffset = -40,
            firstSize = 100,
            lastIndex = 14,
            lastOffset = 760,
            lastSize = 100,
            totalItems = 20,
            viewportStart = 0,
            viewportEnd = 800
        )
        // firstProgress=0.4, lastOverflow=0.6, rowsBefore=6.4, rowsAfter=5+0.6
        assertEquals(6.4f / (6.4f + 5.6f), smoothed, 0.01f)
    }

    @Test
    fun scrollStateFractionIsExactFromKnownMaxValue() {
        val mid = OpenPodScrollbar.fromScrollValues(
            viewportPx = 800f,
            maxScrollPx = 1520f,
            scrollPx = 760f,
            endInsetPx = endInset,
            thumbLengthPx = thumb
        )!!
        assertEquals(0.5f, mid.fraction, 0.01f)
        assertEquals(thumb, mid.thumbLength, 0f)
        assertEquals(760, OpenPodScrollbar.scrollPxFromFraction(0.5f, 1520f))
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
