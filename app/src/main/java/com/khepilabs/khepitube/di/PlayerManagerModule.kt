package com.khepilabs.khepitube.di

import com.khepilabs.khepitube.data.localmedia.LocalMediaIds
import com.khepilabs.khepitube.data.localmedia.LocalSubtitles
import com.khepilabs.khepitube.data.recommendation.KhepiNeuroEngine
import com.khepilabs.khepitube.data.video.OfflineSubtitleStore
import com.khepilabs.khepitube.data.video.VideoDownloadManager
import com.khepilabs.khepitube.player.EnhancedPlayerManager
import com.khepilabs.khepitube.player.FeedExclusionsSource
import com.khepilabs.khepitube.player.LocalCaptionSource
import com.khepilabs.khepitube.player.LocalCaptions
import com.khepilabs.khepitube.player.LocalCopySource
import com.khepilabs.khepitube.utils.PerformanceDispatcher
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkIoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Transitional providers around the player-path singletons so consumers can inject them instead of
 * reaching for the static accessors. Every provider delegates to the existing singleton, so object
 * identity and first-creation timing are unchanged (see CLAUDE.md, DI rule 10).
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerManagerModule {
    @Provides
    fun provideEnhancedPlayerManager(
        videoDownloadManager: VideoDownloadManager,
        neuroEngine: Lazy<KhepiNeuroEngine>,
        localSubtitles: Lazy<LocalSubtitles>,
        offlineSubtitleStore: Lazy<OfflineSubtitleStore>,
    ): EnhancedPlayerManager =
        EnhancedPlayerManager.getInstance().also {
            it.localCopySource =
                LocalCopySource { videoId ->
                    LocalMediaIds.videoUri(videoId)?.toString() ?: videoDownloadManager.localCopyPath(videoId)
                }
            it.localCaptionSource =
                LocalCaptionSource { videoId, path ->
                    val subtitles = localSubtitles.get()
                    val own =
                        if (LocalMediaIds.isLocal(videoId)) subtitles.beside(path) else offlineSubtitleStore.get().load(videoId)
                    LocalCaptions(own + subtitles.picked(videoId), subtitles.offsetMs(videoId))
                }
            it.feedExclusionsSource = FeedExclusionsSource { neuroEngine.get().feedExclusions() }
        }

    @Provides
    @NetworkIoDispatcher
    fun provideNetworkIoDispatcher(): CoroutineDispatcher = PerformanceDispatcher.networkIO

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = PerformanceDispatcher.diskIO
}
