package com.khepilabs.khepitube.data.update

import androidx.compose.runtime.Immutable

/**
 * A release fetched from GitHub Releases API for KhepiTube.
 */
@Immutable
data class UpdateRelease(
    val tagName: String,
    val versionName: String,
    val title: String,
    val notes: String,
    val publishedAt: String,
    val downloadUrl: String?,
    val htmlUrl: String,
    val isNewer: Boolean,
)
