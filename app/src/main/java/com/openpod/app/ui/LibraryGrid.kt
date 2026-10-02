package com.openpod.app.ui

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.openpod.app.data.LibraryOrder
import com.openpod.app.data.db.PodcastEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

internal fun libraryPodcastCellTag(id: Long) = "library-podcast-$id"

/**
 * Thumbnail grid for the library. Long-press a show and drag it to another slot.
 * The new id sequence is reported from [onReorder] when the gesture ends.
 */
@Composable
internal fun LibraryPodcastGrid(
    podcasts: List<PodcastEntity>,
    onOpen: (Long) -> Unit,
    onReorder: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 5
) {
    val gridState = rememberLazyGridState()
    val itemsState = remember { mutableStateOf(podcasts) }
    val draggingState = remember { mutableStateOf(false) }
    val draggingIdState = remember { mutableStateOf<Long?>(null) }
    val dragOffsetState = remember { mutableStateOf(Offset.Zero) }
    val pointerState = remember { mutableStateOf<Offset?>(null) }
    val droppedOrderState = remember { mutableStateOf<List<Long>?>(null) }
    val suppressClickIdState = remember { mutableStateOf<Long?>(null) }
    val onReorderState = rememberUpdatedState(onReorder)
    val onOpenState = rememberUpdatedState(onOpen)
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val dragging = draggingState.value

    LaunchedEffect(podcasts, dragging) {
        if (draggingState.value) return@LaunchedEffect
        val dropped = droppedOrderState.value
        if (dropped != null) {
            val ids = podcasts.map { it.id }
            if (ids == LibraryOrder.merge(ids, dropped)) {
                droppedOrderState.value = null
                itemsState.value = podcasts
            }
            return@LaunchedEffect
        }
        itemsState.value = podcasts
    }

    LaunchedEffect(suppressClickIdState.value) {
        val suppressed = suppressClickIdState.value ?: return@LaunchedEffect
        delay(400)
        if (suppressClickIdState.value == suppressed) suppressClickIdState.value = null
    }

    fun finishDrag() {
        val id = draggingIdState.value ?: return
        val ids = itemsState.value.map { it.id }
        draggingIdState.value = null
        draggingState.value = false
        pointerState.value = null
        dragOffsetState.value = Offset.Zero
        droppedOrderState.value = ids
        suppressClickIdState.value = id
        onReorderState.value(ids)
    }

    fun moveDragged(id: Long) {
        val current = itemsState.value
        val from = current.indexOfFirst { it.id == id }
        if (from < 0) return
        val visible = gridState.layoutInfo.visibleItemsInfo
        val draggedInfo = visible.firstOrNull { it.key == id } ?: return
        if (draggedInfo.index != from) return
        val offset = dragOffsetState.value
        val centerX = draggedInfo.offset.x + draggedInfo.size.width / 2f + offset.x
        val centerY = draggedInfo.offset.y + draggedInfo.size.height / 2f + offset.y
        val cells = visible.mapNotNull { info ->
            if (info.key == id || info.index !in current.indices) return@mapNotNull null
            LibraryOrder.CellBounds(
                index = info.index,
                left = info.offset.x,
                top = info.offset.y,
                width = info.size.width,
                height = info.size.height
            )
        }
        val target = LibraryOrder.hitIndex(cells, centerX, centerY) ?: return
        if (target == from) return
        val targetInfo = visible.firstOrNull { it.index == target } ?: return
        itemsState.value = LibraryOrder.move(current, from, target)
        val (x, y) = LibraryOrder.compensatedDrag(
            dragX = offset.x,
            dragY = offset.y,
            fromLeft = draggedInfo.offset.x,
            fromTop = draggedInfo.offset.y,
            toLeft = targetInfo.offset.x,
            toTop = targetInfo.offset.y
        )
        dragOffsetState.value = Offset(x, y)
    }

    LaunchedEffect(gridState) {
        val edgePx = with(density) { 28.dp.toPx() }
        val maxSpeed = with(density) { 14.dp.toPx() }
        while (isActive) {
            val id = draggingIdState.value
            val pointer = pointerState.value
            if (id == null || pointer == null) {
                delay(32)
                continue
            }
            val info = gridState.layoutInfo
            val delta = LibraryOrder.autoScrollDelta(
                pointerY = pointer.y,
                viewportStart = info.viewportStartOffset,
                viewportEnd = info.viewportEndOffset,
                edgePx = edgePx,
                maxSpeed = maxSpeed
            )
            if (delta != 0f) {
                var consumed = 0f
                gridState.scroll(MutatePriority.UserInput) {
                    consumed = scrollBy(delta)
                }
                if (consumed != 0f) {
                    dragOffsetState.value = dragOffsetState.value.copy(
                        y = dragOffsetState.value.y + consumed
                    )
                    moveDragged(id)
                }
            }
            delay(16)
        }
    }

    val shown = itemsState.value
    val dragOffset = dragOffsetState.value
    val draggingId = draggingIdState.value
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        modifier = modifier,
        contentPadding = PaddingValues(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(shown, key = { it.id }) { podcast ->
            val isDragging = draggingId == podcast.id
            Box(
                Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        if (isDragging) {
                            translationX = dragOffset.x
                            translationY = dragOffset.y
                            scaleX = 1.05f
                            scaleY = 1.05f
                        }
                    }
                    .then(
                        if (isDragging) Modifier.shadow(8.dp, RoundedCornerShape(6.dp)) else Modifier
                    )
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .testTag(libraryPodcastCellTag(podcast.id))
                    .pointerInput(podcast.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { start ->
                                suppressClickIdState.value = podcast.id
                                draggingIdState.value = podcast.id
                                draggingState.value = true
                                dragOffsetState.value = Offset.Zero
                                val item = gridState.layoutInfo.visibleItemsInfo
                                    .firstOrNull { it.key == podcast.id }
                                pointerState.value = if (item == null) {
                                    null
                                } else {
                                    Offset(item.offset.x + start.x, item.offset.y + start.y)
                                }
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                dragOffsetState.value += amount
                                val pointer = pointerState.value
                                if (pointer != null) pointerState.value = pointer + amount
                                moveDragged(podcast.id)
                            },
                            onDragEnd = { finishDrag() },
                            onDragCancel = { finishDrag() }
                        )
                    }
                    .clickable {
                        if (suppressClickIdState.value == podcast.id) {
                            suppressClickIdState.value = null
                        } else if (draggingIdState.value == null) {
                            onOpenState.value(podcast.id)
                        }
                    }
            ) {
                AsyncImage(
                    model = podcast.imageUrl,
                    contentDescription = podcast.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
