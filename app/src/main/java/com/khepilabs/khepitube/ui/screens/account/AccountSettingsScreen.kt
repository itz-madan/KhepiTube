package com.khepilabs.khepitube.ui.screens.account

import android.webkit.CookieManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.local.PlayerPreferences
import com.khepilabs.khepitube.innertube.YouTube
import com.khepilabs.khepitube.innertube.models.AccountInfo
import com.khepilabs.khepitube.ui.components.settings.SettingsPage
import com.khepilabs.khepitube.ui.components.shared.ChannelAvatarImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AccountSettingsScreen(
    onBack: (() -> Unit)?,
    highlight: String? = null,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences = remember { PlayerPreferences(context) }
    val youtubeCookie by preferences.youtubeCookie.collectAsStateWithLifecycle(initialValue = null)

    var accountInfo by remember { mutableStateOf<AccountInfo?>(null) }
    var isLoadingAccount by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    LaunchedEffect(youtubeCookie) {
        if (!youtubeCookie.isNullOrBlank()) {
            isLoadingAccount = true
            withContext(Dispatchers.IO) {
                YouTube.cookie = youtubeCookie
                val result = YouTube.accountInfo()
                withContext(Dispatchers.Main) {
                    accountInfo = result.getOrNull()
                    isLoadingAccount = false
                }
            }
        } else {
            accountInfo = null
            isLoadingAccount = false
        }
    }

    SettingsPage(
        title = stringResource(R.string.scrobbling_account),
        onBack = onBack,
        highlight = highlight,
    ) {
        group(key = "account.main", header = R.string.scrobbling_account) {
            row("account.status") { shape ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (!youtubeCookie.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                val avatarUrl = accountInfo?.thumbnailUrl.orEmpty()
                                val accountName = accountInfo?.name ?: stringResource(R.string.account_signed_in)
                                ChannelAvatarImage(
                                    url = avatarUrl,
                                    contentDescription = accountName,
                                    modifier = Modifier.size(56.dp),
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = accountName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = stringResource(R.string.account_signed_in),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        preferences.setYoutubeCookie(null)
                                        YouTube.cookie = null
                                        CookieManager.getInstance().removeAllCookies(null)
                                        CookieManager.getInstance().flush()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                    ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error,
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                )
                                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                                Text(stringResource(R.string.account_sign_out))
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.account_not_signed_in),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                            Button(
                                onClick = { showLoginDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                )
                                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                                Text(stringResource(R.string.account_sign_in_title))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLoginDialog) {
        YoutubeLoginDialog(
            onDismiss = { showLoginDialog = false },
            onLoginSuccess = { cookie ->
                coroutineScope.launch {
                    preferences.setYoutubeCookie(cookie)
                    YouTube.cookie = cookie
                    showLoginDialog = false
                }
            },
        )
    }
}
