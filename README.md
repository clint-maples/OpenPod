# OpenPod

Android podcast app. Pick a folder, set a filename pattern like `{podcast} - {episode}`, and OpenPod downloads/organizes episodes there.

**Current version:** 1.1  
**Download:** [OpenPod 1.1 APK](releases/OpenPod-1.1.apk)

Previous: [1.0](releases/OpenPod-1.0.apk)

**Stack:** Kotlin, Jetpack Compose, Room, Media3, WorkManager, iTunes search + RSS.

## Library

- Long-press an episode, or tap **Select episodes**, to multi-select
- **Select all** selects every episode listed for that podcast; **Downloaded** / **Not downloaded** select only those
- Batch **download**, **delete downloaded files**, **archive / unarchive**, or **add to a playlist**
- Remove a show from the podcast screen (trash icon), not the main library list
- Archive hides episodes; turn on **Show archived** on the podcast screen to see them again

## Playlists

- Regular playlists you add episodes to
- Smart playlists that filter by play status (unplayed / in progress / played) and download status (all / downloaded / not downloaded)

## Playback

Now Playing speed is a text box from **0.5x to 5.0x**, with **− / +** stepping by **0.1x**.

## Sync schedule

Settings → **Check for new episodes**: hourly, daily, or weekly. Daily and weekly also take a time of day.

Built for Clint Maples.