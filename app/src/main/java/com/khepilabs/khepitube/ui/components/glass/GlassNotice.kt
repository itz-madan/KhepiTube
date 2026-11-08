package com.khepilabs.khepitube.ui.components.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val NoticePadding = 16.dp
private val NoticeSpacing = 12.dp
private val NoticeIconSize = 20.dp
private val NoticeTopPadding = 8.dp

/** A Liquid Glass notification or alert card. */
@Composable
fun GlassNotice(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String? = null,
    isWarning: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val containerColor =
        if (isWarning) colors.errorContainer.copy(alpha = 0.5f) else colors.surfaceContainerHigh.copy(alpha = 0.6f)
    val textColor = if (isWarning) colors.onErrorContainer else colors.onSurfaceVariant

    GlassSurface(
        shape = MaterialTheme.shapes.large,
        containerColor = containerColor,
        borderColor = if (isWarning) colors.error else colors.outlineVariant,
        modifier = modifier.fillMaxWidth().padding(top = NoticeTopPadding),
    ) {
        Row(
            modifier = Modifier.padding(NoticePadding),
            horizontalArrangement = Arrangement.spacedBy(NoticeSpacing),
            verticalAlignment = if (title == null) Alignment.CenterVertically else Alignment.Top,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(NoticeIconSize),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isWarning) colors.onErrorContainer else colors.onSurface,
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                )
            }
        }
    }
}
