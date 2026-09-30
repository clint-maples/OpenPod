# OpenPod

Android podcast app. Pick a folder, set a filename pattern like `{podcast} - {episode}`, and OpenPod downloads/organizes episodes there.

**Current version:** 1.35  
**Download:** [OpenPod 1.35 APK](https://github.com/clint-maples/OpenPod/releases/download/1.35/OpenPod-1.35.apk)

**Previous**

- [1.34](https://github.com/clint-maples/OpenPod/releases/download/1.34/OpenPod-1.34.apk)
- [1.33](https://github.com/clint-maples/OpenPod/releases/download/1.33/OpenPod-1.33.apk)
- [1.32](https://github.com/clint-maples/OpenPod/releases/download/1.32/OpenPod-1.32.apk)

<!-- Maintainer: when shipping a new version, keep Current plus the previous three APK links only. -->

**Stack:** Kotlin, Jetpack Compose, Room, Media3, WorkManager, iTunes search + RSS.

1.19 and earlier sideload APKs were signed with the Android **debug** certificate. **1.20 is release-signed** and needs a fresh install â€” uninstall the older APK first.

Published 1.20 fingerprints:

- Signing-cert SHA-256: `0e9a887ef0005854b068ae3cd60678c87e489f4bae4cd54eb51c7033a826c21e`
- APK SHA-256: `ff78d7f3f569c33d54278c22672647a4ef6c892c1ed5a9397b8be2d7c533278e`

## Release signing

`assembleRelease` signs with an off-repo keystore. Set these environment variables (never commit the `.jks` or passwords):

- `CLINT_ANDROID_KEYSTORE` â€” absolute path to the keystore file
- `CLINT_ANDROID_STORE_PASSWORD`
- `CLINT_ANDROID_KEY_ALIAS` â€” `openpod` for this app
- `CLINT_ANDROID_KEY_PASSWORD`

The release build fails if any of those are missing. Debug builds do not need them. Git ignores `*.jks`, `*.keystore`, and `*passwords.json`.

## Subscriptions

Settings â†’ **Export subscriptions** writes an OPML file you can keep or open in other podcast apps. **Import subscriptions** adds missing shows (default) or can replace the library after a confirmation. OpenPod extras (auto-download, auto-delete, folder name, skip first/last seconds) are stored in an `openpod:` extension and round-trip on import. After import, feeds are fetched so episodes appear.

## Downloads

On a podcast screen, **Find existing downloads** scans that showâ€™s folder for files that already match OpenPodâ€™s naming pattern, episode title, or a distinctive guid. High-confidence matches are marked downloaded and play locally; **Not downloaded** / bulk download skip them.

## Library

- Long-press an episode, or tap **Select episodes**, to multi-select
- **Select all** selects every episode listed for that podcast; **Downloaded** / **Not downloaded** select only those
- Batch **download**, **delete downloaded files**, **archive / unarchive**, or **add to a playlist**
- Remove a show from the podcast screen (trash icon), not the main library list
- Archive hides episodes; turn on **Show archived** on the podcast screen to see them again
- Library layout toggle: list, or a scrolling **5-wide thumbnail grid**
- On the thumbnail grid, long-press a cover and drag it to reorder shows. The order is saved and the list uses it too
- On a podcast screen, tap the gear for that show’s settings: auto-download, auto-delete, and skip-first / skip-last seconds so every episode trims intro and/or outro
- On a podcast screen, search/filter episodes by title (useful for long feeds)
- Episode rows show remaining time when in progress, a downloaded mark, **In queue** when queued, and one-tap play/pause without opening Now Playing
- Each episode row’s three-dot menu has **Play next**, **Add to Up Next**, **Share**, and **Archive episode**. **Archive episode** hides that one episode; the subscription stays. With **Show archived** on, the same menu offers **Unarchive episode**

## Playlists

- Regular playlists you add episodes to
- Smart playlists that filter by play status (unplayed / in progress / played) and download status (all / downloaded / not downloaded)
- Separate from **Up Next**: playlists are saved lists; the queue is what plays after the current episode

## Playback

Now Playing stacks artwork in the top third, then the episode and show, the scrubber, and skip/play transport. The whole player scrolls so publisher notes, chapters, and the transcript sit below the transport. The pinned bottom action bar is Speed (current rate, for example **1.0x**), graphic equalizer, sleep timer, and share. The equalizer button opens the graphic equalizer screen. Up Next stays on the mini player. Speed opens a menu that is still a text box from **0.5x to 5.0x**, with **- / +** stepping by **0.1x**. Skip back/forward on Now Playing and the mini player use the Settings amounts (free-text seconds; changes apply when you leave the field). The mini player skip buttons are icon-only — they do not show the second counts. Per-show settings can skip the first and/or last N seconds of every episode (intro/outro trim). The sleep menu can fade out at the end of the episode, after **5–60** minutes, or whichever comes first — cancel it while it is running. Scroll down on Now Playing for the publisher episode notes. Playback continues from the lock screen and notification with play/pause, rewind, and skip. The equalizer puts the volume boost slider at the top (center = default; left quieter, right louder than system volume), then the enable switch, reset, and frequency bands. The mini player stays on other tabs with artwork, skip, play/pause, tap-to-expand, and an **Up Next** shortcut. Up Next is a real queue (not a playlist): **Play next** inserts at the front, **Add to Up Next** appends, and you can reorder or clear. Playing an episode starts it now and removes it from the queue if it was listed; when an episode ends, the next queued item starts. On Now Playing, chapter arrows appear under the episode name when the episode has chapter metadata. Tap the artwork on Now Playing to open that show's episode list. Share an episode from Now Playing, the episode screen, or the episode-row overflow -- the system share sheet sends the show name, episode title, and public stream URL (plus permalink when we have one).

## Sync schedule

Settings â†’ **Check for new episodes**: hourly, daily, or weekly. Daily and weekly also take a time of day.

Built for Clint Maples.


## Discover

One field on Discover: type a show name and press **Search** on the keyboard, or paste an RSS / podcast URL and press Search to add it. The **x** at the end of the field clears the text.

## 1.35
On the library thumbnail grid, long-press a podcast cover and drag it to another slot to reorder the shows. The new order is saved and the list layout follows it.

Release-signed with the shared OpenPod keystore. APK SHA-256: `c2ff5002a991e718eb20d7edadeda647a7b5f7c16e4405c1b50786e9b2a117b7`

## 1.34
On a podcast’s episode list, the episode-row three-dot menu (**Play next**, **Add to Up Next**, **Share**) now includes **Archive episode**. That archives the one episode and leaves the subscription. The library home grid and list are back to the 1.32 overflow: a plain three-dot control, with **Archive podcast** still archiving every available episode. The 1.33 dark circular grid button and split list click target are gone.

Release-signed with the shared OpenPod keystore. APK SHA-256: `c537bd40d8ee946b6a03bb686402b7c1610a9de8892e5d1a2ee1a405a28bb354`

## 1.33
The library **Archive** action is on the podcast three-dot menu in both list and grid. On the grid, that control is a dark circular button on the cover so it stays visible on light artwork. **Archive** marks every available episode archived; the subscription stays. 1.34 puts single-episode archive on the episode row and restores the 1.32 library-home overflow.

Release-signed with the shared OpenPod keystore. APK SHA-256: `0a3a8fde5775de5eb9e7ec32412fd236a84cf49c428e759ed0ba0dc70f9e618b`

## 1.32
- Played episodes are dimmed in the episode list.
- Per-podcast auto-archive of played episodes (Library toggle, default off, OPML `openpod:autoArchive`).
- Archive all episodes of a podcast from the library three-dot menu. The subscription stays.

Release-signed with the shared OpenPod keystore. APK SHA-256: `21fed11f689b11553a64a6bcc225d9cf62d87e3292d617547ab2df8ac0436f70`

## 1.31
Now Playing leaves the title off above the artwork. The bottom action bar is Speed, graphic equalizer, sleep timer, and share. Up Next stays on the mini player. The equalizer button opens the graphic equalizer screen. Artwork, episode details, the scrubber, transport, and publisher notes scroll together; the bottom action bar stays pinned.

Release-signed with the shared OpenPod keystore. APK SHA-256: `bcf93046217723d721380326e5a9e6a66d384e99ddf16fe2b510e4d4b6437b37`

## 1.30
The graphic equalizer screen puts the volume boost slider at the top. The long explanation under volume boost is removed; the control keeps its title, the current level, and the quieter / default / louder labels.

Release-signed with the shared OpenPod keystore. APK SHA-256: `40dc1a038fddc9fb2896d5bb480f2de18bbf93d302e6cf88fa0fc8ddbe876206`

## 1.29
Now Playing is a vertical stack: large rounded artwork in the top third, then the episode and show, the scrubber, and skip/play transport. Action icons are no longer in the upper right. The bottom row is Speed (current rate, such as 1.0x), graphic equalizer, Up Next (the existing playlist icon), sleep timer, and share. Speed and sleep open menus instead of sitting on the player. Skip amounts still come from Settings. Publisher notes, chapters, and the transcript stay below the transport.

Release-signed with the shared OpenPod keystore. APK SHA-256: `a9c64d23cb0a71743cb616f40d4637239004c4b3fbe392978fe3e56912f1244d`

## 1.28
Each subscribed show has its own **podcast settings** screen (gear on the podcast screen) for auto-download, auto-delete, skip first seconds, and skip last seconds. Those per-show controls are no longer inline on the episode list. Mini player skip / play / pause sit in the horizontal center. Global Settings still has skip interval (back/forward seconds) and defaults for newly added shows.

Release-signed with the shared OpenPod keystore. APK SHA-256: `d8921111d667355f3f7a4f06787310339b372a5bdb38cc5dab2cefb76239d99c`

## 1.27
Skip back/forward in Settings are free-text seconds (1–600) instead of 10/15/30/45/60 chips. Mini player skip buttons are icon-only. Per-show settings can skip the first and/or last N seconds of every episode so playback starts after the intro and finishes/auto-advances before the outro.

Release-signed with the shared OpenPod keystore. APK SHA-256: `e94fbdd8ba14b036331220911a8b82e904424512a1a4de8634bed579cde21c08`

## 1.26
Mini player on other tabs no longer shows podcast or episode name text. Artwork, skip, play/pause, progress, sleep remaining, Up Next, and tap-to-expand stay.

Release-signed with the shared OpenPod keystore. APK SHA-256: `c6b76adcdcc0e49f9de1c1c9c6d05d9cd5d0706e7bd81461af0d4bab8bd03e5b`

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