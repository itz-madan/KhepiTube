package com.khepilabs.khepitube.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoDelete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.localmedia.LocalMediaItem
import com.khepilabs.khepitube.ui.components.shared.KhepiBottomSheet
import com.khepilabs.khepitube.ui.components.shared.KhepiEmptyState
import com.khepilabs.khepitube.ui.components.shared.KhepiSheetHeader
import com.khepilabs.khepitube.ui.components.shared.defaultSheetExpandedHeight
import com.khepilabs.khepitube.utils.formatDurationMillis
import java.util.Locale

private enum class TrashFilter { ALL, VIDEOS, MUSIC }

/**
 * Bottom sheet modal for full Trash Bin management of local video and audio files.
 * Provides item multi-selection, type filtering, storage usage summary, individual & bulk restore,
 * and permanent deletion confirmation dialogs.
 */
@Composable
internal fun LocalMediaTrashSheet(
    trashedItems: List<LocalMediaItem>,
    onRestore: (List<LocalMediaItem>) -> Unit,
    onDeletePermanent: (List<LocalMediaItem>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedFilter by rememberSaveable { mutableStateOf(TrashFilter.ALL) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var itemsToDelete by remember { mutableStateOf<List<LocalMediaItem>?>(null) }

    val videoCount = remember(trashedItems) { trashedItems.count { it.isVideo } }
    val musicCount = remember(trashedItems) { trashedItems.count { !it.isVideo } }

    val filteredItems =
        remember(trashedItems, selectedFilter) {
            when (selectedFilter) {
                TrashFilter.ALL -> trashedItems
                TrashFilter.VIDEOS -> trashedItems.filter { it.isVideo }
                TrashFilter.MUSIC -> trashedItems.filter { !it.isVideo }
            }
        }

    val totalBytes = remember(filteredItems) { filteredItems.sumOf { it.sizeBytes } }
    val formattedTotalSize = remember(totalBytes) { formatSize(totalBytes) }

    val selectedItems by remember(filteredItems, selectedIds) {
        derivedStateOf { filteredItems.filter { it.mediaId in selectedIds } }
    }

    KhepiBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
        expandedHeight = defaultSheetExpandedHeight(),
        dismissOnOutsideTap = false,
        shape = RectangleShape,
        containerColor = MaterialTheme.colorScheme.surface,
        header = { dragModifier ->
            KhepiSheetHeader(
                title = stringResource(R.string.local_trash_title),
                onClose = onDismiss,
                modifier = dragModifier,
                contentPadding = PaddingValues(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 8.dp),
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.local_trash_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            if (trashedItems.isNotEmpty()) {
                // Filter chips & total storage info
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedFilter == TrashFilter.ALL,
                            onClick = { selectedFilter = TrashFilter.ALL },
                            label = { Text(stringResource(R.string.local_trash_filter_all, trashedItems.size)) },
                        )
                        if (videoCount > 0) {
                            FilterChip(
                                selected = selectedFilter == TrashFilter.VIDEOS,
                                onClick = { selectedFilter = TrashFilter.VIDEOS },
                                label = { Text(stringResource(R.string.local_trash_filter_videos, videoCount)) },
                            )
                        }
                        if (musicCount > 0) {
                            FilterChip(
                                selected = selectedFilter == TrashFilter.MUSIC,
                                onClick = { selectedFilter = TrashFilter.MUSIC },
                                label = { Text(stringResource(R.string.local_trash_filter_music, musicCount)) },
                            )
                        }
                    }

                    if (totalBytes > 0) {
                        Text(
                            text = formattedTotalSize,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // Batch action bar
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (selectedIds.isNotEmpty()) {
                        Button(
                            onClick = {
                                onRestore(selectedItems)
                                selectedIds = emptySet()
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Outlined.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.local_trash_restore_selected, selectedIds.size))
                        }
                        OutlinedButton(
                            onClick = { itemsToDelete = selectedItems },
                            modifier = Modifier.weight(1f),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                        ) {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.local_trash_delete_selected, selectedIds.size))
                        }
                    } else {
                        Button(
                            onClick = { onRestore(filteredItems) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Outlined.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.local_trash_restore_all))
                        }
                        OutlinedButton(
                            onClick = { itemsToDelete = filteredItems },
                            modifier = Modifier.weight(1f),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                        ) {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.local_trash_empty_all))
                        }
                    }
                }

                // Selection toggle bar
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val allSelected = filteredItems.isNotEmpty() && selectedIds.size == filteredItems.size
                    TextButton(
                        onClick = {
                            selectedIds =
                                if (allSelected) {
                                    emptySet()
                                } else {
                                    filteredItems.mapTo(HashSet()) { it.mediaId }
                                }
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = if (allSelected) stringResource(R.string.deselect_all) else stringResource(R.string.local_trash_select_all),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    Text(
                        text = stringResource(R.string.local_trash_items_summary, filteredItems.size, formattedTotalSize),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = filteredItems,
                        key = { "trash-${it.id}" },
                    ) { item ->
                        val isSelected = item.mediaId in selectedIds
                        TrashItemRow(
                            item = item,
                            isSelected = isSelected,
                            onToggleSelect = {
                                selectedIds = if (isSelected) selectedIds - item.mediaId else selectedIds + item.mediaId
                            },
                            onRestore = { onRestore(listOf(item)) },
                            onDelete = { itemsToDelete = listOf(item) },
                        )
                    }
                }
            } else {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    KhepiEmptyState(
                        title = stringResource(R.string.local_trash_empty),
                        subtitle = stringResource(R.string.local_trash_subtitle),
                        icon = Icons.Outlined.AutoDelete,
                    )
                }
            }
        }
    }

    // Confirmation dialog for permanent deletion
    itemsToDelete?.let { targets ->
        AlertDialog(
            onDismissRequest = { itemsToDelete = null },
            title = { Text(stringResource(R.string.local_trash_confirm_delete_title)) },
            text = {
                val message =
                    if (targets.size == trashedItems.size && trashedItems.isNotEmpty()) {
                        stringResource(R.string.local_trash_confirm_empty_msg, targets.size)
                    } else {
                        stringResource(R.string.local_trash_confirm_delete_msg)
                    }
                Text(message)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePermanent(targets)
                        selectedIds = selectedIds - targets.map { it.mediaId }.toSet()
                        itemsToDelete = null
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                ) {
                    Text(stringResource(R.string.local_trash_delete_permanent))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemsToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun TrashItemRow(
    item: LocalMediaItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ).clickable(onClick = onToggleSelect)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelect() },
        )
        Icon(
            imageVector = if (item.isVideo) Icons.Outlined.Movie else Icons.Outlined.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val sub =
                buildList {
                    if (item.durationMs > 0) add(formatDurationMillis(item.durationMs))
                    if (item.sizeBytes > 0) add(formatSize(item.sizeBytes))
                }.joinToString(" • ")
            if (sub.isNotBlank()) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onRestore) {
            Icon(
                imageVector = Icons.Outlined.Restore,
                contentDescription = stringResource(R.string.local_trash_restore),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.DeleteForever,
                contentDescription = stringResource(R.string.local_trash_delete_permanent),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun formatSize(bytes: Long): String {
    val mb = bytes / (1024f * 1024f)
    return if (mb >= 1024f) {
        String.format(Locale.getDefault(), "%.1f GB", mb / 1024f)
    } else {
        String.format(Locale.getDefault(), "%.1f MB", mb)
    }
}
