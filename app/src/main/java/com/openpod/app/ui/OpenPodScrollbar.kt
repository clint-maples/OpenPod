package com.openpod.app.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * sb-v1 right-edge scrollbar: a 4 dp #7FB7C9 thumb on a faint 2 dp track.
 * The thumb length is proportional to visible / total content. The bar is
 * always shown when the content can scroll, and never auto-hides.
 *
 * 1.51 never painted the bar: overflow was decided in a @Composable Modifier
 * factory from [LazyListState.canScrollForward] / [ScrollState.maxValue]
 * during composition, before the first layout, and those pre-layout values
 * (false / 0) were captured into [Modifier.drawWithContent]. The factory did
 * not reliably restart after layout, so the draw pass kept skipping the thumb.
 * The modifiers below use [Modifier.composed] so scroll and layout state are
 * observed, and they re-read that state inside the draw pass itself.
 */
object OpenPodScrollbar {
    const val THUMB_WIDTH_DP = 4
    const val TRACK_WIDTH_DP = 2
    const val MIN_THUMB_DP = 36
    const val END_INSET_DP = 4
    const val TRACK_RIGHT_INSET_DP = 4
    const val TRACK_ALPHA = 0.22f

    val THUMB_COLOR = Color(0xFF7FB7C9)
    val TRACK_COLOR = Color(0xFFE8F1F5).copy(alpha = TRACK_ALPHA)

    data class Metrics(
        val thumbLength: Float,
        val thumbOffset: Float,
        val trackLength: Float
    ) {
        val isVisible: Boolean get() = thumbLength > 0f && trackLength > 0f
    }

    data class LazyItemMeasure(
        val index: Int,
        val offset: Int,
        val size: Int
    )

    fun shouldShow(canScrollForward: Boolean, canScrollBackward: Boolean): Boolean {
        return canScrollForward || canScrollBackward
    }

    /**
     * @param viewportPx visible height of the scrollable area
     * @param contentPx total content height
     * @param scrollPx how far the content has been scrolled (0 at the top)
     * @param trackLengthPx drawable track length (viewport minus end insets)
     * @param minThumbPx minimum thumb length, typically 36 dp in pixels
     */
    fun layout(
        viewportPx: Float,
        contentPx: Float,
        scrollPx: Float,
        trackLengthPx: Float,
        minThumbPx: Float
    ): Metrics? {
        if (viewportPx <= 0f || trackLengthPx <= 0f) return null
        if (contentPx <= viewportPx) return null
        val thumb = (trackLengthPx * (viewportPx / contentPx))
            .coerceAtLeast(minThumbPx)
            .coerceAtMost(trackLengthPx)
        val maxScroll = contentPx - viewportPx
        val scroll = scrollPx.coerceIn(0f, maxScroll)
        val travel = (trackLengthPx - thumb).coerceAtLeast(0f)
        val offset = if (maxScroll <= 0f) 0f else travel * (scroll / maxScroll)
        return Metrics(thumbLength = thumb, thumbOffset = offset, trackLength = trackLengthPx)
    }

    fun fromScrollValues(
        viewportPx: Float,
        maxScrollPx: Float,
        scrollPx: Float,
        endInsetPx: Float,
        minThumbPx: Float
    ): Metrics? {
        if (maxScrollPx <= 0f) return null
        val track = (viewportPx - 2f * endInsetPx).coerceAtLeast(0f)
        return layout(
            viewportPx = viewportPx,
            contentPx = viewportPx + maxScrollPx,
            scrollPx = scrollPx,
            trackLengthPx = track,
            minThumbPx = minThumbPx
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
        minThumbPx: Float
    ): Metrics? {
        if (!canScroll) return null
        val viewport = (viewportEnd - viewportStart).toFloat()
        if (viewport <= 0f || totalItems <= 0 || visible.isEmpty()) return null
        val cols = columns.coerceAtLeast(1)
        val rowOffsets = visible.map { it.offset }.distinct().sorted()
        val avgRow = if (rowOffsets.size >= 2) {
            (rowOffsets.last() - rowOffsets.first()).toFloat() / (rowOffsets.size - 1)
        } else {
            visible.maxOf { it.size }.toFloat()
        }.coerceAtLeast(1f)
        val totalRows = ((totalItems + cols - 1) / cols).toFloat()
        var content = totalRows * avgRow + beforeContentPadding + afterContentPadding
        if (content <= viewport) {
            // We already know the list can scroll; don't hide on a tight estimate.
            content = viewport + 1f
        }
        val first = visible.minBy { it.index }
        val firstRow = first.index / cols
        val scroll = firstRow * avgRow - (first.offset - viewportStart) + beforeContentPadding
        val track = (viewport - 2f * endInsetPx).coerceAtLeast(0f)
        return layout(viewport, content, scroll, track, minThumbPx)
    }

    fun fromLazyList(
        state: LazyListState,
        endInsetPx: Float,
        minThumbPx: Float
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
            minThumbPx = minThumbPx
        )
    }

