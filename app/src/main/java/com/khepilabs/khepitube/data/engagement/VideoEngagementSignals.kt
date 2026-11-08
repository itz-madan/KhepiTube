package com.khepilabs.khepitube.data.engagement

import android.content.Context
import com.khepilabs.khepitube.data.model.Video
import com.khepilabs.khepitube.data.recommendation.InteractionType
import com.khepilabs.khepitube.data.recommendation.KhepiNeuroEngine
import com.khepilabs.khepitube.data.repository.YouTubeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * The learning signals an engagement action feeds the recommendation engine.
 *
 * [KhepiNeuroEngine] is still reached through a context-keyed global, so this is the one place that
 * touches it: every engagement caller injects this instead, which keeps the global access isolated
 * behind a dependency a test can replace.
 */
class VideoEngagementSignals
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val repository: YouTubeRepository,
    ) {
        suspend fun channelSubscriptionChanged(
            channelId: String,
            channelName: String,
            subscribed: Boolean,
        ) = KhepiNeuroEngine.onChannelSubscriptionChanged(context, channelId, channelName, subscribed)

        suspend fun videoInteraction(
            video: Video,
            interactionType: InteractionType,
        ) = KhepiNeuroEngine.onVideoInteraction(context, video, interactionType)

        /** Learns a newly subscribed channel's declared keyword tags. */
        suspend fun channelTagsLearned(channelId: String) = repository.learnChannelTags(context, channelId)
    }
