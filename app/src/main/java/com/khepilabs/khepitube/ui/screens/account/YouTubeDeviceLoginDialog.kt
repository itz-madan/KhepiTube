package com.khepilabs.khepitube.ui.screens.account

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.data.auth.YouTubeDeviceAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
internal fun YouTubeDeviceLoginDialog(
    onDismiss: () -> Unit,
    onApproved: (YouTubeDeviceAuth.SignedInAccount) -> Unit,
    onUseWebViewFallback: () -> Unit,
) {
    val context = LocalContext.current
    val auth = remember { YouTubeDeviceAuth(context) }
    var deviceCode by remember { mutableStateOf<YouTubeDeviceAuth.DeviceCode?>(null) }
    var status by remember { mutableStateOf("Connecting to YouTube…") }
    var error by remember { mutableStateOf<String?>(null) }
    var isPolling by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val code = withContext(Dispatchers.IO) { auth.requestDeviceCode() }
            deviceCode = code
            status = "Open the activation page and approve this device. This screen will update automatically."
            isPolling = true
            val deadline = System.currentTimeMillis() + code.expiresInSeconds * 1000L
            var interval = code.pollIntervalSeconds.coerceAtLeast(1L)
            while (isPolling && System.currentTimeMillis() < deadline) {
                delay(interval * 1000L)
                val approved = withContext(Dispatchers.IO) { auth.pollApproval(code.deviceCode) }
                if (approved != null) {
                    isPolling = false
                    status = "Account connected."
                    onApproved(approved)
                    break
                }
            }
            if (isPolling) {
                isPolling = false
                error = "This activation code expired. Start sign-in again."
            }
        } catch (e: Exception) {
            isPolling = false
            error = e.message ?: "Could not start YouTube sign-in."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect YouTube account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(status, style = MaterialTheme.typography.bodyMedium)
                deviceCode?.let { code ->
                    Text("Your activation code", style = MaterialTheme.typography.labelLarge)
                    Text(
                        code.userCode,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text("Tap below to open YouTube with this code pre-filled.")
                }
                if (isPolling) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator()
                        Text("Waiting for Google approval…")
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                deviceCode?.let { code ->
                    Button(
                        onClick = {
                            val url = YouTubeActivationLink.forUserCode(code.userCode)
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Open activation page") }
                }
                OutlinedButton(
                    onClick = {
                        isPolling = false
                        onUseWebViewFallback()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Use browser sign-in instead") }
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }
        },
    )
}
