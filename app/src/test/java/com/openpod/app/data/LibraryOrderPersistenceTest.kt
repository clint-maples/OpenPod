package com.openpod.app.data

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.openpod.app.data.db.AppDatabase
import com.openpod.app.data.db.PodcastEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class LibraryOrderPersistenceTest {
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
    fun unorderedPodcastsStayAlphabetical() = runBlocking(Dispatchers.IO) {
        insert("Zebra", "https://z.example/rss")
        insert("alpha", "https://a.example/rss")
        insert("Beta", "https://b.example/rss")
        assertEquals(listOf("alpha", "Beta", "Zebra"), db.podcasts().all().map { it.title })
        assertEquals(
            listOf(PodcastEntity.UNORDERED, PodcastEntity.UNORDERED, PodcastEntity.UNORDERED),
            db.podcasts().all().map { it.libraryOrder }
        )
    }

    @Test
    fun customOrderSurvivesReopenAndNewShowsAppend() = runBlocking(Dispatchers.IO) {
        val alpha = insert("alpha", "https://a.example/rss")
        val beta = insert("Beta", "https://b.example/rss")
        val zebra = insert("Zebra", "https://z.example/rss")
        db.podcasts().setLibraryOrder(zebra, 0)
        db.podcasts().setLibraryOrder(alpha, 1)
        db.podcasts().setLibraryOrder(beta, 2)
        db.close()

        db = open()
        assertEquals(listOf(zebra, alpha, beta), db.podcasts().all().map { it.id })

        val middle = insert("Middle", "https://m.example/rss")
        assertEquals(PodcastEntity.UNORDERED, db.podcasts().byId(middle)!!.libraryOrder)
        assertEquals(listOf(zebra, alpha, beta, middle), db.podcasts().all().map { it.id })
    }

    @Test
    fun reorderPersistsDraggedOrderAndAppendsShowsMissingFromTheGesture() = runBlocking(Dispatchers.IO) {
        val alpha = insert("alpha", "https://a.example/rss")
        val beta = insert("Beta", "https://b.example/rss")
        val zebra = insert("Zebra", "https://z.example/rss")
        reorderPodcastRows(db.podcasts(), listOf(zebra, beta, alpha))
        assertEquals(listOf(zebra, beta, alpha), db.podcasts().all().map { it.id })
        assertEquals(listOf(0, 1, 2), db.podcasts().all().map { it.libraryOrder })

        val extra = insert("Extra", "https://e.example/rss")
        reorderPodcastRows(db.podcasts(), listOf(beta, alpha))
        assertEquals(listOf(beta, alpha, zebra, extra), db.podcasts().all().map { it.id })
    }

    @Test
    fun migrationFromVersion9KeepsTitleOrder() = runBlocking(Dispatchers.IO) {
        if (::db.isInitialized) db.close()
        context.deleteDatabase(DB_NAME)
        val file = context.getDatabasePath(DB_NAME)
        file.parentFile?.mkdirs()
        val sqlite = SQLiteDatabase.openOrCreateDatabase(file, null)
        sqlite.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `podcasts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `feedUrl` TEXT NOT NULL,
                `siteUrl` TEXT NOT NULL,
                `author` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `imageUrl` TEXT NOT NULL,
                `autoDownload` INTEGER NOT NULL,
                `autoDelete` INTEGER NOT NULL,
                `folderName` TEXT NOT NULL,
                `skipIntroSec` INTEGER NOT NULL,
                `skipOutroSec` INTEGER NOT NULL,
                `autoArchive` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `episodes` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `podcastId` INTEGER NOT NULL,
                `guid` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `audioUrl` TEXT NOT NULL,
                `permalink` TEXT NOT NULL,
                `mimeType` TEXT NOT NULL,
                `pubDate` INTEGER NOT NULL,
                `durationMs` INTEGER NOT NULL,
                `episodeNumber` INTEGER,
                `seasonNumber` INTEGER,
                `imageUrl` TEXT NOT NULL,
                `transcriptUrl` TEXT NOT NULL,
                `transcriptType` TEXT NOT NULL,
                `chaptersUrl` TEXT NOT NULL,
                `localUri` TEXT NOT NULL,
                `fileName` TEXT NOT NULL,
                `downloadStatus` TEXT NOT NULL,
                `positionMs` INTEGER NOT NULL,
                `completed` INTEGER NOT NULL,
                `archived` INTEGER NOT NULL,
                FOREIGN KEY(`podcastId`) REFERENCES `podcasts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        sqlite.execSQL("CREATE INDEX IF NOT EXISTS `index_episodes_podcastId` ON `episodes` (`podcastId`)")
        sqlite.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_episodes_podcastId_guid` ON `episodes` (`podcastId`, `guid`)"
        )
        sqlite.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlists` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `isSmart` INTEGER NOT NULL,
                `playFilter` TEXT NOT NULL,
                `downloadFilter` TEXT NOT NULL
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlist_items` (
                `playlistId` INTEGER NOT NULL,
                `episodeId` INTEGER NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                PRIMARY KEY(`playlistId`, `episodeId`),
                FOREIGN KEY(`playlistId`) REFERENCES `playlists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`episodeId`) REFERENCES `episodes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_playlist_items_episodeId` ON `playlist_items` (`episodeId`)"
        )
        sqlite.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `queue_items` (
                `episodeId` INTEGER NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                PRIMARY KEY(`episodeId`),
                FOREIGN KEY(`episodeId`) REFERENCES `episodes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_queue_items_sortOrder` ON `queue_items` (`sortOrder`)"
        )
        sqlite.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `settings` (
                `id` INTEGER NOT NULL,
                `downloadTreeUri` TEXT NOT NULL,
                `namingPattern` TEXT NOT NULL,
                `skipForwardSec` INTEGER NOT NULL,
                `skipBackSec` INTEGER NOT NULL,
                `autoDownloadDefault` INTEGER NOT NULL,
                `autoDeleteDefault` INTEGER NOT NULL,
                `eqEnabled` INTEGER NOT NULL,
                `eqBandsJson` TEXT NOT NULL,
                `showArchived` INTEGER NOT NULL,
                `syncFrequency` TEXT NOT NULL,
                `syncHour` INTEGER NOT NULL,
                `syncMinute` INTEGER NOT NULL,
                `lastEpisodeId` INTEGER NOT NULL,
                `volumeBoost` REAL NOT NULL,
                `libraryLayout` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO podcasts (
                title, feedUrl, siteUrl, author, description, imageUrl,
                autoDownload, autoDelete, folderName, skipIntroSec, skipOutroSec, autoArchive
            ) VALUES ('Zebra', 'https://z.example/rss', '', '', '', '', 0, 0, '', 0, 0, 0)
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO podcasts (
                title, feedUrl, siteUrl, author, description, imageUrl,
                autoDownload, autoDelete, folderName, skipIntroSec, skipOutroSec, autoArchive
            ) VALUES ('alpha', 'https://a.example/rss', '', '', '', '', 0, 0, '', 0, 0, 0)
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO episodes (
                podcastId, guid, title, description, audioUrl, permalink, mimeType,
                pubDate, durationMs, imageUrl, transcriptUrl, transcriptType, chaptersUrl,
                localUri, fileName, downloadStatus, positionMs, completed, archived
            ) VALUES (
                1, 'ep-1', 'Pilot', '', 'https://cdn.example/pilot.mp3', '', 'audio/mpeg',
                0, 0, '', '', '', '', '', '', 'none', 0, 0, 0
            )
            """.trimIndent()
        )
        sqlite.version = 9
        sqlite.close()

        db = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .addMigrations(AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11)
            .allowMainThreadQueries()
            .build()
        assertEquals(listOf("alpha", "Zebra"), db.podcasts().all().map { it.title })
        assertEquals(
            listOf(PodcastEntity.UNORDERED, PodcastEntity.UNORDERED),
            db.podcasts().all().map { it.libraryOrder }
        )
        assertEquals(false, db.episodes().forPodcast(1).single().favorite)
    }

    private suspend fun insert(title: String, feedUrl: String): Long {
        return db.podcasts().insert(PodcastEntity(title = title, feedUrl = feedUrl))
    }

    private fun open(): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .allowMainThreadQueries()
            .build()
    }

    private companion object {
        const val DB_NAME = "library-order-test.db"
    }
}
