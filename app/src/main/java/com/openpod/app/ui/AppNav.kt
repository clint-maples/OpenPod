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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import com.openpod.app.BuildConfig
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.openpod.app.data.db.EpisodeEntity
import com.openpod.app.data.db.PlaylistEntity
import com.openpod.app.data.db.PodcastEntity
import com.openpod.app.data.db.SettingsEntity
import com.openpod.app.download.DownloadProgress
import com.openpod.app.download.EpisodeDownloadAction
import com.openpod.app.download.episodeDownloadAction
import com.openpod.app.playback.PlayerService
import com.openpod.app.playback.formatVolumeBoost

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
            NavigationBar {
                NavItem("Library", Icons.Default.LibraryMusic, back.startsWith("library") || back.startsWith("podcast") || back.startsWith("episode")) {
                    nav.navigate("library") { popUpTo(nav.graph.findStartDestination().id); launchSingleTop = true }
                }
                NavItem("Discover", Icons.Default.Explore, back.startsWith("discover")) {
                    nav.navigate("discover") { launchSingleTop = true }
                }
                NavItem("Playlists", Icons.Default.QueueMusic, back.startsWith("playlist")) {
                    nav.navigate("playlists") { launchSingleTop = true }
                }
                NavItem("Now playing", Icons.Default.PlayArrow, back == "player" || back == "eq") {
                    nav.navigate("player") { launchSingleTop = true }
                }
                NavItem("Settings", Icons.Default.Settings, back == "settings") {
                    nav.navigate("settings") { launchSingleTop = true }
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
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
                        onRemoved = { nav.popBackStack() }
                    )
                }
                composable("episode/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                    EpisodeScreen(vm, it.arguments!!.getLong("id")) { nav.navigate("player") }
                }
                composable("player") {
                    PlayerScreen(
                        vm,
                        onEq = { nav.navigate("eq") },
                        onOpenPodcast = { podcastId -> nav.navigate("podcast/$podcastId") }
                    )
                }
                composable("eq") { EqScreen(vm) }
                composable("settings") { SettingsScreen(vm) }
            }
            if (player.episodeId != 0L && back != "player") {
                MiniPlayer(player, vm) { nav.navigate("player") }
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
        label = { Text(label) }
    )
}

