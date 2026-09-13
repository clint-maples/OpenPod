package com.openpod.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PodcastEntity::class,
        EpisodeEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun podcasts(): PodcastDao
    abstract fun episodes(): EpisodeDao
    abstract fun playlists(): PlaylistDao
    abstract fun settings(): SettingsDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "openpod.db"
                ).build().also { instance = it }
            }
        }
    }
}
