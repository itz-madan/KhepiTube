package com.khepilabs.khepitube.ui.screens.player.stage

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Stable
import com.khepilabs.khepitube.data.model.Video
import com.khepilabs.khepitube.player.state.EnhancedPlayerState
import com.khepilabs.khepitube.ui.components.videoplayer.PlayerDraggableState
import com.khepilabs.khepitube.ui.screens.player.VideoPlayerViewModel
import com.khepilabs.khepitube.ui.screens.player.effects.AudioSystemInfo
import com.khepilabs.khepitube.ui.screens.player.effects.PipPreferences
import com.khepilabs.khepitube.ui.screens.player.state.PlayerScreenState
import com.khepilabs.khepitube.ui.screens.player.state.VideoPlayerPreferencesState
import com.khepilabs.khepitube.ui.screens.player.state.VideoPlayerUiState
import kotlinx.coroutines.CoroutineScope

/**
 * Everything the player's stage surfaces read about the session they are drawing. Built fresh by
 * the host on each composition, so the values it carries are exactly the ones the host itself
 * observed that frame.
 */
@Stable
internal class VideoPlayerStageSession(
    val video: Video,
    val context: Context,
    val activity: ComponentActivity,
    val scope: CoroutineScope,
    val screenState: PlayerScreenState,
    val playerState: EnhancedPlayerState,
    val uiState: VideoPlayerUiState,
    val viewModel: VideoPlayerViewModel,
    val sheetState: PlayerDraggableState,
    val prefs: VideoPlayerPreferencesState,
    val audioSystemInfo: AudioSystemInfo,
    val pipPreferences: PipPreferences,
    /** Whether the window shows the fullscreen layout yet, which trails the request while it rotates. */
    val isFullscreenShown: Boolean,
)
