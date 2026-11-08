package com.khepilabs.khepitube.ui.screens.settings.localmedia

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.MovieFilter
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.localmedia.LocalMediaSettings
import com.khepilabs.khepitube.ui.components.settings.SettingsPage
import com.khepilabs.khepitube.ui.components.settings.choice
import com.khepilabs.khepitube.ui.components.settings.info
import com.khepilabs.khepitube.ui.components.settings.switch
import com.khepilabs.khepitube.ui.components.shared.KhepiChoice
import com.khepilabs.khepitube.ui.components.shared.KhepiChoiceDialog
import com.khepilabs.khepitube.ui.components.shared.KhepiNavRow
import com.khepilabs.khepitube.ui.screens.settings.index.LocalMediaIndex

/** What Local media leaves out: voice notes and recordings, short audio, and folders the viewer hid. */
@Composable
internal fun LocalMediaSettingsScreen(
    onBack: (() -> Unit)?,
    highlight: String?,
    viewModel: LocalMediaSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val hidden by viewModel.hiddenFolders.collectAsStateWithLifecycle()
    var pickingMinAudio by rememberSaveable { mutableStateOf(false) }
    var pickingMinVideo by rememberSaveable { mutableStateOf(false) }
    val noHiddenFolders = stringResource(R.string.local_setting_no_hidden_folders)

    SettingsPage(
        title = stringResource(R.string.settings_local_media_title),
        onBack = onBack,
        highlight = highlight,
    ) {
        group(key = "local_media.rules", header = R.string.settings_local_media_title) {
            switch(LocalMediaIndex.hideAppAudio, settings.hideAppAudio, viewModel::setHideAppAudio, icon = Icons.Outlined.MicOff)
            choice(LocalMediaIndex.minAudio, onClick = { pickingMinAudio = true }, icon = Icons.Outlined.Timer) {
                minimumLabel(settings.minAudioSeconds)
            }
            choice(LocalMediaIndex.minVideo, onClick = { pickingMinVideo = true }, icon = Icons.Outlined.MovieFilter) {
                minimumLabel(settings.minVideoSeconds)
            }
        }

        group(key = "local_media.appearance", header = R.string.local_setting_section_appearance) {
            switch(
                LocalMediaIndex.showExtensions,
                settings.showFileExtensions,
                viewModel::setShowFileExtensions,
                icon = Icons.Outlined.FontDownload,
            )
            switch(
                LocalMediaIndex.showFileSizes,
                settings.showFileSizes,
                viewModel::setShowFileSizes,
                icon = Icons.Outlined.SdCard,
            )
        }

        group(key = "local_media.playback", header = R.string.local_setting_section_playback) {
            switch(
                LocalMediaIndex.resumePlayback,
                settings.resumeLocalPlayback,
                viewModel::setResumeLocalPlayback,
                icon = Icons.Outlined.PlayCircle,
            )
            switch(
                LocalMediaIndex.autoPlayNext,
                settings.autoPlayNextLocal,
                viewModel::setAutoPlayNextLocal,
                icon = Icons.Outlined.SkipNext,
            )
            switch(
                LocalMediaIndex.showContinueWatching,
                settings.showContinueWatching,
                viewModel::setShowContinueWatching,
                icon = Icons.Outlined.History,
            )
        }

        group(key = "local_media.folders", header = R.string.local_setting_hidden_folders) {
            if (hidden.isEmpty()) {
                info(LocalMediaIndex.hiddenFolders, value = noHiddenFolders)
            } else {
                hidden.forEach { folder ->
                    row("local_media.folder.${folder.id}") { shape ->
                        KhepiNavRow(
                            title = folder.name,
                            onClick = { viewModel.showFolder(folder.id) },
                            leadingIcon = Icons.Outlined.FolderOff,
                            shape = shape,
                            trailingContent = {
                                TextButton(
                                    onClick = { viewModel.showFolder(folder.id) },
                                ) { Text(stringResource(R.string.local_setting_show_folder)) }
                            },
                        )
                    }
                }
            }
        }
    }

    if (pickingMinAudio) {
        KhepiChoiceDialog(
            title = stringResource(R.string.local_setting_min_audio),
            options = LocalMediaSettings.MIN_AUDIO_CHOICES.map { KhepiChoice(it, minimumLabel(it)) },
            selected = settings.minAudioSeconds,
            onSelect = viewModel::setMinAudioSeconds,
            onDismiss = { pickingMinAudio = false },
        )
    }

    if (pickingMinVideo) {
        KhepiChoiceDialog(
            title = stringResource(R.string.local_setting_min_video),
            options = LocalMediaSettings.MIN_VIDEO_CHOICES.map { KhepiChoice(it, minimumLabel(it)) },
            selected = settings.minVideoSeconds,
            onSelect = viewModel::setMinVideoSeconds,
            onDismiss = { pickingMinVideo = false },
        )
    }
}

@Composable
private fun minimumLabel(seconds: Int): String =
    if (seconds <= 0) {
        stringResource(R.string.local_setting_min_audio_off)
    } else {
        pluralStringResource(R.plurals.local_setting_seconds, seconds, seconds)
    }
