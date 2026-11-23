package com.khepilabs.khepitube.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.khepilabs.khepitube.data.model.Video
import com.khepilabs.khepitube.player.GlobalPlayerState
import com.khepilabs.khepitube.ui.components.videoplayer.PlayerDraggableState
import com.khepilabs.khepitube.ui.screens.player.VideoPlayerViewModel
import com.khepilabs.khepitube.ui.screens.player.state.VideoPlayerUiState
import com.khepilabs.khepitube.ui.screens.player.state.shouldExpandInsteadOfPlaying
import com.khepilabs.khepitube.ui.screens.playlists.LinkedPlaylistViewModel

/** Routes a link from another app opens to start playback; each plays, then leaves at once. */
internal fun NavGraphBuilder.linkPlaybackRoutes(
    navController: NavHostController,
    currentRoute: MutableState<String>,
    playerViewModel: VideoPlayerViewModel,
    playerUiStateResult: State<VideoPlayerUiState>,
    playerSheetState: PlayerDraggableState,
    playerVisibleState: MutableState<Boolean>,
    defaultStartRoute: String,
) {
    // A video linked from inside a playlist: it plays with the playlist as its queue, then this route leaves.
    composable(
        route = LINKED_PLAYLIST_ROUTE_PATTERN,
        arguments =
            listOf(
                navArgument(LINKED_PLAYLIST_ARG) { type = NavType.StringType },
                navArgument(LINKED_VIDEO_ARG) { type = NavType.StringType },
            ),
    ) { backStackEntry ->
        val playlistId = backStackEntry.arguments?.getString(LINKED_PLAYLIST_ARG).orEmpty()
        val videoId = backStackEntry.arguments?.getString(LINKED_VIDEO_ARG).orEmpty()
        val links: LinkedPlaylistViewModel = hiltViewModel()

        LaunchedEffect(playlistId, videoId) {
            val queue = links.videoQueue(playlistId, videoId)
            if (queue != null) {
                playerViewModel.playPlaylist(queue.items, queue.startIndex, queue.title)
            } else {
                playerViewModel.playVideo(linkedVideo(videoId))
            }
            withFrameNanos { }
            navController.popTransientRouteOrNavigateStart(defaultStartRoute)
        }
    }

    composable(
        route = "player/{videoId}",
        arguments = listOf(navArgument("videoId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val videoId = backStackEntry.arguments?.getString("videoId")
        val effectiveVideoId =
            when {
                !videoId.isNullOrEmpty() && videoId != "sample" -> videoId
                else -> "jNQXAC9IVRw"
            }

        // Use passed state
        val playerUiState = playerUiStateResult.value
        LaunchedEffect(effectiveVideoId) {
            if (!playerUiState.shouldExpandInsteadOfPlaying(effectiveVideoId)) {
                val video =
                    playerUiState.cachedVideo?.takeIf { it.id == effectiveVideoId }
                        ?: linkedVideo(effectiveVideoId)
                playerViewModel.playVideo(video)
                GlobalPlayerState.setCurrentVideo(video)
            } else {
                playerViewModel.showVideoPlayer()
                playerVisibleState.value = true
                playerSheetState.expand()
            }
            withFrameNanos { }
            navController.popTransientRouteOrNavigateStart(defaultStartRoute)
        }

        Box(modifier = Modifier.fillMaxSize())
    }
}

private const val LINKED_PLAYLIST_ARG = "playlistId"
private const val LINKED_VIDEO_ARG = "videoId"
private const val LINKED_PLAYLIST_ROUTE_PATTERN = "linkedPlaylist/{$LINKED_PLAYLIST_ARG}/{$LINKED_VIDEO_ARG}"

internal fun linkedPlaylistRoute(
    playlistId: String,
    videoId: String,
): String = "linkedPlaylist/${Uri.encode(playlistId)}/${Uri.encode(videoId)}"

/** A video known only by the id a link carries; the player fills in the rest as it loads. */
internal fun linkedVideo(videoId: String) =
    Video(
        id = videoId,
        title = "",
        channelName = "",
        channelId = "",
        thumbnailUrl = "",
        duration = 0,
        viewCount = 0L,
        uploadDate = "",
        description = "",
        channelThumbnailUrl = "",
    )
