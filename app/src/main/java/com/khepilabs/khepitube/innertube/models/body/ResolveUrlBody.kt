package com.khepilabs.khepitube.innertube.models.body

import com.khepilabs.khepitube.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class ResolveUrlBody(
    val context: Context,
    val url: String,
)
