package com.openpod.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.openpod.app.data.db.AppDatabase
import com.openpod.app.data.db.SettingsEntity
import com.openpod.app.work.PodcastSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OpenPodApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    "playback_media",
                    "Now playing",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    setShowBadge(false)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    "playback",
                    "Playback",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            val settings = AppDatabase.get(this@OpenPodApp).settings().get() ?: SettingsEntity()
            PodcastSyncWorker.enqueue(
                this@OpenPodApp,
                settings.syncFrequency,
                settings.syncHour,
                settings.syncMinute
            )
        }
    }
}
