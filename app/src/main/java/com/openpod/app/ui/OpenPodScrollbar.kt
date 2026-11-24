package com.openpod.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Right-edge scrollbar: a fixed 48 dp tall, 4 dp #7FB7C9 thumb on a faint
 * 2 dp track. The thumb does not change size with estimated content. Its
 * position tracks the scroll fraction 0..1. Drag the thumb (or tap the
 * track) to jump; the thumb widens to 8 dp while it is being dragged.
 *
 * The 1.53 proportional thumb looked huge on Now Playing until items were
 * measured, then shrank. A fixed thumb removes that glitch.
 *
 * 1.54 still estimated a lazy list's scroll fraction from the average of
 * currently visible rows, so a tall first item (Now Playing artwork)
 * made the thumb crawl and then jump once shorter items were measured.
 * Now Playing (and the episode screen) use a non-lazy Column with a
 * [ScrollState], so fraction is value/maxValue from the first layout.
 * Other lazy lists accumulate measured item heights and estimate
 * unmeasured rows from a typical (outlier-resistant) average, and
 * drag mapping uses the same numbers. Fairly uniform lists can also
 * use the index-and-offset fraction.
 *
 * This host is a [Box] with the list as a child and a sibling rail drawn
 * on top at the right edge, both wired to the same scroll state. The rail's
 * touch target is a narrow 28 dp strip so row overflow buttons stay tappable.
 */
object OpenPodScrollbar {
    const val THUMB_WIDTH_DP = 4
    const val THUMB_DRAGGING_WIDTH_DP = 8
    const val THUMB_HEIGHT_DP = 48
    const val TRACK_WIDTH_DP = 2
    const val END_INSET_DP = 4
    const val TRACK_RIGHT_INSET_DP = 4
    const val TOUCH_WIDTH_DP = 28
    const val TRACK_ALPHA = 0.22f
    const val RAIL_WIDTH_DP = TOUCH_WIDTH_DP
    const val RAIL_TAG = "openpod-scrollbar-rail"

    val THUMB_COLOR = Color(0xFF7FB7C9)
    val TRACK_COLOR = Color(0xFFE8F1F5).copy(alpha = TRACK_ALPHA)

    data class Metrics(
        val thumbLength: Float,
        val thumbOffset: Float,
        val trackLength: Float,
        val fraction: Float
    ) {
        val isVisible: Boolean get() = thumbLength > 0f && trackLength > 0f
    }

    data class LazyItemMeasure(
        val index: Int,
        val offset: Int,
        val size: Int
    )

    data class LazyScrollTarget(
        val index: Int,
        val scrollOffset: Int
    )

    fun shouldShow(canScrollForward: Boolean, canScrollBackward: Boolean): Boolean {
        return canScrollForward || canScrollBackward
    }

    /**
     * Fixed-size thumb whose offset is [fraction] along the travel of the track.
     * [thumbLengthPx] is always used (coerced into the track), never estimated
     * from visible/total content.
     */
    fun layout(
        canScroll: Boolean,
        fraction: Float,
        trackLengthPx: Float,
        thumbLengthPx: Float
    ): Metrics? {
        if (!canScroll || trackLengthPx <= 0f || thumbLengthPx <= 0f) return null
        val thumb = thumbLengthPx.coerceAtMost(trackLengthPx)
        val travel = (trackLengthPx - thumb).coerceAtLeast(0f)
        val clamped = fraction.coerceIn(0f, 1f)
        return Metrics(
            thumbLength = thumb,
            thumbOffset = travel * clamped,
            trackLength = trackLengthPx,
            fraction = clamped
        )
    }

    fun scrollFraction(scrollPx: Float, maxScrollPx: Float): Float {
        if (maxScrollPx <= 0f) return 0f
        return (scrollPx / maxScrollPx).coerceIn(0f, 1f)
    }

