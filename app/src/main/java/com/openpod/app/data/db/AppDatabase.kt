package com.openpod.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PodcastEntity::class,
        EpisodeEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        QueueItemEntity::class,
        SettingsEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun podcasts(): PodcastDao
    abstract fun episodes(): EpisodeDao
    abstract fun playlists(): PlaylistDao
    abstract fun queue(): QueueDao
    abstract fun settings(): SettingsDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE episodes ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE playlists ADD COLUMN isSmart INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE playlists ADD COLUMN playFilter TEXT NOT NULL DEFAULT 'all'")
                db.execSQL("ALTER TABLE playlists ADD COLUMN downloadFilter TEXT NOT NULL DEFAULT 'all'")
                db.execSQL("ALTER TABLE settings ADD COLUMN showArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN syncFrequency TEXT NOT NULL DEFAULT 'daily'")
                db.execSQL("ALTER TABLE settings ADD COLUMN syncHour INTEGER NOT NULL DEFAULT 6")
                db.execSQL("ALTER TABLE settings ADD COLUMN syncMinute INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN lastEpisodeId INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN volumeBoost REAL NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN libraryLayout TEXT NOT NULL DEFAULT 'list'")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE episodes ADD COLUMN permalink TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `queue_items` (
                        `episodeId` INTEGER NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        PRIMARY KEY(`episodeId`),
                        FOREIGN KEY(`episodeId`) REFERENCES `episodes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_queue_items_sortOrder` ON `queue_items` (`sortOrder`)"
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE podcasts ADD COLUMN skipIntroSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE podcasts ADD COLUMN skipOutroSec INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE podcasts ADD COLUMN autoArchive INTEGER NOT NULL DEFAULT 0")
            }
        }

        internal val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE podcasts ADD COLUMN libraryOrder INTEGER NOT NULL DEFAULT -1"
                )
            }
        }

        internal val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE episodes ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /** Old installs left on the daily default move to every 30 minutes. Hourly and weekly stay. */
        internal val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "UPDATE settings SET syncFrequency = '${SettingsEntity.SYNC_30_MIN}' " +
                        "WHERE syncFrequency = '${SettingsEntity.SYNC_DAILY}'"
                )
                db.execSQL(
                    "ALTER TABLE settings ADD COLUMN skipSilence INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        internal val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN playbackSpeed REAL NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE podcasts ADD COLUMN playbackSpeed REAL")
                db.execSQL("ALTER TABLE podcasts ADD COLUMN skipSilence INTEGER")
                db.execSQL("ALTER TABLE podcasts ADD COLUMN volumeBoost REAL")
            }
        }

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "openpod.db"
                ).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13
                ).build().also { instance = it }
            }
        }
    }
}
