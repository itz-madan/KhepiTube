package com.khepilabs.khepitube.ui.screens.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.khepilabs.khepitube.data.update.UpdateRelease
import com.khepilabs.khepitube.data.update.UpdateRepository
import com.khepilabs.khepitube.ui.components.glass.GlassSurface
import com.khepilabs.khepitube.ui.components.shared.rememberKhepiWindowBlurEnabled
import kotlinx.coroutines.launch

@Composable
fun InAppUpdateDialog(
    release: UpdateRelease,
    repository: UpdateRepository,
    onDismiss: () -> Unit,
    onSkipVersion: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        val windowBlurEnabled = rememberKhepiWindowBlurEnabled()
        GlassSurface(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                alpha = if (windowBlurEnabled) 0.76f else 0.94f,
            ),
            borderWidth = 0.75.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "New Update Available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Version ${release.versionName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (!release.notes.isNullOrBlank()) {
                    Text(
                        text = "What's New:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                    ) {
                        Text(
                            text = release.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (isDownloading) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Downloading update...",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { onSkipVersion(release.versionName) }) {
                            Text(text = "Skip", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedButton(onClick = onDismiss) {
                            Text(text = "Later", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                isDownloading = true
                                coroutineScope.launch {
                                    val success = repository.downloadAndInstallApk(release)
                                    isDownloading = false
                                    if (success) onDismiss()
                                }
                            },
                        ) {
                            Text(text = "Update Now", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
