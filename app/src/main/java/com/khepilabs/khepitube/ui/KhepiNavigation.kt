package com.khepilabs.khepitube.ui

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.local.PlaylistRepository
import com.khepilabs.khepitube.data.localmedia.LocalMediaIds
import com.khepilabs.khepitube.data.localmedia.toMusicTrack
import com.khepilabs.khepitube.data.localmedia.toVideo
import com.khepilabs.khepitube.data.model.Video
import com.khepilabs.khepitube.data.music.model.toMusicTrack
import com.khepilabs.khepitube.data.music.model.toVideo
import com.khepilabs.khepitube.data.shorts.queue.ShortsQueueSource
import com.khepilabs.khepitube.player.GlobalPlayerState
import com.khepilabs.khepitube.ui.components.layout.LocalKhepiBottomInsets
import com.khepilabs.khepitube.ui.components.layout.navigation.MediaNavigator
import com.khepilabs.khepitube.ui.components.musicplayer.sheet.MusicPlayerSheetState
import com.khepilabs.khepitube.ui.components.settings.SettingsDestination
import com.khepilabs.khepitube.ui.components.settings.SettingsTarget
import com.khepilabs.khepitube.ui.components.shared.MediaKind
import com.khepilabs.khepitube.ui.components.videoplayer.PlayerDraggableState
import com.khepilabs.khepitube.ui.screens.categories.CategoriesScreen
import com.khepilabs.khepitube.ui.screens.channel.ChannelScreen
import com.khepilabs.khepitube.ui.screens.equalizer.EqualizerScreen
import com.khepilabs.khepitube.ui.screens.history.HistoryScreen
import com.khepilabs.khepitube.ui.screens.home.HomeScreen
import com.khepilabs.khepitube.ui.screens.home.HomeViewModel
import com.khepilabs.khepitube.ui.screens.library.DownloadsScreen
import com.khepilabs.khepitube.ui.screens.library.LibraryScreen
import com.khepilabs.khepitube.ui.screens.library.LocalMediaScreen
import com.khepilabs.khepitube.ui.screens.library.SavedShortsGridScreen
import com.khepilabs.khepitube.ui.screens.music.sharedMusicPlayerViewModel
import com.khepilabs.khepitube.ui.screens.notes.NotesScreen
import com.khepilabs.khepitube.ui.screens.notifications.NotificationScreen
import com.khepilabs.khepitube.ui.screens.onboarding.OnboardingScreen
import com.khepilabs.khepitube.ui.screens.player.VideoPlayerViewModel
import com.khepilabs.khepitube.ui.screens.player.state.VideoPlayerUiState
import com.khepilabs.khepitube.ui.screens.player.state.blankVideo
import com.khepilabs.khepitube.ui.screens.playlists.PlaylistDetailScreen
import com.khepilabs.khepitube.ui.screens.playlists.PlaylistsScreen
import com.khepilabs.khepitube.ui.screens.recap.RecapRoutes
import com.khepilabs.khepitube.ui.screens.recap.RecapScreen
import com.khepilabs.khepitube.ui.screens.recap.story.RecapStoryScreen
import com.khepilabs.khepitube.ui.screens.search.SearchScreen
import com.khepilabs.khepitube.ui.screens.settings.SettingsHost
import com.khepilabs.khepitube.ui.screens.shorts.ShortsScreen
import com.khepilabs.khepitube.ui.screens.subscriptions.SubscriptionsScreen
import kotlinx.coroutines.flow.first

