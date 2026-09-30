package com.openpod.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryOrderTest {
    @Test
    fun movePlacesItemAtTargetIndex() {
        assertEquals(listOf(2L, 3L, 1L, 4L), LibraryOrder.move(listOf(1L, 2L, 3L, 4L), 0, 2))
        assertEquals(listOf(1L, 4L, 2L, 3L), LibraryOrder.move(listOf(1L, 2L, 3L, 4L), 3, 1))
    }

    @Test
    fun moveIgnoresNoOpsAndBadIndexes() {
        val ids = listOf(1L, 2L, 3L)
        assertEquals(ids, LibraryOrder.move(ids, 1, 1))
        assertEquals(ids, LibraryOrder.move(ids, -1, 0))
        assertEquals(ids, LibraryOrder.move(ids, 0, 3))
    }

    @Test
    fun mergeKeepsDraggedOrderAndAppendsNewShows() {
        assertEquals(
            listOf(3L, 1L, 2L, 4L),
            LibraryOrder.merge(currentIds = listOf(1L, 2L, 3L, 4L), savedOrder = listOf(3L, 1L))
        )
    }

    @Test
    fun mergeDropsUnsubscribedShowsAndDuplicateIds() {
        assertEquals(
            listOf(3L, 1L, 4L),
            LibraryOrder.merge(currentIds = listOf(1L, 4L, 3L), savedOrder = listOf(3L, 9L, 3L, 1L))
        )
    }

    @Test
    fun hitIndexUsesCellBounds() {
        val cells = listOf(
            LibraryOrder.CellBounds(index = 0, left = 0, top = 0, width = 100, height = 100),
            LibraryOrder.CellBounds(index = 1, left = 110, top = 0, width = 100, height = 100)
        )
        assertEquals(0, LibraryOrder.hitIndex(cells, 50f, 50f))
        assertEquals(1, LibraryOrder.hitIndex(cells, 150f, 40f))
        assertNull(LibraryOrder.hitIndex(cells, 105f, 50f))
    }

    @Test
    fun compensatedDragKeepsTheThumbnailUnderTheFinger() {
        val (x, y) = LibraryOrder.compensatedDrag(
            dragX = 60f,
            dragY = 10f,
            fromLeft = 0,
            fromTop = 0,
            toLeft = 110,
            toTop = 0
        )
        assertEquals(-50f, x)
        assertEquals(10f, y)
        assertEquals(60f, 110f + x)
    }

    @Test
    fun autoScrollDeltaIsZeroAwayFromTheEdges() {
        assertEquals(
            0f,
            LibraryOrder.autoScrollDelta(
                pointerY = 400f,
                viewportStart = 0,
                viewportEnd = 800,
                edgePx = 48f,
                maxSpeed = 20f
            )
        )
    }

    @Test
    fun autoScrollDeltaMovesTowardTheNearEdge() {
        val up = LibraryOrder.autoScrollDelta(
            pointerY = 10f,
            viewportStart = 0,
            viewportEnd = 800,
            edgePx = 48f,
            maxSpeed = 20f
        )
        val down = LibraryOrder.autoScrollDelta(
            pointerY = 790f,
            viewportStart = 0,
            viewportEnd = 800,
            edgePx = 48f,
            maxSpeed = 20f
        )
        assertEquals(-20f * (38f / 48f), up, 0.01f)
        assertEquals(20f * (38f / 48f), down, 0.01f)
    }

    @Test
    fun autoScrollDeltaStaysPutWhenTheEdgeZonesOverlap() {
        assertEquals(
            0f,
            LibraryOrder.autoScrollDelta(
                pointerY = 10f,
                viewportStart = 0,
                viewportEnd = 40,
                edgePx = 48f,
                maxSpeed = 20f
            )
        )
    }
}
