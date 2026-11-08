package com.khepilabs.khepitube.innertube.pages

import com.khepilabs.khepitube.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
