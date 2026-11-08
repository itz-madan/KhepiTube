package com.khepilabs.khepitube.di

import android.content.Context
import com.khepilabs.khepitube.data.local.PlayerPreferences
import com.khepilabs.khepitube.data.repository.YouTubeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideYouTubeRepository(playerPreferences: PlayerPreferences): YouTubeRepository = YouTubeRepository.getInstance(playerPreferences)

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        @ApplicationContext context: Context,
    ): com.khepilabs.khepitube.data.local.SubscriptionRepository =
        com.khepilabs.khepitube.data.local.SubscriptionRepository
            .getInstance(context)

    @Provides
    @Singleton
    fun provideLikedVideosRepository(
        @ApplicationContext context: Context,
    ): com.khepilabs.khepitube.data.local.LikedVideosRepository =
        com.khepilabs.khepitube.data.local.LikedVideosRepository
            .getInstance(context)

    @Provides
    @Singleton
    fun provideViewHistory(
        @ApplicationContext context: Context,
    ): com.khepilabs.khepitube.data.local.ViewHistory =
        com.khepilabs.khepitube.data.local.ViewHistory
            .getInstance(context)

    @Provides
    @Singleton
    fun provideHomeFeedCacheRepository(
        @ApplicationContext context: Context,
    ): com.khepilabs.khepitube.data.local.HomeFeedCacheRepository =
        com.khepilabs.khepitube.data.local
            .HomeFeedCacheRepository(context)

    @Provides
    @Singleton
    fun provideMusicPlaylistRepository(
        @ApplicationContext context: Context,
    ): com.khepilabs.khepitube.data.music.PlaylistRepository =
        com.khepilabs.khepitube.data.music
            .PlaylistRepository(context)

    // VideoDownloadManager is now @Singleton @Inject — Hilt provides it automatically
    @Provides
    @Singleton
    fun providePlayerPreferences(
        @ApplicationContext context: Context,
    ): com.khepilabs.khepitube.data.local.PlayerPreferences =
        com.khepilabs.khepitube.data.local
            .PlayerPreferences(context)

    /** Transitional: the music player manager still reaches the queue store through getInstance, so this hands injected consumers the same instance. */
    @Provides
    @Singleton
    fun provideQueuePersistence(
        @ApplicationContext context: Context,
    ): com.khepilabs.khepitube.data.local.QueuePersistence =
        com.khepilabs.khepitube.data.local.QueuePersistence
            .getInstance(context)

    /**
     * Transitional: [com.khepilabs.khepitube.data.local.BackupRepository] still builds its own
     * collaborators, so it is provided here rather than injected through its constructor. One
     * instance serves the whole app.
     */
    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
    ): com.khepilabs.khepitube.data.local.BackupRepository =
        com.khepilabs.khepitube.data.local
            .BackupRepository(context)
}
