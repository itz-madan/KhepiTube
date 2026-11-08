package com.khepilabs.khepitube.innertube.models.body

import com.khepilabs.khepitube.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDeleteBody(
    val context: Context,
    val playlistId: String,
)