    fun scrollPxFromFraction(fraction: Float, maxScrollPx: Float): Int {
        if (maxScrollPx <= 0f) return 0
        return (fraction.coerceIn(0f, 1f) * maxScrollPx).toInt().coerceIn(0, maxScrollPx.toInt())
    }

    /**
     * Maps a pointer Y on the rail to a 0..1 fraction. [grabOffsetInThumb]
     * is the distance from the thumb's top to the contact point (use half
     * the thumb height for a track tap so the thumb centers on the tap).
     */
    fun fractionFromPointer(
        pointerY: Float,
        trackTop: Float,
        trackLength: Float,
        thumbLength: Float,
        grabOffsetInThumb: Float
    ): Float {
        val travel = (trackLength - thumbLength).coerceAtLeast(0f)
        if (travel <= 0f) return 0f
        val thumbTop = pointerY - trackTop - grabOffsetInThumb
        return (thumbTop / travel).coerceIn(0f, 1f)
    }

    fun pointerIsOnThumb(
        pointerY: Float,
        trackTop: Float,
        thumbOffset: Float,
        thumbLength: Float
    ): Boolean {
        val top = trackTop + thumbOffset
        return pointerY >= top && pointerY <= top + thumbLength
    }

    fun averageRowPx(visible: List<LazyItemMeasure>, columns: Int): Float {
        if (visible.isEmpty()) return 1f
        val rowOffsets = visible.map { it.offset }.distinct().sorted()
        return if (rowOffsets.size >= 2) {
            (rowOffsets.last() - rowOffsets.first()).toFloat() / (rowOffsets.size - 1)
        } else {
            visible.maxOf { it.size }.toFloat()
        }.coerceAtLeast(1f)
    }

    fun lazyScrollPx(
        viewportStart: Int,
        beforeContentPadding: Int,
        visible: List<LazyItemMeasure>,
        columns: Int,
        averageRowPx: Float
    ): Float {
        if (visible.isEmpty()) return 0f
        val cols = columns.coerceAtLeast(1)
        val first = visible.minBy { it.index }
        val firstRow = first.index / cols
        return firstRow * averageRowPx - (first.offset - viewportStart) + beforeContentPadding
    }

    fun lazyContentPx(
        totalItems: Int,
        columns: Int,
        averageRowPx: Float,
        beforeContentPadding: Int,
        afterContentPadding: Int
    ): Float {
        val cols = columns.coerceAtLeast(1)
        val totalRows = ((totalItems + cols - 1) / cols).toFloat()
        return totalRows * averageRowPx + beforeContentPadding + afterContentPadding
    }

    fun lazyScrollTarget(
        fraction: Float,
        totalItems: Int,
        columns: Int,
        averageRowPx: Float,
        viewportPx: Float,
        beforeContentPadding: Int,
        afterContentPadding: Int
    ): LazyScrollTarget {
        if (totalItems <= 0) return LazyScrollTarget(0, 0)
        val cols = columns.coerceAtLeast(1)
        val content = lazyContentPx(totalItems, cols, averageRowPx, beforeContentPadding, afterContentPadding)
        val maxScroll = (content - viewportPx).coerceAtLeast(0f)
        val scrollPx = fraction.coerceIn(0f, 1f) * maxScroll
        val intoContent = (scrollPx - beforeContentPadding).coerceAtLeast(0f)
        val row = (intoContent / averageRowPx).toInt()
            .coerceAtMost(((totalItems + cols - 1) / cols - 1).coerceAtLeast(0))
        val offset = (intoContent - row * averageRowPx).toInt().coerceAtLeast(0)
        val index = (row * cols).coerceIn(0, totalItems - 1)
        return LazyScrollTarget(index = index, scrollOffset = offset)
    }

