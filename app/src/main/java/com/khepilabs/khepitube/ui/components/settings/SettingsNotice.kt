package com.khepilabs.khepitube.ui.components.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.ui.components.glass.GlassNotice

private val FootnotePadding = 16.dp
private val FootnoteTopPadding = 6.dp

/** Explanatory text under a group, lined up with the row titles above it. */
@Composable
fun SettingsFootnote(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = FootnotePadding, end = FootnotePadding, top = FootnoteTopPadding),
    )
}

/**
 * A standalone note on a page: what a setting costs, a restart it needs, a risk it carries. Delegated
 * to [GlassNotice] for frosted glass styling.
 */
@Composable
fun SettingsNotice(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String? = null,
    isWarning: Boolean = false,
) {
    GlassNotice(
        text = text,
        modifier = modifier,
        icon = icon,
        title = title,
        isWarning = isWarning,
    )
}
