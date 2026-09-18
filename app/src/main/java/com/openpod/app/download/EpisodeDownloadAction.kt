package com.openpod.app.download

import com.openpod.app.data.db.EpisodeEntity

/** Per-episode library control: Download, or Delete once a local file is saved. */
enum class EpisodeDownloadAction {
    DOWNLOAD,
    DELETE,
    IN_PROGRESS
}

fun episodeDownloadAction(
    episode: EpisodeEntity,
    downloading: Boolean
): EpisodeDownloadAction {
    if (downloading) return EpisodeDownloadAction.IN_PROGRESS
    if (episodeHasLocalDownload(episode)) return EpisodeDownloadAction.DELETE
    return EpisodeDownloadAction.DOWNLOAD
}

fun episodeHasLocalDownload(episode: EpisodeEntity): Boolean {
    return episode.downloadStatus == EpisodeEntity.STATUS_DONE || episode.localUri.isNotBlank()
}
