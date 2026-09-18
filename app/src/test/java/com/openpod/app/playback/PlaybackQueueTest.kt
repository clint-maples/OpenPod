package com.openpod.app.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueTest {
    @Test
    fun playNowRemovesEpisodeFromUpcoming() {
        val result = PlaybackQueue.playNow(listOf(2L, 3L, 4L), 3L)
        assertEquals(listOf(2L, 4L), result.upcoming)
    }

    @Test
    fun playNowLeavesUnrelatedQueueAlone() {
        val result = PlaybackQueue.playNow(listOf(2L, 3L), 9L)
        assertEquals(listOf(2L, 3L), result.upcoming)
    }

    @Test
    fun playNextInsertsAtFrontAndDedupes() {
        val result = PlaybackQueue.playNext(1L, listOf(2L, 3L, 4L), 4L)
        assertEquals(listOf(4L, 2L, 3L), result.upcoming)
    }

    @Test
    fun playNextIgnoresCurrentEpisode() {
        val result = PlaybackQueue.playNext(7L, listOf(2L, 3L), 7L)
        assertEquals(listOf(2L, 3L), result.upcoming)
    }

    @Test
    fun addAppendsOnce() {
        val first = PlaybackQueue.add(1L, listOf(2L), 3L)
        assertEquals(listOf(2L, 3L), first.upcoming)
        val again = PlaybackQueue.add(1L, first.upcoming, 3L)
        assertEquals(listOf(2L, 3L), again.upcoming)
    }

    @Test
    fun addIgnoresNowPlaying() {
        val result = PlaybackQueue.add(5L, listOf(2L), 5L)
        assertEquals(listOf(2L), result.upcoming)
    }

    @Test
    fun moveReordersWithinBounds() {
        val up = PlaybackQueue.move(listOf(2L, 3L, 4L), 4L, -1)
        assertEquals(listOf(2L, 4L, 3L), up.upcoming)
        val top = PlaybackQueue.move(listOf(2L, 3L, 4L), 2L, -1)
        assertEquals(listOf(2L, 3L, 4L), top.upcoming)
        val down = PlaybackQueue.move(listOf(2L, 3L, 4L), 2L, 1)
        assertEquals(listOf(3L, 2L, 4L), down.upcoming)
    }

    @Test
    fun advancePopsHead() {
        val (next, rest) = PlaybackQueue.advance(listOf(8L, 9L))
        assertEquals(8L, next)
        assertEquals(listOf(9L), rest.upcoming)
        val empty = PlaybackQueue.advance(emptyList())
        assertEquals(0L, empty.first)
        assertTrue(empty.second.upcoming.isEmpty())
    }

    @Test
    fun clearAndContains() {
        assertTrue(PlaybackQueue.clear().upcoming.isEmpty())
        assertTrue(PlaybackQueue.contains(listOf(1L, 2L), 2L))
        assertFalse(PlaybackQueue.contains(listOf(1L, 2L), 0L))
        assertEquals(listOf(1L), PlaybackQueue.remove(listOf(1L, 2L), 2L).upcoming)
    }
}
