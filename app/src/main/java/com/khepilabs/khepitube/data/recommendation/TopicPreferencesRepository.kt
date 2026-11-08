package com.khepilabs.khepitube.data.recommendation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The topics the recommendation engine follows and hides, behind an injectable seam. It adapts the
 * engine's static entry points so callers take this as a dependency instead of reaching the engine
 * globally; the engine keeps its single instance underneath.
 */
@Singleton
class TopicPreferencesRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        suspend fun preferredTopics(): Set<String> = KhepiNeuroEngine.getPreferredTopics()

        suspend fun blockedTopics(): Set<String> = KhepiNeuroEngine.getBlockedTopics()

        suspend fun addPreferred(topic: String) = KhepiNeuroEngine.addPreferredTopic(context, topic)

        suspend fun removePreferred(topic: String) = KhepiNeuroEngine.removePreferredTopic(context, topic)

        suspend fun addBlocked(topic: String) = KhepiNeuroEngine.addBlockedTopic(context, topic)

        suspend fun removeBlocked(topic: String) = KhepiNeuroEngine.removeBlockedTopic(context, topic)
    }
