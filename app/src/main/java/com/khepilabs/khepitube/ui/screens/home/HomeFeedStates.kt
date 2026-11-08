package com.khepilabs.khepitube.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.ui.components.shared.KhepiEmptyState

@Composable
internal fun HomeFeedDisabledState(modifier: Modifier = Modifier) {
    KhepiEmptyState(
        title = stringResource(R.string.content_settings_home_feed_disabled_title),
        subtitle = stringResource(R.string.content_settings_home_feed_disabled_body),
        icon = Icons.Outlined.SmartDisplay,
        modifier = modifier,
    )
}
