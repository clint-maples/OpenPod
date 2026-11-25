# OpenPod

Android podcast app. Pick a folder, set a filename pattern like `{podcast} - {episode}`, and OpenPod downloads/organizes episodes there.

**Current version:** 1.62  
**Download:** [OpenPod 1.62 APK](https://github.com/clint-maples/OpenPod/releases/download/1.62/OpenPod-1.62.apk)

**Previous**

- [1.61](https://github.com/clint-maples/OpenPod/releases/download/1.61/OpenPod-1.61.apk)
- [1.60](https://github.com/clint-maples/OpenPod/releases/download/1.60/OpenPod-1.60.apk)
- [1.59](https://github.com/clint-maples/OpenPod/releases/download/1.59/OpenPod-1.59.apk)

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

Settings → Data → **Export subscriptions** writes an OPML file you can keep or open in other podcast apps. **Import subscriptions** adds missing shows (default) or can replace the library after a confirmation. OpenPod extras (auto-download, auto-delete, folder name, skip first/last seconds) are stored in an `openpod:` extension and round-trip on import. After import, feeds are fetched so episodes appear.

## Downloads

On a podcast screen, **Find existing downloads** scans that showâ€™s folder for files that already match OpenPodâ€™s naming pattern, episode title, or a distinctive guid. High-confidence matches are marked downloaded and play locally; **Not downloaded** / bulk download skip them.

## Library

- Long-press an episode, or tap **Select episodes**, to multi-select
- **Select all** selects every episode listed for that podcast; **Downloaded**, **Not downloaded**, and **Favorites** select only those
- Batch **download**, **delete downloaded files**, **archive / unarchive**, or **add to a playlist**
- Remove a show from the podcast screen (trash icon), not the main library list
- Archive hides episodes; turn on **Show archived** on the podcast screen to see them again
- Library layout toggle: list, or a scrolling **5-wide thumbnail grid**
- On the thumbnail grid, long-press a cover and drag it to reorder shows. The order is saved and the list uses it too
- On a podcast screen, tap the gear for that show’s settings: auto-download, auto-delete, and skip-first / skip-last seconds so every episode trims intro and/or outro
- On a podcast screen, search/filter episodes by title (useful for long feeds)
- Episode rows show remaining time when in progress, a downloaded mark, a heart under that mark (outlined in the same blue as the downloaded check when it is not a favorite, filled pink when it is), **In queue** when queued, and one-tap play/pause without opening Now Playing
- Each episode row’s three-dot menu has **Play next**, **Add to Up Next**, **Share**, **Add to favorites** (or **Remove from favorites** when it already is), and **Archive episode**. **Archive episode** hides that one episode; the subscription stays. With **Show archived** on, the same menu offers **Unarchive episode**

## Playlists

- Regular playlists you add episodes to
- Smart playlists that filter by play status (unplayed / in progress / played) and download status (all / downloaded / not downloaded)
- Separate from **Up Next**: playlists are saved lists; the queue is what plays after the current episode

## Playback

Now Playing stacks artwork in the top third of the area above the footer, then the episode and show. Publisher notes, chapters, and the transcript scroll in that area, with room to bring the last line clear of the footer. The pinned footer is three separated bars: a 32 dp black seek row with elapsed time on the left and time remaining on the right (a countdown with a leading minus, for example **-42:10** or **-1:02:10**) inline at the ends of a thin rounded track and a centered round thumb, then a 78 dp black rewind / play/pause / fast-forward row, then a 45 dp actions bar on the same gray as the mini player — Speed (current rate, for example **1.0x**), an equalizer icon, sleep timer, share, and a favorite heart at the right. Fast-forward uses a mirrored rewind icon; skip counts under the buttons have no plus or minus sign. A fixed 48 dp tall, 4 dp accent scrollbar on a faint 2 dp track sits at the right edge of every scrolling list and screen, stays visible while the content can scroll (it does not auto-hide), and hides when everything fits. On Now Playing (and the episode screen) the notes, chapters, and transcript are a regular scrolling column, so the thumb tracks the real page length from the first frame. Drag the thumb (or tap the track) to jump through the list; the thumb widens while you drag it. A thin accent line sits above the bottom navigation bar on every screen. It stays above the system navigation bar. The equalizer button opens the graphic equalizer screen. Up Next stays on the mini player. Speed opens a menu that is still a text box from **0.5x to 5.0x**, with **- / +** stepping by **0.1x**. Skip back/forward on Now Playing and the mini player use the Settings → Playback amounts (10/15/30/60 second chips, or a custom value from 1–600 seconds). The mini player skip buttons are icon-only — they do not show the second counts — and fast-forward is a horizontally mirrored rewind icon, the same shape used on Now Playing. Per-show settings can skip the first and/or last N seconds of every episode (intro/outro trim). The sleep menu can fade out at the end of the episode, after **5–60** minutes, or whichever comes first — cancel it while it is running. Scroll down on Now Playing for the publisher episode notes. Playback continues from the lock screen and notification with play/pause, rewind, and skip. The equalizer puts the volume boost slider at the top (center = default; left quieter, right louder than system volume), then the enable switch, reset, and frequency bands. The mini player stays on other tabs with artwork, the episode and show names, skip, play/pause, and an **Up Next** shortcut. Tapping the artwork, those titles, the progress bar, or any other blank area opens Now Playing. Skip, play/pause, and Up Next keep their own actions. Up Next is a real queue (not a playlist): **Play next** inserts at the front, **Add to Up Next** appends, and you can reorder or clear. Playing an episode starts it now and removes it from the queue if it was listed; when an episode ends, the next queued item starts. On Now Playing, chapter arrows appear under the episode name when the episode has chapter metadata. Tap the artwork on Now Playing to open that show's episode list. Share an episode from Now Playing, the episode screen, or the episode-row overflow -- the system share sheet sends the show name, episode title, and public stream URL (plus permalink when we have one).

## Sync schedule

Settings → Downloads → **Check for new episodes**: every 30 minutes all day (the default), hourly, daily, or weekly. Daily and weekly also take a time of day. Trim silence is optional and off by default; it lives in Settings → Playback and in the Now Playing Playback Effects sheet.

Built for Clint Maples.


## Discover

One field on Discover: type a show name and press **Search** on the keyboard, or paste an RSS / podcast URL and press Search to add it. The **x** at the end of the field clears the text. With the field empty, Discover shows **Top Podcasts** for the US. A filter under the heading opens All and the genre charts. Add uses the same iTunes lookup as search.


## 1.61
The Now Playing equalizer action is icon-only. The "EQ" label under the bar-chart icon was taller than the 45 dp actions bar and was clipped at the bottom. The icon is now centered on the same line as Speed, Sleep, Share, and Favorite, with content description "Equalizer". The bar height is unchanged.

Appearance now includes Theme (Dark, Light, or System), a Dark-only Background (Soft dark or True black), and six Accent swatches: Teal, Amber, Coral, Green, Purple, and Graphite. Dark + Soft dark + Teal is the default, so existing installs keep today's look. Light uses an Accent wash of the selected color, automatically half-tinted for Coral and Amber, with Option D tonal buttons on transport, actions, and the mini player. The favorite heart stays pink. Hardcoded chrome (scrollbar, seek track, nav pill, Settings hairlines, playback-effects sheet, and system bars) now follows theme tokens, and the inactive seek track is #8A939C in dark and #7A858E in light. Choices are stored in Settings with a Room migration and apply immediately.

## 1.59
Settings is now a category hub: Playback, Downloads, Notifications, Appearance, Data, and About, each with a one-line summary. Sub-screens use compact rows with the current value as the subtitle. Skip back/forward use 10/15/30/60 chips plus Custom (1–600 seconds). Default playback speed and trim silence live in Playback, library layout in Appearance, and a global Rescan downloads action in Data. File naming is under Downloads → Advanced; the headset-button reference is under Playback → Advanced. Long explanatory copy is gone. Stored values are unchanged.

## 1.58
The Now Playing progress thumb is vertically centered on the 3 dp track again. Material's Slider was laying the 12 dp dot out in a 16 dp box and aligning it to the top, which sat the thumb about 2 dp above the track. The seek bar is now drawn directly so the thumb and track share one center line at 0%, 50%, 100%, and while scrubbing. Time remaining on the right (-42:10) is unchanged.

## 1.57
The mini player's fast-forward button is now a horizontal mirror of its rewind button (the same Replay vector flipped with scaleX = -1). It previously used Material's distinct FastForward icon. Skip amounts and behavior are unchanged, and the buttons stay icon-only with no second counts. Now Playing already used this mirrored rewind glyph and was not changed.

Release-signed with the shared OpenPod keystore. APK SHA-256: `e2afb7a9a308ab547da65145d3c663682afd57b2505944e2d5c03c1b014cf18d`

## 1.56
The right-hand time on the Now Playing progress row is time remaining, not total duration, written with a leading minus (-42:10, or -1:02:10 when more than an hour is left). It updates as you play and while you scrub. The left-hand time is still elapsed. The label keeps a steady width so the seek track does not jump. The mini player has only a thin progress line and no time labels, so it is unchanged.

Release-signed with the shared OpenPod keystore. APK SHA-256: `c3c939f0ad73b6d5ff9a3b7676a77cf235f9aabc7ac4dd180dd2b90190f55e00`

## 1.55
On Now Playing in 1.54, the scrollbar thumb crawled at first and then jumped once more of the page had been measured. The list was lazy, so the scroll fraction was guessed from the items on screen — a tall artwork block made the rest of the page look much longer than it was. 1.55 renders Now Playing show notes, chapters, and the transcript as a regular scrolling column, so the thumb uses the real page length from the first frame. The episode screen does the same. Other lists keep a fixed 48 dp thumb and still let you drag it; they remember measured row heights and do not let a tall header set the length of the rows below.

Release-signed with the shared OpenPod keystore. APK SHA-256: `d2f9c7e2a24a204988bde4504831315531c33979c5ae8b1b1b3a6f6f3303c10a`

## 1.54
The Now Playing show-notes thumb in 1.53 first appeared very large, then shrank once items were measured, because thumb length came from an early content-size estimate. 1.54 uses a fixed 48 dp tall, 4 dp `#7FB7C9` thumb on the faint 2 dp track, so the handle never changes size; its position still tracks how far you have scrolled. You can drag the thumb (or tap the track) on a narrow right-edge rail to jump through the list, and the thumb widens to 8 dp while it is being dragged. Same screens as 1.53; not used on the Playback Effects sheet.

Release-signed with the shared OpenPod keystore. APK SHA-256: `7de86898b5b3707025fde841f559412966d224856674feb305eeebee39f76f4b`

## 1.53
Fixes the scrollbar still never appearing in 1.51 and 1.52. The bar was wired to a different scroll state than the list (LazyColumn created its own implicit state), and drawing on the list itself could be covered by item layers. 1.53 uses a Box overlay rail on top of the content, always visible when the list can scroll, with a 4 dp `#7FB7C9` thumb on a faint 2 dp track. Applied to every scrolling screen and list; not used on the Playback Effects sheet.

Release-signed with the shared OpenPod keystore. APK SHA-256: `4cfcde7ad891ad65932cf5b07a1425b870fbfed79c15b67acc965301ba9dc971`

## 1.52
Fixes the 1.51 scrollbar never appearing. The bar now stays visible whenever a list or screen can scroll (no auto-hide), with a 4 dp `#7FB7C9` thumb on a faint but visible 2 dp track, and is applied to every scrollable screen, list, and sheet — including the Playback Effects sheet and a Playlists list that can actually scroll.

Release-signed with the shared OpenPod keystore. APK SHA-256: `6364f1d44875a2f423e53c566c0285ba103276e6a8828fcf2306cc8f8dd7611b`

## 1.51
A thin 4 dp accent (`#7FB7C9`) scrollbar on a faint 2 dp track appears at the right edge of overflowing lists and scrolling screens: the podcast screen, library list and grid, Discover, episode details, Now Playing show notes, Up Next, playlists, the equalizer, podcast settings, Settings, and the Discover category picker. The thumb length is proportional to visible over total content (minimum 36 dp). The bar is hidden when everything fits.

Release-signed with the shared OpenPod keystore. APK SHA-256: `3b27b4be91e55065fe22176586763ea87de5115d62365a2ebff33d732935c4b1`

## 1.50
Now Playing progress and transport rows are pure black and tighter: the seek row is 32 dp with elapsed and remaining times inline at the ends of the bar; transport is 78 dp; the actions bar stays 45 dp on the mini-player gray. The 3 dp track, 12 dp centered thumb, unsigned 15/60 labels, mirrored fast-forward, band gaps, and the accent line above nav are unchanged.

Release-signed with the shared OpenPod keystore. APK SHA-256: `4fda0b720af4407067f0b848542087316ccd44ae1144ff65052599ce790fe4d8`

## 1.49
Now Playing footer is three separated bars (progress, transport, actions) on the mini-player gray, with a 3 dp rounded seek track and a 12 dp round thumb. Transport glyphs are 10% smaller, fast-forward is a mirrored rewind icon, and skip labels are unsigned (15 / 60). A 1 dp accent line sits above the bottom navigation bar on every screen.

## 1.48
Settings no longer has a Trim silence switch. Trim silence stays in the Now Playing Playback Effects sheet, including All podcasts and This podcast.

Release-signed with the shared OpenPod keystore. APK SHA-256: `bda16d5537493799e0795324dfeb1d55f59ff638ed8248fea54cc634d4371364`

## 1.47
The Playback Effects sheet border sits on the sheet, with a dimmed scrim behind it. Speed, trim silence, and volume boost can apply to every show or just the one that is playing. Discover's genre charts open from a filter under Top Podcasts instead of a scrolling chip row. All stays first; the genres are alphabetical.

Release-signed with the shared OpenPod keystore. APK SHA-256: `3620a2577ab28afb80bab911447c969906ad9bde278c2f246c3387f16a6db3e9`

## 1.46
Discover shows Apple's US Top Podcasts, with a genre chip row for the 19 top-level podcast categories. Each chart is cached for about an hour. Adding a chart show looks up its RSS feed the same way search does.

Release-signed with the shared OpenPod keystore. APK SHA-256: `4633c2bcedebeaea5fc6f21f3ee641ec7ee20ff300f0c694f3ac3b1cbf8c6757`

## 1.45
The bottom navigation **Now Playing** label stays centered when it wraps onto two lines. Other nav labels are unchanged.

Release-signed with the shared OpenPod keystore. APK SHA-256: `8041a14429839d13f27b8c4cb00f0565b0374546d953bf8e08bfb2b0609e7424`

## 1.44
New episodes are checked about every 30 minutes, all day. Installs still on the old daily default move to that schedule; hourly, daily, and weekly remain available. Auto-download uses the same per-episode download job as a manual download, and retries episodes that were missed or stuck. Trim silence is an optional playback effect, off by default, in Settings and in the Now Playing Playback Effects sheet.

Release-signed with the shared OpenPod keystore. APK SHA-256: `e96d7eccdeb6665efb6fcb64ffa672ada1747ccddf23bc3692f062297fe7a01c`

## 1.43
The outlined heart uses the same blue as the downloaded check mark in the episode list, on Now Playing, and in the episode menu. Only the filled favorite heart stays pink.

Release-signed with the shared OpenPod keystore. APK SHA-256: `08074649c8bd3c8dc6d66488aeffcf203ad372da96a24ffdba5c6a00abb1203c`

## 1.42
Favorite hearts are pink in the episode list, on Now Playing, and in the episode menu, whether outlined or filled. In **Select episodes**, **Favorites** selects every favorited episode in the current list.

Release-signed with the shared OpenPod keystore. APK SHA-256: `319ffc1856133b64e70bde204963f6dc4abd2e9bff2b99367204c3366c8c1b31`

## 1.41
Episode rows use a heart under the download mark: outlined when the episode is not a favorite, filled when it is. The same heart is the favorite control on Now Playing. Each episode's three-dot menu adds **Add to favorites**, or **Remove from favorites** when it already is, using the existing favorite flag. The mini player shows the episode and show names beside the artwork. Tapping the artwork, those titles, the progress bar, or any other blank area opens Now Playing. Skip, play/pause, and Up Next keep their own actions.

Release-signed with the shared OpenPod keystore. APK SHA-256: `ccb2faf2ef9553650abb7718de8278bed2ac9fda5472a37508976d166384b019`

## 1.40
Now Playing keeps the seek bar, its times, and the rewind, play/pause, and fast-forward buttons in a fixed footer with Speed, EQ, Sleep, Share, and Favorite. The footer stays on screen while artwork, titles, and notes scroll above it. Favorite remains the rightmost control. The transport buttons stay large, and the footer sits above the system navigation bar.

Release-signed with the shared OpenPod keystore. APK SHA-256: `be573dc6b13c7dd687a01e2dde081b8077dbd639cd60ba7cc639648b65442793`

## 1.39
On a podcast’s episode list, a filled gold star sits directly under the downloaded check when that episode is a favorite. Nothing is shown in that spot otherwise. It uses the same per-episode favorite flag as the Now Playing heart, and it updates as soon as that heart is toggled. The heart stays the rightmost control on the Now Playing bottom bar.

Release-signed with the shared OpenPod keystore. APK SHA-256: `dde4b49dd04962e9c44cc991c2300d551d66713f15b8ea246709062770392a6e`

## 1.38
Now Playing’s bottom bar keeps Speed, EQ, Sleep, and Share, with slightly smaller icons and labels and the same tap size. A heart at the far right marks the current episode as a favorite. That flag is stored on the episode and stays after a restart.

Release-signed with the shared OpenPod keystore. APK SHA-256: `4e40f92d4eb04137f3a29cdfd6029bc6efb6d2bac0bcaaacb74df6c17f5dc094`

## 1.37
Play works again after OpenPod has been idle, backgrounded, or killed. The first Play tap re-prepares the saved episode at its saved position instead of calling play on an idle player that ignores it. A downloaded file is used when it is still on disk; otherwise the episode's enclosure URL is refreshed. If playback still cannot start, OpenPod shows a short error toast.

Release-signed with the shared OpenPod keystore. APK SHA-256: `3ec53d872ff4a14d7ac9be2918d1143127f6116f8781e2c23102ea4c3c66cc7c`

## 1.36
The main library no longer shows a three-dot menu on each podcast, in the thumbnail grid or the list. Episode-row menus inside a podcast are unchanged: **Play next**, **Add to Up Next**, **Share**, and **Archive episode** (or **Unarchive episode** when archived episodes are shown).

Release-signed with the shared OpenPod keystore. APK SHA-256: `e96d7b82a985a3657ffd614dbf20ba59a337e0c9e1f067f82626c49f27990ec4`

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

## License

MIT. See [LICENSE](LICENSE).
