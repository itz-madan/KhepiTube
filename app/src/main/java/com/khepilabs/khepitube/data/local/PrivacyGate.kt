package com.khepilabs.khepitube.data.local

import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * What Deep Flow and paused watch history allow to be recorded. Writers ask here instead of reading
 * the preferences themselves, so every one of them follows the same rules.
 */
@Singleton
class PrivacyGate internal constructor(
    private val deepFlowActive: suspend () -> Boolean,
    private val watchHistoryPaused: suspend () -> Boolean,
) {
    @Inject
    constructor(preferences: PlayerPreferences) : this(
        deepFlowActive = preferences::isDeepFlowCurrentlyActive,
        watchHistoryPaused = { preferences.watchHistoryPaused.first() },
    )

    /** Deep Flow is on and its timer has not run out. */
    suspend fun isDeepFlowActive(): Boolean = deepFlowActive()

    suspend fun isWatchHistoryPaused(): Boolean = watchHistoryPaused()
}
