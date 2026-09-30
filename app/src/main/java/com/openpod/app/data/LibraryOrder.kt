package com.openpod.app.data

/**
 * Library thumbnail order. Podcasts with [com.openpod.app.data.db.PodcastEntity.UNORDERED]
 * stay in title order. A completed drag stores 0..n-1. Later subscriptions are not in that
 * saved sequence, so [merge] appends them.
 */
object LibraryOrder {
    data class CellBounds(
        val index: Int,
        val left: Int,
        val top: Int,
        val width: Int,
        val height: Int
    )

    fun <T> move(items: List<T>, fromIndex: Int, toIndex: Int): List<T> {
        if (fromIndex == toIndex) return items
        if (fromIndex !in items.indices || toIndex !in items.indices) return items
        val next = items.toMutableList()
        next.add(toIndex, next.removeAt(fromIndex))
        return next
    }

    /**
     * @param currentIds podcasts currently in the library, already in display order
     * @param savedOrder ids from a drag. Missing ids are dropped. Current ids that were not
     * part of the drag (a subscribe that landed mid-gesture) are appended.
     */
    fun merge(currentIds: List<Long>, savedOrder: List<Long>): List<Long> {
        val current = currentIds.toSet()
        val kept = savedOrder.filter { it in current }.distinct()
        val keptSet = kept.toSet()
        return kept + currentIds.filter { it !in keptSet }
    }

    fun hitIndex(cells: List<CellBounds>, x: Float, y: Float): Int? {
        return cells.firstOrNull { cell ->
            cell.width > 0 &&
                cell.height > 0 &&
                x >= cell.left &&
                x < cell.left + cell.width &&
                y >= cell.top &&
                y < cell.top + cell.height
        }?.index
    }

    fun compensatedDrag(
        dragX: Float,
        dragY: Float,
        fromLeft: Int,
        fromTop: Int,
        toLeft: Int,
        toTop: Int
    ): Pair<Float, Float> {
        return (dragX + (fromLeft - toLeft)) to (dragY + (fromTop - toTop))
    }

    /**
     * Vertical auto-scroll while a thumbnail is held near the top or bottom of the grid.
     * Negative scrolls toward the start.
     */
    fun autoScrollDelta(
        pointerY: Float,
        viewportStart: Int,
        viewportEnd: Int,
        edgePx: Float,
        maxSpeed: Float
    ): Float {
        if (edgePx <= 0f || maxSpeed <= 0f || viewportEnd <= viewportStart) return 0f
        val topZoneEnd = viewportStart + edgePx
        val bottomZoneStart = viewportEnd - edgePx
        if (bottomZoneStart <= topZoneEnd) return 0f
        return when {
            pointerY < topZoneEnd -> {
                val depth = (topZoneEnd - pointerY).coerceIn(0f, edgePx)
                -maxSpeed * (depth / edgePx)
            }
            pointerY > bottomZoneStart -> {
                val depth = (pointerY - bottomZoneStart).coerceIn(0f, edgePx)
                maxSpeed * (depth / edgePx)
            }
            else -> 0f
        }
    }
}
