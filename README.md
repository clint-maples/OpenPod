# OpenPod

Android podcast app. Pick a folder, set a filename pattern like `{podcast} - {episode}`, and OpenPod downloads/organizes episodes there.

**Current version:** 1.25  
**Download:** [OpenPod 1.25 APK](https://github.com/clint-maples/OpenPod/releases/download/1.25/OpenPod-1.25.apk)

Previous: [1.24](https://github.com/clint-maples/OpenPod/releases/download/1.24/OpenPod-1.24.apk) � [1.23](https://github.com/clint-maples/OpenPod/releases/download/1.23/OpenPod-1.23.apk) � [1.22](https://github.com/clint-maples/OpenPod/releases/download/1.22/OpenPod-1.22.apk) � [1.21](https://github.com/clint-maples/OpenPod/releases/download/1.21/OpenPod-1.21.apk) � [1.20](https://github.com/clint-maples/OpenPod/releases/download/1.20/OpenPod-1.20.apk) � [1.19](https://github.com/clint-maples/OpenPod/releases/download/1.19/OpenPod-1.19.apk) · [1.18](https://github.com/clint-maples/OpenPod/releases/download/1.18/OpenPod-1.18.apk) · [1.17](https://github.com/clint-maples/OpenPod/releases/download/1.17/OpenPod-1.17.apk) · [1.16](https://github.com/clint-maples/OpenPod/releases/download/1.16/OpenPod-1.16.apk) · [1.15](https://github.com/clint-maples/OpenPod/releases/download/1.15/OpenPod-1.15.apk) · [1.14](https://github.com/clint-maples/OpenPod/releases/download/1.14/OpenPod-1.14.apk) · [1.13](https://github.com/clint-maples/OpenPod/releases/download/1.13/OpenPod-1.13.apk) · [1.12](https://github.com/clint-maples/OpenPod/releases/download/1.12/OpenPod-1.12.apk) · [1.11](https://github.com/clint-maples/OpenPod/releases/download/1.11/OpenPod-1.11.apk) · [1.10](https://github.com/clint-maples/OpenPod/releases/download/1.10/OpenPod-1.10.apk) · [1.9](https://github.com/clint-maples/OpenPod/releases/download/1.9/OpenPod-1.9.apk) · [1.8](https://github.com/clint-maples/OpenPod/releases/download/1.8/OpenPod-1.8.apk) · [1.7](https://github.com/clint-maples/OpenPod/releases/download/1.7/OpenPod-1.7.apk) · [1.6](https://github.com/clint-maples/OpenPod/releases/download/1.6/OpenPod-1.6.apk) · [1.5](https://github.com/clint-maples/OpenPod/releases/download/1.5/OpenPod-1.5.apk) · [1.4](https://github.com/clint-maples/OpenPod/releases/download/1.4/OpenPod-1.4.apk) · [1.3](https://github.com/clint-maples/OpenPod/releases/download/1.3/OpenPod-1.3.apk) · [1.2](https://github.com/clint-maples/OpenPod/releases/download/1.2/OpenPod-1.2.apk) · [1.1](https://github.com/clint-maples/OpenPod/releases/download/1.1/OpenPod-1.1.apk) · [1.0](https://github.com/clint-maples/OpenPod/releases/download/1.0/OpenPod-1.0.apk)

**Stack:** Kotlin, Jetpack Compose, Room, Media3, WorkManager, iTunes search + RSS.

1.19 and earlier sideload APKs were signed with the Android **debug** certificate. **1.20 is release-signed** and needs a fresh install — uninstall the older APK first.

Published 1.20 fingerprints:

- Signing-cert SHA-256: `0e9a887ef0005854b068ae3cd60678c87e489f4bae4cd54eb51c7033a826c21e`
- APK SHA-256: `ff78d7f3f569c33d54278c22672647a4ef6c892c1ed5a9397b8be2d7c533278e`

## Release signing

`assembleRelease` signs with an off-repo keystore. Set these environment variables (never commit the `.jks` or passwords):

- `CLINT_ANDROID_KEYSTORE` — absolute path to the keystore file
- `CLINT_ANDROID_STORE_PASSWORD`
- `CLINT_ANDROID_KEY_ALIAS` — `openpod` for this app
- `CLINT_ANDROID_KEY_PASSWORD`

The release build fails if any of those are missing. Debug builds do not need them. Git ignores `*.jks`, `*.keystore`, and `*passwords.json`.

## Subscriptions

Settings → **Export subscriptions** writes an OPML file you can keep or open in other podcast apps. **Import subscriptions** adds missing shows (default) or can replace the library after a confirmation. OpenPod extras (auto-download, auto-delete, folder name) are stored in an `openpod:` extension and round-trip on import. After import, feeds are fetched so episodes appear.

## Downloads

On a podcast screen, **Find existing downloads** scans that show’s folder for files that already match OpenPod’s naming pattern, episode title, or a distinctive guid. High-confidence matches are marked downloaded and play locally; **Not downloaded** / bulk download skip them.

## Library

- Long-press an episode, or tap **Select episodes**, to multi-select
- **Select all** selects every episode listed for that podcast; **Downloaded** / **Not downloaded** select only those
- Batch **download**, **delete downloaded files**, **archive / unarchive**, or **add to a playlist**
- Remove a show from the podcast screen (trash icon), not the main library list
- Archive hides episodes; turn on **Show archived** on the podcast screen to see them again
- Library layout toggle: list, or a scrolling **5-wide thumbnail grid**
- On a podcast screen, search/filter episodes by title (useful for long feeds)
- Episode rows show remaining time when in progress, a downloaded mark, **In queue** when queued, and one-tap play/pause without opening Now Playing

## Playlists

- Regular playlists you add episodes to
- Smart playlists that filter by play status (unplayed / in progress / played) and download status (all / downloaded / not downloaded)
- Separate from **Up Next**: playlists are saved lists; the queue is what plays after the current episode

## Playback

Now Playing speed is a text box from **0.5x to 5.0x**, with **- / +** stepping by **0.1x**. Skip back/forward on Now Playing and the mini player use the Settings amounts (**10 / 15 / 30 / 45 / 60** seconds; changes apply immediately). Sleep timer on Now Playing can fade out at the end of the episode, after **5�60** minutes, or whichever comes first � cancel it while it is running. Scroll down on Now Playing for the publisher episode notes. Playback continues from the lock screen and notification with play/pause, rewind, and skip. The equalizer has a reset button and a volume boost slider (center = default; left quieter, right louder than system volume). The mini player stays on other tabs with artwork, title, skip, play/pause, tap-to-expand, and an **Up Next** shortcut. Up Next is a real queue (not a playlist): **Play next** inserts at the front, **Add to Up Next** appends, and you can reorder or clear. Playing an episode starts it now and removes it from the queue if it was listed; when an episode ends, the next queued item starts. On Now Playing, chapter arrows appear under the episode name when the episode has chapter metadata. Tap the artwork on Now Playing to open that show's episode list. Share an episode from Now Playing, the episode screen, or the episode-row overflow -- the system share sheet sends the show name, episode title, and public stream URL (plus permalink when we have one).

## Sync schedule

Settings → **Check for new episodes**: hourly, daily, or weekly. Daily and weekly also take a time of day.

Built for Clint Maples.


## Discover

One field on Discover: type a show name and press **Search** on the keyboard, or paste an RSS / podcast URL and press Search to add it. The **x** at the end of the field clears the text.

## 1.25
Up Next is a real queue, separate from playlists: play next, add to queue, reorder, and clear. Playing an episode starts it now and leaves the rest of the queue; the next item starts when the current episode ends. Episode rows show remaining time / in progress, a downloaded mark, **In queue**, and one-tap play/pause without opening Now Playing.

Release-signed with the shared OpenPod keystore. APK SHA-256: `0edf935dc376011b7789ac83a19f831da9c12fa150d239e7332e3b9d72c0cfb8`

## 1.24
Skip interval chips in Settings (10/15/30/45/60s) drive Now Playing and mini player skip buttons. Sleep timer on Now Playing: end of episode and/or timed fade-out (5-60 min), with cancel. Persistent mini player on other tabs (artwork, title, skip, play/pause, tap-to-expand) plus an Up Next shortcut stub for 1.25.

Release-signed with the shared OpenPod keystore. APK SHA-256: `94f410fdc63e8dd5fe50f803db1db26cd32456b40bbe7b778d83b0a8398f33e1`

## 1.23
Unified Discover search (keyboard Search / Enter, trailing x, add-by-URL when the field looks like a feed). Interoperable episode share from Now Playing and episode overflow (podcast title, episode title, enclosure URL, permalink if available). Episode title search on the podcast screen.

Release-signed with the shared OpenPod keystore. APK SHA-256: `24dbaa22516c28e0d65a37065527edee963a17125d33de3eef3fc82bbdc77f2a`

## 1.22
Chapters: Experts Only Radio #062 run-on HTML tracklists (glued clocks, including `00: 28:27`). Now Playing artwork opens that podcast's episode list.

Release-signed with the shared OpenPod keystore. APK SHA-256: `70c8792b252e378320163dd7fad3be3b1963791cbbebf0ac9cdae4bce39c3f6b`

## 1.20
Release APKs are signed from `CLINT_ANDROID_*` environment variables. The keystore and passwords stay off-repo; `assembleRelease` fails if those variables are missing.

## 1.9
Lock-screen / notification player: satisfy foreground-service start immediately, remove custom MediaSession.Callback that could block system UI, and refresh Media3 media notifications on play-state changes.