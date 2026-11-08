package com.khepilabs.khepitube.ui.startup

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps each launch to one prompt. The update page goes first.
 */
@Singleton
class LaunchPrompts
    @Inject
    constructor() {
        private val updateSettled = MutableStateFlow(false)

        @Volatile
        private var updateShown = false

        fun updateCheckFinished(shownUpdate: Boolean) {
            updateShown = updateShown || shownUpdate
            updateSettled.value = true
        }

        fun donationMayShow(waitMs: Long = 0L): Boolean = false
    }
