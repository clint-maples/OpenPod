package com.openpod.app.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.openpod.app.BuildConfig
import com.openpod.app.data.db.SettingsEntity
import com.openpod.app.playback.SkipIntervals

private val SettingsCardShape = RoundedCornerShape(12.dp)
private val SettingsHairline: Color
    @Composable get() = LocalOpenPodColors.current.hairline

object SettingsTags {
    const val HUB = "settings-hub"
    const val PLAYBACK = "settings-playback"
    const val DOWNLOADS = "settings-downloads"
    const val NOTIFICATIONS = "settings-notifications"
    const val APPEARANCE = "settings-appearance"
    const val DATA = "settings-data"
    const val ABOUT = "settings-about"
    fun category(route: String) = "settings-category-$route"
}

@Composable
fun SettingsHubRoute(vm: AppViewModel, onOpen: (String) -> Unit) {
    val settings by vm.settings.collectAsState()
    val context = LocalContext.current
    var notificationsOn by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    ObserveResumes {
        notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    SettingsHubScreen(
        playbackSummary = SettingsCopy.playbackSummary(
            settings.skipBackSec,
            settings.skipForwardSec,
            settings.playbackSpeed
        ),
        downloadsSummary = SettingsCopy.downloadsSummary(
            settings.downloadTreeUri,
            settings.autoDownloadDefault
        ),
        notificationsSummary = SettingsCopy.notificationsSummary(notificationsOn),
        appearanceSummary = SettingsCopy.appearanceSummary(
            settings.themeMode,
            settings.accent,
            settings.libraryLayout
        ),
        dataSummary = SettingsCopy.dataSummary(),
        aboutSummary = SettingsCopy.aboutSummary(BuildConfig.VERSION_NAME),
        onOpen = onOpen
    )
}

@Composable
fun PlaybackSettingsRoute(vm: AppViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    PlaybackSettingsScreen(
        skipBackSec = settings.skipBackSec,
        skipForwardSec = settings.skipForwardSec,
        speed = settings.playbackSpeed,
        trimSilence = settings.skipSilence,
        onSkipBack = { vm.setSkip(settings.skipForwardSec, it) },
        onSkipForward = { vm.setSkip(it, settings.skipBackSec) },
        onSpeed = { vm.setScopedSpeed(forThisPodcast = false, speed = it) },
        onTrimSilence = { vm.setSkipSilence(it) },
        onBack = onBack
    )
}

@Composable
fun DownloadsSettingsRoute(vm: AppViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            vm.setDownloadFolder(uri)
        }
    }
    DownloadsSettingsScreen(
        folderLabel = SettingsCopy.folderLabel(settings.downloadTreeUri),
        autoDownload = settings.autoDownloadDefault,
        autoDelete = settings.autoDeleteDefault,
        syncFrequency = settings.syncFrequency,
        syncHour = settings.syncHour,
        syncMinute = settings.syncMinute,
        namingPattern = settings.namingPattern,
        onChooseFolder = { picker.launch(null) },
        onAutoDownload = { vm.setDefaults(it, settings.autoDeleteDefault) },
        onAutoDelete = { vm.setDefaults(settings.autoDownloadDefault, it) },
        onSync = { frequency, hour, minute -> vm.setSyncSchedule(frequency, hour, minute) },
        onNaming = { vm.setNaming(it) },
        onBack = onBack
    )
}

@Composable
fun NotificationsSettingsRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    var notificationsOn by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    val notifyPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    ObserveResumes {
        notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    fun openSystemNotificationSettings() {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        )
    }
    NotificationsSettingsScreen(
        enabled = notificationsOn,
        onToggle = {
            if (notificationsOn) {
                openSystemNotificationSettings()
            } else if (Build.VERSION.SDK_INT >= 33) {
                notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                openSystemNotificationSettings()
            }
        },
        onBack = onBack
    )
}

