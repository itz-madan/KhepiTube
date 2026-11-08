package com.khepilabs.khepitube.notification

import android.content.Context
import com.khepilabs.khepitube.data.local.PlayerPreferences
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Keeps the periodic background checks in step with the notification switches.
 */
internal object BackgroundCheckScheduler {
    fun subscriptionCheckWanted(
        notificationsEnabled: Boolean,
        newVideosEnabled: Boolean,
    ): Boolean = notificationsEnabled && newVideosEnabled

    /**
     * Runs for the life of the process. The first value only confirms the schedule a previous
     * process left; later changes replace it.
     */
    suspend fun follow(
        context: Context,
        preferences: PlayerPreferences,
    ) = coroutineScope {
        launch {
            combine(
                preferences.notificationsEnabled,
                preferences.notifNewVideosEnabled,
                preferences.subscriptionCheckIntervalMinutes,
            ) { enabled, newVideos, minutes -> if (subscriptionCheckWanted(enabled, newVideos)) minutes else null }
                .distinctUntilChanged()
                .collectIndexed { index, minutes ->
                    if (minutes == null) {
                        SubscriptionCheckWorker.cancelScheduledChecks(context)
                    } else {
                        SubscriptionCheckWorker.schedulePeriodicCheck(context, minutes.toLong(), reschedule = index > 0)
                    }
                }
        }
    }
}
