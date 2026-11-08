package com.khepilabs.khepitube.ui.components.musicplayer.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.local.PlayerPreferences
import com.khepilabs.khepitube.player.EnhancedMusicPlayerManager
import com.khepilabs.khepitube.ui.components.equalizer.EqQuickPanel
import com.khepilabs.khepitube.ui.components.equalizer.PanelRow
import com.khepilabs.khepitube.ui.components.layout.navigation.LocalMediaNavigator
import com.khepilabs.khepitube.ui.components.shared.KhepiModalSheetDefaults
import com.khepilabs.khepitube.ui.components.shared.KhepiNavRow
import com.khepilabs.khepitube.ui.components.shared.KhepiRowGroup
import com.khepilabs.khepitube.ui.components.shared.KhepiSectionHeader
import com.khepilabs.khepitube.ui.components.shared.KhepiSheetHeader
import com.khepilabs.khepitube.ui.components.shared.KhepiSwitchRow
import com.khepilabs.khepitube.ui.components.shared.MediaPlaybackSpeedPicker
import com.khepilabs.khepitube.ui.components.shared.khepiRowGroupShape
import com.khepilabs.khepitube.ui.components.shared.rememberKhepiSheetState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TEMPO_ROWS = 3
private const val PITCH_STEPS = 23
private val SliderRowPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

/** The music player's audio settings: the equalizer panel, tempo and pitch, and loudness normalisation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSettingsSheet(onDismiss: () -> Unit) {
    val speed by EnhancedMusicPlayerManager.playbackSpeed.collectAsState()
    val pitch by EnhancedMusicPlayerManager.playbackPitch.collectAsState()
    val navigator = LocalMediaNavigator.current
    val context = LocalContext.current
    val preferences = remember { PlayerPreferences(context) }
    val normalizationEnabled by preferences.musicLoudnessNormalizationEnabled.collectAsState(initial = true)
    val customSpeedsEnabled by preferences.customSpeedsEnabled.collectAsState(initial = false)
    val customSpeedPresetsRaw by preferences.customSpeedPresets.collectAsState(initial = "")
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberKhepiSheetState(),
        modifier = KhepiModalSheetDefaults.modifier,
        contentWindowInsets = KhepiModalSheetDefaults.contentWindowInsets,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
        ) {
            KhepiSheetHeader(title = stringResource(R.string.audio_settings_title), onClose = onDismiss, showDragHandle = false)

            KhepiSectionHeader(stringResource(R.string.equalizer))
            EqQuickPanel(
                onEdit = {
                    onDismiss()
                    navigator.openEqualizer()
                },
            )

            KhepiSectionHeader(stringResource(R.string.label_tempo_pitch))
            KhepiRowGroup {
                PanelRow(shape = khepiRowGroupShape(0, TEMPO_ROWS)) {
                    MediaPlaybackSpeedPicker(
                        currentSpeed = speed,
                        sliderEnabled = true,
                        customSpeedsEnabled = customSpeedsEnabled,
                        customSpeedPresetsRaw = customSpeedPresetsRaw,
                        onSpeedSelected = { EnhancedMusicPlayerManager.setPlaybackSpeed(it, persist = false) },
                        onSliderSelectionFinished = { EnhancedMusicPlayerManager.setPlaybackSpeed(it) },
                    )
                }
                PanelRow(shape = khepiRowGroupShape(1, TEMPO_ROWS)) {
                    SliderRow(
                        label = stringResource(R.string.template_pitch, pitch.roundToInt()),
                        value = pitch,
                        onValueChange = { EnhancedMusicPlayerManager.setPlaybackPitch(it, persist = false) },
                        onValueChangeFinished = {
                            EnhancedMusicPlayerManager.setPlaybackPitch(
                                EnhancedMusicPlayerManager.playbackPitch.value,
                            )
                        },
                        range = -12f..12f,
                        steps = PITCH_STEPS,
                    )
                }
                KhepiNavRow(
                    title = stringResource(R.string.action_reset),
                    onClick = {
                        EnhancedMusicPlayerManager.setPlaybackSpeed(1f)
                        EnhancedMusicPlayerManager.setPlaybackPitch(0f)
                    },
                    leadingIcon = Icons.Filled.Refresh,
                    showChevron = false,
                    shape = khepiRowGroupShape(2, TEMPO_ROWS),
                )
            }

            KhepiRowGroup(modifier = Modifier.padding(top = 16.dp)) {
                KhepiSwitchRow(
                    title = stringResource(R.string.music_normalize_volume_title),
                    supportingText = stringResource(R.string.music_normalize_volume_desc),
                    checked = normalizationEnabled,
                    onCheckedChange = { enabled -> scope.launch { preferences.setMusicLoudnessNormalizationEnabled(enabled) } },
                    leadingIcon = Icons.Filled.GraphicEq,
                    shape = khepiRowGroupShape(0, 1),
                )
            }
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(SliderRowPadding)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = range,
            steps = steps,
        )
    }
}
