package com.khepilabs.khepitube.ui.screens.library

import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.ui.components.glass.GlassSurface
import com.khepilabs.khepitube.data.model.Video
import com.khepilabs.khepitube.data.music.DownloadedTrack
import com.khepilabs.khepitube.data.music.model.MusicTrack
import com.khepilabs.khepitube.data.video.DownloadedVideo
import com.khepilabs.khepitube.innertube.models.AccountInfo
import com.khepilabs.khepitube.ui.OnTabReselected
import com.khepilabs.khepitube.ui.components.layout.khepiBottomContentPadding
import com.khepilabs.khepitube.ui.components.layout.navigation.KhepiTab
import com.khepilabs.khepitube.ui.components.layout.topbar.KhepiTopBar
import com.khepilabs.khepitube.ui.components.library.LibraryNavigationRow
import com.khepilabs.khepitube.ui.components.library.LibrarySection
import com.khepilabs.khepitube.ui.components.shared.ChannelAvatarImage
import com.khepilabs.khepitube.ui.components.shared.KhepiEmptyState
import com.khepilabs.khepitube.ui.components.shared.KhepiMaxContentWidth
import com.khepilabs.khepitube.ui.components.shared.MediaKind
import com.khepilabs.khepitube.ui.screens.account.YoutubeLoginDialog

private val ListVerticalPadding = 12.dp
private val ShelfSpacing = 24.dp

