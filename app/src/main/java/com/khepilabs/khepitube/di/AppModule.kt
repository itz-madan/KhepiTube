package com.khepilabs.khepitube.di

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import coil3.video.VideoFrameDecoder
import com.khepilabs.khepitube.BuildConfig
import com.khepilabs.khepitube.data.localmedia.MediaRetrieverDecoderFactory
import com.khepilabs.khepitube.data.localmedia.MediaStoreThumbnailFetcher
import com.khepilabs.khepitube.data.recommendation.KhepiNeuroEngine
import com.khepilabs.khepitube.innertube.YouTube
import com.khepilabs.khepitube.player.cache.PlayerCacheManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import javax.inject.Singleton

/** The image cache folder under the app's cache directory. */
const val IMAGE_CACHE_DIR = "image_cache"

private const val AUTOMATIC_DISK_SHARE = 0.02

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideYouTube(): YouTube = YouTube

    /** Delegates to the legacy singleton so injected consumers share the one engine the app already runs. */
    @Provides
    @Singleton
    fun provideKhepiNeuroEngine(
        @ApplicationContext context: Context,
    ): KhepiNeuroEngine = KhepiNeuroEngine.getInstance(context)

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
    ): ImageLoader =
        ImageLoader
            .Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
                add(MediaRetrieverDecoderFactory(VideoFrameDecoder.Factory()))
                add(MediaStoreThumbnailFetcher.Factory(context))
            }.memoryCache {
                MemoryCache
                    .Builder()
                    .maxSizePercent(context, 0.10)
                    .build()
            }.diskCache {
                // Coil builds this lazily on its first disk access, off the main thread, so the size
                // setting is read there rather than on the cold-start path.
                val limit = PlayerCacheManager.configuredLimits(context).artworkBytes
                DiskCache
                    .Builder()
                    .directory(context.cacheDir.resolve(IMAGE_CACHE_DIR).toOkioPath())
                    .apply { if (limit != null) maxSizeBytes(limit) else maxSizePercent(AUTOMATIC_DISK_SHARE) }
                    .build()
            }.crossfade(true)
            .apply { if (BuildConfig.DEBUG) logger(DebugLogger()) }
            .build()
}
