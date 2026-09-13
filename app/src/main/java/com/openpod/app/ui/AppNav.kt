package com.openpod.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
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
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.openpod.app.playback.PlayerService

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
                    PodcastScreen(vm, it.arguments!!.getLong("id")) { epId -> nav.navigate("episode/$epId") }
                }
                composable("episode/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                    EpisodeScreen(vm, it.arguments!!.getLong("id")) { nav.navigate("player") }
                }
                composable("player") { PlayerScreen(vm) { nav.navigate("eq") } }
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
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(player.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(player.podcastTitle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
        IconButton({ vm.skip(false) }) { Icon(Icons.Default.FastRewind, "Back") }
        IconButton({ vm.togglePlay() }) {
            Icon(if (player.playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Play")
        }
        IconButton({ vm.skip(true) }) { Icon(Icons.Default.FastForward, "Forward") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(vm: AppViewModel, onOpen: (Long) -> Unit, onRefresh: () -> Unit) {
    val podcasts by vm.podcasts.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Library") }, actions = {
                IconButton(onRefresh) { Icon(Icons.Default.Refresh, "Refresh") }
            })
        }
    ) { padding ->
        if (podcasts.isEmpty()) {
            Text("Add a show from Discover, or paste an RSS URL.", Modifier.padding(padding).padding(24.dp))
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
    Scaffold(topBar = { TopAppBar(title = { Text("Discover") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search podcasts") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                Button({ vm.search(query) }) { Text("Search") }
            }
            item {
                OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("Or paste RSS / podcast URL") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                Button({ if (url.isNotBlank()) vm.subscribe(url.trim()) }) { Text("Add by URL") }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun PodcastScreen(vm: AppViewModel, podcastId: Long, onEpisode: (Long) -> Unit) {
    val podcasts by vm.podcasts.collectAsState()
    val settings by vm.settings.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val podcast = podcasts.find { it.id == podcastId }
    val episodes by vm.episodes(podcastId, settings.showArchived).collectAsState(initial = emptyList())
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var moveDialog by remember { mutableStateOf(false) }
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
                } else {
                    Row(Modifier.padding(horizontal = 4.dp)) {
                        TextButton({ selected = episodes.map { it.id }.toSet() }) { Text("Select all") }
                        TextButton({ selected = emptySet() }) { Text("Clear") }
                    }
                }
            }
            items(episodes, key = { it.id }) { episode ->
                EpisodeRow(
                    episode = episode,
                    selecting = selecting,
                    checked = episode.id in selected,
                    onOpen = { onEpisode(episode.id) },
                    onPlay = { vm.play(episode, podcast) },
                    onDownload = { vm.download(episode.id) },
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpisodeRow(
    episode: EpisodeEntity,
    selecting: Boolean,
    checked: Boolean,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onToggle: () -> Unit,
    onLongPress: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = { if (selecting) onToggle() else onOpen() }, onLongClick = onLongPress)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                    if (episode.downloadStatus == EpisodeEntity.STATUS_DONE) "Saved" else null,
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
            IconButton(onDownload) { Icon(Icons.Default.Download, "Download") }
            IconButton(onPlay) { Icon(Icons.Default.PlayArrow, "Play") }
        }
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
                Row {
                    Button({ vm.download(current.id) }) { Text("Download") }
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
                Text("Status: ${current.downloadStatus}" + if (current.fileName.isNotBlank()) " · ${current.fileName}" else "")
            }
            item {
                Text("From the publisher", style = MaterialTheme.typography.titleMedium)
                Text(stripHtml(current.description), style = MaterialTheme.typography.bodyMedium)
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
private fun PlayerScreen(vm: AppViewModel, onEq: () -> Unit) {
    val player by vm.player.collectAsState()
    val settings by vm.settings.collectAsState()
    val speeds = listOf(0.8f, 1f, 1.1f, 1.25f, 1.5f, 1.75f, 2f)
    Scaffold(topBar = {
        TopAppBar(title = { Text("Now playing") }, actions = {
            IconButton(onEq) { Icon(Icons.Default.Equalizer, "Equalizer") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(player.artwork, player.title, Modifier.size(220.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.height(16.dp))
            Text(player.title, style = MaterialTheme.typography.titleLarge, maxLines = 2)
            Text(player.podcastTitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Slider(
                value = if (player.durationMs > 0) player.positionMs.toFloat() / player.durationMs else 0f,
                onValueChange = { if (player.durationMs > 0) vm.seek((it * player.durationMs).toLong()) }
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatMs(player.positionMs))
                Text(formatMs(player.durationMs))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton({ vm.skip(false) }) { Icon(Icons.Default.SkipPrevious, "Back ${settings.skipBackSec}s") }
                IconButton({ vm.togglePlay() }, Modifier.size(72.dp)) {
                    Icon(if (player.playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Play", Modifier.size(48.dp))
                }
                IconButton({ vm.skip(true) }) { Icon(Icons.Default.SkipNext, "Forward ${settings.skipForwardSec}s") }
            }
            Text("Skip ${settings.skipBackSec}s / ${settings.skipForwardSec}s")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                speeds.forEach { speed ->
                    FilterChip(selected = player.speed == speed, onClick = { vm.setSpeed(speed) }, label = { Text("${speed}x") })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EqScreen(vm: AppViewModel) {
    val settings by vm.settings.collectAsState()
    val bands = remember { PlayerService.instance?.equalizer()?.bands().orEmpty() }
    val range = remember { PlayerService.instance?.equalizer()?.range() ?: (0.toShort() to 0.toShort()) }
    Scaffold(topBar = { TopAppBar(title = { Text("Graphic equalizer") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled", Modifier.weight(1f))
                Switch(settings.eqEnabled, { vm.setEq(it) })
            }
            if (bands.isEmpty()) {
                Text("Start playback to load equalizer bands for this device.")
            } else {
                bands.forEach { band ->
                    var value by remember(band.index, band.levelMilliBel) { mutableStateOf(band.levelMilliBel.toFloat()) }
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
    var pattern by remember(settings.namingPattern) { mutableStateOf(settings.namingPattern) }
    var fwd by remember(settings.skipForwardSec) { mutableStateOf(settings.skipForwardSec.toFloat()) }
    var back by remember(settings.skipBackSec) { mutableStateOf(settings.skipBackSec.toFloat()) }
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                Text("Tokens: {podcast} {title} {episode} {season} {date}", style = MaterialTheme.typography.bodySmall)
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
        }
    }
}
