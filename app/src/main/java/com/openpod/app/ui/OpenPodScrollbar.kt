package com.openpod.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * sb-v1 right-edge scrollbar: a 4 dp #7FB7C9 thumb on a faint 2 dp track.
 * The thumb length is proportional to visible / total content. The bar is
 * always shown when the content can scroll, and never auto-hides.
 *
 * 1.51 and 1.52 never showed the bar. Two separate bugs:
 *
 * 1. LazyColumn call sites created a [LazyListState] for the scrollbar but
 *    did not pass it as LazyColumn's `state` parameter, so LazyColumn made
 *    its own implicit state. The bar watched a list that never scrolled and
 *    always reported "no overflow."
 * 2. The previous implementation drew with [androidx.compose.ui.draw.drawWithContent]
 *    on the scrolling container itself. LazyColumn / LazyVerticalGrid
 *    composite their item layers after that draw pass, so even a correctly
 *    wired thumb (the library grid) was covered by full-width cells.
 *
 * This host is a [Box] with the list as a child and a sibling [Canvas] rail
 * drawn on top at the right edge, both wired to the same scroll state.
 */
object OpenPodScrollbar {
    const val THUMB_WIDTH_DP = 4
    const val TRACK_WIDTH_DP = 2
    const val MIN_THUMB_DP = 36
    const val END_INSET_DP = 4
    const val TRACK_RIGHT_INSET_DP = 4
    const val TRACK_ALPHA = 0.22f
    const val RAIL_WIDTH_DP = THUMB_WIDTH_DP + TRACK_RIGHT_INSET_DP

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

fun DrawScope.drawOpenPodScrollbarRail(metrics: OpenPodScrollbar.Metrics) {
    val thumbWidth = OpenPodScrollbar.THUMB_WIDTH_DP.dp.toPx()
    val trackWidth = OpenPodScrollbar.TRACK_WIDTH_DP.dp.toPx()
    val endInset = OpenPodScrollbar.END_INSET_DP.dp.toPx()
    val trackLeft = (size.width - trackWidth) / 2f
    val thumbLeft = (size.width - thumbWidth) / 2f
    drawRoundRect(
        color = OpenPodScrollbar.TRACK_COLOR,
        topLeft = Offset(trackLeft, endInset),
        size = Size(trackWidth, metrics.trackLength),
        cornerRadius = CornerRadius(trackWidth / 2f)
    )
    drawRoundRect(
        color = OpenPodScrollbar.THUMB_COLOR,
        topLeft = Offset(thumbLeft, endInset + metrics.thumbOffset),
        size = Size(thumbWidth, metrics.thumbLength),
        cornerRadius = CornerRadius(thumbWidth / 2f)
    )
}

@Composable
fun BoxScope.OpenPodScrollbarRail(metrics: OpenPodScrollbar.Metrics?) {
    if (metrics == null) return
    Canvas(
        Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .width(OpenPodScrollbar.RAIL_WIDTH_DP.dp)
    ) {
        drawOpenPodScrollbarRail(metrics)
    }
}

@Composable
fun OpenPodScrollbarBox(
    state: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val metrics by remember(state, density) {
        derivedStateOf {
            OpenPodScrollbar.fromLazyList(
                state = state,
                endInsetPx = with(density) { OpenPodScrollbar.END_INSET_DP.dp.toPx() },
                minThumbPx = with(density) { OpenPodScrollbar.MIN_THUMB_DP.dp.toPx() }
            )
        }
    }
    Box(modifier) {
        content()
        OpenPodScrollbarRail(metrics)
    }
}

@Composable
fun OpenPodScrollbarBox(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val metrics by remember(state, density) {
        derivedStateOf {
            OpenPodScrollbar.fromLazyGrid(
                state = state,
                endInsetPx = with(density) { OpenPodScrollbar.END_INSET_DP.dp.toPx() },
                minThumbPx = with(density) { OpenPodScrollbar.MIN_THUMB_DP.dp.toPx() }
            )
        }
    }
    Box(modifier) {
        content()
        OpenPodScrollbarRail(metrics)
    }
}

@Composable
fun OpenPodScrollbarBox(
    state: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val metrics by remember(state, density) {
        derivedStateOf {
            OpenPodScrollbar.fromScrollState(
                state = state,
                viewportPx = state.viewportSize.toFloat().coerceAtLeast(0f),
                endInsetPx = with(density) { OpenPodScrollbar.END_INSET_DP.dp.toPx() },
                minThumbPx = with(density) { OpenPodScrollbar.MIN_THUMB_DP.dp.toPx() }
            )
        }
    }
    Box(modifier) {
        content()
        OpenPodScrollbarRail(metrics)
    }
}