    fun fromLazyGrid(
        state: LazyGridState,
        endInsetPx: Float,
        minThumbPx: Float
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
            minThumbPx = minThumbPx
        )
    }

    fun fromScrollState(
        state: ScrollState,
        viewportPx: Float,
        endInsetPx: Float,
        minThumbPx: Float
    ): Metrics? {
        return fromScrollValues(
            viewportPx = viewportPx,
            maxScrollPx = state.maxValue.toFloat(),
            scrollPx = state.value.toFloat(),
            endInsetPx = endInsetPx,
            minThumbPx = minThumbPx
        )
    }
}

fun DrawScope.drawOpenPodScrollbar(
    metrics: OpenPodScrollbar.Metrics,
    thumbColor: Color = OpenPodScrollbar.THUMB_COLOR,
    trackColor: Color = OpenPodScrollbar.TRACK_COLOR,
    thumbWidthPx: Float,
    trackWidthPx: Float,
    trackRightInsetPx: Float,
    endInsetPx: Float
) {
    val trackRight = size.width - trackRightInsetPx
    val trackLeft = trackRight - trackWidthPx
    val trackTop = endInsetPx
    drawRoundRect(
        color = trackColor,
        topLeft = Offset(trackLeft, trackTop),
        size = Size(trackWidthPx, metrics.trackLength),
        cornerRadius = CornerRadius(trackWidthPx / 2f)
    )
    val thumbLeft = trackLeft + (trackWidthPx - thumbWidthPx) / 2f
    drawRoundRect(
        color = thumbColor,
        topLeft = Offset(thumbLeft, trackTop + metrics.thumbOffset),
        size = Size(thumbWidthPx, metrics.thumbLength),
        cornerRadius = CornerRadius(thumbWidthPx / 2f)
    )
}

/**
 * Observe layout and scroll in composition so the first post-layout pass
 * (when [LazyListState.canScrollForward] becomes true) invalidates this
 * modifier, then read the same state again while drawing so the thumb is
 * not stuck on a pre-layout snapshot.
 */
fun Modifier.openPodScrollbar(
    state: LazyListState,
    onDrawn: ((OpenPodScrollbar.Metrics?) -> Unit)? = null
): Modifier = composed {
    state.firstVisibleItemIndex
    state.firstVisibleItemScrollOffset
    state.layoutInfo
    state.canScrollForward
    state.canScrollBackward
    this.drawWithContent {
        drawContent()
        val endInset = OpenPodScrollbar.END_INSET_DP.dp.toPx()
        val minThumb = OpenPodScrollbar.MIN_THUMB_DP.dp.toPx()
        val metrics = OpenPodScrollbar.fromLazyList(state, endInset, minThumb)
        onDrawn?.invoke(metrics)
        if (metrics != null) {
            drawOpenPodScrollbar(
                metrics = metrics,
                thumbWidthPx = OpenPodScrollbar.THUMB_WIDTH_DP.dp.toPx(),
                trackWidthPx = OpenPodScrollbar.TRACK_WIDTH_DP.dp.toPx(),
                trackRightInsetPx = OpenPodScrollbar.TRACK_RIGHT_INSET_DP.dp.toPx(),
                endInsetPx = endInset
            )
        }
    }
}

fun Modifier.openPodScrollbar(
    state: LazyGridState,
    onDrawn: ((OpenPodScrollbar.Metrics?) -> Unit)? = null
): Modifier = composed {
    state.firstVisibleItemIndex
    state.firstVisibleItemScrollOffset
    state.layoutInfo
    state.canScrollForward
    state.canScrollBackward
    this.drawWithContent {
        drawContent()
        val endInset = OpenPodScrollbar.END_INSET_DP.dp.toPx()
        val minThumb = OpenPodScrollbar.MIN_THUMB_DP.dp.toPx()
        val metrics = OpenPodScrollbar.fromLazyGrid(state, endInset, minThumb)
        onDrawn?.invoke(metrics)
        if (metrics != null) {
            drawOpenPodScrollbar(
                metrics = metrics,
                thumbWidthPx = OpenPodScrollbar.THUMB_WIDTH_DP.dp.toPx(),
                trackWidthPx = OpenPodScrollbar.TRACK_WIDTH_DP.dp.toPx(),
                trackRightInsetPx = OpenPodScrollbar.TRACK_RIGHT_INSET_DP.dp.toPx(),
                endInsetPx = endInset
            )
        }
    }
}

fun Modifier.openPodScrollbar(
    state: ScrollState,
    onDrawn: ((OpenPodScrollbar.Metrics?) -> Unit)? = null
): Modifier = composed {
    state.value
    state.maxValue
    this.drawWithContent {
        drawContent()
        val endInset = OpenPodScrollbar.END_INSET_DP.dp.toPx()
        val minThumb = OpenPodScrollbar.MIN_THUMB_DP.dp.toPx()
        val metrics = OpenPodScrollbar.fromScrollState(
            state = state,
            viewportPx = size.height,
            endInsetPx = endInset,
            minThumbPx = minThumb
        )
        onDrawn?.invoke(metrics)
        if (metrics != null) {
            drawOpenPodScrollbar(
                metrics = metrics,
                thumbWidthPx = OpenPodScrollbar.THUMB_WIDTH_DP.dp.toPx(),
                trackWidthPx = OpenPodScrollbar.TRACK_WIDTH_DP.dp.toPx(),
                trackRightInsetPx = OpenPodScrollbar.TRACK_RIGHT_INSET_DP.dp.toPx(),
                endInsetPx = endInset
            )
        }
    }
}
