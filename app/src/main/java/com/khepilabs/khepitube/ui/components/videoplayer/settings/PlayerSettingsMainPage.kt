package com.khepilabs.khepitube.ui.components.videoplayer.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.player.EnhancedPlayerState
import com.khepilabs.khepitube.ui.components.equalizer.equalizerSummary
import com.khepilabs.khepitube.ui.components.shared.KhepiNavRow
import com.khepilabs.khepitube.ui.components.shared.KhepiRowGroup
import com.khepilabs.khepitube.ui.components.shared.KhepiSectionHeader
import com.khepilabs.khepitube.ui.components.shared.KhepiSwitchRow
import com.khepilabs.khepitube.ui.components.shared.khepiRowGroupShape
import com.khepilabs.khepitube.ui.components.shared.playbackSpeedLabel
import com.khepilabs.khepitube.utils.formatDurationMillis

private const val VIDEO_ROWS = 1
private const val PLAYBACK_ROWS = 3
private const val AUDIO_ROWS = 1
private const val CAPTION_ROWS = 2
private const val OVERLAY_ROWS = 3
private const val EFFECT_ROWS = 3
private const val DISPLAY_ROWS = 1

@Composable
internal fun PlayerSettingsMainPage(
    playerState: EnhancedPlayerState,
    autoplayEnabled: Boolean,
    subtitlesEnabled: Boolean,
    ambientModeEnabled: Boolean,
    onNavigateToPage: (PlayerSettingsPage) -> Unit,
    onShowSubtitleStyle: () -> Unit,
    onCastClick: () -> Unit,
    onPipClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onLoopToggle: (Boolean) -> Unit,
    onAutoplayToggle: (Boolean) -> Unit,
    onSkipSilenceToggle: (Boolean) -> Unit,
    onVolumeNormalizationToggle: (Boolean) -> Unit,
    onAmbientModeToggle: (Boolean) -> Unit,
    sponsorBlockSegmentCount: Int = 0,
    sponsorBlockOffForVideo: Boolean = false,
    onSponsorBlockToggle: (off: Boolean) -> Unit = {},
    notePositionMs: Long? = null,
    onAddNote: () -> Unit = {},
) {
    val playbackRows = if (sponsorBlockSegmentCount > 0) PLAYBACK_ROWS + 1 else PLAYBACK_ROWS
    KhepiSectionHeader(stringResource(R.string.video))
    KhepiRowGroup {
        KhepiNavRow(
            leadingIcon = Icons.Filled.HighQuality,
            title = stringResource(R.string.quality),
            trailingText =
                if (playerState.currentQuality == 0) {
                    stringResource(R.string.quality_auto)
                } else {
                    "${playerState.currentQuality}p"
                },
            shape = khepiRowGroupShape(0, VIDEO_ROWS),
            onClick = { onNavigateToPage(PlayerSettingsPage.Quality) },
        )
    }

    KhepiSectionHeader(stringResource(R.string.playback_header))
    KhepiRowGroup {
        KhepiNavRow(
            leadingIcon = Icons.Filled.Speed,
            title = stringResource(R.string.playback_speed),
            trailingText = playbackSpeedLabel(playerState.playbackSpeed),
            shape = khepiRowGroupShape(0, playbackRows),
            onClick = { onNavigateToPage(PlayerSettingsPage.Speed) },
        )
        KhepiSwitchRow(
            leadingIcon = Icons.Rounded.Repeat,
            title = stringResource(R.string.loop_video),
            checked = playerState.isLooping,
            shape = khepiRowGroupShape(1, playbackRows),
            onCheckedChange = onLoopToggle,
        )
        KhepiSwitchRow(
            leadingIcon = Icons.Filled.SkipNext,
            title = stringResource(R.string.autoplay_next),
            checked = autoplayEnabled,
            enabled = !playerState.isLooping,
            shape = khepiRowGroupShape(2, playbackRows),
            onCheckedChange = onAutoplayToggle,
        )
        if (sponsorBlockSegmentCount > 0) {
            KhepiSwitchRow(
                leadingIcon = ImageVector.vectorResource(R.drawable.ic_block),
                title = stringResource(R.string.player_settings_sponsorblock_video),
                supportingText =
                    pluralStringResource(R.plurals.sb_video_segments, sponsorBlockSegmentCount, sponsorBlockSegmentCount),
                checked = !sponsorBlockOffForVideo,
                shape = khepiRowGroupShape(3, playbackRows),
                onCheckedChange = { on -> onSponsorBlockToggle(!on) },
            )
        }
    }

    if (notePositionMs != null) {
        KhepiSectionHeader(stringResource(R.string.note_title))
        KhepiRowGroup {
            KhepiNavRow(
                leadingIcon = Icons.Outlined.MoreTime,
                title = stringResource(R.string.note_at_time, formatDurationMillis(notePositionMs)),
                shape = khepiRowGroupShape(0, 1),
                onClick = onAddNote,
            )
        }
    }

    KhepiSectionHeader(stringResource(R.string.audio_settings_title))
    KhepiRowGroup {
        KhepiNavRow(
            leadingIcon = Icons.Filled.AudioFile,
            title = stringResource(R.string.audio_track),
            trailingText =
                audioTrackDisplayLabel(
                    playerState.availableAudioTracks.getOrNull(playerState.currentAudioTrack),
                    playerState.currentAudioTrack,
                ),
            shape = khepiRowGroupShape(0, AUDIO_ROWS),
            onClick = { onNavigateToPage(PlayerSettingsPage.Audio) },
        )
    }

    KhepiSectionHeader(stringResource(R.string.captions))
    KhepiRowGroup {
        KhepiNavRow(
            leadingIcon = Icons.Filled.Subtitles,
            title = stringResource(R.string.filter_subtitles),
            trailingText =
                if (subtitlesEnabled) stringResource(R.string.on) else stringResource(R.string.off),
            shape = khepiRowGroupShape(0, CAPTION_ROWS),
            onClick = { onNavigateToPage(PlayerSettingsPage.Subtitles) },
        )
        KhepiNavRow(
            leadingIcon = Icons.Filled.Tune,
            title = stringResource(R.string.subtitle_style),
            shape = khepiRowGroupShape(1, CAPTION_ROWS),
            onClick = onShowSubtitleStyle,
        )
    }

    KhepiSectionHeader(stringResource(R.string.player_settings_overlay_controls))
    KhepiRowGroup {
        KhepiNavRow(
            leadingIcon = Icons.Filled.Cast,
            title = stringResource(R.string.cast_to_tv),
            shape = khepiRowGroupShape(0, OVERLAY_ROWS),
            onClick = onCastClick,
        )
        KhepiNavRow(
            leadingIcon = Icons.Filled.PictureInPicture,
            title = stringResource(R.string.pip_mode),
            shape = khepiRowGroupShape(1, OVERLAY_ROWS),
            onClick = onPipClick,
        )
        KhepiNavRow(
            leadingIcon = Icons.Filled.Bedtime,
            title = stringResource(R.string.sleep_timer),
            shape = khepiRowGroupShape(2, OVERLAY_ROWS),
            onClick = onSleepTimerClick,
        )
    }

    KhepiSectionHeader(stringResource(R.string.audio_effects))
    KhepiRowGroup {
        KhepiNavRow(
            leadingIcon = Icons.Filled.Equalizer,
            title = stringResource(R.string.equalizer),
            trailingText = equalizerSummary(),
            shape = khepiRowGroupShape(0, EFFECT_ROWS),
            onClick = { onNavigateToPage(PlayerSettingsPage.Equalizer) },
        )
        KhepiSwitchRow(
            leadingIcon = Icons.Rounded.GraphicEq,
            title = stringResource(R.string.player_settings_skip_silence),
            checked = playerState.isSkipSilenceEnabled,
            shape = khepiRowGroupShape(1, EFFECT_ROWS),
            onCheckedChange = onSkipSilenceToggle,
        )
        KhepiSwitchRow(
            leadingIcon = Icons.AutoMirrored.Rounded.VolumeUp,
            title = stringResource(R.string.player_settings_normalize_volume),
            checked = playerState.isVolumeNormalizationEnabled,
            shape = khepiRowGroupShape(2, EFFECT_ROWS),
            onCheckedChange = onVolumeNormalizationToggle,
        )
    }

    KhepiSectionHeader(stringResource(R.string.player_settings_display))
    KhepiRowGroup {
        KhepiSwitchRow(
            leadingIcon = ImageVector.vectorResource(R.drawable.ic_ambient_mode),
            title = stringResource(R.string.player_settings_ambient_mode),
            checked = ambientModeEnabled,
            shape = khepiRowGroupShape(0, DISPLAY_ROWS),
            onCheckedChange = onAmbientModeToggle,
        )
    }
}
