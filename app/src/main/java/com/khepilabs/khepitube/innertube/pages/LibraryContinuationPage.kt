package com.khepilabs.khepitube.innertube.pages

import com.khepilabs.khepitube.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
