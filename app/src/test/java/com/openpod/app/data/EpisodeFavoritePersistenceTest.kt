package com.openpod.app.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.openpod.app.data.db.AppDatabase
import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.data.db.PodcastEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class EpisodeFavoritePersistenceTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteDatabase(DB_NAME)
        db = open()
    }

    @After
    fun tearDown() {
        if (::db.isInitialized) db.close()
        context.deleteDatabase(DB_NAME)
    }

    @Test
    fun favoriteSurvivesReopen() = runBlocking(Dispatchers.IO) {
        val podcastId = db.podcasts().insert(
            PodcastEntity(title = "Show", feedUrl = "https://example.com/rss")
        )
        val episodeId = db.episodes().insert(
            EpisodeEntity(
                podcastId = podcastId,
                guid = "ep-1",
                title = "Pilot",
                audioUrl = "https://cdn.example/pilot.mp3"
            )
        )
        assertFalse(db.episodes().byId(episodeId)!!.favorite)

        db.episodes().toggleFavorite(episodeId)
        db.close()
        db = open()
        assertTrue(db.episodes().byId(episodeId)!!.favorite)

        db.episodes().toggleFavorite(episodeId)
        assertFalse(db.episodes().byId(episodeId)!!.favorite)
    }

    private fun open(): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .allowMainThreadQueries()
            .build()
    }

    private companion object {
        const val DB_NAME = "episode-favorite-test.db"
    }
}
