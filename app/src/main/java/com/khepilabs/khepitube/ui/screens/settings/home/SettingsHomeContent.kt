package com.khepilabs.khepitube.ui.screens.settings.home

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.khepilabs.khepitube.BuildConfig
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.recommendation.KhepiPersona
import com.khepilabs.khepitube.ui.components.settings.SettingsDestination
import com.khepilabs.khepitube.ui.components.settings.SettingsGroupScope
import com.khepilabs.khepitube.ui.components.settings.SettingsListScope
import com.khepilabs.khepitube.ui.components.settings.SettingsTarget
import com.khepilabs.khepitube.ui.components.settings.nav
import com.khepilabs.khepitube.ui.components.shared.DeepFlowState
import com.khepilabs.khepitube.ui.screens.settings.index.DestinationIndex
import com.khepilabs.khepitube.ui.screens.settings.index.HomeIndex

@Immutable
internal data class SettingsHomeState(
    val selected: SettingsDestination?,
    val persona: KhepiPersona?,
    val deepFlow: DeepFlowState,
    /** Shown on the update row while a check the user started is running; null when idle. */
    val updateCheckingLabel: String?,
    /** Attached to the [selected] page's row, so a host can return focus to it. */
    val selectedRowFocus: FocusRequester? = null,
)

internal class SettingsHomeActions(
    val onOpen: (SettingsTarget) -> Unit,
    val onOpenDonations: () -> Unit = {},
    val onDeepFlowChange: (Boolean) -> Unit = {},
    val onDurationClick: () -> Unit = {},
    val onSaveHistoryChange: (Boolean) -> Unit = {},
    val onShowOnSearchChange: (Boolean) -> Unit = {},
    val onCheckForUpdates: () -> Unit,
)

/** The settings list when no search is running. */
internal fun SettingsListScope.homeContent(
    state: SettingsHomeState,
    actions: SettingsHomeActions,
) {
    fun SettingsGroupScope.page(destination: SettingsDestination) =
        nav(
            entry = DestinationIndex.entry(destination),
            onClick = { actions.onOpen(SettingsTarget(destination)) },
            icon = destinationIcon(destination),
            selected = state.selected == destination,
            showChevron = state.selected == null,
            modifier = state.selectedFocusModifier(destination),
        )

    group(key = "home.look", header = R.string.settings_group_look) {
        page(SettingsDestination.APPEARANCE)
        page(SettingsDestination.LANGUAGE_REGION)
    }
    group(key = "home.watching", header = R.string.settings_group_watching) {
        page(SettingsDestination.PLAYBACK)
        page(SettingsDestination.QUALITY)
        page(SettingsDestination.CONTENT)
        page(SettingsDestination.INTEGRATIONS)
    }
    group(key = "home.data", header = R.string.settings_group_data) {
        page(SettingsDestination.HISTORY)
        page(SettingsDestination.DOWNLOADS)
    }
    group(key = "home.system", header = R.string.settings_group_system) {
        page(SettingsDestination.NOTIFICATIONS)
    }
    group(key = "home.about", header = R.string.settings_header_about) {
        page(SettingsDestination.ABOUT)
        if (BuildConfig.UPDATER_ENABLED) {
            nav(
                HomeIndex.checkForUpdates,
                onClick = actions.onCheckForUpdates,
                value = state.updateCheckingLabel,
                enabled = state.updateCheckingLabel == null,
                showChevron = false,
                icon = HomeRowIcons.Updates,
            )
        }
    }
}

private fun SettingsHomeState.selectedFocusModifier(destination: SettingsDestination): Modifier =
    selectedRowFocus?.takeIf { selected == destination }?.let { Modifier.focusRequester(it) } ?: Modifier
