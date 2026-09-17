# OpenPod

Android podcast app. Pick a folder, set a filename pattern like `{podcast} - {episode}`, and OpenPod downloads/organizes episodes there.

**Current version:** 1.15  
**Download:** [OpenPod 1.15 APK](https://github.com/clint-maples/OpenPod/releases/download/1.15/OpenPod-1.15.apk)

Previous: [1.14](https://github.com/clint-maples/OpenPod/releases/download/1.14/OpenPod-1.14.apk) · [1.13](https://github.com/clint-maples/OpenPod/releases/download/1.13/OpenPod-1.13.apk) · [1.12](https://github.com/clint-maples/OpenPod/releases/download/1.12/OpenPod-1.12.apk) · [1.11](https://github.com/clint-maples/OpenPod/releases/download/1.11/OpenPod-1.11.apk) · [1.10](https://github.com/clint-maples/OpenPod/releases/download/1.10/OpenPod-1.10.apk) · [1.9](https://github.com/clint-maples/OpenPod/releases/download/1.9/OpenPod-1.9.apk) · [1.8](https://github.com/clint-maples/OpenPod/releases/download/1.8/OpenPod-1.8.apk) · [1.7](https://github.com/clint-maples/OpenPod/releases/download/1.7/OpenPod-1.7.apk) · [1.6](https://github.com/clint-maples/OpenPod/releases/download/1.6/OpenPod-1.6.apk) · [1.5](https://github.com/clint-maples/OpenPod/releases/download/1.5/OpenPod-1.5.apk) · [1.4](https://github.com/clint-maples/OpenPod/releases/download/1.4/OpenPod-1.4.apk) · [1.3](https://github.com/clint-maples/OpenPod/releases/download/1.3/OpenPod-1.3.apk) · [1.2](https://github.com/clint-maples/OpenPod/releases/download/1.2/OpenPod-1.2.apk) · [1.1](https://github.com/clint-maples/OpenPod/releases/download/1.1/OpenPod-1.1.apk) · [1.0](https://github.com/clint-maples/OpenPod/releases/download/1.0/OpenPod-1.0.apk)

**Stack:** Kotlin, Jetpack Compose, Room, Media3, WorkManager, iTunes search + RSS.

## Library

- Long-press an episode, or tap **Select episodes**, to multi-select
- **Select all** selects every episode listed for that podcast; **Downloaded** / **Not downloaded** select only those
- Batch **download**, **delete downloaded files**, **archive / unarchive**, or **add to a playlist**
- Remove a show from the podcast screen (trash icon), not the main library list
- Archive hides episodes; turn on **Show archived** on the podcast screen to see them again
- Library layout toggle: list, or a scrolling **5-wide thumbnail grid**

## Playlists

- Regular playlists you add episodes to
- Smart playlists that filter by play status (unplayed / in progress / played) and download status (all / downloaded / not downloaded)

## Playback

Now Playing speed is a text box from **0.5x to 5.0x**, with **− / +** stepping by **0.1x**. Scroll down on Now Playing for the publisher episode notes. Playback continues from the lock screen and notification with play/pause, rewind, and skip. The equalizer has a reset button and a volume boost slider (center = default; left quieter, right louder than system volume). The mini player on other tabs shows the podcast artwork with skip and play/pause centered in the bar. On Now Playing, chapter arrows appear under the episode name when the episode has chapter metadata.

## Sync schedule

Settings → **Check for new episodes**: hourly, daily, or weekly. Daily and weekly also take a time of day.

Built for Clint Maples.

## 1.9
Lock-screen / notification player: satisfy foreground-service start immediately, remove custom MediaSession.Callback that could block system UI, and refresh Media3 media notifications on play-state changes.