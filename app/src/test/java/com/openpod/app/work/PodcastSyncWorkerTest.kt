package com.openpod.app.work

import com.openpod.app.data.db.SettingsEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class PodcastSyncWorkerTest {
    @Test
    fun thirtyMinuteSyncIsTheDefaultAndRunsAllDay() {
        assertEquals("30min", SettingsEntity.SYNC_30_MIN)
        assertEquals(SettingsEntity.SYNC_30_MIN, SettingsEntity().syncFrequency)
        assertEquals(30L to TimeUnit.MINUTES, PodcastSyncWorker.repeatInterval(SettingsEntity.SYNC_30_MIN))
        assertEquals(0L, PodcastSyncWorker.initialDelayMs(SettingsEntity.SYNC_30_MIN, hour = 6, minute = 0))
    }

    @Test
    fun hourlyDailyAndWeeklyStillSchedule() {
        assertEquals(1L to TimeUnit.HOURS, PodcastSyncWorker.repeatInterval(SettingsEntity.SYNC_HOURLY))
        assertEquals(0L, PodcastSyncWorker.initialDelayMs(SettingsEntity.SYNC_HOURLY, hour = 6, minute = 0))
        assertEquals(1L to TimeUnit.DAYS, PodcastSyncWorker.repeatInterval(SettingsEntity.SYNC_DAILY))
        assertEquals(7L to TimeUnit.DAYS, PodcastSyncWorker.repeatInterval(SettingsEntity.SYNC_WEEKLY))
    }
}
