package com.khepilabs.khepitube.ui.screens.settings.downloads

import com.khepilabs.khepitube.data.local.MediaCacheSizes
import com.khepilabs.khepitube.data.local.PlayerPreferences
import com.khepilabs.khepitube.data.repository.MediaCacheRepository
import com.khepilabs.khepitube.data.repository.MediaCacheType
import com.khepilabs.khepitube.data.repository.MediaCacheUsage
import com.khepilabs.khepitube.ui.screens.settings.SettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** The Cache group of Downloads: what each cache holds, its size limit, and clearing it. */
@HiltViewModel
class DownloadCacheViewModel
    @Inject
    constructor(
        private val preferences: PlayerPreferences,
        private val repository: MediaCacheRepository,
    ) : SettingsViewModel() {
        val videoLimitMb = preferences.mediaCacheSizeMb.asState(MediaCacheSizes.DEFAULT_MEDIA_MB)
        val songLimitMb = preferences.musicCacheSizeMb.asState(MediaCacheSizes.DEFAULT_MEDIA_MB)
        val artworkLimitMb = preferences.artworkCacheSizeMb.asState(MediaCacheSizes.ARTWORK_AUTOMATIC_MB)

        private val measured = MutableStateFlow<MediaCacheUsage?>(null)

        /** Null until the page has measured the caches once. */
        val usage: StateFlow<MediaCacheUsage?> = measured.asStateFlow()

        /** Measured each time the page opens; nothing polls while it is open. */
        fun measure() = write { measured.value = repository.usage() }

        fun clear(type: MediaCacheType) =
            write {
                repository.clear(type)
                measured.value = repository.usage()
            }

        fun setLimit(
            type: MediaCacheType,
            megabytes: Int,
        ) = write {
            when (type) {
                MediaCacheType.VIDEOS -> preferences.setMediaCacheSizeMb(megabytes)
                MediaCacheType.SONGS -> preferences.setMusicCacheSizeMb(megabytes)
                MediaCacheType.ARTWORK -> preferences.setArtworkCacheSizeMb(megabytes)
                MediaCacheType.OTHER -> Unit
            }
        }
    }
