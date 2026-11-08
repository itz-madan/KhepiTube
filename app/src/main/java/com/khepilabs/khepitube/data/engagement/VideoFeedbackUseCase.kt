package com.khepilabs.khepitube.data.engagement

import android.content.Context
import com.khepilabs.khepitube.data.local.PlaylistRepository
import com.khepilabs.khepitube.data.local.ViewHistory
import com.khepilabs.khepitube.data.local.WatchLaterCleanup
import com.khepilabs.khepitube.data.model.Video
import com.khepilabs.khepitube.data.recommendation.BlockedChannelNames
import com.khepilabs.khepitube.data.recommendation.InteractionType
import com.khepilabs.khepitube.data.recommendation.KhepiNeuroEngine
import com.khepilabs.khepitube.data.repository.YouTubeRepository
import com.khepilabs.khepitube.data.stats.LedgerAction
import com.khepilabs.khepitube.data.stats.VideoStatsRecorder
import com.khepilabs.khepitube.utils.ThumbnailUrlResolver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** A channel a block applied to, named as the viewer knows it. */
data class BlockedChannel(
    val channelId: String,
    val channelName: String,
)

/**
 * What a video's menu tells the recommendation engine and the library: Watch later, not interested,
 * more like this, watched, and hiding a channel. Each call does the writes and the learning signal
 * that belong together, and says nothing to the viewer; the caller decides how to confirm it.
 */
class VideoFeedbackUseCase
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val repository: YouTubeRepository,
        private val playlistRepository: PlaylistRepository,
        private val viewHistory: ViewHistory,
        private val videoStats: VideoStatsRecorder,
        private val watchLaterCleanup: WatchLaterCleanup,
        private val blockedNames: BlockedChannelNames,
    ) {
        /** Adds [video] to Watch later, or takes it out; returns whether it is saved afterwards. */
        suspend fun toggleWatchLater(video: Video): Boolean {
            if (saveToWatchLater(video)) return true
            playlistRepository.removeFromWatchLater(video.id)
            return false
        }

        /** Adds [video] to Watch later unless it is there already; returns whether it was added. */
        suspend fun saveToWatchLater(video: Video): Boolean {
            if (playlistRepository.isInWatchLater(video.id)) return false
            playlistRepository.addToWatchLater(video)
            videoStats.onAction(LedgerAction.SAVE)
            runCatching { KhepiNeuroEngine.onVideoInteraction(context, video, InteractionType.SAVED) }
            return true
        }

        suspend fun setWatchLater(
            video: Video,
            saved: Boolean,
        ) {
            if (saved) playlistRepository.addToWatchLater(video) else playlistRepository.removeFromWatchLater(video.id)
        }

        /**
         * Hides [video]'s channel from every feed. A live or premiere entry can carry a handle instead
         * of a channel id, so that case asks the watch page for the real id first.
         */
        suspend fun blockChannel(video: Video): BlockedChannel {
            val metadata = if (video.channelId.startsWith("UC")) null else repository.getLiveWatchMetadata(video.id)
            val channelId = metadata?.channelId.orEmpty().ifBlank { video.channelId }
            check(channelId.isNotBlank()) { "No channel id for ${video.id}" }
            val channelName = metadata?.channelName.orEmpty().ifBlank { video.channelName }
            KhepiNeuroEngine.blockChannel(context, channelId)
            blockedNames.remember(channelId, channelName)
            videoStats.onAction(LedgerAction.BLOCK_CHANNEL)
            FeedInvalidationBus.emit(FeedInvalidationBus.Event.ChannelBlocked(channelId, video.id))
            return BlockedChannel(channelId, channelName)
        }

        suspend fun unblockChannel(channelId: String) {
            KhepiNeuroEngine.unblockChannel(context, channelId)
            blockedNames.forget(channelId)
        }

        suspend fun markNotInterested(video: Video) {
            KhepiNeuroEngine.markNotInterested(context, video)
            videoStats.onAction(LedgerAction.NOT_INTERESTED)
            FeedInvalidationBus.emit(FeedInvalidationBus.Event.NotInterested(video.id, video.channelId))
        }

        suspend fun markInterested(video: Video) {
            KhepiNeuroEngine.onVideoInteraction(context, video, InteractionType.LIKED, percentWatched = 0f)
        }

        /** Records [video] as watched to the end, so every card shows it full and the engine learns from it. */
        suspend fun markWatched(video: Video) {
            KhepiNeuroEngine.onVideoInteraction(context, video, InteractionType.WATCHED, percentWatched = 1.0f)
            val durationMs = if (video.duration > 0) video.duration * 1000L else 1000L
            viewHistory.savePlaybackPosition(
                videoId = video.id,
                position = durationMs,
                duration = durationMs,
                title = video.title,
                thumbnailUrl = video.thumbnailUrl.ifEmpty { ThumbnailUrlResolver.buildHighQualityYoutubeThumbnail(video.id) },
                channelName = video.channelName,
                channelId = video.channelId,
                isMusic = false,
                isShort = video.isShort,
            )
            FeedInvalidationBus.emit(FeedInvalidationBus.Event.MarkedWatched(video.id))
            watchLaterCleanup.onFinished(video.id)
        }

        /**
         * Clears the progress that makes [video] read as watched. The history row stays, and the
         * recommendation signal from the earlier watched interaction is left as it was.
         */
        suspend fun markUnwatched(video: Video) {
            viewHistory.clearProgress(video.id)
        }
    }
