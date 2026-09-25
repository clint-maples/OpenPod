package com.openpod.app.data

import com.openpod.app.data.db.EpisodeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoArchiveTest {
    @Test
    fun archivesOnlyTheFirstTimeAnEpisodeFinishes() {
        assertTrue(
            AutoArchive.shouldArchiveOnProgress(
                autoArchive = true,
                completed = true,
                wasCompleted = false
            )
        )
        assertFalse(
            AutoArchive.shouldArchiveOnProgress(
                autoArchive = true,
                completed = true,
                wasCompleted = true
            )
        )
        assertFalse(
            AutoArchive.shouldArchiveOnProgress(
                autoArchive = false,
                completed = true,
                wasCompleted = false
            )
        )
        assertFalse(
            AutoArchive.shouldArchiveOnProgress(
                autoArchive = true,
                completed = false,
                wasCompleted = false
            )
        )
    }

    @Test
    fun backlogIsPlayedEpisodesThatAreStillAvailable() {
        val episodes = listOf(
            episode(id = 1, completed = true, archived = false),
            episode(id = 2, completed = true, archived = true),
            episode(id = 3, completed = false, archived = false),
            episode(id = 4, completed = false, archived = true)
        )
        assertEquals(listOf(1L), AutoArchive.backlogIds(episodes))
    }

    private fun episode(id: Long, completed: Boolean, archived: Boolean) = EpisodeEntity(
        id = id,
        podcastId = 1,
        guid = "guid-$id",
        title = "Episode $id",
        audioUrl = "https://cdn.example.com/$id.mp3",
        completed = completed,
        archived = archived
    )
}
