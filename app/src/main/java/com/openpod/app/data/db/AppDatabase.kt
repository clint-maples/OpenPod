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
        SettingsEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun podcasts(): PodcastDao
    abstract fun episodes(): EpisodeDao
    abstract fun playlists(): PlaylistDao
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

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "openpod.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { instance = it }
            }
        }
    }
}
