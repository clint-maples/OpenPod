package com.openpod.app.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.app.NotificationManagerCompat
import com.openpod.app.BuildConfig
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.flowOf
import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.data.db.PlaylistEntity
import com.openpod.app.data.db.PodcastEntity
import com.openpod.app.data.db.SettingsEntity
import com.openpod.app.data.search.DiscoverQuery
import com.openpod.app.data.search.TopPodcastGenres
import com.openpod.app.download.DownloadProgress
import com.openpod.app.download.EpisodeDownloadAction
import com.openpod.app.download.episodeDownloadAction
import com.openpod.app.playback.EpisodeTrim
import com.openpod.app.playback.PlayerService
import com.openpod.app.playback.SkipIntervals
import com.openpod.app.playback.SleepTimerLogic
import com.openpod.app.playback.formatVolumeBoost
import com.openpod.app.share.EpisodeShare

/** One-line flip for the Playback Effects sheet accent border. */
private const val PLAYBACK_EFFECTS_SHEET_BORDER = true

/** One-line flip for the dimmed scrim behind the Playback Effects sheet. */
private const val PLAYBACK_EFFECTS_SHEET_SCRIM = true

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenPodRoot(vm: AppViewModel) {
    val nav = rememberNavController()
    val snack = remember { SnackbarHostState() }
    val message by vm.message.collectAsState()
    val busy by vm.busy.collectAsState()
    val player by vm.player.collectAsState()
    LaunchedEffect(message) {
        val text = message ?: return@LaunchedEffect
        snack.showSnackbar(text)
        vm.consumeMessage()
    }
    val back = nav.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                Spacer(Modifier.height(NowPlayingChrome.FOOTER_BAND_GAP_DP.dp))
                HorizontalDivider(
                    thickness = NowPlayingChrome.NAV_ACCENT_LINE_DP.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                NavItem("Library", Icons.Default.LibraryMusic, back.startsWith("library") || back.startsWith("podcast") || back.startsWith("episode")) {
                    nav.navigate("library") { popUpTo(nav.graph.findStartDestination().id); launchSingleTop = true }
                }
                NavItem("Discover", Icons.Default.Explore, back.startsWith("discover")) {
                    nav.navigate("discover") { launchSingleTop = true }
                }
                NavItem("Playlists", Icons.Default.QueueMusic, back.startsWith("playlist")) {
                    nav.navigate("playlists") { launchSingleTop = true }
                }
                NavItem(
                    "Now playing",
                    Icons.Default.PlayArrow,
                    back == "player" || back == NowPlayingChrome.EQUALIZER_ROUTE || back == "upnext"
                ) {
                    nav.navigate("player") { launchSingleTop = true }
                }
                NavItem("Settings", Icons.Default.Settings, back == "settings") {
                    nav.navigate("settings") { launchSingleTop = true }
                }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize()
        ) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            NavHost(navController = nav, startDestination = "library", modifier = Modifier.weight(1f)) {
                composable("library") { LibraryScreen(vm, { nav.navigate("podcast/$it") }, { vm.refreshAll() }) }
                composable("discover") { DiscoverScreen(vm) }
                composable("playlists") {
                    PlaylistsScreen(vm, { nav.navigate("playlist/$it") })
                }
                composable("playlist/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                    PlaylistDetailScreen(vm, it.arguments!!.getLong("id"))
                }
                composable("podcast/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                    PodcastScreen(
                        vm,
                        it.arguments!!.getLong("id"),
                        onEpisode = { epId -> nav.navigate("episode/$epId") },
                        onSettings = { podcastId -> nav.navigate("podcast/$podcastId/settings") },
                        onRemoved = { nav.popBackStack() }
                    )
                }
                composable(
                    "podcast/{id}/settings",
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) {
                    PodcastSettingsScreen(
                        vm,
                        it.arguments!!.getLong("id"),
                        onBack = { nav.popBackStack() }
                    )
                }
                composable("episode/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                    EpisodeScreen(vm, it.arguments!!.getLong("id")) { nav.navigate("player") }
                }
                composable("player") {
                    PlayerScreen(
                        vm,
                        onEq = {
                            val route = NowPlayingChrome.equalizerRoute(NowPlayingChrome.BottomAction.Equalizer)
                                ?: NowPlayingChrome.EQUALIZER_ROUTE
                            nav.navigate(route) { launchSingleTop = true }
                        },
                        onOpenPodcast = { podcastId -> nav.navigate("podcast/$podcastId") }
                    )
                }
                composable("upnext") {
                    UpNextScreen(vm) { nav.navigate("player") }
                }
                composable(NowPlayingChrome.EQUALIZER_ROUTE) {
                    EqScreen(vm, onBack = { nav.popBackStack() })
                }
                composable("settings") { SettingsScreen(vm) }
            }
            if (MiniPlayerVisibility.shouldShow(back, player.episodeId)) {
                MiniPlayer(
                    player,
                    onOpen = { nav.navigate("player") },
                    onUpNext = { nav.navigate("upnext") },
                    onTogglePlay = { vm.togglePlay() },
                    onSkipBack = { vm.skip(false) },
                    onSkipForward = { vm.skip(true) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.NavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, textAlign = TextAlign.Center) }
    )
}