    fun mergeMeasuredSizes(
        stored: Map<Int, Int>,
        visible: List<LazyItemMeasure>,
        totalItems: Int
    ): Map<Int, Int> {
        val out = LinkedHashMap<Int, Int>(stored.size + visible.size)
        stored.forEach { (index, size) ->
            if (index in 0 until totalItems) out[index] = size
        }
        visible.forEach { item ->
            if (item.index in 0 until totalItems) out[item.index] = item.size
        }
        return out
    }

    /**
     * Typical height for items not yet measured. A single tall header
     * (or any outlier more than 2x the median) is kept for that item
     * but is not applied to the rest of the list.
     */
    fun averageForUnmeasured(measuredSizes: Map<Int, Int>): Float {
        if (measuredSizes.isEmpty()) return 1f
        val values = measuredSizes.values.map { it.toFloat() }.sorted()
        val median = values[values.size / 2]
        val typical = values.filter { it <= median * 2f }
        val used = if (typical.isEmpty()) values else typical
        return (used.sum() / used.size).coerceAtLeast(1f)
    }

    fun rowHeight(
        row: Int,
        columns: Int,
        totalItems: Int,
        measuredSizes: Map<Int, Int>,
        unmeasuredAvg: Float
    ): Float {
        val cols = columns.coerceAtLeast(1)
        val start = row * cols
        val end = (start + cols).coerceAtMost(totalItems)
        if (start >= end) return unmeasuredAvg.coerceAtLeast(1f)
        var height = 0f
        for (index in start until end) {
            val size = measuredSizes[index]?.toFloat() ?: unmeasuredAvg
            if (size > height) height = size
        }
        return height.coerceAtLeast(1f)
    }

    fun lazyContentPxFromSizes(
        totalItems: Int,
        columns: Int,
        measuredSizes: Map<Int, Int>,
        unmeasuredAvg: Float,
        beforeContentPadding: Int,
        afterContentPadding: Int
    ): Float {
        val cols = columns.coerceAtLeast(1)
        val totalRows = (totalItems + cols - 1) / cols
        var height = 0f
        for (row in 0 until totalRows) {
            height += rowHeight(row, cols, totalItems, measuredSizes, unmeasuredAvg)
        }
        return height + beforeContentPadding + afterContentPadding
    }

    fun lazyScrollPxFromSizes(
        viewportStart: Int,
        beforeContentPadding: Int,
        visible: List<LazyItemMeasure>,
        columns: Int,
        totalItems: Int,
        measuredSizes: Map<Int, Int>,
        unmeasuredAvg: Float
    ): Float {
        if (visible.isEmpty() || totalItems <= 0) return 0f
        val cols = columns.coerceAtLeast(1)
        val first = visible.minBy { it.index }
        val firstRow = first.index / cols
        var before = 0f
        for (row in 0 until firstRow) {
            before += rowHeight(row, cols, totalItems, measuredSizes, unmeasuredAvg)
        }
        val intoRow = (viewportStart - first.offset).toFloat()
        return (before + intoRow + beforeContentPadding).coerceAtLeast(0f)
    }

    fun lazyScrollTargetFromSizes(
        fraction: Float,
        totalItems: Int,
        columns: Int,
        viewportPx: Float,
        beforeContentPadding: Int,
        afterContentPadding: Int,
        measuredSizes: Map<Int, Int>,
        unmeasuredAvg: Float
    ): LazyScrollTarget {
        if (totalItems <= 0) return LazyScrollTarget(0, 0)
        val cols = columns.coerceAtLeast(1)
        val content = lazyContentPxFromSizes(
            totalItems = totalItems,
            columns = cols,
            measuredSizes = measuredSizes,
            unmeasuredAvg = unmeasuredAvg,
            beforeContentPadding = beforeContentPadding,
            afterContentPadding = afterContentPadding
        )
        val maxScroll = (content - viewportPx).coerceAtLeast(0f)
        val scrollPx = fraction.coerceIn(0f, 1f) * maxScroll
        val intoContent = (scrollPx - beforeContentPadding).coerceAtLeast(0f)
        val totalRows = (totalItems + cols - 1) / cols
        var acc = 0f
        for (row in 0 until totalRows) {
            val height = rowHeight(row, cols, totalItems, measuredSizes, unmeasuredAvg)
            if (acc + height > intoContent || row == totalRows - 1) {
                val offset = (intoContent - acc).toInt().coerceAtLeast(0)
                val index = (row * cols).coerceIn(0, totalItems - 1)
                return LazyScrollTarget(index = index, scrollOffset = offset)
            }
            acc += height
        }
        return LazyScrollTarget((totalItems - 1).coerceAtLeast(0), 0)
    }