@Composable
private fun MiniPlayer(player: com.openpod.app.playback.PlayerUiState, vm: AppViewModel, onOpen: () -> Unit) {
    val nowPlayingLabel = listOf(player.podcastTitle, player.title)
        .filter { it.isNotBlank() }
        .joinToString(" — ")
        .ifBlank { "Now playing" }
    Box(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen, onClickLabel = "Open now playing")
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        AsyncImage(
            player.artwork,
            nowPlayingLabel,
            Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Row(
            Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton({ vm.skip(false) }) { Icon(Icons.Default.FastRewind, "Back") }
            IconButton({ vm.togglePlay() }) {
                Icon(if (player.playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Play")
            }
            IconButton({ vm.skip(true) }) { Icon(Icons.Default.FastForward, "Forward") }
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                gridItems(podcasts, key = { it.id }) { podcast ->
                    AsyncImage(
                        model = podcast.imageUrl,
                        contentDescription = podcast.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpen(podcast.id) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(podcasts, key = { it.id }) { podcast ->
                    PodcastRow(podcast) { onOpen(podcast.id) }
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
    var url by remember { mutableStateOf("") }
    val results by vm.searchResults.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    fun hideKeyboard() {
        keyboard?.hide()
        focusManager.clearFocus()
    }
    fun runSearch() {
        hideKeyboard()
        if (query.isNotBlank()) vm.search(query)
    }
    fun clearQuery() {
        hideKeyboard()
        query = ""
        vm.clearSearch()
    }
    fun clearUrl() {
        hideKeyboard()
        url = ""
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Discover") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedTextField(
                    query,
                    { query = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Search podcasts") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { runSearch() })
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({ runSearch() }) { Text("Search") }
                    Button(
                        onClick = { clearQuery() },
                        enabled = query.isNotBlank() || results.isNotEmpty()
                    ) { Text("Clear") }
                }
            }
            item {
                OutlinedTextField(
                    url,
                    { url = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Or paste RSS / podcast URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { hideKeyboard() })
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({
                        hideKeyboard()
                        if (url.isNotBlank()) vm.subscribe(url.trim())
                    }) { Text("Add by URL") }
                    Button(
                        onClick = { clearUrl() },
                        enabled = url.isNotBlank()
                    ) { Text("Clear") }
                }
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun PodcastScreen(vm: AppViewModel, podcastId: Long, onEpisode: (Long) -> Unit, onRemoved: () -> Unit) {
    val podcasts by vm.podcasts.collectAsState()
    val settings by vm.settings.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val podcast = podcasts.find { it.id == podcastId }
    val episodes by vm.episodes(podcastId, settings.showArchived).collectAsState(initial = emptyList())
    val progressMap by vm.downloadProgress.collectAsState()
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var moveDialog by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf(false) }
    if (podcast == null) {
        Text("Podcast not found")
        return
    }
    val selectedEpisodes = episodes.filter { it.id in selected }
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
                            selected = if (selected.size == episodes.size) emptySet() else episodes.map { it.id }.toSet()
                        }) { Icon(Icons.Default.SelectAll, "Select all") }
                    } else {
                        IconButton({ vm.refresh(podcastId) }) { Icon(Icons.Default.Refresh, "Refresh") }
                        IconButton({ confirmRemove = true }) { Icon(Icons.Default.Delete, "Remove from library") }
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
        LazyColumn(Modifier.padding(padding)) {
            item {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(podcast.imageUrl, podcast.title, Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(podcast.author)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Auto-download", Modifier.weight(1f))
                            Switch(podcast.autoDownload, { vm.setFlags(podcastId, autoDownload = it) })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Auto-delete", Modifier.weight(1f))
                            Switch(podcast.autoDelete, { vm.setFlags(podcastId, autoDelete = it) })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Show archived", Modifier.weight(1f))
                            Switch(settings.showArchived, { vm.setShowArchived(it) })
                        }
                    }
                }
                Text(stripHtml(podcast.description), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, maxLines = 6)
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
                        TextButton({ selected = episodes.map { it.id }.toSet() }) { Text("Select all") }
                        TextButton({
                            selected = episodes.filter { it.downloadStatus == EpisodeEntity.STATUS_DONE }
                                .map { it.id }.toSet()
                        }) { Text("Downloaded") }
                        TextButton({
                            selected = episodes.filter { it.downloadStatus != EpisodeEntity.STATUS_DONE }
                                .map { it.id }.toSet()
                        }) { Text("Not downloaded") }
                        TextButton({ selected = emptySet() }) { Text("Clear") }
                    }
                }
            }
            items(episodes, key = { it.id }) { episode ->
                EpisodeRow(
                    episode = episode,
                    selecting = selecting,
                    checked = episode.id in selected,
                    progress = progressMap[episode.id],
                    onOpen = { onEpisode(episode.id) },
                    onPlay = { vm.play(episode, podcast) },
                    onDownload = { vm.download(episode.id) },
                    onDeleteDownload = { vm.deleteDownload(episode.id) },
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpisodeRow(
    episode: EpisodeEntity,
    selecting: Boolean,
    checked: Boolean,
    progress: DownloadProgress?,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onDeleteDownload: () -> Unit,
    onToggle: () -> Unit,
    onLongPress: () -> Unit
) {
    val downloading = isDownloadActive(episode, progress)
    val downloadAction = episodeDownloadAction(episode, downloading)
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
                Text(episode.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(
                        formatDate(episode.pubDate).ifBlank { null },
                        episode.episodeNumber?.let { "Ep $it" },
                        downloadStatusLabel(episode, progress),
                        if (episode.archived) "Archived" else null,
                        when {
                            episode.completed -> "Played"
                            episode.positionMs > 0 -> "In progress"
                            else -> "Unplayed"
                        }
                    ).joinToString(" · "),
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
                IconButton(onPlay) { Icon(Icons.Default.PlayArrow, "Play") }
            }
        }
        DownloadProgressBlock(episode, progress)
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
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerScreen(vm: AppViewModel, onEq: () -> Unit, onOpenPodcast: (Long) -> Unit) {
    val player by vm.player.collectAsState()
    val settings by vm.settings.collectAsState()
    val episode by vm.episode(player.episodeId).collectAsState(initial = null)
    val chapters by vm.chapters.collectAsState()
    val transcript by vm.transcript.collectAsState()
    LaunchedEffect(player.episodeId) {
        if (player.episodeId != 0L) {
            vm.repo.db.episodes().byId(player.episodeId)?.let { vm.loadExtras(it) }
        }
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Now playing") }, actions = {
            IconButton(onEq) { Icon(Icons.Default.Equalizer, "Equalizer") }
        })
    }) { padding ->
        if (player.episodeId == 0L) {
            Text(
                "Nothing playing yet. Pick an episode from your library, and OpenPod will remember it here.",
                Modifier.padding(padding).padding(24.dp)
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    val podcastId = player.podcastId.takeIf { it != 0L } ?: episode?.podcastId ?: 0L
                    val artworkModifier = Modifier.size(220.dp).clip(RoundedCornerShape(16.dp))
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
                    Spacer(Modifier.height(16.dp))
                    Text(player.title, style = MaterialTheme.typography.titleLarge, maxLines = 2)
                    Text(player.podcastTitle, style = MaterialTheme.typography.bodyMedium)
                    if (chapters.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
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
                    Spacer(Modifier.height(16.dp))
                    Slider(
                        value = if (player.durationMs > 0) (player.positionMs.toFloat() / player.durationMs).coerceIn(0f, 1f) else 0f,
                        onValueChange = { if (player.durationMs > 0) vm.seek((it * player.durationMs).toLong()) }
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatMs(player.positionMs))
                        Text(formatMs(player.durationMs))
                    }
                    val controlSize = 108.dp
                    val iconSize = 72.dp
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton({ vm.skip(false) }, Modifier.size(controlSize)) {
                            Icon(Icons.Default.SkipPrevious, "Back ${settings.skipBackSec}s", Modifier.size(iconSize))
                        }
                        IconButton({ vm.togglePlay() }, Modifier.size(controlSize)) {
                            Icon(
                                if (player.playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                "Play",
                                Modifier.size(iconSize)
                            )
                        }
                        IconButton({ vm.skip(true) }, Modifier.size(controlSize)) {
                            Icon(Icons.Default.SkipNext, "Forward ${settings.skipForwardSec}s", Modifier.size(iconSize))
                        }
                    }
                    Text("Skip ${settings.skipBackSec}s / ${settings.skipForwardSec}s")
                    Spacer(Modifier.height(8.dp))
                    SpeedControl(player.speed, { vm.setSpeed(it) })
                }
            }
            val current = episode
            if (current != null) {
                if (current.description.isNotBlank()) {
                    item {
                        Column(Modifier.fillMaxWidth()) {
                            Text("From the publisher", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            PublisherText(current.description)
                        }
                    }
                }
                if (chapters.isNotEmpty()) {
                    item { Text("Chapters", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth()) }
                    items(chapters) { chapter ->
                        Text(
                            "${formatMs(chapter.startMs)}  ${chapter.title}",
                            Modifier.fillMaxWidth().clickable { vm.seek(chapter.startMs) }.padding(vertical = 6.dp)
                        )
                    }
                }
                if (transcript.isNotEmpty()) {
                    item { Text("Transcript", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth()) }
                    items(transcript.take(400)) { line ->
                        val prefix = line.startMs?.let { formatMs(it) + "  " }.orEmpty()
                        Text(prefix + line.text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
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
private fun SpeedControl(speed: Float, onSpeed: (Float) -> Unit) {
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(formatSpeed(speed)) }
    LaunchedEffect(speed) {
        if (!focused) text = formatSpeed(speed)
    }
    fun apply(raw: String) {
        val parsed = raw.trim().removeSuffix("x").replace(',', '.').toFloatOrNull()
        if (parsed != null) {
            val clamped = clampSpeed(parsed)
            text = formatSpeed(clamped)
            onSpeed(clamped)
        } else {
            text = formatSpeed(speed)
        }
    }
    fun nudge(delta: Float) {
        val current = text.toFloatOrNull() ?: speed
        val next = clampSpeed(current + delta)
        text = formatSpeed(next)
        onSpeed(next)
    }
    val current = text.toFloatOrNull() ?: speed
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Playback speed", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            IconButton({ nudge(-SPEED_STEP) }, enabled = current > SPEED_MIN) {
                Icon(Icons.Default.Remove, "Slower")
            }
            OutlinedTextField(
                value = text,
                onValueChange = { value ->
                    text = value.filter { it.isDigit() || it == '.' || it == ',' }.take(4)
                },
                modifier = Modifier
                    .width(70.dp)
                    .onFocusChanged { focused = it.isFocused },
                singleLine = true,
                textStyle = TextStyle(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    apply(text)
                    focusManager.clearFocus()
                })
            )
            IconButton({ nudge(SPEED_STEP) }, enabled = current < SPEED_MAX) {
                Icon(Icons.Default.Add, "Faster")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EqScreen(vm: AppViewModel) {
    val settings by vm.settings.collectAsState()
    var eqEpoch by remember { mutableStateOf(0) }
    val bands = remember(eqEpoch, settings.eqBandsJson) {
        PlayerService.instance?.equalizer()?.bands().orEmpty()
    }
    val range = remember(eqEpoch) {
        PlayerService.instance?.equalizer()?.range() ?: (0.toShort() to 0.toShort())
    }
    var boost by remember(settings.volumeBoost, eqEpoch) { mutableStateOf(settings.volumeBoost) }
    Scaffold(topBar = { TopAppBar(title = { Text("Graphic equalizer") }) }) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
            item {
                Spacer(Modifier.height(8.dp))
                Text("Volume boost", style = MaterialTheme.typography.titleMedium)
                Text(
                    formatVolumeBoost(boost),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Center is the unadjusted level. Left is quieter than the system volume; right is louder, even if the system volume is already at max.",
                    style = MaterialTheme.typography.bodySmall
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
        Column(Modifier.padding(padding).padding(16.dp)) {
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
            LazyColumn {
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
        LazyColumn(Modifier.padding(padding).padding(16.dp)) {
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
                        vm.play(episode, "", episode.imageUrl)
                    }) { Icon(Icons.Default.PlayArrow, "Play") }
                    if (list?.isSmart != true) {
                        IconButton({ vm.removeFromPlaylist(playlistId, episode.id) }) { Icon(Icons.Default.Delete, "Remove") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var fwd by remember(settings.skipForwardSec) { mutableStateOf(settings.skipForwardSec.toFloat()) }
    var back by remember(settings.skipBackSec) { mutableStateOf(settings.skipBackSec.toFloat()) }
    var notificationsOn by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    val notifyPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                Text("Skip backward: ${back.toInt()} seconds")
                Slider(back, { back = it }, valueRange = 5f..60f, steps = 10)
                Text("Skip forward: ${fwd.toInt()} seconds")
                Slider(fwd, { fwd = it }, valueRange = 5f..90f, steps = 16)
                TextButton({ vm.setSkip(fwd.toInt(), back.toInt()) }) { Text("Save skip amounts") }
            }
            item {
                Text("Headset buttons", style = MaterialTheme.typography.titleMedium)
                Text("Single press play/pause · double skip forward · triple skip back. Next/previous on the headset also skip by those amounts.")
            }
            item {
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(frequency == SettingsEntity.SYNC_HOURLY, { frequency = SettingsEntity.SYNC_HOURLY }, label = { Text("Hourly") })
                    FilterChip(frequency == SettingsEntity.SYNC_DAILY, { frequency = SettingsEntity.SYNC_DAILY }, label = { Text("Daily") })
                    FilterChip(frequency == SettingsEntity.SYNC_WEEKLY, { frequency = SettingsEntity.SYNC_WEEKLY }, label = { Text("Weekly") })
                }
                if (frequency != SettingsEntity.SYNC_HOURLY) {
                    Text("Time of day: ${hour.toInt().toString().padStart(2, '0')}:${minute.toInt().toString().padStart(2, '0')}")
                    Text("Hour")
                    Slider(hour, { hour = it }, valueRange = 0f..23f, steps = 22)
                    Text("Minute")
                    Slider(minute, { minute = it }, valueRange = 0f..59f, steps = 58)
                } else {
                    Text("OpenPod will look for new episodes about every hour.")
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