@Composable
internal fun MiniPlayer(
    player: com.openpod.app.playback.PlayerUiState,
    onOpen: () -> Unit,
    onUpNext: () -> Unit,
    onTogglePlay: () -> Unit,
    onSkipBack: () -> Unit,
    onSkipForward: () -> Unit
) {
    val nowPlayingLabel = listOf(player.podcastTitle, player.title)
        .filter { it.isNotBlank() }
        .joinToString(" — ")
        .ifBlank { "Now playing" }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(MiniPlayerVisibility.containerTag)
            .clickable(onClick = onOpen, onClickLabel = "Open now playing")
    ) {
        Column {
            HorizontalDivider()
            val miniDuration = player.effectiveDurationMs.takeIf { it > 0L } ?: player.durationMs
            if (miniDuration > 0L) {
                LinearProgressIndicator(
                    progress = { (player.positionMs.toFloat() / miniDuration).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val transportWidth = 48.dp * 3
                val sideWidth = ((maxWidth - transportWidth) / 2).coerceAtLeast(48.dp)
                Row(
                    Modifier
                        .align(Alignment.CenterStart)
                        .width(sideWidth),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        player.artwork,
                        nowPlayingLabel,
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            player.title.ifBlank { "Now playing" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (player.podcastTitle.isNotBlank()) {
                            Text(
                                player.podcastTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (player.sleepActive) {
                            Text(
                                "Sleep ${SleepTimerLogic.formatRemaining(player.sleepRemainingMs)}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Row(
                    Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SkipIconButton(
                        seconds = player.skipBackSec,
                        forward = false,
                        onClick = onSkipBack,
                        compact = true,
                        showAmount = false
                    )
                    IconButton(onTogglePlay) {
                        Icon(if (player.playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Play")
                    }
                    SkipIconButton(
                        seconds = player.skipForwardSec,
                        forward = true,
                        onClick = onSkipForward,
                        compact = true,
                        showAmount = false
                    )
                }
                IconButton(
                    onClick = onUpNext,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Default.QueueMusic, "Up Next")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(vm: AppViewModel, onOpen: (Long) -> Unit, onRefresh: () -> Unit) {
    val podcasts by vm.podcasts.collectAsState()
    val settings by vm.settings.collectAsState()
    val grid = settings.libraryLayout == SettingsEntity.LAYOUT_GRID
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Library") }, actions = {
                IconButton({
                    vm.setLibraryLayout(
                        if (grid) SettingsEntity.LAYOUT_LIST else SettingsEntity.LAYOUT_GRID
                    )
                }) {
                    Icon(
                        if (grid) Icons.Default.ViewList else Icons.Default.GridView,
                        if (grid) "List layout" else "Grid layout"
                    )
                }
                IconButton(onRefresh) { Icon(Icons.Default.Refresh, "Refresh") }
            })
        }
    ) { padding ->
        if (podcasts.isEmpty()) {
            Text("Add a show from Discover, or paste an RSS URL.", Modifier.padding(padding).padding(24.dp))
        } else if (grid) {
            LibraryPodcastGrid(
                podcasts = podcasts,
                onOpen = onOpen,
                onReorder = { vm.reorderLibrary(it) },
                modifier = Modifier.padding(padding).fillMaxSize()
            )
        } else {
            val listState = rememberLazyListState()
            LazyColumn(Modifier.padding(padding).openPodScrollbar(listState)) {
                items(podcasts, key = { it.id }) { podcast ->
                    PodcastRow(
                        podcast,
                        onClick = { onOpen(podcast.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PodcastRow(podcast: PodcastEntity, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(podcast.imageUrl, podcast.title, Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(podcast.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(podcast.author, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoverScreen(vm: AppViewModel) {
    var query by remember { mutableStateOf("") }
    val results by vm.searchResults.collectAsState()
    val charts by vm.topCharts.collectAsState()
    LaunchedEffect(Unit) { vm.loadTopCharts(null) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    fun hideKeyboard() {
        keyboard?.hide()
        focusManager.clearFocus()
    }
    fun submit() {
        hideKeyboard()
        val raw = query.trim()
        if (raw.isEmpty()) return
        if (DiscoverQuery.looksLikeFeedUrl(raw)) {
            vm.subscribe(DiscoverQuery.normalizeFeedUrl(raw))
        } else {
            vm.search(raw)
        }
    }
    fun clearQuery() {
        hideKeyboard()
        query = ""
        vm.clearSearch()
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Discover") }) }) { padding ->
        val listState = rememberLazyListState()
        LazyColumn(Modifier.padding(padding).openPodScrollbar(listState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedTextField(
                    query,
                    { query = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Search podcasts or paste RSS URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submit() }),
                    trailingIcon = {
                        if (query.isNotEmpty() || results.isNotEmpty()) {
                            IconButton(onClick = { clearQuery() }) {
                                Text("x", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                )
            }
            items(results, key = { it.feedUrl }) { hit ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(hit.imageUrl, hit.title, Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(hit.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(hit.author, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                    TextButton({ vm.subscribe(hit.feedUrl) }) { Text("Add") }
                }
            }
            if (query.isBlank() && results.isEmpty()) {
                item {
                    Text("Top Podcasts", style = MaterialTheme.typography.titleMedium)
                }
                if (charts.showGenres) {
                    item {
                        var genreListOpen by remember { mutableStateOf(false) }
                        val selectedName = TopPodcastGenres.all
                            .find { it.id == charts.selectedGenreId }
                            ?.name
                            ?: "All categories"
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { genreListOpen = true }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Categories",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(selectedName, style = MaterialTheme.typography.bodyMedium)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        if (genreListOpen) {
                            OpenPodBottomSheet(onDismiss = { genreListOpen = false }) {
                                CategoryPicker(
                                    selectedGenreId = charts.selectedGenreId,
                                    onSelect = { genreId ->
                                        genreListOpen = false
                                        vm.loadTopCharts(genreId)
                                    }
                                )
                            }
                        }
                    }
                }
                if (charts.needsRetry && charts.shows.isEmpty()) {
                    item {
                        Text("Couldn't load Top Podcasts.")
                        TextButton({ vm.loadTopCharts(null) }) { Text("Retry") }
                    }
                } else {
                    items(charts.shows, key = { it.appleId }) { show ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                show.artworkUrl,
                                show.name,
                                Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(show.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(show.artistName, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            TextButton({ vm.subscribeApplePodcast(show.appleId) }) { Text("Add") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun PodcastScreen(
    vm: AppViewModel,
    podcastId: Long,
    onEpisode: (Long) -> Unit,
    onSettings: (Long) -> Unit,
    onRemoved: () -> Unit
) {
    val podcasts by vm.podcasts.collectAsState()
    val settings by vm.settings.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val podcast = podcasts.find { it.id == podcastId }
    val episodes by vm.episodes(podcastId, settings.showArchived).collectAsState(initial = emptyList())
    val player by vm.player.collectAsState()
    val queuedIds by vm.queuedIds.collectAsState()
    val progressMap by vm.downloadProgress.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var episodeQuery by remember { mutableStateOf("") }
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var moveDialog by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf(false) }
    if (podcast == null) {
        Text("Podcast not found")
        return
    }
    val visibleEpisodes = EpisodeTitleFilter.filter(episodes, episodeQuery)
    val selectedEpisodes = visibleEpisodes.filter { it.id in selected }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selecting) "${selected.size} selected" else podcast.title,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    if (selecting) {
                        IconButton({
                            selecting = false
                            selected = emptySet()
                        }) { Icon(Icons.Default.Close, "Cancel") }
                    }
                },
                actions = {
                    if (selecting) {
                        IconButton({
                            selected = if (selected.size == visibleEpisodes.size) {
                                emptySet()
                            } else {
                                visibleEpisodes.map { it.id }.toSet()
                            }
                        }) { Icon(Icons.Default.SelectAll, "Select all") }
                    } else {
                        IconButton({ vm.refresh(podcastId) }) { Icon(Icons.Default.Refresh, "Refresh") }
                        IconButton({ confirmRemove = true }) { Icon(Icons.Default.Delete, "Remove from library") }
                        IconButton({ onSettings(podcastId) }) { Icon(Icons.Default.Settings, "Podcast settings") }
                    }
                }
            )
        },
        bottomBar = {
            if (selecting && selected.isNotEmpty()) {
                val archiveNext = selectedEpisodes.any { !it.archived }
                BottomAppBar {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        IconButton({ vm.downloadMany(selected.toList()); selecting = false; selected = emptySet() }) {
                            Icon(Icons.Default.Download, "Download")
                        }
                        IconButton({ vm.deleteDownloadsMany(selected.toList()); selecting = false; selected = emptySet() }) {
                            Icon(Icons.Default.Delete, "Delete files")
                        }
                        IconButton({
                            vm.archiveMany(selected.toList(), archived = archiveNext)
                            selecting = false
                            selected = emptySet()
                        }) {
                            Icon(
                                if (archiveNext) Icons.Default.Archive else Icons.Default.Unarchive,
                                if (archiveNext) "Archive" else "Unarchive"
                            )
                        }
                        IconButton({ moveDialog = true }) { Icon(Icons.Default.PlaylistAdd, "Move to playlist") }
                    }
                }
            }
        }
    ) { padding ->
        val listState = rememberLazyListState()
        LazyColumn(Modifier.padding(padding).openPodScrollbar(listState)) {
            item {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(podcast.imageUrl, podcast.title, Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(podcast.author)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Show archived", Modifier.weight(1f))
                            Switch(settings.showArchived, { vm.setShowArchived(it) })
                        }
                    }
                }
                Text(stripHtml(podcast.description), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, maxLines = 6)
                OutlinedTextField(
                    episodeQuery,
                    { episodeQuery = it },
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text("Search episodes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboard?.hide()
                        focusManager.clearFocus()
                    }),
                    trailingIcon = {
                        if (episodeQuery.isNotEmpty()) {
                            IconButton(onClick = { episodeQuery = "" }) {
                                Text("x", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                )
                if (!selecting) {
                    TextButton({
                        selecting = true
                        selected = emptySet()
                    }) { Text("Select episodes") }
                    TextButton({ vm.scanDownloads(podcastId) }) { Text("Find existing downloads") }
                } else {
                    FlowRow(
                        Modifier.padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton({ selected = visibleEpisodes.map { it.id }.toSet() }) { Text("Select all") }
                        TextButton({
                            selected = visibleEpisodes.filter { it.downloadStatus == EpisodeEntity.STATUS_DONE }
                                .map { it.id }.toSet()
                        }) { Text("Downloaded") }
                        TextButton({
                            selected = visibleEpisodes.filter { it.downloadStatus != EpisodeEntity.STATUS_DONE }
                                .map { it.id }.toSet()
                        }) { Text("Not downloaded") }
                        TextButton({
                            selected = EpisodeRowChrome.favoriteEpisodeIds(visibleEpisodes)
                        }) { Text("Favorites") }
                        TextButton({ selected = emptySet() }) { Text("Clear") }
                    }
                }
            }
            if (visibleEpisodes.isEmpty() && episodeQuery.isNotBlank()) {
                item {
                    Text(
                        "No episodes match “${episodeQuery.trim()}”",
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(visibleEpisodes, key = { it.id }) { episode ->
                EpisodeRow(
                    episode = episode,
                    selecting = selecting,
                    checked = episode.id in selected,
                    progress = progressMap[episode.id],
                    player = player,
                    queued = episode.id in queuedIds,
                    onOpen = { onEpisode(episode.id) },
                    onPlay = { vm.playOrToggle(episode, podcast) },
                    onPlayNext = { vm.playNext(episode) },
                    onAddToQueue = { vm.addToQueue(episode) },
                    onDownload = { vm.download(episode.id) },
                    onDeleteDownload = { vm.deleteDownload(episode.id) },
                    onShare = {
                        context.startActivity(EpisodeShare.chooserIntent(podcast.title, episode))
                    },
                    onToggleFavorite = { vm.toggleFavorite(episode.id) },
                    onArchive = {
                        vm.archiveMany(listOf(episode.id), archived = !episode.archived)
                    },
                    onToggle = {
                        selected = if (episode.id in selected) selected - episode.id else selected + episode.id
                    },
                    onLongPress = {
                        selecting = true
                        selected = selected + episode.id
                    }
                )
            }
        }
    }
    if (moveDialog) {
        AlertDialog(
            onDismissRequest = { moveDialog = false },
            title = { Text("Move to playlist") },
            text = {
                Column {
                    playlists.filter { !it.isSmart }.forEach { list ->
                        Text(list.name, Modifier.fillMaxWidth().clickable {
                            vm.addManyToPlaylist(list.id, selected.toList())
                            moveDialog = false
                            selecting = false
                            selected = emptySet()
                        }.padding(8.dp))
                    }
                    if (playlists.none { !it.isSmart }) Text("Create a regular playlist first.")
                }
            },
            confirmButton = { TextButton({ moveDialog = false }) { Text("Close") } }
        )
    }
    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remove podcast") },
            text = {
                Text("Remove ${podcast.title} from your library? Downloaded episodes for this show will be deleted.")
            },
            confirmButton = {
                TextButton({
                    vm.unsubscribe(podcast.id)
                    confirmRemove = false
                    onRemoved()
                }) { Text("Remove") }
            },
            dismissButton = { TextButton({ confirmRemove = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PodcastSettingsScreen(vm: AppViewModel, podcastId: Long, onBack: () -> Unit) {
    val podcasts by vm.podcasts.collectAsState()
    val podcast = podcasts.find { it.id == podcastId }
    if (podcast == null) {
        Text("Podcast not found")
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Podcast settings", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        LazyColumn(
            Modifier.padding(padding).openPodScrollbar(listState).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        podcast.imageUrl,
                        podcast.title,
                        Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(podcast.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(podcast.author, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            item {
                Text("Downloads", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-download", Modifier.weight(1f))
                    Switch(podcast.autoDownload, { vm.setFlags(podcastId, autoDownload = it) })
                }
                Text(
                    "Download new episodes for this show when they appear.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-delete", Modifier.weight(1f))
                    Switch(podcast.autoDelete, { vm.setFlags(podcastId, autoDelete = it) })
                }
                Text(
                    "Delete the downloaded file when an episode finishes.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                Text("Library", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-archive played episodes", Modifier.weight(1f))
                    Switch(podcast.autoArchive, { vm.setFlags(podcastId, autoArchive = it) })
                }
                Text(
                    "Archive an episode when you finish listening so it leaves the available list. Episodes already played are archived when you turn this on.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                Text("Playback", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Skip the intro and/or outro on every episode of this show. Leave 0 to keep the original start and end.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                SecondsTextField(
                    committedSec = podcast.skipIntroSec,
                    onCommit = { vm.setFlags(podcastId, skipIntroSec = it) },
                    label = "Skip first seconds",
                    supporting = "Start every episode after this many seconds. 0 keeps the intro.",
                    minSec = EpisodeTrim.MIN_SEC,
                    maxSec = EpisodeTrim.MAX_SEC
                )
                SecondsTextField(
                    committedSec = podcast.skipOutroSec,
                    onCommit = { vm.setFlags(podcastId, skipOutroSec = it) },
                    label = "Skip last seconds",
                    supporting = "Treat the episode as finished this many seconds early. 0 keeps the outro.",
                    minSec = EpisodeTrim.MIN_SEC,
                    maxSec = EpisodeTrim.MAX_SEC
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpisodeRow(
    episode: EpisodeEntity,
    selecting: Boolean,
    checked: Boolean,
    progress: DownloadProgress?,
    player: com.openpod.app.playback.PlayerUiState,
    queued: Boolean,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onDownload: () -> Unit,
    onDeleteDownload: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onToggle: () -> Unit,
    onLongPress: () -> Unit
) {
    val downloading = isDownloadActive(episode, progress)
    val downloadAction = episodeDownloadAction(episode, downloading)
    val isCurrent = player.episodeId == episode.id
    val durationMs = if (isCurrent && player.durationMs > 0L) player.durationMs else episode.durationMs
    val positionMs = if (isCurrent) player.positionMs else episode.positionMs
    val completed = if (isCurrent) false else episode.completed
    val listingColor = if (EpisodeRowChrome.usesMutedText(completed)) {
        MaterialTheme.colorScheme.secondary
    } else {
        Color.Unspecified
    }
    Column(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = { if (selecting) onToggle() else onOpen() }, onLongClick = onLongPress)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selecting) {
                Checkbox(checked, { onToggle() })
                Spacer(Modifier.width(8.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        episode.title,
                        Modifier.weight(1f),
                        color = listingColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    EpisodeStatusMarks(episode)
                }
                Text(
                    EpisodeRowChrome.metaParts(
                        dateLabel = formatDate(episode.pubDate),
                        episodeNumber = episode.episodeNumber,
                        playbackLabel = EpisodeRowChrome.playbackLabel(durationMs, positionMs, completed),
                        downloadLabel = downloadStatusLabel(episode, progress),
                        queued = queued,
                        archived = episode.archived
                    ),
                    color = listingColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!selecting) {
                IconButton(
                    onClick = if (downloadAction == EpisodeDownloadAction.DELETE) onDeleteDownload else onDownload,
                    enabled = downloadAction != EpisodeDownloadAction.IN_PROGRESS
                ) {
                    when (downloadAction) {
                        EpisodeDownloadAction.IN_PROGRESS -> {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        }
                        EpisodeDownloadAction.DELETE -> {
                            Icon(Icons.Default.Delete, "Delete download")
                        }
                        EpisodeDownloadAction.DOWNLOAD -> {
                            Icon(Icons.Default.Download, "Download")
                        }
                    }
                }
                IconButton(onPlay) {
                    Icon(
                        if (isCurrent && player.playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (isCurrent && player.playing) "Pause" else "Play"
                    )
                }
                EpisodeOverflowMenu(
                    archived = episode.archived,
                    favorite = episode.favorite,
                    onPlayNext = onPlayNext,
                    onAddToQueue = onAddToQueue,
                    onShare = onShare,
                    onArchive = onArchive,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
        DownloadProgressBlock(episode, progress)
    }
}

@Composable
private fun EpisodeStatusMarks(episode: EpisodeEntity) {
    val downloaded = EpisodeRowChrome.isDownloaded(episode)
    val favorite = EpisodeRowChrome.isFavorite(episode)
    Spacer(Modifier.width(6.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (downloaded) {
            Icon(
                Icons.Default.CheckCircle,
                "Downloaded",
                Modifier.size(16.dp),
                tint = EpisodeRowChrome.downloadedCheckColor()
            )
        } else {
            Spacer(Modifier.size(16.dp))
        }
        Spacer(Modifier.height(2.dp))
        Icon(
            if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            EpisodeRowChrome.favoriteMarkContentDescription(favorite),
            Modifier.size(16.dp),
            tint = EpisodeRowChrome.favoriteHeartColor(favorite)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeScreen(vm: AppViewModel, episodeId: Long, onPlay: () -> Unit) {
    val episode by vm.episode(episodeId).collectAsState(initial = null)
    val podcasts by vm.podcasts.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val chapters by vm.chapters.collectAsState()
    val transcript by vm.transcript.collectAsState()
    val progressMap by vm.downloadProgress.collectAsState()
    var addPlaylist by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val current = episode
    LaunchedEffect(episodeId) {
        vm.repo.db.episodes().byId(episodeId)?.let { vm.loadExtras(it) }
    }
    if (current == null) {
        Text("Loading…")
        return
    }
    val podcast = podcasts.find { it.id == current.podcastId }
    val progress = progressMap[current.id]
    val downloading = isDownloadActive(current, progress)
    Scaffold(
        topBar = { TopAppBar(title = { Text(current.title, maxLines = 1) }) },
        floatingActionButton = {
            FloatingActionButton({
                if (podcast != null) vm.play(current, podcast)
                onPlay()
            }) { Icon(Icons.Default.PlayArrow, "Play") }
        }
    ) { padding ->
        val listState = rememberLazyListState()
        LazyColumn(Modifier.padding(padding).openPodScrollbar(listState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(current.title, style = MaterialTheme.typography.titleLarge)
                Text(formatDate(current.pubDate))
                if (current.episodeNumber != null) Text("Episode ${current.episodeNumber}")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button({ vm.download(current.id) }, enabled = !downloading) {
                        if (downloading) Text("Downloading…") else Text("Download")
                    }
                    Spacer(Modifier.width(8.dp))
                    if (current.localUri.isNotBlank()) TextButton({ vm.deleteDownload(current.id) }) { Text("Remove file") }
                    IconButton({ addPlaylist = true }) { Icon(Icons.Default.PlaylistAdd, "Playlist") }
                    IconButton({ vm.playNext(current) }) { Icon(Icons.Default.QueuePlayNext, "Play next") }
                    IconButton({ vm.addToQueue(current) }) { Icon(Icons.Default.PlaylistPlay, "Add to Up Next") }
                    IconButton({
                        context.startActivity(
                            EpisodeShare.chooserIntent(podcast?.title.orEmpty(), current)
                        )
                    }) { Icon(Icons.Default.Share, "Share episode") }
                    IconButton({ vm.archiveMany(listOf(current.id), !current.archived) }) {
                        Icon(
                            if (current.archived) Icons.Default.Unarchive else Icons.Default.Archive,
                            if (current.archived) "Unarchive" else "Archive"
                        )
                    }
                }
                DownloadProgressBlock(current, progress)
                Text(
                    buildString {
                        append(downloadStatusLabel(current, progress) ?: "Not downloaded")
                        if (current.fileName.isNotBlank()) {
                            append(" · ")
                            append(current.fileName)
                        }
                    }
                )
            }
            if (current.description.isNotBlank()) {
                item {
                    Text("From the publisher", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    PublisherText(current.description)
                }
            }
            if (chapters.isNotEmpty()) {
                item { Text("Chapters", style = MaterialTheme.typography.titleMedium) }
                items(chapters) { chapter ->
                    Text(
                        "${formatMs(chapter.startMs)}  ${chapter.title}",
                        Modifier.clickable {
                            vm.seek(chapter.startMs)
                            if (podcast != null) vm.play(current, podcast)
                            onPlay()
                        }.padding(vertical = 6.dp)
                    )
                }
            }
            if (transcript.isNotEmpty()) {
                item { Text("Transcript", style = MaterialTheme.typography.titleMedium) }
                items(transcript.take(400)) { line ->
                    val prefix = line.startMs?.let { formatMs(it) + "  " }.orEmpty()
                    Text(prefix + line.text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
    if (addPlaylist) {
        AlertDialog(
            onDismissRequest = { addPlaylist = false },
            title = { Text("Add to playlist") },
            text = {
                Column {
                    playlists.forEach { list ->
                        Text(list.name, Modifier.fillMaxWidth().clickable {
                            vm.addToPlaylist(list.id, current.id)
                            addPlaylist = false
                        }.padding(8.dp))
                    }
                    if (playlists.isEmpty()) Text("Create a playlist first.")
                }
            },
            confirmButton = { TextButton({ addPlaylist = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun PlayerScreen(
    vm: AppViewModel,
    onEq: () -> Unit,
    onOpenPodcast: (Long) -> Unit
) {
    val player by vm.player.collectAsState()
    val settings by vm.settings.collectAsState()
    val episode by vm.episode(player.episodeId).collectAsState(initial = null)
    val chapters by vm.chapters.collectAsState()
    val transcript by vm.transcript.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(player.episodeId) {
        if (player.episodeId != 0L) {
            vm.repo.db.episodes().byId(player.episodeId)?.let { vm.loadExtras(it) }
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (player.episodeId != 0L) {
                NowPlayingActionBar(
                    player = player,
                    episode = episode,
                    skipBackSec = settings.skipBackSec,
                    skipForwardSec = settings.skipForwardSec,
                    onEq = onEq,
                    onShare = {
                        val current = episode ?: return@NowPlayingActionBar
                        context.startActivity(
                            EpisodeShare.chooserIntent(player.podcastTitle, current)
                        )
                    },
                    vm = vm
                )
            }
        }
    ) { padding ->
        if (player.episodeId == 0L) {
            Text(
                "Nothing playing yet. Pick an episode from your library, and OpenPod will remember it here.",
                Modifier.padding(padding).padding(24.dp)
            )
            return@Scaffold
        }
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val horizontalPad = 20.dp
            val artSide = minOf(
                (maxWidth - horizontalPad * 2).coerceAtLeast(96.dp),
                (maxHeight * NowPlayingChrome.ARTWORK_HEIGHT_FRACTION).coerceAtLeast(96.dp)
            )
            val listState = rememberLazyListState()
            LazyColumn(
                Modifier.fillMaxSize().openPodScrollbar(listState),
                contentPadding = PaddingValues(
                    start = horizontalPad,
                    end = horizontalPad,
                    top = 12.dp,
                    bottom = 12.dp + NowPlayingChrome.NOTES_END_PADDING_DP.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(8.dp))
                    val podcastId = player.podcastId.takeIf { it != 0L } ?: episode?.podcastId ?: 0L
                    val artworkModifier = Modifier.size(artSide).clip(RoundedCornerShape(20.dp))
                    AsyncImage(
                        player.artwork,
                        player.podcastTitle.ifBlank { player.title },
                        if (podcastId != 0L) {
                            artworkModifier.clickable(
                                onClickLabel = "Open ${player.podcastTitle.ifBlank { "podcast" }} episodes"
                            ) { onOpenPodcast(podcastId) }
                        } else {
                            artworkModifier
                        },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        player.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        player.podcastTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (chapters.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton({ vm.skipChapter(false) }) {
                                Icon(Icons.Default.ChevronLeft, "Previous chapter", Modifier.size(22.dp))
                            }
                            IconButton({ vm.skipChapter(true) }) {
                                Icon(Icons.Default.ChevronRight, "Next chapter", Modifier.size(22.dp))
                            }
                        }
                    }
                }
                }
                val current = episode
                if (current != null && current.description.isNotBlank()) {
                    item {
                        Column(Modifier.fillMaxWidth()) {
                            Text("From the publisher", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            PublisherText(current.description)
                        }
                    }
                }
                if (chapters.isNotEmpty()) {
                    item {
                        Text(
                            "Chapters",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(chapters) { chapter ->
                        Text(
                            "${formatMs(chapter.startMs)}  ${chapter.title}",
                            Modifier.fillMaxWidth().clickable { vm.seek(chapter.startMs) }.padding(vertical = 6.dp)
                        )
                    }
                }
                if (transcript.isNotEmpty()) {
                    item {
                        Text(
                            "Transcript",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(transcript.take(400)) { line ->
                        val prefix = line.startMs?.let { formatMs(it) + "  " }.orEmpty()
                        Text(
                            prefix + line.text,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingActionBar(
    player: com.openpod.app.playback.PlayerUiState,
    episode: EpisodeEntity?,
    skipBackSec: Int,
    skipForwardSec: Int,
    onEq: () -> Unit,
    onShare: () -> Unit,
    vm: AppViewModel
) {
    var speedMenuOpen by remember { mutableStateOf(false) }
    var sleepMenuOpen by remember { mutableStateOf(false) }
    val displayDuration = player.effectiveDurationMs.takeIf { it > 0L } ?: player.durationMs
    val transportSize = NowPlayingChrome.TRANSPORT_BUTTON_DP.dp
    val progressTransportColor = Color(NowPlayingChrome.PROGRESS_TRANSPORT_COLOR)
    val actionsColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val bandGap = Modifier.height(NowPlayingChrome.FOOTER_BAND_GAP_DP.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(
                WindowInsets.navigationBars.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
    ) {
        Surface(color = progressTransportColor, tonalElevation = 0.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(NowPlayingChrome.PROGRESS_BAND_HEIGHT_DP.dp)
                    .padding(horizontal = NowPlayingChrome.PROGRESS_HORIZONTAL_PADDING_DP.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatMs(player.positionMs),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(Modifier.width(NowPlayingChrome.PROGRESS_INLINE_GAP_DP.dp))
                NowPlayingSeekBar(
                    fraction = if (displayDuration > 0) {
                        (player.positionMs.toFloat() / displayDuration).coerceIn(0f, 1f)
                    } else {
                        0f
                    },
                    onFraction = { fraction ->
                        if (displayDuration > 0) vm.seek((fraction * displayDuration).toLong())
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(NowPlayingChrome.PROGRESS_INLINE_GAP_DP.dp))
                Text(
                    formatMs(displayDuration),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
        Spacer(bandGap)
        Surface(color = progressTransportColor, tonalElevation = 0.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(NowPlayingChrome.TRANSPORT_BAND_HEIGHT_DP.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SkipIconButton(
                    seconds = skipBackSec,
                    forward = false,
                    onClick = { vm.skip(false) },
                    controlSize = transportSize,
                    iconSize = NowPlayingChrome.TRANSPORT_SKIP_ICON_DP.dp
                )
                IconButton({ vm.togglePlay() }, Modifier.size(transportSize)) {
                    Icon(
                        if (player.playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (player.playing) "Pause" else "Play",
                        Modifier.size(NowPlayingChrome.TRANSPORT_PLAY_ICON_DP.dp)
                    )
                }
                SkipIconButton(
                    seconds = skipForwardSec,
                    forward = true,
                    onClick = { vm.skip(true) },
                    controlSize = transportSize,
                    iconSize = NowPlayingChrome.TRANSPORT_SKIP_ICON_DP.dp,
                    mirrorForwardGlyph = true
                )
            }
        }
        Spacer(bandGap)
        Surface(color = actionsColor, tonalElevation = 0.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(NowPlayingChrome.ACTIONS_BAND_HEIGHT_DP.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NowPlayingChrome.bottomActions.forEach { action ->
                    val slot = if (action == NowPlayingChrome.BottomAction.Favorite) {
                        Modifier
                    } else {
                        Modifier.weight(1f)
                    }
                    Box(
                        slot.clipToBounds(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (action) {
                            NowPlayingChrome.BottomAction.Speed -> {
                                val label = NowPlayingChrome.speedButtonLabel(player.speed)
                                val speedStyle = MaterialTheme.typography.titleMedium
                                TextButton(
                                    onClick = { speedMenuOpen = true },
                                    modifier = Modifier.heightIn(min = NowPlayingChrome.MIN_TAP_TARGET_DP.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        label,
                                        style = speedStyle.copy(
                                            fontSize = speedStyle.fontSize * NowPlayingChrome.BOTTOM_CONTROL_SCALE
                                        )
                                    )
                                }
                                if (speedMenuOpen) {
                                    val effectSettings by vm.settings.collectAsState()
                                    val effectPodcasts by vm.podcasts.collectAsState()
                                    val effectPodcast = effectPodcasts.find { it.id == player.podcastId }
                                    val hasOverride = effectPodcast?.hasPlaybackOverride() == true
                                    var forThisPodcast by remember(effectPodcast?.id, speedMenuOpen) {
                                        mutableStateOf(hasOverride)
                                    }
                                    val speed = if (forThisPodcast) {
                                        effectPodcast?.playbackSpeed ?: effectSettings.playbackSpeed
                                    } else {
                                        effectSettings.playbackSpeed
                                    }
                                    val silence = if (forThisPodcast) {
                                        effectPodcast?.skipSilence ?: effectSettings.skipSilence
                                    } else {
                                        effectSettings.skipSilence
                                    }
                                    val boost = if (forThisPodcast) {
                                        effectPodcast?.volumeBoost ?: effectSettings.volumeBoost
                                    } else {
                                        effectSettings.volumeBoost
                                    }
                                    OpenPodBottomSheet(onDismiss = { speedMenuOpen = false }) {
                                        PlaybackEffectsSheet(
                                            forThisPodcast = forThisPodcast,
                                            thisPodcastEnabled = effectPodcast != null,
                                            onForThisPodcast = { forThisPodcast = it },
                                            speed = speed,
                                            onSpeed = { vm.setScopedSpeed(forThisPodcast, it) },
                                            skipSilence = silence,
                                            onSkipSilence = { vm.setScopedSkipSilence(forThisPodcast, it) },
                                            volumeBoost = boost,
                                            onVolumeBoost = { vm.setScopedVolumeBoost(forThisPodcast, it) }
                                        )
                                    }
                                }
                            }
                            NowPlayingChrome.BottomAction.Equalizer -> {
                                val eqLabel = MaterialTheme.typography.labelMedium
                                TextButton(
                                    onClick = onEq,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = NowPlayingChrome.MIN_TAP_TARGET_DP.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Equalizer,
                                            "Graphic equalizer",
                                            modifier = Modifier.size(NowPlayingChrome.scaledDp(NowPlayingChrome.EQUALIZER_ICON_DP).dp)
                                        )
                                        Text(
                                            "EQ",
                                            style = eqLabel.copy(
                                                fontSize = eqLabel.fontSize * NowPlayingChrome.BOTTOM_CONTROL_SCALE
                                            )
                                        )
                                    }
                                }
                            }
                            NowPlayingChrome.BottomAction.Sleep -> {
                                IconButton(
                                    onClick = { sleepMenuOpen = true },
                                    modifier = Modifier.size(NowPlayingChrome.MIN_TAP_TARGET_DP.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Bedtime,
                                        if (player.sleepActive) {
                                            "Sleep timer, ${SleepTimerLogic.formatRemaining(player.sleepRemainingMs)} remaining"
                                        } else {
                                            "Sleep timer"
                                        },
                                        modifier = Modifier.size(NowPlayingChrome.scaledDp(NowPlayingChrome.STANDARD_ICON_DP).dp),
                                        tint = if (player.sleepActive) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                                DropdownMenu(
                                    expanded = sleepMenuOpen,
                                    onDismissRequest = { sleepMenuOpen = false }
                                ) {
                                    SleepTimerSection(
                                        player = player,
                                        vm = vm,
                                        modifier = Modifier.width(300.dp).padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            NowPlayingChrome.BottomAction.Share -> {
                                IconButton(
                                    onClick = onShare,
                                    enabled = episode != null,
                                    modifier = Modifier.size(NowPlayingChrome.MIN_TAP_TARGET_DP.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        "Share episode",
                                        modifier = Modifier.size(NowPlayingChrome.scaledDp(NowPlayingChrome.STANDARD_ICON_DP).dp)
                                    )
                                }
                            }
                            NowPlayingChrome.BottomAction.Favorite -> {
                                val favorite = episode?.favorite == true
                                IconButton(
                                    onClick = { episode?.let { vm.toggleFavorite(it.id) } },
                                    enabled = episode != null,
                                    modifier = Modifier.size(NowPlayingChrome.MIN_TAP_TARGET_DP.dp)
                                ) {
                                    Icon(
                                        if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        NowPlayingChrome.favoriteContentDescription(favorite),
                                        modifier = Modifier.size(NowPlayingChrome.scaledDp(NowPlayingChrome.STANDARD_ICON_DP).dp),
                                        tint = EpisodeRowChrome.favoriteHeartColor(favorite)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublisherText(html: String, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurface
    val linkColor = MaterialTheme.colorScheme.primary
    val sizeSp = MaterialTheme.typography.bodyMedium.fontSize.value
    val spanned = remember(html) { htmlToSpanned(html) }
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            TextView(context).apply {
                setTextIsSelectable(true)
                movementMethod = LinkMovementMethod.getInstance()
                setLineSpacing(4f, 1.15f)
            }
        },
        update = { view ->
            view.setTextColor(color.toArgb())
            view.setLinkTextColor(linkColor.toArgb())
            view.textSize = sizeSp
            view.text = spanned
        }
    )
}

@Composable
private fun PlaybackEffectsSheet(
    forThisPodcast: Boolean,
    thisPodcastEnabled: Boolean,
    onForThisPodcast: (Boolean) -> Unit,
    speed: Float,
    onSpeed: (Float) -> Unit,
    skipSilence: Boolean,
    onSkipSilence: (Boolean) -> Unit,
    volumeBoost: Float,
    onVolumeBoost: (Float) -> Unit
) {
    val iconTint = Color(0xFF8E8E93)
    val divider = Color.White.copy(alpha = 0.12f)
    val boostOn = volumeBoost > 0.02f
    var restoreBoost by remember { mutableStateOf(if (boostOn) volumeBoost else 0.5f) }
    LaunchedEffect(volumeBoost) {
        if (volumeBoost > 0.02f) restoreBoost = volumeBoost
    }
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxWidth()
            .openPodScrollbar(scrollState)
            .verticalScroll(scrollState)
            .padding(bottom = 28.dp)
    ) {
        Text(
            "Playback Effects",
            Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            style = MaterialTheme.typography.titleMedium
        )
        EffectScopeToggle(
            forThisPodcast = forThisPodcast,
            thisPodcastEnabled = thisPodcastEnabled,
            onForThisPodcast = onForThisPodcast
        )
        HorizontalDivider(color = divider)
        PlaybackEffectRow(
            icon = Icons.Outlined.Speed,
            title = "Speed",
            iconTint = iconTint
        ) {
            val current = clampSpeed(speed)
            CircleStepButton(
                icon = Icons.Default.Remove,
                contentDescription = "Slower",
                enabled = current > SPEED_MIN,
                onClick = { onSpeed(clampSpeed(current - SPEED_STEP)) }
            )
            Box(
                Modifier
                    .padding(horizontal = 6.dp)
                    .heightIn(min = 32.dp)
                    .border(1.dp, iconTint, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("${formatSpeed(current)}x", style = MaterialTheme.typography.bodyMedium)
            }
            CircleStepButton(
                icon = Icons.Default.Add,
                contentDescription = "Faster",
                enabled = current < SPEED_MAX,
                onClick = { onSpeed(clampSpeed(current + SPEED_STEP)) }
            )
        }
        HorizontalDivider(color = divider)
        PlaybackEffectRow(
            icon = Icons.Outlined.ContentCut,
            title = "Trim Silence",
            subtitle = "Reduces the length of an episode by trimming silence in conversations.",
            iconTint = iconTint
        ) {
            Switch(
                checked = skipSilence,
                onCheckedChange = onSkipSilence,
                modifier = Modifier.scale(0.85f)
            )
        }
        HorizontalDivider(color = divider)
        PlaybackEffectRow(
            icon = Icons.Outlined.GraphicEq,
            title = "Volume Boost",
            subtitle = "Voices sound louder",
            iconTint = iconTint
        ) {
            Switch(
                checked = boostOn,
                onCheckedChange = { enabled ->
                    onVolumeBoost(if (enabled) restoreBoost.coerceIn(0.05f, 1f) else 0f)
                },
                modifier = Modifier.scale(0.85f)
            )
        }
    }
}

@Composable
private fun CategoryPicker(
    selectedGenreId: String?,
    onSelect: (String?) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 480.dp)
            .openPodScrollbar(scrollState)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        Text(
            "Categories",
            Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.titleMedium
        )
        CategoryRow("All", selected = selectedGenreId == null) { onSelect(null) }
        TopPodcastGenres.all.forEach { genre ->
            CategoryRow(genre.name, selected = selectedGenreId == genre.id) { onSelect(genre.id) }
        }
    }
}

@Composable
private fun CategoryRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpenPodBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val accent = MaterialTheme.colorScheme.primary
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = shape,
        containerColor = Color(0xFF1C1C1E),
        contentColor = Color(0xFFF2F2F7),
        scrimColor = if (PLAYBACK_EFFECTS_SHEET_SCRIM) {
            Color.Black.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        },
        dragHandle = null
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(if (PLAYBACK_EFFECTS_SHEET_BORDER) Modifier.border(1.dp, accent, shape) else Modifier)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .background(Color(0xFFD1D1D6), RoundedCornerShape(2.dp))
                )
            }
            content()
        }
    }
}

@Composable
private fun EffectScopeToggle(
    forThisPodcast: Boolean,
    thisPodcastEnabled: Boolean,
    onForThisPodcast: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        EffectScopeButton("All podcasts", selected = !forThisPodcast, enabled = true) {
            onForThisPodcast(false)
        }
        EffectScopeButton("This podcast", selected = forThisPodcast, enabled = thisPodcastEnabled) {
            onForThisPodcast(true)
        }
    }
}

@Composable
private fun RowScope.EffectScopeButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        selected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    val text = when {
        !enabled -> Color(0xFF8E8E93)
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> Color(0xFFF2F2F7)
    }
    Box(
        Modifier
            .weight(1f)
            .height(36.dp)
            .background(background)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = text, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PlaybackEffectRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconTint: Color,
    subtitle: String? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = if (subtitle == null) 48.dp else 56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = iconTint)
            }
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}

@Composable
private fun CircleStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val color = Color(0xFF8E8E93).copy(alpha = if (enabled) 1f else 0.35f)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingSeekBar(
    fraction: Float,
    onFraction: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val inactive = SliderDefaults.colors().inactiveTrackColor
    val interactionSource = remember { MutableInteractionSource() }
    val trackH = NowPlayingChrome.PROGRESS_TRACK_DP.dp
    val thumbD = NowPlayingChrome.PROGRESS_THUMB_DP.dp
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Slider(
            value = fraction,
            onValueChange = onFraction,
            modifier = modifier.height(NowPlayingChrome.PROGRESS_BAND_HEIGHT_DP.dp),
            colors = SliderDefaults.colors(
                thumbColor = primary,
                activeTrackColor = primary,
                inactiveTrackColor = inactive
            ),
            interactionSource = interactionSource,
            thumb = {
                Box(
                    Modifier
                        .size(thumbD)
                        .background(primary, CircleShape)
                )
            },
            track = { state ->
                val range = state.valueRange
                val span = range.endInclusive - range.start
                val pos = if (span <= 0f) 0f else ((state.value - range.start) / span).coerceIn(0f, 1f)
                // Fill the 32 dp row and center the 3 dp track so it shares a vertical
                // center with the 12 dp thumb.
                Box(
                    Modifier.fillMaxWidth().fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxWidth().height(trackH)) {
                        val radius = size.height / 2
                        drawRoundRect(
                            color = inactive,
                            size = size,
                            cornerRadius = CornerRadius(radius, radius)
                        )
                        if (pos > 0f) {
                            drawRoundRect(
                                color = primary,
                                size = Size(size.width * pos, size.height),
                                cornerRadius = CornerRadius(radius, radius)
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun SkipIconButton(
    seconds: Int,
    forward: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false,
    showAmount: Boolean = true,
    controlSize: androidx.compose.ui.unit.Dp = if (compact) 48.dp else 72.dp,
    iconSize: androidx.compose.ui.unit.Dp = if (compact) 22.dp else 36.dp,
    mirrorForwardGlyph: Boolean = false
) {
    IconButton(onClick, Modifier.size(controlSize)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val glyph = if (forward && !mirrorForwardGlyph) {
                Icons.Default.FastForward
            } else {
                Icons.Default.Replay
            }
            Icon(
                glyph,
                SkipIntervals.contentDescription(seconds, forward),
                Modifier
                    .size(iconSize)
                    .then(
                        if (forward && mirrorForwardGlyph) {
                            Modifier.graphicsLayer { scaleX = -1f }
                        } else {
                            Modifier
                        }
                    )
            )
            if (showAmount) {
                Text(
                    SkipIntervals.compactLabel(seconds, forward),
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun SecondsTextField(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SleepTimerSection(
    player: com.openpod.app.playback.PlayerUiState,
    vm: AppViewModel,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Sleep timer", style = MaterialTheme.typography.titleMedium)
        }
        if (player.sleepActive) {
            val detail = buildString {
                append("Sleeps in ${SleepTimerLogic.formatRemaining(player.sleepRemainingMs)}")
                if (player.sleepEndOfEpisode && player.sleepDeadlineMs > 0L) {
                    append(" or at end of episode")
                } else if (player.sleepEndOfEpisode) {
                    append(" (end of episode)")
                }
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            TextButton({ vm.cancelSleepTimer() }) { Text("Cancel sleep timer") }
        } else {
            Text(
                "Fade out and pause at the end of the episode, after a set time, or whichever comes first.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = player.sleepEndOfEpisode,
                onClick = { vm.setSleepEndOfEpisode(!player.sleepEndOfEpisode) },
                label = { Text("End of episode") }
            )
            SleepTimerLogic.MINUTE_PRESETS.forEach { minutes ->
                FilterChip(
                    selected = player.sleepMinutes == minutes,
                    onClick = { vm.setSleepMinutes(minutes) },
                    label = { Text("${minutes} min") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpNextScreen(vm: AppViewModel, onOpenPlayer: () -> Unit) {
    val player by vm.player.collectAsState()
    val upcoming by vm.upcomingQueue.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Up Next") },
                actions = {
                    IconButton(
                        onClick = { confirmClear = true },
                        enabled = upcoming.isNotEmpty()
                    ) { Icon(Icons.Default.ClearAll, "Clear queue") }
                }
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().openPodScrollbar(listState),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "This queue is separate from playlists. Play next inserts at the front; Add to Up Next appends. When an episode finishes, the next item starts automatically.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (player.episodeId == 0L && upcoming.isEmpty()) {
                item {
                    Text("Nothing in the queue yet. Use Play next or Add to Up Next on an episode row.")
                }
            }
            if (player.episodeId != 0L) {
                item {
                    Text("Playing now", style = MaterialTheme.typography.titleMedium)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenPlayer, onClickLabel = "Open now playing")
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            player.artwork,
                            player.title,
                            Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(player.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(player.podcastTitle, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${formatMs(player.positionMs)} / ${formatMs(player.durationMs)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton({ vm.togglePlay() }) {
                            Icon(
                                if (player.playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                if (player.playing) "Pause" else "Play"
                            )
                        }
                    }
                }
            }
            if (upcoming.isNotEmpty()) {
                item { Text("Up next", style = MaterialTheme.typography.titleMedium) }
            } else if (player.episodeId != 0L) {
                item {
                    Text(
                        "Nothing else queued. Use Play next or Add to Up Next from an episode.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(upcoming, key = { it.episode.id }) { item ->
                val episode = item.episode
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        item.podcastImage.ifBlank { episode.imageUrl },
                        episode.title,
                        Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f).clickable { vm.playOrToggle(episode, item.podcastTitle, item.podcastImage) }) {
                        Text(episode.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(item.podcastTitle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        Text(
                            EpisodeRowChrome.playbackLabel(episode.durationMs, episode.positionMs, episode.completed),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(
                        onClick = { vm.moveQueueItem(episode.id, -1) },
                        enabled = upcoming.firstOrNull()?.episode?.id != episode.id
                    ) { Icon(Icons.Default.KeyboardArrowUp, "Move up") }
                    IconButton(
                        onClick = { vm.moveQueueItem(episode.id, 1) },
                        enabled = upcoming.lastOrNull()?.episode?.id != episode.id
                    ) { Icon(Icons.Default.KeyboardArrowDown, "Move down") }
                    IconButton({ vm.playOrToggle(episode, item.podcastTitle, item.podcastImage) }) {
                        Icon(Icons.Default.PlayArrow, "Play now")
                    }
                    IconButton({ vm.removeFromQueue(episode.id) }) {
                        Icon(Icons.Default.Delete, "Remove from queue")
                    }
                }
            }
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear queue?") },
            text = { Text("Remove every upcoming episode from Up Next. The episode playing now stays.") },
            confirmButton = {
                TextButton({
                    vm.clearQueue()
                    confirmClear = false
                }) { Text("Clear") }
            },
            dismissButton = { TextButton({ confirmClear = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EqScreen(vm: AppViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    var eqEpoch by remember { mutableStateOf(0) }
    val generation by (
        PlayerService.instance?.equalizer()?.generation ?: flowOf(0)
    ).collectAsState(initial = 0)
    val bands = remember(generation, eqEpoch, settings.eqBandsJson) {
        runCatching { PlayerService.instance?.equalizer()?.bands().orEmpty() }.getOrDefault(emptyList())
    }
    val range = remember(generation, eqEpoch) {
        val raw = runCatching { PlayerService.instance?.equalizer()?.range() }.getOrNull()
            ?: (0.toShort() to 0.toShort())
        if (raw.second > raw.first) raw else (-1500).toShort() to 1500.toShort()
    }
    var boost by remember(settings.volumeBoost, eqEpoch) { mutableStateOf(settings.volumeBoost.coerceIn(-1f, 1f)) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Graphic equalizer") },
                navigationIcon = {
                    IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().openPodScrollbar(listState),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Volume boost", style = MaterialTheme.typography.titleMedium)
                Text(
                    formatVolumeBoost(boost),
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = boost,
                    onValueChange = {
                        boost = it
                        vm.previewVolumeBoost(it)
                    },
                    onValueChangeFinished = { vm.setVolumeBoost(boost) },
                    valueRange = -1f..1f
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Quieter", style = MaterialTheme.typography.bodySmall)
                    Text("Default", style = MaterialTheme.typography.bodySmall)
                    Text("Louder", style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enabled", Modifier.weight(1f))
                    Switch(settings.eqEnabled, { vm.setEq(it) })
                }
            }
            item {
                Button({
                    vm.resetEq()
                    boost = 0f
                    eqEpoch++
                }) { Text("Reset") }
            }
            if (bands.isEmpty()) {
                item { Text("Start playback to load equalizer bands for this device.") }
            } else {
                items(bands, key = { it.index }) { band ->
                    var value by remember(eqEpoch, band.index, band.levelMilliBel) {
                        mutableStateOf(band.levelMilliBel.toFloat())
                    }
                    Text("${band.freqHz} Hz")
                    Slider(
                        value = value,
                        onValueChange = { value = it },
                        onValueChangeFinished = { vm.setEqBand(band.index, value.toInt().toShort()) },
                        valueRange = range.first.toFloat()..range.second.toFloat()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PlaylistsScreen(vm: AppViewModel, onOpen: (Long) -> Unit) {
    val lists by vm.playlists.collectAsState()
    var name by remember { mutableStateOf("") }
    var smart by remember { mutableStateOf(false) }
    var playFilter by remember { mutableStateOf(PlaylistEntity.PLAY_ALL) }
    var downloadFilter by remember { mutableStateOf(PlaylistEntity.DOWNLOAD_ALL) }
    Scaffold(topBar = { TopAppBar(title = { Text("Playlists") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("New playlist name") }, singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Smart playlist (filters)", Modifier.weight(1f))
                Switch(smart, { smart = it })
            }
            if (smart) {
                Text("Play status", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(playFilter == PlaylistEntity.PLAY_ALL, { playFilter = PlaylistEntity.PLAY_ALL }, label = { Text("All") })
                    FilterChip(playFilter == PlaylistEntity.PLAY_UNPLAYED, { playFilter = PlaylistEntity.PLAY_UNPLAYED }, label = { Text("Unplayed") })
                    FilterChip(playFilter == PlaylistEntity.PLAY_IN_PROGRESS, { playFilter = PlaylistEntity.PLAY_IN_PROGRESS }, label = { Text("In progress") })
                    FilterChip(playFilter == PlaylistEntity.PLAY_PLAYED, { playFilter = PlaylistEntity.PLAY_PLAYED }, label = { Text("Played") })
                }
                Text("Download status", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(downloadFilter == PlaylistEntity.DOWNLOAD_ALL, { downloadFilter = PlaylistEntity.DOWNLOAD_ALL }, label = { Text("All") })
                    FilterChip(downloadFilter == PlaylistEntity.DOWNLOAD_DOWNLOADED, { downloadFilter = PlaylistEntity.DOWNLOAD_DOWNLOADED }, label = { Text("Downloaded") })
                    FilterChip(downloadFilter == PlaylistEntity.DOWNLOAD_NOT_DOWNLOADED, { downloadFilter = PlaylistEntity.DOWNLOAD_NOT_DOWNLOADED }, label = { Text("Not downloaded") })
                }
            }
            Button({
                if (name.isNotBlank()) {
                    if (smart) vm.createSmartPlaylist(name.trim(), playFilter, downloadFilter)
                    else vm.createPlaylist(name.trim())
                    name = ""
                }
            }) { Text("Create") }
            val listState = rememberLazyListState()
            LazyColumn(Modifier.weight(1f).openPodScrollbar(listState)) {
                items(lists, key = { it.id }) { list ->
                    Row(Modifier.fillMaxWidth().clickable { onOpen(list.id) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(list.name)
                            if (list.isSmart) {
                                Text(
                                    "Smart · ${list.playFilter} · ${list.downloadFilter}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        IconButton({ vm.deletePlaylist(list) }) { Icon(Icons.Default.Delete, "Delete") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistDetailScreen(vm: AppViewModel, playlistId: Long) {
    val lists by vm.playlists.collectAsState()
    val items by vm.playlistItems(playlistId).collectAsState(initial = emptyList())
    val player by vm.player.collectAsState()
    val list = lists.find { it.id == playlistId }
    var episodes by remember { mutableStateOf<List<EpisodeEntity>>(emptyList()) }
    LaunchedEffect(items, list?.isSmart, list?.playFilter, list?.downloadFilter) {
        episodes = if (list?.isSmart == true) {
            vm.smartEpisodes(list.playFilter, list.downloadFilter)
        } else {
            items.mapNotNull { vm.repo.db.episodes().byId(it.episodeId) }
        }
    }
    var rename by remember(list?.name) { mutableStateOf(list?.name.orEmpty()) }
    Scaffold(topBar = { TopAppBar(title = { Text(list?.name ?: "Playlist") }) }) { padding ->
        val listState = rememberLazyListState()
        LazyColumn(Modifier.padding(padding).openPodScrollbar(listState).padding(16.dp)) {
            item {
                OutlinedTextField(rename, { rename = it }, Modifier.fillMaxWidth(), label = { Text("Rename") })
                TextButton({ if (rename.isNotBlank()) vm.renamePlaylist(playlistId, rename.trim()) }) { Text("Save name") }
                if (list?.isSmart == true) {
                    Text("This smart playlist updates from filters: ${list.playFilter} / ${list.downloadFilter}.")
                }
            }
            items(episodes, key = { it.id }) { episode ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(episode.title, Modifier.weight(1f), maxLines = 2)
                    IconButton({
                        vm.playOrToggle(episode, "", episode.imageUrl)
                    }) {
                        Icon(
                            if (player.episodeId == episode.id && player.playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (player.episodeId == episode.id && player.playing) "Pause" else "Play"
                        )
                    }
                    if (list?.isSmart != true) {
                        IconButton({ vm.removeFromPlaylist(playlistId, episode.id) }) { Icon(Icons.Default.Delete, "Remove") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(vm: AppViewModel) {
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
    var pattern by remember(settings.namingPattern) { mutableStateOf(settings.namingPattern) }
    var notificationsOn by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    val notifyPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        val listState = rememberLazyListState()
        LazyColumn(Modifier.padding(padding).openPodScrollbar(listState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Subscriptions", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Save every show you follow as an OPML file. Import to add missing podcasts, or replace the library if you want a clean restore. OPML also works in other podcast apps.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Button({ exportSubscriptions.launch("OpenPod-subscriptions.opml") }) {
                    Text("Export subscriptions")
                }
                Spacer(Modifier.height(8.dp))
                Button({
                    importSubscriptions.launch(
                        arrayOf("text/xml", "text/x-opml", "application/xml", "application/octet-stream", "*/*")
                    )
                }) {
                    Text("Import subscriptions")
                }
            }
            item {
                Text("Episode save location", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (settings.downloadTreeUri.isBlank()) "Not set — downloads will fail until you pick a folder."
                    else Uri.parse(settings.downloadTreeUri).lastPathSegment ?: settings.downloadTreeUri,
                    style = MaterialTheme.typography.bodySmall
                )
                Button({ picker.launch(null) }) {
                    Icon(Icons.Default.Folder, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Choose folder")
                }
            }
            item {
                Text("File naming", style = MaterialTheme.typography.titleMedium)
                Text("Tokens: {podcast} {title} {episode} {season} {date} {guid}", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(pattern, { pattern = it }, Modifier.fillMaxWidth(), singleLine = true)
                TextButton({ vm.setNaming(pattern) }) { Text("Save pattern") }
            }
            item {
                Text("Skip intervals", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Back and forward amounts on Now Playing, the mini player, lock screen, and headset skips. Enter seconds and leave the field to save.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                SecondsTextField(
                    committedSec = settings.skipBackSec,
                    onCommit = { vm.setSkip(settings.skipForwardSec, it) },
                    label = "Skip backward (seconds)",
                    supporting = "Positive whole seconds, 1–${SkipIntervals.MAX_SEC}.",
                    minSec = SkipIntervals.MIN_SEC,
                    maxSec = SkipIntervals.MAX_SEC
                )
                SecondsTextField(
                    committedSec = settings.skipForwardSec,
                    onCommit = { vm.setSkip(it, settings.skipBackSec) },
                    label = "Skip forward (seconds)",
                    supporting = "Positive whole seconds, 1–${SkipIntervals.MAX_SEC}.",
                    minSec = SkipIntervals.MIN_SEC,
                    maxSec = SkipIntervals.MAX_SEC
                )
            }
            item {
                Text("Headset buttons", style = MaterialTheme.typography.titleMedium)
                Text("Single press play/pause · double skip forward · triple skip back. Next/previous on the headset also skip by those amounts.")
            }
            item {
                Text("New show defaults", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Used when you add a podcast. Change a subscribed show from its podcast settings (gear on that show).",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-download new episodes by default", Modifier.weight(1f))
                    Switch(settings.autoDownloadDefault, { vm.setDefaults(it, settings.autoDeleteDefault) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-delete after completion by default", Modifier.weight(1f))
                    Switch(settings.autoDeleteDefault, { vm.setDefaults(settings.autoDownloadDefault, it) })
                }
            }
            item {
                Text("Check for new episodes", style = MaterialTheme.typography.titleMedium)
                var frequency by remember(settings.syncFrequency) { mutableStateOf(settings.syncFrequency) }
                var hour by remember(settings.syncHour) { mutableStateOf(settings.syncHour.toFloat()) }
                var minute by remember(settings.syncMinute) { mutableStateOf(settings.syncMinute.toFloat()) }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(frequency == SettingsEntity.SYNC_30_MIN, { frequency = SettingsEntity.SYNC_30_MIN }, label = { Text("Every 30 minutes") })
                    FilterChip(frequency == SettingsEntity.SYNC_HOURLY, { frequency = SettingsEntity.SYNC_HOURLY }, label = { Text("Hourly") })
                    FilterChip(frequency == SettingsEntity.SYNC_DAILY, { frequency = SettingsEntity.SYNC_DAILY }, label = { Text("Daily") })
                    FilterChip(frequency == SettingsEntity.SYNC_WEEKLY, { frequency = SettingsEntity.SYNC_WEEKLY }, label = { Text("Weekly") })
                }
                if (frequency == SettingsEntity.SYNC_30_MIN) {
                    Text("OpenPod checks for new episodes about every 30 minutes, all day.")
                } else if (frequency == SettingsEntity.SYNC_HOURLY) {
                    Text("OpenPod will look for new episodes about every hour.")
                } else {
                    Text("Time of day: ${hour.toInt().toString().padStart(2, '0')}:${minute.toInt().toString().padStart(2, '0')}")
                    Text("Hour")
                    Slider(hour, { hour = it }, valueRange = 0f..23f, steps = 22)
                    Text("Minute")
                    Slider(minute, { minute = it }, valueRange = 0f..59f, steps = 58)
                }
                TextButton({ vm.setSyncSchedule(frequency, hour.toInt(), minute.toInt()) }) { Text("Save sync schedule") }
            }
            item {
                Text("Notifications", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (notificationsOn) {
                        "Notifications are on. Lock-screen and notification-shade controls appear while an episode is playing."
                    } else {
                        "Notifications are off. Android 13 and newer require this permission for the lock-screen and notification player."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                if (!notificationsOn) {
                    Button({
                        if (Build.VERSION.SDK_INT >= 33) {
                            notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                            )
                        }
                    }) { Text("Enable notifications") }
                } else {
                    TextButton({
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                        )
                    }) { Text("Notification settings") }
                }
            }
            item {
                Text("About", style = MaterialTheme.typography.titleMedium)
                Text("OpenPod ${BuildConfig.VERSION_NAME}")
                Text("Build ${BuildConfig.VERSION_CODE}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    val importUri = pendingImport
    if (importUri != null && !confirmReplace) {
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Import subscriptions") },
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

private fun isDownloadActive(episode: EpisodeEntity, progress: DownloadProgress?): Boolean {
    return progress != null ||
        episode.downloadStatus == EpisodeEntity.STATUS_DOWNLOADING ||
        episode.downloadStatus == EpisodeEntity.STATUS_QUEUED
}

private fun downloadStatusLabel(episode: EpisodeEntity, progress: DownloadProgress?): String? {
    return when {
        progress != null && progress.knownTotal -> "Downloading ${(progress.fraction * 100).toInt()}%"
        progress != null -> "Downloading"
        episode.downloadStatus == EpisodeEntity.STATUS_QUEUED -> "Queued"
        episode.downloadStatus == EpisodeEntity.STATUS_DOWNLOADING -> "Downloading"
        episode.downloadStatus == EpisodeEntity.STATUS_DONE -> "Saved"
        episode.downloadStatus == EpisodeEntity.STATUS_ERROR -> "Download failed"
        else -> null
    }
}

@Composable
private fun DownloadProgressBlock(episode: EpisodeEntity, progress: DownloadProgress?) {
    val showError = episode.downloadStatus == EpisodeEntity.STATUS_ERROR && progress == null
    val showBar = progress != null ||
        episode.downloadStatus == EpisodeEntity.STATUS_DOWNLOADING ||
        episode.downloadStatus == EpisodeEntity.STATUS_QUEUED
    if (!showBar && !showError) return
    Spacer(Modifier.height(8.dp))
    when {
        progress != null && progress.knownTotal -> {
            LinearProgressIndicator(progress = { progress.fraction }, modifier = Modifier.fillMaxWidth())
            Text(
                "${formatBytes(progress.bytesRead)} / ${formatBytes(progress.totalBytes)}  ${(progress.fraction * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
        }
        progress != null -> {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Downloading ${formatBytes(progress.bytesRead)}", style = MaterialTheme.typography.bodySmall)
        }
        episode.downloadStatus == EpisodeEntity.STATUS_QUEUED -> {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Queued", style = MaterialTheme.typography.bodySmall)
        }
        episode.downloadStatus == EpisodeEntity.STATUS_DOWNLOADING -> {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Downloading…", style = MaterialTheme.typography.bodySmall)
        }
        showError -> {
            Text(
                "Download failed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
