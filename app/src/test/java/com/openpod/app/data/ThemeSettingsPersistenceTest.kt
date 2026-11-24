package com.openpod.app.data

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.openpod.app.data.db.AppDatabase
import com.openpod.app.data.db.SettingsEntity
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
class ThemeSettingsPersistenceTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteDatabase(DB_NAME)
    }

    @After
    fun tearDown() {
        if (::db.isInitialized) db.close()
        context.deleteDatabase(DB_NAME)
    }

    @Test
    fun freshDatabaseDefaultsToDarkSoftTeal() = runBlocking(Dispatchers.IO) {
        db = open()
        db.settings().upsert(SettingsEntity(libraryLayout = SettingsEntity.LAYOUT_GRID))
        val settings = db.settings().get()!!
        assertEquals(SettingsEntity.THEME_DARK, settings.themeMode)
        assertEquals(SettingsEntity.BACKGROUND_SOFT, settings.darkBackground)
        assertEquals(SettingsEntity.ACCENT_TEAL, settings.accent)
        assertEquals(SettingsEntity.LAYOUT_GRID, settings.libraryLayout)
    }

    @Test
    fun themeChoicesRoundTrip() = runBlocking(Dispatchers.IO) {
        db = open()
        db.settings().upsert(
            SettingsEntity(
                themeMode = SettingsEntity.THEME_LIGHT,
                darkBackground = SettingsEntity.BACKGROUND_TRUE_BLACK,
                accent = SettingsEntity.ACCENT_CORAL
            )
        )
        val settings = db.settings().get()!!
        assertEquals(SettingsEntity.THEME_LIGHT, settings.themeMode)
        assertEquals(SettingsEntity.BACKGROUND_TRUE_BLACK, settings.darkBackground)
        assertEquals(SettingsEntity.ACCENT_CORAL, settings.accent)
    }

    @Test
    fun migration13To14AddsDarkSoftTealDefaultsAndKeepsLayout() = runBlocking(Dispatchers.IO) {
        val file = context.getDatabasePath(DB_NAME)
        file.parentFile?.mkdirs()
        val sqlite = SQLiteDatabase.openOrCreateDatabase(file, null)
        createV13Schema(sqlite)
        sqlite.execSQL(
            """
            INSERT INTO settings (
                id, downloadTreeUri, namingPattern, skipForwardSec, skipBackSec,
                autoDownloadDefault, autoDeleteDefault, eqEnabled, eqBandsJson,
                showArchived, syncFrequency, syncHour, syncMinute, lastEpisodeId,
                volumeBoost, libraryLayout, skipSilence, playbackSpeed
            ) VALUES (
                1, '', '{podcast} - {episode}', 30, 15,
                0, 0, 0, '',
                0, '30min', 6, 0, 0,
                0, 'grid', 0, 1
            )
            """.trimIndent()
        )
        sqlite.version = 13
        sqlite.close()

        db = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .addMigrations(AppDatabase.MIGRATION_13_14)
            .allowMainThreadQueries()
            .build()
        val settings = db.settings().get()!!
        assertEquals(SettingsEntity.THEME_DARK, settings.themeMode)
        assertEquals(SettingsEntity.BACKGROUND_SOFT, settings.darkBackground)
        assertEquals(SettingsEntity.ACCENT_TEAL, settings.accent)
        assertEquals(SettingsEntity.LAYOUT_GRID, settings.libraryLayout)
    }

    private fun open(): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .allowMainThreadQueries()
            .build()
    }

    private fun createV13Schema(sqlite: SQLiteDatabase) {
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
                `autoArchive` INTEGER NOT NULL,
                `libraryOrder` INTEGER NOT NULL DEFAULT -1,
                `playbackSpeed` REAL,
                `skipSilence` INTEGER,
                `volumeBoost` REAL
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
                `favorite` INTEGER NOT NULL DEFAULT 0,
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
        sqlite.execSQL("CREATE INDEX IF NOT EXISTS `index_playlist_items_episodeId` ON `playlist_items` (`episodeId`)")
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
        sqlite.execSQL("CREATE INDEX IF NOT EXISTS `index_queue_items_sortOrder` ON `queue_items` (`sortOrder`)")
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
                `skipSilence` INTEGER NOT NULL,
                `playbackSpeed` REAL NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }

    private companion object {
        const val DB_NAME = "theme-settings-1.60.db"
    }
}
