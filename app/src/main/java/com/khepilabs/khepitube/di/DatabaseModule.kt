package com.khepilabs.khepitube.di

import android.content.Context
import com.khepilabs.khepitube.data.local.AppDatabase
import com.khepilabs.khepitube.data.local.dao.NotificationDao
import com.khepilabs.khepitube.data.local.dao.PlaylistDao
import com.khepilabs.khepitube.data.local.dao.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = AppDatabase.getDatabase(context)

    @Provides
    fun provideVideoDao(database: AppDatabase): VideoDao = database.videoDao()

    @Provides
    fun providePlaylistDao(database: AppDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao = database.notificationDao()

    @Provides
    fun provideNoteDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.NoteDao = database.noteDao()

    @Provides
    fun provideCacheDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.CacheDao = database.cacheDao()

    @Provides
    fun provideHomeFeedCacheDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.HomeFeedCacheDao =
        database.homeFeedCacheDao()

    @Provides
    fun provideDownloadDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.DownloadDao = database.downloadDao()

    @Provides
    fun provideDownloadCollectionDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.DownloadCollectionDao =
        database.downloadCollectionDao()

    @Provides
    fun provideRecognitionHistoryDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.RecognitionHistoryDao =
        database.recognitionHistoryDao()

    @Provides
    fun provideSubscriptionGroupDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.SubscriptionGroupDao =
        database.subscriptionGroupDao()

    @Provides
    fun provideWatchHistoryDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.WatchHistoryDao = database.watchHistoryDao()

    @Provides
    fun provideSyncLogDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.SyncLogDao = database.syncLogDao()

    @Provides
    fun provideSyncPeerDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.SyncPeerDao = database.syncPeerDao()

    @Provides
    fun provideMusicGraphDao(database: AppDatabase): com.khepilabs.khepitube.data.local.dao.MusicGraphDao = database.musicGraphDao()
}
