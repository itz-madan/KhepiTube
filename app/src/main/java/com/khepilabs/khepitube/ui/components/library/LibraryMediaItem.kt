package com.khepilabs.khepitube.ui.components.library

import com.khepilabs.khepitube.data.local.LikedVideoInfo
import com.khepilabs.khepitube.data.local.VideoHistoryEntry
import com.khepilabs.khepitube.data.model.Video
import com.khepilabs.khepitube.data.model.toMusicTrack
import com.khepilabs.khepitube.data.model.toVideo
import com.khepilabs.khepitube.data.music.DownloadedTrack
import com.khepilabs.khepitube.data.music.model.MusicTrack
import com.khepilabs.khepitube.data.video.DownloadedVideo

internal const val LIBRARY_SHELF_ITEM_LIMIT = 20

internal sealed interface LibraryMediaItem {
    val key: String

    data class VideoItem(
        val video: Video,
    ) : LibraryMediaItem {
        override val key: String = "video:${video.id}"
    }

    data class MusicItem(
        val track: MusicTrack,
    ) : LibraryMediaItem {
        override val key: String = "music:${track.videoId}"
    }

    data class DownloadedVideoItem(
        val download: DownloadedVideo,
    ) : LibraryMediaItem {
        override val key: String = "downloaded-video:${download.video.id}"
    }

    data class DownloadedMusicItem(
        val download: DownloadedTrack,
    ) : LibraryMediaItem {
        override val key: String = "downloaded-music:${download.track.videoId}"
    }
}

internal fun VideoHistoryEntry.toLibraryMediaItem(): LibraryMediaItem =
    if (isMusic) {
        LibraryMediaItem.MusicItem(toMusicTrack())
    } else {
        LibraryMediaItem.VideoItem(toVideo())
    }

internal fun LikedVideoInfo.toLibraryMediaItem(): LibraryMediaItem =
    if (isMusic) {
        LibraryMediaItem.MusicItem(toMusicTrack())
    } else {
        LibraryMediaItem.VideoItem(toVideo())
    }