    /**
     * Stable 0..1 mapping for fairly uniform rows: fractional index of
     * the first visible row over the remaining scrollable rows, using
     * the first and last item's pixel offset for smoothing. Does not
     * invent a pixel length for unmeasured items.
     */
    fun lazyFractionFromIndex(
        firstIndex: Int,
        firstOffset: Int,
        firstSize: Int,
        lastIndex: Int,
        lastOffset: Int,
        lastSize: Int,
        totalItems: Int,
        viewportStart: Int,
        viewportEnd: Int,
        columns: Int = 1
    ): Float {
        if (totalItems <= 0) return 0f
        val cols = columns.coerceAtLeast(1)
        val totalRows = (totalItems + cols - 1) / cols
        val firstRow = firstIndex / cols
        val lastRow = lastIndex / cols
        val firstSizeSafe = firstSize.coerceAtLeast(1)
        val lastSizeSafe = lastSize.coerceAtLeast(1)
        val firstProgress = ((viewportStart - firstOffset).toFloat() / firstSizeSafe).coerceIn(0f, 1f)
        val lastOverflow = ((lastOffset + lastSizeSafe - viewportEnd).toFloat() / lastSizeSafe).coerceIn(0f, 1f)
        val rowsBefore = firstRow + firstProgress
        val rowsAfter = (totalRows - 1 - lastRow) + lastOverflow
        val denom = rowsBefore + rowsAfter
        if (denom <= 0f) return 0f
        return (rowsBefore / denom).coerceIn(0f, 1f)
    }

    fun fromScrollValues(
        viewportPx: Float,
        maxScrollPx: Float,
        scrollPx: Float,
        endInsetPx: Float,
        thumbLengthPx: Float
    ): Metrics? {
        if (maxScrollPx <= 0f) return null
        val track = (viewportPx - 2f * endInsetPx).coerceAtLeast(0f)
        return layout(
            canScroll = true,
            fraction = scrollFraction(scrollPx, maxScrollPx),
            trackLengthPx = track,
            thumbLengthPx = thumbLengthPx
        )
    }

    fun fromLazyMeasure(
        viewportStart: Int,
        viewportEnd: Int,
        totalItems: Int,
        beforeContentPadding: Int,
        afterContentPadding: Int,
        visible: List<LazyItemMeasure>,
        canScroll: Boolean,
        columns: Int,
        endInsetPx: Float,
        thumbLengthPx: Float,
        measuredSizes: Map<Int, Int> = emptyMap()
    ): Metrics? {
        if (!canScroll) return null
        val viewport = (viewportEnd - viewportStart).toFloat()
        if (viewport <= 0f || totalItems <= 0 || visible.isEmpty()) return null
        val cols = columns.coerceAtLeast(1)
        val sizes = mergeMeasuredSizes(measuredSizes, visible, totalItems)
        val unmeasuredAvg = averageForUnmeasured(sizes)
        var content = lazyContentPxFromSizes(
            totalItems = totalItems,
            columns = cols,
            measuredSizes = sizes,
            unmeasuredAvg = unmeasuredAvg,
            beforeContentPadding = beforeContentPadding,
            afterContentPadding = afterContentPadding
        )
        if (content <= viewport) {
            content = viewport + 1f
        }
        val scroll = lazyScrollPxFromSizes(
            viewportStart = viewportStart,
            beforeContentPadding = beforeContentPadding,
            visible = visible,
            columns = cols,
            totalItems = totalItems,
            measuredSizes = sizes,
            unmeasuredAvg = unmeasuredAvg
        )
        val maxScroll = content - viewport
        val track = (viewport - 2f * endInsetPx).coerceAtLeast(0f)
        return layout(
            canScroll = true,
            fraction = scrollFraction(scroll, maxScroll),
            trackLengthPx = track,
            thumbLengthPx = thumbLengthPx
        )
    }

