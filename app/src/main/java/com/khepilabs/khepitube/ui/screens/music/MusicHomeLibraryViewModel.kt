package com.khepilabs.khepitube.ui.screens.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khepilabs.khepitube.data.local.PlayerPreferences
import com.khepilabs.khepitube.data.local.PlaylistRepository
import com.khepilabs.khepitube.data.local.SubscriptionRepository
import com.khepilabs.khepitube.data.music.model.ArtistDetails
import com.khepilabs.khepitube.data.music.model.MusicPlaylist
import com.khepilabs.khepitube.data.playlist.sortedFor
import com.khepilabs.khepitube.ui.components.music.section.MusicHomeShelf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val SHELF_LIMIT = 20

/**
 * The viewer's own corner of the music home: their music playlists in the playlists page's order,
 * the music channels they subscribe to, newest first, and the sections they chose to hide.
 */
@HiltViewModel
class MusicHomeLibraryViewModel
    @Inject
    constructor(
        playlistRepository: PlaylistRepository,
        subscriptionRepository: SubscriptionRepository,
        preferences: PlayerPreferences,
    ) : ViewModel() {
        private val sharing = SharingStarted.WhileSubscribed(5_000)

        val playlists: StateFlow<List<MusicPlaylist>> =
            combine(playlistRepository.getMusicPlaylistsFlow(), preferences.playlistListOrder) { playlists, order ->
                playlists.sortedFor(order).take(SHELF_LIMIT).map { playlist ->
                    MusicPlaylist(
                        id = playlist.id,
                        title = playlist.name,
                        thumbnailUrl = playlist.thumbnailUrl,
                        trackCount = playlist.videoCount,
                    )
                }
            }.stateIn(viewModelScope, sharing, emptyList())

        val subscriptions: StateFlow<List<ArtistDetails>> =
            subscriptionRepository
                .getAllSubscriptions()
                .map { subscriptions ->
                    subscriptions
                        .filter { it.isMusic }
                        .sortedByDescending { it.subscribedAt }
                        .map {
                            ArtistDetails(
                                name = it.channelName,
                                channelId = it.channelId,
                                thumbnailUrl = it.channelThumbnail,
                                subscriberCount = 0L,
                            )
                        }
                }.stateIn(viewModelScope, sharing, emptyList())

        val hiddenShelves: StateFlow<Set<MusicHomeShelf>> =
            preferences.hiddenMusicHomeShelves
                .map(MusicHomeShelf::fromStored)
                .stateIn(viewModelScope, sharing, emptySet())
    }
