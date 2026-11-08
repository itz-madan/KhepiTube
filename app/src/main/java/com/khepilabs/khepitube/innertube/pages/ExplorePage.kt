package com.khepilabs.khepitube.innertube.pages

import com.khepilabs.khepitube.innertube.models.AlbumItem

data class ExplorePage(
    val newReleaseAlbums: List<AlbumItem>,
    val moodAndGenres: List<MoodAndGenres.Item>,
)
