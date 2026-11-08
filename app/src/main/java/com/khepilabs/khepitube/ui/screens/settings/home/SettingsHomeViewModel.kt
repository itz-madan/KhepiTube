package com.khepilabs.khepitube.ui.screens.settings.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khepilabs.khepitube.data.local.PlayerPreferences
import com.khepilabs.khepitube.data.recommendation.KhepiNeuroEngine
import com.khepilabs.khepitube.data.recommendation.KhepiPersona
import com.khepilabs.khepitube.data.update.UpdateRelease
import com.khepilabs.khepitube.data.update.UpdateRepository
import com.khepilabs.khepitube.player.DeepFlowManager
import com.khepilabs.khepitube.ui.components.shared.DeepFlowState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The update check the settings list runs on request. */
sealed interface UpdateCheckState {
    data object Idle : UpdateCheckState

    data object Checking : UpdateCheckState

    data object UpToDate : UpdateCheckState

    data object Failed : UpdateCheckState

    data class Available(
        val release: UpdateRelease,
    ) : UpdateCheckState
}

@HiltViewModel
class SettingsHomeViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val playerPreferences: PlayerPreferences,
        private val updates: UpdateRepository,
    ) : ViewModel() {
        val deepFlow: StateFlow<DeepFlowState> =
            combine(
                playerPreferences.deepFlowActive,
                playerPreferences.deepFlowActivatedAt,
                playerPreferences.deepFlowExpireHours,
                playerPreferences.deepFlowSaveToHistory,
            ) { active, activatedAt, expireHours, saveToHistory ->
                DeepFlowState(active, activatedAt, expireHours, saveToHistory)
            }.combine(playerPreferences.deepFlowOnSearch) { state, onSearch ->
                state.copy(showOnSearch = onSearch)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DeepFlowState())

        private val _persona = MutableStateFlow<KhepiPersona?>(null)
        val persona: StateFlow<KhepiPersona?> = _persona.asStateFlow()

        private val _updateCheck = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
        val updateCheck: StateFlow<UpdateCheckState> = _updateCheck.asStateFlow()

        init {
            refreshPersona()
        }

        /** Re-reads the persona; the engine exposes no stream, so the list refreshes it when shown. */
        fun refreshPersona() {
            viewModelScope.launch {
                _persona.value = runCatching { KhepiNeuroEngine.getPersona(KhepiNeuroEngine.getBrainSnapshot()) }.getOrNull()
            }
        }

        fun setDeepFlowEnabled(enabled: Boolean) {
            viewModelScope.launch { DeepFlowManager.setEnabled(context, enabled) }
        }

        fun setDeepFlowExpireHours(hours: Int) {
            viewModelScope.launch { playerPreferences.setDeepFlowExpireHours(hours) }
        }

        fun setDeepFlowSaveToHistory(enabled: Boolean) {
            viewModelScope.launch { playerPreferences.setDeepFlowSaveToHistory(enabled) }
        }

        fun setDeepFlowOnSearch(enabled: Boolean) {
            viewModelScope.launch { playerPreferences.setDeepFlowOnSearch(enabled) }
        }

        fun checkForUpdates() {
            if (_updateCheck.value == UpdateCheckState.Checking) return
            _updateCheck.value = UpdateCheckState.Checking
            viewModelScope.launch {
                val release = updates.checkLatestRelease()
                _updateCheck.value =
                    when {
                        release == null -> UpdateCheckState.Failed
                        release.isNewer -> UpdateCheckState.Available(release)
                        else -> UpdateCheckState.UpToDate
                    }
            }
        }

        fun consumeUpdateCheck() {
            _updateCheck.update { if (it == UpdateCheckState.Checking) it else UpdateCheckState.Idle }
        }

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