@Composable
fun LibraryScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToPlaylists: (MediaKind?) -> Unit,
    onNavigateToLikedVideos: () -> Unit,
    onNavigateToLikedMusic: () -> Unit,
    onNavigateToWatchLater: () -> Unit,
    onNavigateToSavedShorts: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToLocalMedia: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onManageData: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onMusicClick: (MusicTrack, List<MusicTrack>, String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onMusicPlaylistClick: (String) -> Unit,
    onDownloadedVideoClick: (List<DownloadedVideo>, Int) -> Unit,
    onDownloadedMusicClick: (List<DownloadedTrack>, Int) -> Unit,
    onSavedShortClick: (Video) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val shortsEnabled by viewModel.shortsEnabled.collectAsStateWithLifecycle()
    val shelfPreviewsEnabled by viewModel.shelfPreviewsEnabled.collectAsStateWithLifecycle()
    val separatePlaylistKinds by viewModel.separatePlaylistKinds.collectAsStateWithLifecycle()
    val isLibraryEmpty by viewModel.isLibraryEmpty.collectAsStateWithLifecycle()
    val hiddenSections by viewModel.hiddenSections.collectAsStateWithLifecycle()
    val youtubeCookie by viewModel.youtubeCookie.collectAsStateWithLifecycle()
    val accountInfo by viewModel.accountInfo.collectAsStateWithLifecycle()

    var showLoginDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Check cookies when returning from Chrome browser
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val cookieManager = CookieManager.getInstance()
                val cookies = cookieManager.getCookie("https://www.youtube.com")
                if (!cookies.isNullOrBlank() && cookies.contains("SAPISID")) {
                    viewModel.setYoutubeCookie(cookies)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val listState = rememberLazyListState()
    OnTabReselected(KhepiTab.Library.route) { listState.animateScrollToItem(0) }
    val openSection: (LibrarySection) -> Unit = { section ->
        when (section) {
            LibrarySection.HISTORY -> onNavigateToHistory()
            LibrarySection.PLAYLISTS -> onNavigateToPlaylists(null)
            LibrarySection.VIDEO_PLAYLISTS -> onNavigateToPlaylists(MediaKind.Videos)
            LibrarySection.MUSIC_PLAYLISTS -> onNavigateToPlaylists(MediaKind.Music)
            LibrarySection.WATCH_LATER -> onNavigateToWatchLater()
            LibrarySection.LIKED_VIDEOS -> onNavigateToLikedVideos()
            LibrarySection.LIKED_MUSIC -> onNavigateToLikedMusic()
            LibrarySection.DOWNLOADS -> onNavigateToDownloads()
            LibrarySection.SAVED_SHORTS -> onNavigateToSavedShorts()
            LibrarySection.LOCAL_MEDIA -> onNavigateToLocalMedia()
            LibrarySection.NOTES -> onNavigateToNotes()
            LibrarySection.SETTINGS -> onManageData()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = { KhepiTopBar(title = stringResource(R.string.library)) },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(top = ListVerticalPadding, bottom = khepiBottomContentPadding(ListVerticalPadding)),
            verticalArrangement = Arrangement.spacedBy(ShelfSpacing),
        ) {
            // YouTube Account Card at top of Library
            item(key = "youtube-account-card", contentType = "account") {
                YouTubeAccountCard(
                    youtubeCookie = youtubeCookie,
                    accountInfo = accountInfo,
                    onSignInChrome = {
                        runCatching {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://accounts.google.com/AccountChooser?service=youtube&continue=https://www.youtube.com/"),
                            )
                            context.startActivity(intent)
                        }.onFailure {
                            showLoginDialog = true
                        }
                    },
                    onSignInDialog = { showLoginDialog = true },
                    onSignOut = {
                        viewModel.setYoutubeCookie(null)
                        CookieManager.getInstance().removeAllCookies(null)
                        CookieManager.getInstance().flush()
                    },
                )
            }

            val hidden = hiddenSections ?: return@LazyColumn
            val contentSections = visibleContentSections(hidden, separatePlaylistKinds, shortsEnabled)
            val settingsSections = visibleSettingsSections(hidden)

            if (shelfPreviewsEnabled && isLibraryEmpty) {
                item(key = "library-empty", contentType = "empty") {
                    KhepiEmptyState(
                        title = stringResource(R.string.library_empty_title),
                        subtitle = stringResource(R.string.library_empty_body),
                        icon = Icons.Outlined.VideoLibrary,
                    )
                }
            } else if (shelfPreviewsEnabled) {
                libraryShelves(
                    viewModel = viewModel,
                    sections = contentSections,
                    onOpenSection = openSection,
                    onVideoClick = onVideoClick,
                    onMusicClick = onMusicClick,
                    onPlaylistClick = onPlaylistClick,
                    onMusicPlaylistClick = onMusicPlaylistClick,
                    onDownloadedVideoClick = onDownloadedVideoClick,
                    onDownloadedMusicClick = onDownloadedMusicClick,
                    onSavedShortClick = onSavedShortClick,
                )
            } else if (contentSections.isNotEmpty()) {
                item(key = "sections", contentType = "navigation-section") {
                    val counts by viewModel.counts.collectAsStateWithLifecycle()
                    LibrarySectionList(sections = contentSections, counts = counts, onOpenSection = openSection)
                }
            }

            // An empty library has no shelves, but downloads from an earlier install can still be found.
            val emptyLibraryDownloads = shelfPreviewsEnabled && isLibraryEmpty && LibrarySection.DOWNLOADS in contentSections
            if (emptyLibraryDownloads || settingsSections.isNotEmpty()) {
                item(key = "settings-data", contentType = "navigation-section") {
                    Column(modifier = Modifier.widthIn(max = KhepiMaxContentWidth).padding(horizontal = 16.dp)) {
                        LibrarySectionHeader(stringResource(R.string.library_settings_data_header))
                        if (emptyLibraryDownloads) {
                            LibraryNavigationRow(
                                icon = LibrarySection.DOWNLOADS.icon,
                                title = LibrarySection.DOWNLOADS.title,
                                subtitle = stringResource(R.string.empty_downloads),
                                onClick = onNavigateToDownloads,
                            )
                        }
                        settingsSections.forEach { section ->
                            LibrarySectionRow(section = section, counts = null, onClick = { openSection(section) })
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showLoginDialog) {
        YoutubeLoginDialog(
            onDismiss = { showLoginDialog = false },
            onLoginSuccess = { cookie ->
                viewModel.setYoutubeCookie(cookie)
                showLoginDialog = false
            },
        )
    }
}

@Composable
private fun YouTubeAccountCard(
    youtubeCookie: String?,
    accountInfo: AccountInfo?,
    onSignInChrome: () -> Unit,
    onSignInDialog: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            )
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.62f),
        borderWidth = 0.75.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        modifier = Modifier.size(52.dp),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = accountName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = accountInfo?.channelHandle ?: stringResource(R.string.account_signed_in),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.account_sign_out), fontSize = 12.sp)
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.account_sign_in_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Sync YouTube playlists, likes & subscriptions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onSignInChrome,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Google Sign-In", fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onSignInDialog,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(text = "Web Login", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

internal fun LazyListScope.libraryShelves(
    viewModel: LibraryViewModel,
    sections: List<LibrarySection>,
    onOpenSection: (LibrarySection) -> Unit,
    onVideoClick: (Video) -> Unit,
    onMusicClick: (MusicTrack, List<MusicTrack>, String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onMusicPlaylistClick: (String) -> Unit,
    onDownloadedVideoClick: (List<DownloadedVideo>, Int) -> Unit,
    onDownloadedMusicClick: (List<DownloadedTrack>, Int) -> Unit,
    onSavedShortClick: (Video) -> Unit,
) {
    sections.forEach { section ->
        val onTitleClick = { onOpenSection(section) }
        when (section) {
            LibrarySection.HISTORY, LibrarySection.LIKED_MUSIC -> {
                item(key = section.name, contentType = "media-shelf") {
                    LibraryMediaShelfRoute(
                        section = section,
                        itemsFlow = if (section == LibrarySection.HISTORY) viewModel.history else viewModel.likedMusic,
                        onTitleClick = onTitleClick,
                        onVideoClick = onVideoClick,
                        onMusicClick = onMusicClick,
                        onDownloadedVideoClick = onDownloadedVideoClick,
                        onDownloadedMusicClick = onDownloadedMusicClick,
                    )
                }
            }

            LibrarySection.PLAYLISTS, LibrarySection.VIDEO_PLAYLISTS, LibrarySection.MUSIC_PLAYLISTS -> {
                item(key = section.name, contentType = "playlist-shelf") {
                    LibraryPlaylistsShelf(
                        section = section,
                        videoPlaylistsFlow = viewModel.playlists.takeIf { section != LibrarySection.MUSIC_PLAYLISTS },
                        musicPlaylistsFlow = viewModel.musicPlaylists.takeIf { section != LibrarySection.VIDEO_PLAYLISTS },
                        onTitleClick = onTitleClick,
                        onVideoPlaylistClick = onPlaylistClick,
                        onMusicPlaylistClick = onMusicPlaylistClick,
                    )
                }
            }

            LibrarySection.WATCH_LATER, LibrarySection.LIKED_VIDEOS -> {
                item(key = section.name, contentType = "video-shelf") {
                    LibraryVideoShelf(
                        section = section,
                        videosFlow = if (section == LibrarySection.WATCH_LATER) viewModel.watchLater else viewModel.likedVideos,
                        onTitleClick = onTitleClick,
                        onVideoClick = onVideoClick,
                    )
                }
            }

            LibrarySection.DOWNLOADS -> {
                item(key = section.name, contentType = "media-shelf") {
                    val activeDownloads by viewModel.activeDownloadCount.collectAsStateWithLifecycle()
                    LibraryMediaShelfRoute(
                        section = section,
                        itemsFlow = viewModel.downloads,
                        onTitleClick = onTitleClick,
                        onVideoClick = onVideoClick,
                        onMusicClick = onMusicClick,
                        onDownloadedVideoClick = onDownloadedVideoClick,
                        onDownloadedMusicClick = onDownloadedMusicClick,
                        emptyMessage =
                            stringResource(
                                if ((activeDownloads ?: 0) > 0) R.string.downloads_section_in_progress else R.string.empty_downloads,
                            ),
                    )
                }
            }

            LibrarySection.SAVED_SHORTS -> {
                item(key = section.name, contentType = "shorts-shelf") {
                    LibraryShortsShelfRoute(
                        section = section,
                        shortsFlow = viewModel.savedShorts,
                        onTitleClick = onTitleClick,
                        onShortClick = onSavedShortClick,
                    )
                }
            }

            LibrarySection.LOCAL_MEDIA, LibrarySection.NOTES, LibrarySection.SETTINGS -> {
                Unit
            }
        }
    }
}
