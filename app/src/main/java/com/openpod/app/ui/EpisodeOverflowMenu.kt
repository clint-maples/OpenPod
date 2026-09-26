package com.openpod.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Three-dot menu on a podcast episode row (Play next, Add to Up Next, Share).
 * Archive episode marks that one episode archived through the existing
 * episode archive path. The podcast subscription is not changed.
 */
internal object EpisodeOverflowMenu {
    const val playNextLabel = "Play next"
    const val upNextLabel = "Add to Up Next"
    const val shareLabel = "Share"
    const val archiveLabel = "Archive episode"
    const val unarchiveLabel = "Unarchive episode"
    const val overflowContentDescription = "More"
    const val overflowTag = "episode-overflow"

    fun actionLabel(archived: Boolean): String = if (archived) unarchiveLabel else archiveLabel

    /** True when the menu action should set archived, so the episode leaves the available list. */
    fun archivesEpisode(archived: Boolean): Boolean = !archived
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpisodeOverflowMenu(
    archived: Boolean,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onShare: () -> Unit,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(
            onClick = { menuOpen = true },
            modifier = Modifier.testTag(EpisodeOverflowMenu.overflowTag)
        ) {
            Icon(Icons.Default.MoreVert, EpisodeOverflowMenu.overflowContentDescription)
        }
        DropdownMenu(menuOpen, { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text(EpisodeOverflowMenu.playNextLabel) },
                onClick = {
                    menuOpen = false
                    onPlayNext()
                },
                leadingIcon = { Icon(Icons.Default.QueuePlayNext, null) }
            )
            DropdownMenuItem(
                text = { Text(EpisodeOverflowMenu.upNextLabel) },
                onClick = {
                    menuOpen = false
                    onAddToQueue()
                },
                leadingIcon = { Icon(Icons.Default.PlaylistPlay, null) }
            )
            DropdownMenuItem(
                text = { Text(EpisodeOverflowMenu.shareLabel) },
                onClick = {
                    menuOpen = false
                    onShare()
                },
                leadingIcon = { Icon(Icons.Default.Share, null) }
            )
            DropdownMenuItem(
                text = { Text(EpisodeOverflowMenu.actionLabel(archived)) },
                onClick = {
                    menuOpen = false
                    onArchive()
                },
                leadingIcon = {
                    Icon(
                        if (EpisodeOverflowMenu.archivesEpisode(archived)) {
                            Icons.Default.Archive
                        } else {
                            Icons.Default.Unarchive
                        },
                        null
                    )
                }
            )
        }
    }
}
