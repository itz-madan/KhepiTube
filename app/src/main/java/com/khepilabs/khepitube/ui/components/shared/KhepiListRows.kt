package com.khepilabs.khepitube.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.khepilabs.khepitube.ui.components.glass.GlassSurface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Enables the settings-only translucent row treatment without changing shared rows elsewhere. */
val LocalKhepiGlassRows = staticCompositionLocalOf { false }

@Composable
private fun khepiGlassRowFill() = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.62f)

@Composable
private fun khepiGlassRowBorder() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)

@Composable
private fun SettingsGlassRowFrame(
    modifier: Modifier,
    shape: Shape,
    glass: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    content: @Composable BoxScope.() -> Unit,
) {
    if (glass) {
        GlassSurface(
            modifier = modifier,
            shape = shape,
            containerColor = containerColor,
            borderColor = khepiGlassRowBorder(),
            content = content,
        )
    } else {
        Box(modifier = modifier.clip(shape).background(containerColor), content = content)
    }
}

fun khepiRowGroupShape(
    index: Int,
    count: Int,
): Shape {
    if (count <= 1) return RoundedCornerShape(16.dp)
    return when (index) {
        0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        count - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(4.dp)
    }
}

@Composable
fun KhepiRowGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        content()
    }
}

@Composable
fun KhepiSelectionRow(
    title: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    showSelectedContainer: Boolean = false,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    val glass = LocalKhepiGlassRows.current
    val fill = if (selected && showSelectedContainer) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (glass) 0.72f else 1f)
    } else if (glass) khepiGlassRowFill() else MaterialTheme.colorScheme.surfaceContainerHigh
    SettingsGlassRowFrame(
        modifier = modifier.fillMaxWidth().clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick),
        shape = shape,
        glass = glass,
        containerColor = fill,
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                if (supportingText != null) Text(supportingText, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(selected = selected, onClick = null, enabled = enabled)
        }
    }
}

@Composable
fun KhepiNavRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingText: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    showChevron: Boolean = true,
    destructive: Boolean = false,
    stateDescription: String? = null,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    val textColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val glass = LocalKhepiGlassRows.current
    SettingsGlassRowFrame(
        modifier = modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        glass = glass,
        containerColor = if (glass) KhepiGlassRowFill else MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(Modifier.width(12.dp))
            } else if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null,
                    tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                if (supportingText != null) Text(supportingText, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (trailingContent != null) {
                trailingContent()
                if (showChevron) Spacer(Modifier.width(8.dp))
            } else if (trailingText != null) {
                Text(trailingText, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (showChevron) Spacer(Modifier.width(8.dp))
            }
            if (showChevron) Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun KhepiSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    leadingPainter: Any? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    val glass = LocalKhepiGlassRows.current
    SettingsGlassRowFrame(
        modifier = modifier.fillMaxWidth().clickable(enabled = enabled, role = Role.Switch) { onCheckedChange(!checked) },
        shape = shape,
        glass = glass,
        containerColor = if (glass) KhepiGlassRowFill else MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(Modifier.width(12.dp))
            } else if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (supportingText != null) Text(supportingText, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    }
}

@Composable
fun KhepiSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
