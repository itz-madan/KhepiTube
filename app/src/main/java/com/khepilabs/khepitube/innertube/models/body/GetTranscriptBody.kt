package com.khepilabs.khepitube.innertube.models.body

import com.khepilabs.khepitube.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)
