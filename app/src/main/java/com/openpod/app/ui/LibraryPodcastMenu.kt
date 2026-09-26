package com.openpod.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage

/**
 * Library overflow for a subscribed show. Archive is always offered: it marks
 * every available episode archived and leaves the subscription in the library.
 * It is not gated on episode count, and both library layouts use the same action.
 */
internal object LibraryPodcastMenu {
    const val archiveLabel = "Archive"
    const val archiveDetail = "All episodes. Subscription stays."
    const val overflowContentDescription = "Podcast menu"
    const val overflowTag = "library-podcast-overflow"

    /** Nearly opaque library surface so the glyph stays readable on any cover. */
    val compactScrim = Color(0xF0121418)

    /** Subscribed shows always get Archive, including when every episode is already archived. */
    fun showsArchive(subscribed: Boolean) = subscribed
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryPodcastOverflowMenu(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onArchive: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box(modifier.zIndex(1f)) {
        IconButton(
            onClick = { menuOpen = true },
            modifier = (
                if (compact) {
                    Modifier
                        .size(36.dp)
                        .background(LibraryPodcastMenu.compactScrim, CircleShape)
                } else {
                    Modifier
                }
            ).testTag(LibraryPodcastMenu.overflowTag)
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = LibraryPodcastMenu.overflowContentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = if (compact) Modifier.size(20.dp) else Modifier
            )
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            if (LibraryPodcastMenu.showsArchive(subscribed = true)) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(LibraryPodcastMenu.archiveLabel)
                            Text(
                                LibraryPodcastMenu.archiveDetail,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    onClick = {
                        menuOpen = false
                        onArchive()
                    },
                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
internal fun LibraryPodcastGridCell(
    title: String,
    imageUrl: String,
    onOpen: () -> Unit,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .fillMaxSize()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                .clickable(onClick = onOpen),
            contentScale = ContentScale.Crop
        )
        LibraryPodcastOverflowMenu(
            Modifier.align(Alignment.TopEnd).padding(3.dp),
            compact = true,
            onArchive = onArchive
        )
    }
}

/**
 * Grid cell whose cover is supplied by the caller. Tests use this to put a
 * solid light cover behind the overflow without loading artwork.
 */
@Composable
internal fun LibraryPodcastGridCell(
    onOpen: () -> Unit,
    onArchive: () -> Unit,
    modifier: Modifier = Modifier,
    artwork: @Composable BoxScope.() -> Unit
) {
    Box(modifier) {
        Box(Modifier.fillMaxSize().clickable(onClick = onOpen), content = artwork)
        LibraryPodcastOverflowMenu(
            Modifier.align(Alignment.TopEnd).padding(3.dp),
            compact = true,
            onArchive = onArchive
        )
    }
}

@Composable
internal fun LibraryPodcastListRow(
    title: String,
    author: String,
    imageUrl: String,
    onOpen: () -> Unit,
    onArchive: () -> Unit
) {
    LibraryPodcastListRow(
        title = title,
        author = author,
        onOpen = onOpen,
        onArchive = onArchive,
        artwork = {
            AsyncImage(
                imageUrl,
                title,
                Modifier.size(56.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    )
}

@Composable
internal fun LibraryPodcastListRow(
    title: String,
    author: String,
    onOpen: () -> Unit,
    onArchive: () -> Unit,
    artwork: @Composable () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier
                .weight(1f)
                .clickable(onClick = onOpen)
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            artwork()
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    author,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        LibraryPodcastOverflowMenu(
            Modifier.padding(end = 4.dp),
            compact = false,
            onArchive = onArchive
        )
    }
}