    fun fromLazyList(
        state: LazyListState,
        endInsetPx: Float,
        thumbLengthPx: Float,
        measuredSizes: Map<Int, Int> = emptyMap()
    ): Metrics? {
        val info = state.layoutInfo
        return fromLazyMeasure(
            viewportStart = info.viewportStartOffset,
            viewportEnd = info.viewportEndOffset,
            totalItems = info.totalItemsCount,
            beforeContentPadding = info.beforeContentPadding,
            afterContentPadding = info.afterContentPadding,
            visible = info.visibleItemsInfo.map {
                LazyItemMeasure(it.index, it.offset, it.size)
            },
            canScroll = shouldShow(state.canScrollForward, state.canScrollBackward),
            columns = 1,
            endInsetPx = endInsetPx,
            thumbLengthPx = thumbLengthPx,
            measuredSizes = measuredSizes
        )
    }

    fun fromLazyGrid(
        state: LazyGridState,
        endInsetPx: Float,
        thumbLengthPx: Float,
        measuredSizes: Map<Int, Int> = emptyMap()
    ): Metrics? {
        val info = state.layoutInfo
        val visible = info.visibleItemsInfo
        val columns = visible.groupBy { it.offset.y }.values.maxOfOrNull { it.size } ?: 1
        return fromLazyMeasure(
            viewportStart = info.viewportStartOffset,
            viewportEnd = info.viewportEndOffset,
            totalItems = info.totalItemsCount,
            beforeContentPadding = info.beforeContentPadding,
            afterContentPadding = info.afterContentPadding,
            visible = visible.map {
                LazyItemMeasure(it.index, it.offset.y, it.size.height)
            },
            canScroll = shouldShow(state.canScrollForward, state.canScrollBackward),
            columns = columns,
            endInsetPx = endInsetPx,
            thumbLengthPx = thumbLengthPx,
            measuredSizes = measuredSizes
        )
    }

    fun fromScrollState(
        state: ScrollState,
        viewportPx: Float,
        endInsetPx: Float,
        thumbLengthPx: Float
    ): Metrics? {
        return fromScrollValues(
            viewportPx = viewportPx,
            maxScrollPx = state.maxValue.toFloat(),
            scrollPx = state.value.toFloat(),
            endInsetPx = endInsetPx,
            thumbLengthPx = thumbLengthPx
        )
    }

    suspend fun scrollListToFraction(
        state: LazyListState,
        fraction: Float,
        measuredSizes: Map<Int, Int> = emptyMap()
    ) {
        val info = state.layoutInfo
        val visibleInfo = info.visibleItemsInfo
        if (visibleInfo.isEmpty() || info.totalItemsCount <= 0) return
        val viewport = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
        val visible = visibleInfo.map { LazyItemMeasure(it.index, it.offset, it.size) }
        val sizes = mergeMeasuredSizes(measuredSizes, visible, info.totalItemsCount)
        val target = lazyScrollTargetFromSizes(
            fraction = fraction,
            totalItems = info.totalItemsCount,
            columns = 1,
            viewportPx = viewport,
            beforeContentPadding = info.beforeContentPadding,
            afterContentPadding = info.afterContentPadding,
            measuredSizes = sizes,
            unmeasuredAvg = averageForUnmeasured(sizes)
        )
        state.scrollToItem(target.index, target.scrollOffset)
    }

