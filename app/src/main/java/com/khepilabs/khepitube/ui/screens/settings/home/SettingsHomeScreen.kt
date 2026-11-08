package com.khepilabs.khepitube.ui.screens.settings.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.ui.components.glass.GlassSearchField
import com.khepilabs.khepitube.ui.components.settings.SettingEntry
import com.khepilabs.khepitube.ui.components.settings.SettingsDestination
import com.khepilabs.khepitube.ui.components.settings.SettingsPage
import com.khepilabs.khepitube.ui.components.settings.SettingsTarget
import com.khepilabs.khepitube.ui.components.shared.KhepiChoice
import com.khepilabs.khepitube.ui.components.shared.KhepiChoiceDialog
import com.khepilabs.khepitube.ui.screens.settings.index.DestinationIndex
import com.khepilabs.khepitube.ui.screens.settings.index.HomeIndex
import com.khepilabs.khepitube.ui.screens.settings.index.SettingsSearch

private val SearchFieldBottomPadding = 8.dp

/**
 * The settings list: search with GlassSearchField, the persona card, Deep Flow, and one row per
 * settings page grouped the way people look for them. Beside a detail pane, [selected] marks the
 * page open next to it.
 */
@Composable
internal fun SettingsHomeScreen(
    selected: SettingsDestination?,
    onOpen: (SettingsTarget) -> Unit,
    onOpenDonations: () -> Unit = {},
    onOpenUpdate: () -> Unit,
    onBack: () -> Unit,
    selectedRowFocus: FocusRequester? = null,
    viewModel: SettingsHomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val deepFlow by viewModel.deepFlow.collectAsStateWithLifecycle()
    val persona by viewModel.persona.collectAsStateWithLifecycle()
    val updateCheck by viewModel.updateCheck.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var query by rememberSaveable { mutableStateOf("") }
    var highlight by remember { mutableStateOf<String?>(null) }
    var showDurationDialog by rememberSaveable { mutableStateOf(false) }

    val checkingLabel = stringResource(R.string.checking_for_updates)
    val searchable = rememberSearchableSettings()
    LifecycleResumeEffect(selected) {
        viewModel.refreshPersona()
        onPauseOrDispose {}
    }
    val results = remember(query, searchable) { SettingsSearch.search(query, searchable) }

    UpdateCheckFeedback(
        state = updateCheck,
        snackbarHostState = snackbarHostState,
        onConsumed = viewModel::consumeUpdateCheck,
    )

    val onResultClick: (SettingEntry) -> Unit = { entry ->
        query = ""
        val page = DestinationIndex.destinationOf(entry)
        when {
            entry.key == HomeIndex.checkForUpdates.key -> viewModel.checkForUpdates()
            page != null -> onOpen(SettingsTarget(page))
            entry.destination == SettingsDestination.HOME -> highlight = entry.key
            else -> onOpen(entry.target)
        }
    }

    SettingsPage(
        title = stringResource(R.string.settings_title),
        onBack = onBack,
        highlight = highlight,
        snackbarHostState = snackbarHostState,
        header = {
            GlassSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = stringResource(R.string.ui_search_settings),
                onClear = { query = "" },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = SearchFieldBottomPadding),
            )
        },
    ) {
        if (query.isNotBlank()) {
            searchResults(query = query, results = results, onResultClick = onResultClick)
        } else {
            homeContent(
                state =
                    SettingsHomeState(
                        selected = selected,
                        persona = persona,
                        deepFlow = deepFlow,
                        updateCheckingLabel = checkingLabel.takeIf { updateCheck == UpdateCheckState.Checking },
                        selectedRowFocus = selectedRowFocus,
                    ),
                actions =
                    SettingsHomeActions(
                        onOpen = onOpen,
                        onOpenDonations = onOpenDonations,
                        onDeepFlowChange = viewModel::setDeepFlowEnabled,
                        onDurationClick = { showDurationDialog = true },
                        onSaveHistoryChange = viewModel::setDeepFlowSaveToHistory,
                        onShowOnSearchChange = viewModel::setDeepFlowOnSearch,
                        onCheckForUpdates = viewModel::checkForUpdates,
                    ),
            )
        }
    }

    if (showDurationDialog) {
        KhepiChoiceDialog(
            title = stringResource(R.string.deep_KhepiTube_dialog_title),
            description = stringResource(R.string.deep_KhepiTube_dialog_body),
            icon = Icons.Outlined.Timer,
            options = DeepFlowDurations.map { KhepiChoice(it, deepFlowDurationLabel(it)) },
            selected = deepFlow.expireHours,
            onSelect = viewModel::setDeepFlowExpireHours,
            onDismiss = { showDurationDialog = false },
        )
    }

    LaunchedEffect(updateCheck) {
        if (updateCheck is UpdateCheckState.Available) {
            viewModel.consumeUpdateCheck()
            onOpenUpdate()
        }
    }
}

@Composable
private fun UpdateCheckFeedback(
    state: UpdateCheckState,
    snackbarHostState: SnackbarHostState,
    onConsumed: () -> Unit,
) {
    val upToDate = stringResource(R.string.KhepiTube_is_up_to_date)
    val failed = stringResource(R.string.update_check_failed)
    LaunchedEffect(state) {
        val message =
            when (state) {
                UpdateCheckState.UpToDate -> upToDate
                UpdateCheckState.Failed -> failed
                else -> return@LaunchedEffect
            }
        try {
            snackbarHostState.showSnackbar(message)
        } finally {
            onConsumed()
        }
    }
}
