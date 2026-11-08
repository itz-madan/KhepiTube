package com.khepilabs.khepitube.innertube.models.response

import com.khepilabs.khepitube.innertube.models.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class ResolveUrlResponse(
    val endpoint: NavigationEndpoint? = null,
)
