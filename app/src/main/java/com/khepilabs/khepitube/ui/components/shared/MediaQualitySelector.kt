package com.khepilabs.khepitube.ui.components.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.khepilabs.khepitube.ui.components.glass.GlassSurface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class MediaQualitySelectorOption<T>(
    val item: T,
    val height: Int,
    val label: String,
    val selected: Boolean,
    val supportingText: String? = null,
    val codecKey: String = "",
    val codecLabel: String = "",
)

@Composable
fun <T> MediaQualitySelectorContent(
    options: List<MediaQualitySelectorOption<T>>,
    groupedByResolution: Boolean,
    onOptionSelected: (T) -> Unit,
) {
    // Minimal merged quality options: Auto + one best stream option per resolution
    val auto = options.firstOrNull { it.height == 0 }
    val resolutionGrouped = options
        .filter { it.height != 0 }
        .groupBy { it.height }
        .entries
        .sortedByDescending { it.key }
        .map { (_, groupOptions) ->
            // Pick the selected option if any in this resolution group is selected, otherwise pick best
            groupOptions.firstOrNull { it.selected } ?: groupOptions.first()
        }

    val finalOptions = buildList {
        if (auto != null) add(auto)
        addAll(resolutionGrouped)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        finalOptions.forEach { option ->
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(option.item) },
                shape = RoundedCornerShape(16.dp),
                containerColor = if (option.selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.34f)
                },
                borderWidth = 0.75.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (option.selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (option.selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                        if (!option.supportingText.isNullOrBlank()) {
                            Text(
                                text = option.supportingText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (option.selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                    if (option.selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
