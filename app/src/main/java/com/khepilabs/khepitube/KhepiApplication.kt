package com.khepilabs.khepitube

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.os.Build
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.khepilabs.khepitube.data.local.PlayerPreferences
import com.khepilabs.khepitube.data.local.SubscriptionRepository
import com.khepilabs.khepitube.data.repository.NewPipeDownloader
import com.khepilabs.khepitube.data.repository.YouTubeRepository
import com.khepilabs.khepitube.innertube.YouTube
import com.khepilabs.khepitube.network.AppProxyConfig
import com.khepilabs.khepitube.network.AppProxyManager
import com.khepilabs.khepitube.network.VpnStateMonitor
import com.khepilabs.khepitube.notification.BackgroundCheckScheduler
import com.khepilabs.khepitube.notification.NotificationHelper
import com.khepilabs.khepitube.player.PlaybackShutdown
import com.khepilabs.khepitube.player.SleepTimerManager
import com.khepilabs.khepitube.ui.screens.home.HomeFeedLanguageReset
import com.khepilabs.khepitube.utils.AppLanguageManager
import com.khepilabs.khepitube.utils.KhepiCrashHandler
import com.khepilabs.khepitube.utils.PerformanceDispatcher
import com.khepilabs.khepitube.utils.cipher.CipherDeobfuscator
import com.khepilabs.khepitube.utils.cipher.PipePipeNsigDecoder
import com.khepilabs.khepitube.utils.contentYouTubeLocale
import com.khepilabs.khepitube.utils.newPipeContentCountry
import com.khepilabs.khepitube.utils.newPipeLocalization
import com.khepilabs.khepitube.utils.potoken.NewPipePoTokenProvider
import com.khepilabs.khepitube.utils.potoken.VisitorIdentityStore
import com.khepilabs.khepitube.utils.potoken.WebPoTokenSession
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import java.security.Security
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
open class KhepiApplication :
    Application(),
    SingletonImageLoader.Factory {
    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var vpnStateMonitor: VpnStateMonitor

    @Inject
    lateinit var visitorIdentityStore: VisitorIdentityStore

    @Inject
    internal lateinit var homeFeedLanguageReset: HomeFeedLanguageReset

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader

    companion object {
        private const val TAG = "KhepiApplication"
        lateinit var appContext: Context
            private set
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLanguageManager.wrapContext(base, AppLanguageManager.resolveLanguageTag(base)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        YouTube.cacheDirectory = cacheDir.resolve("innertube_http_cache")

        val playerPreferences = PlayerPreferences(this)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        }

        KhepiCrashHandler.install(this)

        try {
            val localization = newPipeLocalization(Locale.getDefault().toLanguageTag())
            NewPipe.init(
                NewPipeDownloader.getInstance(this),
                localization,
                newPipeContentCountry(Locale.getDefault().country),
            )
            YoutubeStreamExtractor.setPoTokenProvider(NewPipePoTokenProvider)
            Log.d(TAG, "NewPipe initialized with ${localization.localizationCode}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize NewPipe", e)
        }

        try {
            CipherDeobfuscator
                .initialize(this)
            Log.d(TAG, "CipherDeobfuscator initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize CipherDeobfuscator", e)
        }

        PipePipeNsigDecoder.initialize(this)

        NotificationHelper.createNotificationChannels(this)
        Log.d(TAG, "Notification channels created")

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            BackgroundCheckScheduler.follow(this@KhepiApplication, playerPreferences)
        }
        SleepTimerManager.attachExitCallback { PlaybackShutdown.closeApp(this) }

        val warmups = StartupWarmups(this)
        warmups.startWithFirstActivity()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            playerPreferences.proxyConfig
                .distinctUntilChanged()
                .flatMapLatest { config ->
                    if (config.watchesVpn()) {
                        vpnStateMonitor.vpnActive().map { vpnActive -> config to vpnActive }
                    } else {
                        flowOf(config to false)
                    }
                }.collectLatest { (proxyConfig, vpnActive) ->
                    applyProxyConfig(proxyConfig, vpnActive)
                    warmups.onProxyApplied()
                }
        }

        YouTube.onVisitorDataChanged = visitorIdentityStore::save
        WebPoTokenSession.bindIdentityStore(visitorIdentityStore)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                visitorIdentityStore.restore()?.let { restored ->
                    YouTube.visitorData = restored
                    Log.d(TAG, "visitorData restored")
                }
            } catch (e: Exception) {
                Log.w(TAG, "visitorData restore error: ${e.message}")
            }
            warmups.onIdentityRestored()
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            AppLanguageManager.appliedLanguage.filterNotNull().collect { tag ->
                if (playerPreferences.appLanguage.first() != tag) playerPreferences.setAppLanguage(tag)
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            combine(
                AppLanguageManager.appliedLanguage.filterNotNull(),
                playerPreferences.contentLanguage,
                playerPreferences.trendingRegion,
            ) { appLanguage, contentLanguage, region ->
                contentYouTubeLocale(appLanguage, contentLanguage, region, AppLanguageManager.deviceLocale(this@KhepiApplication))
            }.distinctUntilChanged().collectLatest { newLocale ->
                YouTube.locale = newLocale
                NewPipe.setupLocalization(
                    newPipeLocalization(newLocale.hl),
                    ContentCountry(newLocale.gl),
                )
                Log.d(TAG, "Dynamic YouTube Locale updated: gl=${newLocale.gl}, hl=${newLocale.hl}")
                homeFeedLanguageReset.onContentLanguage(newLocale.hl)
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var lastRegion: String? = null
            playerPreferences.trendingRegion.collectLatest { region ->
                if (lastRegion != null && lastRegion != region) {
                    Log.d(TAG, "Trending region changed from $lastRegion to $region. Invalidate visitor data.")
                    YouTube.visitorData = null

                    YouTube
                        .visitorData()
                        .onSuccess { data ->
                            if (!data.isNullOrEmpty()) {
                                YouTube.visitorData = data
                                Log.d(TAG, "Fresh visitorData fetched for region: $region")
                            }
                        }.onFailure { e ->
                            Log.w(TAG, "Failed to fetch fresh visitorData: ${e.message}")
                        }
                }
                lastRegion = region
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = SubscriptionRepository.getInstance(this@KhepiApplication)
                val youtubeRepository = YouTubeRepository.getInstance(playerPreferences)
                val repaired =
                    repository.repairVideoThumbnailSubscriptions { channelId ->
                        withTimeoutOrNull(20_000L) {
                            youtubeRepository.fetchChannelAvatarById(channelId)
                        }.orEmpty()
                    }
                if (repaired > 0) {
                    Log.i(TAG, "Repaired $repaired subscription thumbnails")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Subscription thumbnail repair failed: ${e.message}")
            }
        }
    }

    private fun applyProxyConfig(
        config: AppProxyConfig,
        vpnActive: Boolean,
    ) {
        AppProxyManager.update(config, vpnActive)
        YouTube.proxy = AppProxyManager.currentProxy()
        YouTube.proxyAuth = AppProxyManager.currentHttpProxyAuthorizationHeader()
    }

    override fun onTerminate() {
        super.onTerminate()
        PerformanceDispatcher.shutdown()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        KhepiCrashHandler.recordPhase("memory", "KhepiApplication.onLowMemory")
        releaseVolatileMemory()
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        KhepiCrashHandler.recordPhase("memory", "KhepiApplication.onTrimMemory level=$level")
        if (level >= TRIM_MEMORY_RUNNING_LOW) {
            releaseVolatileMemory()
        }
    }

    private fun releaseVolatileMemory() {
        if (::imageLoader.isInitialized) {
            imageLoader.memoryCache?.clear()
        }
        if (::okHttpClient.isInitialized) {
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                okHttpClient.connectionPool.evictAll()
            }
        }
    }
}