    suspend fun scrollGridToFraction(
        state: LazyGridState,
        fraction: Float,
        measuredSizes: Map<Int, Int> = emptyMap()
    ) {
        val info = state.layoutInfo
        val visibleInfo = info.visibleItemsInfo
        if (visibleInfo.isEmpty() || info.totalItemsCount <= 0) return
        val columns = visibleInfo.groupBy { it.offset.y }.values.maxOfOrNull { it.size } ?: 1
        val viewport = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
        val visible = visibleInfo.map { LazyItemMeasure(it.index, it.offset.y, it.size.height) }
        val sizes = mergeMeasuredSizes(measuredSizes, visible, info.totalItemsCount)
        val target = lazyScrollTargetFromSizes(
            fraction = fraction,
            totalItems = info.totalItemsCount,
            columns = columns,
            viewportPx = viewport,
            beforeContentPadding = info.beforeContentPadding,
            afterContentPadding = info.afterContentPadding,
            measuredSizes = sizes,
            unmeasuredAvg = averageForUnmeasured(sizes)
        )
        state.scrollToItem(target.index, target.scrollOffset)
    }

    suspend fun scrollToFraction(state: ScrollState, fraction: Float) {
        state.scrollTo(scrollPxFromFraction(fraction, state.maxValue.toFloat()))
    }
}

fun DrawScope.drawOpenPodScrollbarRail(
    metrics: OpenPodScrollbar.Metrics,
    dragging: Boolean = false,
    thumbColor: Color = OpenPodScrollbar.THUMB_COLOR,
    trackColor: Color = OpenPodScrollbar.TRACK_COLOR
) {
    val thumbWidth = (if (dragging) {
        OpenPodScrollbar.THUMB_DRAGGING_WIDTH_DP
    } else {
        OpenPodScrollbar.THUMB_WIDTH_DP
    }).dp.toPx()
    val trackWidth = OpenPodScrollbar.TRACK_WIDTH_DP.dp.toPx()
    val endInset = OpenPodScrollbar.END_INSET_DP.dp.toPx()
    val rightInset = OpenPodScrollbar.TRACK_RIGHT_INSET_DP.dp.toPx()
    val thumbLeft = size.width - rightInset - thumbWidth
    val trackLeft = thumbLeft + (thumbWidth - trackWidth) / 2f
    drawRoundRect(
        color = trackColor,
        topLeft = Offset(trackLeft, endInset),
        size = Size(trackWidth, metrics.trackLength),
        cornerRadius = CornerRadius(trackWidth / 2f)
    )
    drawRoundRect(
        color = thumbColor,
        topLeft = Offset(thumbLeft, endInset + metrics.thumbOffset),
        size = Size(thumbWidth, metrics.thumbLength),
        cornerRadius = CornerRadius(thumbWidth / 2f)
    )
}

@Composable
fun BoxScope.OpenPodScrollbarRail(
    metrics: OpenPodScrollbar.Metrics?,
    onSeek: (Float) -> Unit
) {
    if (metrics == null) return
    var dragging by remember { mutableStateOf(false) }
    val metricsState = rememberUpdatedState(metrics)
    val onSeekState = rememberUpdatedState(onSeek)
    val chrome = LocalOpenPodColors.current
    Canvas(
        Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .width(OpenPodScrollbar.TOUCH_WIDTH_DP.dp)
            .testTag(OpenPodScrollbar.RAIL_TAG)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    val current = metricsState.value
                    val trackTop = OpenPodScrollbar.END_INSET_DP.dp.toPx()
                    val onThumb = OpenPodScrollbar.pointerIsOnThumb(
                        pointerY = down.position.y,
                        trackTop = trackTop,
                        thumbOffset = current.thumbOffset,
                        thumbLength = current.thumbLength
                    )
                    val grab = if (onThumb) {
                        (down.position.y - trackTop - current.thumbOffset)
                            .coerceIn(0f, current.thumbLength)
                    } else {
                        current.thumbLength / 2f
                    }
                    fun seekTo(y: Float) {
                        onSeekState.value(
                            OpenPodScrollbar.fractionFromPointer(
                                pointerY = y,
                                trackTop = trackTop,
                                trackLength = current.trackLength,
                                thumbLength = current.thumbLength,
                                grabOffsetInThumb = grab
                            )
                        )
                    }
                    if (!onThumb) {
                        seekTo(down.position.y)
                    }
                    dragging = true
                    drag(down.id) { change ->
                        change.consume()
                        seekTo(change.position.y)
                    }
                    dragging = false
                }
            }
    ) {
        drawOpenPodScrollbarRail(
            metrics,
            dragging,
            thumbColor = chrome.scrollbarThumb,
            trackColor = chrome.scrollbarTrack
        )
    }
}

