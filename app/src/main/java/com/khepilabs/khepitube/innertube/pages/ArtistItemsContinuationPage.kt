package com.khepilabs.khepitube.innertube.pages

import com.khepilabs.khepitube.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
