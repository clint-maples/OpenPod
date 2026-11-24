package com.openpod.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkipGlyphTest {
    @Test
    fun rewindAlwaysUsesTheReplayVectorUpright() {
        assertEquals(SkipGlyph.rewind, SkipGlyph.vector(forward = false, mirrorForwardGlyph = false))
        assertEquals(SkipGlyph.rewind, SkipGlyph.vector(forward = false, mirrorForwardGlyph = true))
        assertFalse(SkipGlyph.mirrorX(forward = false, mirrorForwardGlyph = false))
        assertFalse(SkipGlyph.mirrorX(forward = false, mirrorForwardGlyph = true))
    }

    @Test
    fun stockForwardUsesTheDistinctFastForwardVector() {
        assertEquals(SkipGlyph.stockForward, SkipGlyph.vector(forward = true, mirrorForwardGlyph = false))
        assertFalse(SkipGlyph.mirrorX(forward = true, mirrorForwardGlyph = false))
    }

    @Test
    fun mirroredForwardUsesTheRewindVectorFlippedHorizontally() {
        assertEquals(SkipGlyph.rewind, SkipGlyph.vector(forward = true, mirrorForwardGlyph = true))
        assertTrue(SkipGlyph.mirrorX(forward = true, mirrorForwardGlyph = true))
        assertTrue(SkipGlyph.rewind !== SkipGlyph.stockForward)
    }
}