@Composable
fun OpenPodScrollbarBox(
    state: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var seekJob by remember { mutableStateOf<Job?>(null) }
    val measured = remember { mutableMapOf<Int, Int>() }
    val metrics by remember(state, density) {
        derivedStateOf {
            val info = state.layoutInfo
            info.visibleItemsInfo.forEach { measured[it.index] = it.size }
            measured.keys.retainAll { it < info.totalItemsCount }
            OpenPodScrollbar.fromLazyList(
                state = state,
                endInsetPx = with(density) { OpenPodScrollbar.END_INSET_DP.dp.toPx() },
                thumbLengthPx = with(density) { OpenPodScrollbar.THUMB_HEIGHT_DP.dp.toPx() },
                measuredSizes = measured
            )
        }
    }
    Box(modifier) {
        content()
        OpenPodScrollbarRail(metrics) { fraction ->
            seekJob?.cancel()
            seekJob = scope.launch { OpenPodScrollbar.scrollListToFraction(state, fraction, measured) }
        }
    }
}

@Composable
fun OpenPodScrollbarBox(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var seekJob by remember { mutableStateOf<Job?>(null) }
    val measured = remember { mutableMapOf<Int, Int>() }
    val metrics by remember(state, density) {
        derivedStateOf {
            val info = state.layoutInfo
            info.visibleItemsInfo.forEach { measured[it.index] = it.size.height }
            measured.keys.retainAll { it < info.totalItemsCount }
            OpenPodScrollbar.fromLazyGrid(
                state = state,
                endInsetPx = with(density) { OpenPodScrollbar.END_INSET_DP.dp.toPx() },
                thumbLengthPx = with(density) { OpenPodScrollbar.THUMB_HEIGHT_DP.dp.toPx() },
                measuredSizes = measured
            )
        }
    }
    Box(modifier) {
        content()
        OpenPodScrollbarRail(metrics) { fraction ->
            seekJob?.cancel()
            seekJob = scope.launch { OpenPodScrollbar.scrollGridToFraction(state, fraction, measured) }
        }
    }
}

@Composable
fun OpenPodScrollbarBox(
    state: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var seekJob by remember { mutableStateOf<Job?>(null) }
    val metrics by remember(state, density) {
        derivedStateOf {
            OpenPodScrollbar.fromScrollState(
                state = state,
                viewportPx = state.viewportSize.toFloat().coerceAtLeast(0f),
                endInsetPx = with(density) { OpenPodScrollbar.END_INSET_DP.dp.toPx() },
                thumbLengthPx = with(density) { OpenPodScrollbar.THUMB_HEIGHT_DP.dp.toPx() }
            )
        }
    }
    Box(modifier) {
        content()
        OpenPodScrollbarRail(metrics) { fraction ->
            seekJob?.cancel()
            seekJob = scope.launch { OpenPodScrollbar.scrollToFraction(state, fraction) }
        }
    }
}