@UnstableApi
fun NavGraphBuilder.khepiAppGraph(
    navController: NavHostController,
    mediaNavigator: MediaNavigator,
    currentRoute: MutableState<String>,
    playerSheetState: PlayerDraggableState,
    musicPlayerSheetState: MusicPlayerSheetState,
    homeViewModel: HomeViewModel,
    playerViewModel: VideoPlayerViewModel,
    playerUiStateResult: State<VideoPlayerUiState>,
    playerVisibleState: MutableState<Boolean>,
    disableShortsPlayer: Boolean = false,
    defaultStartRoute: String = "home",
    onMusicStarted: () -> Unit = {},
) {
    composable("onboarding") {
        currentRoute.value = "onboarding"
        OnboardingScreen(
            onComplete = {
                navController.navigate(defaultStartRoute) {
                    popUpTo("onboarding") { inclusive = true }
                }
            },
        )
    }

    composable("home") {
        currentRoute.value = "home"
        HomeScreen(
            onVideoClick = { video ->
                navController.openVideoOrShorts(video, disableShortsPlayer) {
                    playerViewModel.playVideo(it)
                    GlobalPlayerState.setCurrentVideo(it)
                }
            },
            onShortClick = { source ->
                navController.openShortsOrPlayer(source, disableShortsPlayer)
            },
            onSearchClick = {
                navController.navigate("search")
            },
            onNavigateToHistory = {
                navController.navigate("history")
            },
            onOpenShortsFeed = {
                navController.openShorts(ShortsQueueSource.Feed)
            },
            onPlayMix = { videos, title -> playerViewModel.playPlaylist(videos, 0, title, false) },
            viewModel = homeViewModel,
        )
    }

    composable("notifications") {
        currentRoute.value = "notifications"
        NotificationScreen(
            onBackClick = { navController.popBackStack() },
            onNotificationClick = { videoId ->
                navController.navigateToPlayer(videoId)
            },
            onOpenSettings = {
                navController.navigate("settings?target=${SettingsTarget(SettingsDestination.NOTIFICATIONS).encode()}")
            },
        )
    }

    composable(
        route = SHORTS_ROUTE_PATTERN,
        arguments =
            listOf(
                navArgument(SHORTS_ROUTE_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
    ) { backStackEntry ->
        currentRoute.value = SHORTS_ROUTE_KEY
        val source = ShortsQueueSource.decode(backStackEntry.arguments?.getString(SHORTS_ROUTE_ARG))
        val isRootTab = source == ShortsQueueSource.Feed
        ShortsScreen(
            source = source,
            bottomNavOverlayPadding = if (isRootTab) LocalKhepiBottomInsets.current.barBottom else 0.dp,
            onBack = {
                navController.popBackStack()
            },
            onChannelClick = { channelId ->
                mediaNavigator.openChannel(channelId)
            },
        )
    }

    composable("subscriptions") { backStackEntry ->
        currentRoute.value = "subscriptions"
        val openMusic by backStackEntry.savedStateHandle
            .getStateFlow(OPEN_MUSIC_SUBSCRIPTIONS, false)
            .collectAsStateWithLifecycle()
        SubscriptionsScreen(
            openMusicSubscriptions = openMusic,
            onMusicSubscriptionsOpened = { backStackEntry.savedStateHandle[OPEN_MUSIC_SUBSCRIPTIONS] = false },
            onVideoClick = { video ->
                navController.openVideoOrShorts(video, disableShortsPlayer) {
                    playerViewModel.playVideo(it)
                    GlobalPlayerState.setCurrentVideo(it)
                }
            },
            onShortClick = { source ->
                navController.openShortsOrPlayer(source, disableShortsPlayer)
            },
            onChannelClick = { channel ->
                if (channel.isMusic && channel.id.isNotBlank()) {
                    mediaNavigator.openArtist(channel.id)
                } else {
                    mediaNavigator.openChannel(channel.url.ifBlank { channel.id })
                }
            },
        )
    }

    composable("library") {
        currentRoute.value = "library"
        val musicPlayerViewModel = sharedMusicPlayerViewModel()
        val downloadsSourceName =
            stringResource(
                R.string.library_downloads_label,
            )
        LibraryScreen(
            onOpenRecap = { period -> navController.navigate(period?.let { RecapRoutes.story(it) } ?: RecapRoutes.stats()) },
            onNavigateToHistory = {
                navController.navigate("history")
            },
            onNavigateToPlaylists = { kind ->
                navController.navigate(if (kind == null) "playlists" else "playlists?kind=${kind.name}")
            },
            onNavigateToLikedVideos = {
                navController.navigate("playlist/${PlaylistRepository.LIKED_VIDEOS_ID}")
            },
            onNavigateToLikedMusic = {
                mediaNavigator.openMusicPlaylist(PlaylistRepository.LIKED_MUSIC_ID)
            },
            onNavigateToWatchLater = {
                navController.navigate("playlist/${PlaylistRepository.WATCH_LATER_ID}")
            },
            onNavigateToSavedShorts = {
                navController.navigate("savedShorts")
            },
            onNavigateToDownloads = {
                navController.navigate("downloads")
            },
            onNavigateToLocalMedia = {
                navController.navigate("localMedia")
            },
            onNavigateToNotes = {
                navController.navigate("notes")
            },
            onManageData = {
                navController.navigate("settings")
            },
            onVideoClick = { video ->
                navController.openVideoOrShorts(video, disableShortsPlayer) { playerViewModel.playVideo(it) }
            },
            onMusicClick = { track, queue, sourceName ->
                musicPlayerViewModel.loadAndPlayTrack(track, queue, sourceName)
                onMusicStarted()
            },
            onPlaylistClick = { playlistId ->
                navController.navigate("playlist/$playlistId")
            },
            onMusicPlaylistClick = { playlistId ->
                mediaNavigator.openMusicPlaylist(playlistId)
            },
            onDownloadedVideoClick = { videos, index ->
                val videoList = videos.map { it.video }
                playerViewModel.playPlaylist(videoList, index, downloadsSourceName)
                GlobalPlayerState.setCurrentVideo(videoList[index])
            },
            onDownloadedMusicClick = { tracks, index ->
                val musicTracks = tracks.map { it.track }
                val selectedTrack = musicTracks[index]
                musicPlayerViewModel.loadAndPlayTrack(selectedTrack, musicTracks, downloadsSourceName)
                onMusicStarted()
            },
            onSavedShortClick = { video ->
                navController.openShortsOrPlayer(ShortsQueueSource.Saved(video.id), disableShortsPlayer)
            },
        )
    }

    composable("localMedia") {
        currentRoute.value = "localMedia"
        val musicPlayerViewModel = sharedMusicPlayerViewModel()
        LocalMediaScreen(
            onBackClick = {
                if (!navController.popBackStack()) navController.navigate("library")
            },
            onPlayVideos = { items, startIndex, shuffle ->
                val videoList = items.map { it.toVideo() }
                playerViewModel.playPlaylist(videoList, startIndex, "Local media", shuffle)
                GlobalPlayerState.setCurrentVideo(videoList[startIndex])
            },
            onPlayMusic = { items, startIndex, _ ->
                val musicTracks = items.map { it.toMusicTrack() }
                musicPlayerViewModel.loadAndPlayTrack(musicTracks[startIndex], musicTracks, "Local media")
                onMusicStarted()
            },
            onOpenSettings = {
                navController.navigate("settings?target=${SettingsTarget(SettingsDestination.LOCAL_MEDIA).encode()}")
            },
        )
    }

    composable("history") {
        currentRoute.value = "history"
        val musicPlayerViewModel = sharedMusicPlayerViewModel()
        HistoryScreen(
            onVideoClick = { track ->
                navController.openVideoOrShorts(track.toVideo(), disableShortsPlayer) {
                    playerViewModel.playVideo(it)
                    GlobalPlayerState.setCurrentVideo(it)
                }
            },
            onBackClick = { navController.popBackStack() },
            onShortsQueue = { source ->
                navController.openShortsOrPlayer(source, disableShortsPlayer)
            },
            onMusicClick = { track, queue ->
                musicPlayerViewModel.loadAndPlayTrack(track, queue, "History")
                onMusicStarted()
            },
        )
    }

    composable("downloads") {
        currentRoute.value = "downloads"
        val musicPlayerViewModel = sharedMusicPlayerViewModel()
        val downloadsSourceName = stringResource(R.string.library_downloads_label)
        DownloadsScreen(
            onBackClick = { navController.popBackStack() },
            onVideoClick = { videos, index ->
                val videoList = videos.map { it.video }
                playerViewModel.playPlaylist(videoList, index, downloadsSourceName)
                GlobalPlayerState.setCurrentVideo(videoList[index])
            },
            onMusicClick = { tracks, index ->
                val musicTracks = tracks.map { it.track }
                musicPlayerViewModel.loadAndPlayTrack(musicTracks[index], musicTracks, downloadsSourceName)
                onMusicStarted()
            },
            onOpenCollection = { },
            onHomeClick = { navController.navigate("home") },
        )
    }

    composable(
        route = "playlists?kind={kind}",
        arguments =
            listOf(
                navArgument("kind") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
    ) { backStackEntry ->
        currentRoute.value = "playlists"
        val kindName = backStackEntry.arguments?.getString("kind")
        val fixedKind = kindName?.let { runCatching { MediaKind.valueOf(it) }.getOrNull() }
        PlaylistsScreen(
            onBackClick = { navController.popBackStack() },
            onVideoPlaylistClick = { playlist ->
                navController.navigate("playlist/${playlist.id}")
            },
            onMusicPlaylistClick = { playlist ->
                mediaNavigator.openMusicPlaylist(playlist.id)
            },
            fixedKind = fixedKind,
        )
    }

    composable(
        route = "playlist/{playlistId}",
        arguments =
            listOf(
                navArgument("playlistId") { type = NavType.StringType },
            ),
    ) {
        currentRoute.value = "playlist"
        PlaylistDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onPlayPlaylist = { videos, startIndex, shuffle, title ->
                playerViewModel.playPlaylist(videos, startIndex, title, shuffle)
                GlobalPlayerState.setCurrentVideo(videos[startIndex])
            },
        )
    }

    composable("savedShorts") {
        currentRoute.value = "savedShorts"
        SavedShortsGridScreen(
            onBackClick = { navController.popBackStack() },
            onVideoClick = { videoId ->
                navController.openShortsOrPlayer(ShortsQueueSource.Saved(videoId), disableShortsPlayer)
            },
        )
    }

    composable("notes") {
        currentRoute.value = "notes"
        NotesScreen(
            onBackClick = { navController.popBackStack() },
            onPlay = { video, startPositionMs ->
                navController.openVideoOrShorts(video, disableShortsPlayer) {
                    playerViewModel.playVideo(video, startPositionMs = startPositionMs)
                    GlobalPlayerState.setCurrentVideo(video)
                }
            },
        )
    }

    composable(
        route = "player/{videoId}",
        arguments =
            listOf(
                navArgument("videoId") { type = NavType.StringType },
            ),
    ) { backStackEntry ->
        val videoId = backStackEntry.arguments?.getString("videoId")
        LaunchedEffect(videoId) {
            if (!videoId.isNullOrBlank()) {
                val video = blankVideo(videoId, cached = null)
                playerViewModel.playVideo(video)
                GlobalPlayerState.setCurrentVideo(video)
            }
            navController.popBackStack()
        }
    }

    composable("search") {
        currentRoute.value = "search"
        SearchScreen(
            onVideoClick = { video ->
                navController.openVideoOrShorts(video, disableShortsPlayer) { playerViewModel.playVideo(it) }
            },
            onShortsQueue = { source ->
                navController.openShortsOrPlayer(source, disableShortsPlayer)
            },
            onChannelClick = { channel ->
                mediaNavigator.openChannel(channel.url.ifBlank { channel.id })
            },
            onPlaylistClick = { playlist ->
                navController.navigate("playlist/${playlist.id}")
            },
            onBack = {
                if (!navController.popBackStack()) navController.navigate("home")
            },
        )
    }

    composable(EQUALIZER_ROUTE) {
        currentRoute.value = EQUALIZER_ROUTE
        EqualizerScreen(onBack = { navController.popBackStack() })
    }

    composable("categories") {
        currentRoute.value = "categories"
        CategoriesScreen(
            onVideoClick = { video ->
                navController.openVideoOrShorts(video, disableShortsPlayer) { playerViewModel.playVideo(it) }
            },
            onShortClick = { videoId ->
                navController.openShortsOrPlayer(ShortsQueueSource.SeededFeed(videoId), disableShortsPlayer)
            },
            onPlaylistClick = { playlistId ->
                navController.navigate("playlist/$playlistId")
            },
        )
    }

    composable(
        route = "settings?target={target}",
        arguments =
            listOf(
                navArgument("target") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
    ) { backStackEntry ->
        currentRoute.value = "settings"
        SettingsHost(
            start = SettingsTarget.decode(backStackEntry.arguments?.getString("target")),
            onExit = { navController.popBackStack() },
            onOpenDonations = {},
            onOpenRecap = { navController.navigate(RecapRoutes.stats()) },
            onOpenUpdate = {},
        )
    }

    composable(
        route = RecapRoutes.STATS,
        arguments =
            listOf(
                navArgument(RecapRoutes.ARG_PERIOD) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
    ) { backStackEntry ->
        currentRoute.value = RecapRoutes.STATS
        RecapScreen(
            onBack = { navController.popBackStack() },
            onPlayStory = { period, source -> navController.navigate(RecapRoutes.story(period, source)) },
            startAt = RecapRoutes.decode(backStackEntry.arguments?.getString(RecapRoutes.ARG_PERIOD)),
        )
    }

    composable(
        route = RecapRoutes.STORY,
        arguments =
            listOf(
                navArgument(RecapRoutes.ARG_PERIOD) { type = NavType.StringType },
                navArgument(RecapRoutes.ARG_SOURCE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
    ) { backStackEntry ->
        currentRoute.value = RecapRoutes.STORY
        val period = RecapRoutes.decode(backStackEntry.arguments?.getString(RecapRoutes.ARG_PERIOD))
        if (period == null) {
            LaunchedEffect(Unit) { navController.popBackStack() }
        } else {
            RecapStoryScreen(
                period = period,
                source = RecapRoutes.decodeSource(backStackEntry.arguments?.getString(RecapRoutes.ARG_SOURCE)),
                onClose = { navController.popBackStack() },
            )
        }
    }
}