@Composable
fun AppearanceSettingsRoute(vm: AppViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    AppearanceSettingsScreen(
        themeMode = settings.themeMode,
        darkBackground = settings.darkBackground,
        accent = settings.accent,
        layout = settings.libraryLayout,
        systemIsDark = androidx.compose.foundation.isSystemInDarkTheme(),
        onThemeMode = { vm.setThemeMode(it) },
        onDarkBackground = { vm.setDarkBackground(it) },
        onAccent = { vm.setAccent(it) },
        onLayout = { vm.setLibraryLayout(it) },
        onBack = onBack
    )
}

@Composable
fun DataSettingsRoute(vm: AppViewModel, onBack: () -> Unit) {
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    var confirmReplace by remember { mutableStateOf(false) }
    val exportSubscriptions = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/xml")
    ) { uri ->
        if (uri != null) vm.exportSubscriptions(uri)
    }
    val importSubscriptions = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) pendingImport = uri
    }
    DataSettingsScreen(
        onExport = { exportSubscriptions.launch("OpenPod-subscriptions.opml") },
        onImport = {
            importSubscriptions.launch(
                arrayOf("text/xml", "text/x-opml", "application/xml", "application/octet-stream", "*/*")
            )
        },
        onRescan = { vm.scanAllDownloads() },
        onBack = onBack
    )
    val importUri = pendingImport
    if (importUri != null && !confirmReplace) {
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text(SettingsCopy.IMPORT_SUBSCRIPTIONS) },
            text = {
                Text("Add missing podcasts (recommended), or replace your library with this file? Replace removes shows that are not in the backup and deletes their downloaded files.")
            },
            confirmButton = {
                TextButton({
                    vm.importSubscriptions(importUri, replace = false)
                    pendingImport = null
                }) { Text("Add missing") }
            },
            dismissButton = {
                Row {
                    TextButton({ confirmReplace = true }) { Text("Replace") }
                    TextButton({ pendingImport = null }) { Text("Cancel") }
                }
            }
        )
    }
    if (importUri != null && confirmReplace) {
        AlertDialog(
            onDismissRequest = { confirmReplace = false },
            title = { Text("Replace library?") },
            text = {
                Text("Podcasts that are not in this file will be removed, and their downloaded episode files will be deleted.")
            },
            confirmButton = {
                TextButton({
                    vm.importSubscriptions(importUri, replace = true)
                    pendingImport = null
                    confirmReplace = false
                }) { Text("Replace") }
            },
            dismissButton = {
                TextButton({ confirmReplace = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AboutSettingsRoute(onBack: () -> Unit) {
    AboutSettingsScreen(
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        onBack = onBack
    )
}

@Composable
fun SettingsHubScreen(
    playbackSummary: String,
    downloadsSummary: String,
    notificationsSummary: String,
    appearanceSummary: String,
    dataSummary: String,
    aboutSummary: String,
    onOpen: (String) -> Unit
) {
    SettingsScaffold(title = SettingsCopy.HUB_TITLE, onBack = null, testTag = SettingsTags.HUB) {
        settingsCard {
            CategoryRow(
                icon = Icons.Default.PlayArrow,
                title = SettingsCopy.PLAYBACK_TITLE,
                summary = playbackSummary,
                testTag = SettingsTags.category(SettingsRoutes.PLAYBACK),
                showDivider = true,
                onClick = { onOpen(SettingsRoutes.PLAYBACK) }
            )
            CategoryRow(
                icon = Icons.Default.Storage,
                title = SettingsCopy.DOWNLOADS_TITLE,
                summary = downloadsSummary,
                testTag = SettingsTags.category(SettingsRoutes.DOWNLOADS),
                showDivider = true,
                onClick = { onOpen(SettingsRoutes.DOWNLOADS) }
            )
            CategoryRow(
                icon = Icons.Default.Notifications,
                title = SettingsCopy.NOTIFICATIONS_TITLE,
                summary = notificationsSummary,
                testTag = SettingsTags.category(SettingsRoutes.NOTIFICATIONS),
                showDivider = true,
                onClick = { onOpen(SettingsRoutes.NOTIFICATIONS) }
            )
            CategoryRow(
                icon = Icons.Default.Palette,
                title = SettingsCopy.APPEARANCE_TITLE,
                summary = appearanceSummary,
                testTag = SettingsTags.category(SettingsRoutes.APPEARANCE),
                showDivider = true,
                onClick = { onOpen(SettingsRoutes.APPEARANCE) }
            )
            CategoryRow(
                icon = Icons.Default.SettingsBackupRestore,
                title = SettingsCopy.DATA_TITLE,
                summary = dataSummary,
                testTag = SettingsTags.category(SettingsRoutes.DATA),
                showDivider = true,
                onClick = { onOpen(SettingsRoutes.DATA) }
            )
            CategoryRow(
                icon = Icons.Default.Info,
                title = SettingsCopy.ABOUT_TITLE,
                summary = aboutSummary,
                testTag = SettingsTags.category(SettingsRoutes.ABOUT),
                showDivider = false,
                onClick = { onOpen(SettingsRoutes.ABOUT) }
            )
        }
    }
}

@Composable
fun PlaybackSettingsScreen(
    skipBackSec: Int,
    skipForwardSec: Int,
    speed: Float,
    trimSilence: Boolean,
    onSkipBack: (Int) -> Unit,
    onSkipForward: (Int) -> Unit,
    onSpeed: (Float) -> Unit,
    onTrimSilence: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    SettingsScaffold(title = SettingsCopy.PLAYBACK_TITLE, onBack = onBack, testTag = SettingsTags.PLAYBACK) {
        settingsCard {
            SkipIntervalBlock(SettingsCopy.SKIP_BACK, skipBackSec, onSkipBack)
            SettingsDivider()
            SkipIntervalBlock(SettingsCopy.SKIP_FORWARD, skipForwardSec, onSkipForward)
            SettingsDivider()
            SpeedStepperRow(speed, onSpeed)
            SettingsDivider()
            SwitchRow(
                title = SettingsCopy.TRIM_SILENCE,
                subtitle = SettingsCopy.TRIM_SILENCE_SUBTITLE,
                checked = trimSilence,
                onChecked = onTrimSilence
            )
        }
        sectionLabel(SettingsCopy.ADVANCED)
        settingsCard {
            ValueRow(
                title = SettingsCopy.HEADSET_BUTTONS,
                value = SettingsCopy.HEADSET_SUMMARY,
                chevron = false
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DownloadsSettingsScreen(
    folderLabel: String,
    autoDownload: Boolean,
    autoDelete: Boolean,
    syncFrequency: String,
    syncHour: Int,
    syncMinute: Int,
    namingPattern: String,
    onChooseFolder: () -> Unit,
    onAutoDownload: (Boolean) -> Unit,
    onAutoDelete: (Boolean) -> Unit,
    onSync: (String, Int, Int) -> Unit,
    onNaming: (String) -> Unit,
    onBack: () -> Unit
) {
    SettingsScaffold(title = SettingsCopy.DOWNLOADS_TITLE, onBack = onBack, testTag = SettingsTags.DOWNLOADS) {
        settingsCard {
            ValueRow(SettingsCopy.DOWNLOAD_FOLDER, value = folderLabel, onClick = onChooseFolder)
            SettingsDivider()
            SwitchRow(
                title = SettingsCopy.AUTO_DOWNLOAD,
                subtitle = SettingsCopy.AUTO_DOWNLOAD_SUBTITLE,
                checked = autoDownload,
                onChecked = onAutoDownload
            )
            SettingsDivider()
            SwitchRow(
                title = SettingsCopy.AUTO_DELETE,
                subtitle = SettingsCopy.AUTO_DELETE_SUBTITLE,
                checked = autoDelete,
                onChecked = onAutoDelete
            )
        }
        sectionLabel(SettingsCopy.CHECK_EPISODES)
        settingsCard {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChoiceChip("Every 30 min", syncFrequency == SettingsEntity.SYNC_30_MIN) {
                        onSync(SettingsEntity.SYNC_30_MIN, syncHour, syncMinute)
                    }
                    ChoiceChip("Hourly", syncFrequency == SettingsEntity.SYNC_HOURLY) {
                        onSync(SettingsEntity.SYNC_HOURLY, syncHour, syncMinute)
                    }
                    ChoiceChip("Daily", syncFrequency == SettingsEntity.SYNC_DAILY) {
                        onSync(SettingsEntity.SYNC_DAILY, syncHour, syncMinute)
                    }
                    ChoiceChip("Weekly", syncFrequency == SettingsEntity.SYNC_WEEKLY) {
                        onSync(SettingsEntity.SYNC_WEEKLY, syncHour, syncMinute)
                    }
                }
                if (
                    syncFrequency == SettingsEntity.SYNC_DAILY ||
                    syncFrequency == SettingsEntity.SYNC_WEEKLY
                ) {
                    Spacer(Modifier.height(12.dp))
                    CompactTimeRow(syncHour, syncMinute) { hour, minute ->
                        onSync(syncFrequency, hour, minute)
                    }
                }
            }
        }
        sectionLabel(SettingsCopy.ADVANCED)
        settingsCard { NamingField(namingPattern, onNaming) }
    }
}

@Composable
fun NotificationsSettingsScreen(enabled: Boolean, onToggle: () -> Unit, onBack: () -> Unit) {
    SettingsScaffold(title = SettingsCopy.NOTIFICATIONS_TITLE, onBack = onBack, testTag = SettingsTags.NOTIFICATIONS) {
        settingsCard {
            ValueRow(
                title = SettingsCopy.NOTIFICATIONS_TITLE,
                value = SettingsCopy.notificationsSummary(enabled),
                onClick = onToggle
            )
        }
    }
}

@Composable
fun AppearanceSettingsScreen(
    themeMode: String,
    darkBackground: String,
    accent: String,
    layout: String,
    systemIsDark: Boolean,
    onThemeMode: (String) -> Unit,
    onDarkBackground: (String) -> Unit,
    onAccent: (String) -> Unit,
    onLayout: (String) -> Unit,
    onBack: () -> Unit
) {
    val resolvedDark = resolveOpenPodDark(themeMode, systemIsDark)
    val selectedAccent = OpenPodAccent.fromId(accent)
    SettingsScaffold(title = SettingsCopy.APPEARANCE_TITLE, onBack = onBack, testTag = SettingsTags.APPEARANCE) {
        settingsCard {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    SettingsCopy.THEME,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceChip(
                        SettingsCopy.THEME_DARK,
                        selected = SettingsEntity.sanitizeThemeMode(themeMode) == SettingsEntity.THEME_DARK
                    ) { onThemeMode(SettingsEntity.THEME_DARK) }
                    ChoiceChip(
                        SettingsCopy.THEME_LIGHT,
                        selected = themeMode == SettingsEntity.THEME_LIGHT
                    ) { onThemeMode(SettingsEntity.THEME_LIGHT) }
                    ChoiceChip(
                        SettingsCopy.THEME_SYSTEM,
                        selected = themeMode == SettingsEntity.THEME_SYSTEM
                    ) { onThemeMode(SettingsEntity.THEME_SYSTEM) }
                }
                if (resolvedDark) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        SettingsCopy.BACKGROUND,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceChip(
                            SettingsCopy.BACKGROUND_SOFT,
                            selected = SettingsEntity.sanitizeDarkBackground(darkBackground) ==
                                SettingsEntity.BACKGROUND_SOFT
                        ) { onDarkBackground(SettingsEntity.BACKGROUND_SOFT) }
                        ChoiceChip(
                            SettingsCopy.BACKGROUND_TRUE_BLACK,
                            selected = darkBackground == SettingsEntity.BACKGROUND_TRUE_BLACK
                        ) { onDarkBackground(SettingsEntity.BACKGROUND_TRUE_BLACK) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    SettingsCopy.ACCENT,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OpenPodAccent.all.forEach { option ->
                        val swatch = option.primary(resolvedDark)
                        val selected = option == selectedAccent
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(swatch)
                                .then(
                                    if (selected) {
                                        Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            CircleShape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { onAccent(option.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(option.onPrimary(resolvedDark))
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    SettingsCopy.LIBRARY_LAYOUT,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceChip(
                        SettingsCopy.LAYOUT_LIST,
                        selected = layout != SettingsEntity.LAYOUT_GRID
                    ) { onLayout(SettingsEntity.LAYOUT_LIST) }
                    ChoiceChip(
                        SettingsCopy.LAYOUT_GRID,
                        selected = layout == SettingsEntity.LAYOUT_GRID
                    ) { onLayout(SettingsEntity.LAYOUT_GRID) }
                }
            }
        }
    }
}

@Composable
fun DataSettingsScreen(
    onExport: () -> Unit,
    onImport: () -> Unit,
    onRescan: () -> Unit,
    onBack: () -> Unit
) {
    SettingsScaffold(title = SettingsCopy.DATA_TITLE, onBack = onBack, testTag = SettingsTags.DATA) {
        settingsCard {
            ValueRow(title = SettingsCopy.EXPORT_SUBSCRIPTIONS, onClick = onExport)
            SettingsDivider()
            ValueRow(title = SettingsCopy.IMPORT_SUBSCRIPTIONS, onClick = onImport)
            SettingsDivider()
            ValueRow(
                title = SettingsCopy.RESCAN_DOWNLOADS,
                subtitle = SettingsCopy.RESCAN_SUBTITLE,
                onClick = onRescan
            )
        }
    }
}

@Composable
fun AboutSettingsScreen(versionName: String, versionCode: Int, onBack: () -> Unit) {
    SettingsScaffold(title = SettingsCopy.ABOUT_TITLE, onBack = onBack, testTag = SettingsTags.ABOUT) {
        settingsCard {
            ValueRow(
                title = SettingsCopy.OPEN_POD,
                value = versionName,
                subtitle = "Build $versionCode",
                chevron = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScaffold(
    title: String,
    onBack: (() -> Unit)?,
    testTag: String,
    content: LazyListScope.() -> Unit
) {
    val listState = rememberLazyListState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        OpenPodScrollbarBox(listState, Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(testTag)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                content = {
                    content()
                    item { Spacer(Modifier.height(24.dp)) }
                }
            )
        }
    }
}

private fun LazyListScope.sectionLabel(text: String) {
    item {
        Text(
            text.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
        )
    }
}

private fun LazyListScope.settingsCard(content: @Composable () -> Unit) {
    item {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(SettingsCardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) { content() }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = SettingsHairline, thickness = 1.dp)
}

@Composable
private fun CategoryRow(
    icon: ImageVector,
    title: String,
    summary: String,
    testTag: String,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Column(Modifier.testTag(testTag)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Text(
                    summary,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }
        if (showDivider) SettingsDivider()
    }
}

@Composable
private fun ValueRow(
    title: String,
    value: String? = null,
    subtitle: String? = null,
    chevron: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier.fillMaxWidth().clickable(onClick = onClick)
    } else {
        Modifier.fillMaxWidth()
    }
    Row(
        modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (value != null) {
            Text(
                value,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(max = 160.dp)
                    .padding(start = 8.dp)
            )
        }
        if (chevron && onClick != null) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkipIntervalBlock(title: String, seconds: Int, onSeconds: (Int) -> Unit) {
    val preset = SettingsCopy.isSkipPreset(seconds)
    var customOpen by remember(seconds) { mutableStateOf(!preset) }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsCopy.SKIP_PRESETS.forEach { option ->
                ChoiceChip("${option} s", selected = preset && seconds == option) {
                    customOpen = false
                    onSeconds(option)
                }
            }
            ChoiceChip(SettingsCopy.CUSTOM, selected = !preset || customOpen) {
                customOpen = true
            }
        }
        if (!preset || customOpen) {
            Spacer(Modifier.height(8.dp))
            SecondsTextField(
                committedSec = seconds,
                onCommit = onSeconds,
                label = "Seconds",
                supporting = "${SkipIntervals.MIN_SEC}–${SkipIntervals.MAX_SEC}",
                minSec = SkipIntervals.MIN_SEC,
                maxSec = SkipIntervals.MAX_SEC
            )
        }
    }
}

@Composable
private fun SpeedStepperRow(speed: Float, onSpeed: (Float) -> Unit) {
    val current = clampSpeed(speed)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            SettingsCopy.SPEED,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        StepButton(
            icon = Icons.Default.Remove,
            contentDescription = "Slower",
            enabled = current > SPEED_MIN,
            onClick = { onSpeed(clampSpeed(current - SPEED_STEP)) }
        )
        Text(
            SettingsCopy.speedLabel(current),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        StepButton(
            icon = Icons.Default.Add,
            contentDescription = "Faster",
            enabled = current < SPEED_MAX,
            onClick = { onSpeed(clampSpeed(current + SPEED_STEP)) }
        )
    }
}

@Composable
private fun StepButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val color = MaterialTheme.colorScheme.secondary.copy(alpha = if (enabled) 1f else 0.35f)
    Box(
        Modifier
            .size(32.dp)
            .border(1.dp, color, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = color, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun CompactTimeRow(hour: Int, minute: Int, onTime: (Int, Int) -> Unit) {
    Column {
        Text("Time", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepButton(Icons.Default.Remove, "Earlier hour", hour > 0) {
                onTime(hour - 1, minute)
            }
            Text(
                hour.coerceIn(0, 23).toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            StepButton(Icons.Default.Add, "Later hour", hour < 23) {
                onTime(hour + 1, minute)
            }
            Text(":", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp))
            StepButton(Icons.Default.Remove, "Earlier minute", minute > 0) {
                onTime(hour, minute - 1)
            }
            Text(
                minute.coerceIn(0, 59).toString().padStart(2, '0'),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            StepButton(Icons.Default.Add, "Later minute", minute < 59) {
                onTime(hour, minute + 1)
            }
        }
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val secondary = MaterialTheme.colorScheme.secondary
    Box(
        Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) primary else Color.Transparent)
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, secondary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            color = if (selected) onPrimary else onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun NamingField(pattern: String, onCommit: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    var text by remember(pattern) { mutableStateOf(pattern) }
    LaunchedEffect(pattern) {
        if (!focused) text = pattern
    }
    fun apply() {
        val next = text.trim().ifBlank { pattern }
        text = next
        if (next != pattern) onCommit(next)
    }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .onFocusChanged { state ->
                val nowFocused = state.isFocused
                if (focused && !nowFocused) apply()
                focused = nowFocused
            },
        label = { Text(SettingsCopy.FILE_NAMING) },
        supportingText = { Text(SettingsCopy.FILE_NAMING_TOKENS) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            apply()
            focusManager.clearFocus()
        })
    )
}

@Composable
internal fun SecondsTextField(
    committedSec: Int,
    onCommit: (Int) -> Unit,
    label: String,
    supporting: String,
    minSec: Int,
    maxSec: Int,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    var text by remember(committedSec) { mutableStateOf(committedSec.toString()) }
    LaunchedEffect(committedSec) {
        if (!focused) text = committedSec.toString()
    }
    fun apply() {
        val parsed = text.trim().toIntOrNull()
        val next = (parsed ?: committedSec).coerceIn(minSec, maxSec)
        text = next.toString()
        if (next != committedSec) onCommit(next)
    }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            text = raw.filter { it.isDigit() }.take(4)
        },
        modifier = modifier.onFocusChanged { state ->
            val nowFocused = state.isFocused
            if (focused && !nowFocused) apply()
            focused = nowFocused
        },
        label = { Text(label) },
        supportingText = { Text(supporting) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            apply()
            focusManager.clearFocus()
        })
    )
}

@Composable
private fun ObserveResumes(onResume: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, onResume) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onResume()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
