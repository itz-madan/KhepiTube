package com.khepilabs.khepitube.ui.screens.music

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.music.model.MusicTrack
import com.khepilabs.khepitube.innertube.models.AlbumItem
import com.khepilabs.khepitube.innertube.models.ArtistItem
import com.khepilabs.khepitube.innertube.models.PlaylistItem
import com.khepilabs.khepitube.innertube.models.SongItem
import com.khepilabs.khepitube.innertube.models.YTItem
import com.khepilabs.khepitube.innertube.pages.MoodAndGenres
import com.khepilabs.khepitube.innertube.pages.SearchSummaryKind
import com.khepilabs.khepitube.ui.components.layout.khepiBottomContentPadding
import com.khepilabs.khepitube.ui.components.music.card.TopResultCard
import com.khepilabs.khepitube.ui.components.music.header.MusicSectionHeader
import com.khepilabs.khepitube.ui.components.music.item.MusicCollectionRow
import com.khepilabs.khepitube.ui.components.music.search.SearchFilterChips
import com.khepilabs.khepitube.ui.components.music.search.SearchSuggestionRow
import com.khepilabs.khepitube.ui.components.music.search.musicSearchFilters
import com.khepilabs.khepitube.ui.components.music.search.searchSummaryTitle
import com.khepilabs.khepitube.ui.components.music.section.MusicMoodsShelf
import com.khepilabs.khepitube.ui.components.music.sheet.LocalMusicMenus
import com.khepilabs.khepitube.ui.components.music.sheet.toCollectionActionItem
import com.khepilabs.khepitube.ui.components.search.searchHistoryItems
import com.khepilabs.khepitube.ui.components.shared.KhepiEmptyState
import com.khepilabs.khepitube.ui.components.shared.KhepiErrorState
import com.khepilabs.khepitube.ui.components.shared.KhepiFeedProgress
import com.khepilabs.khepitube.ui.components.shared.KhepiLoadingIndicator

private const val RECOMMENDED_SOURCE = "Recommended"

/**
 * What the music search shows under its bar: history, suggestions and moods while typing, then the
 * filters and results. [dismissSearchInput] hides the keyboard once a result opens; [onSuggestionPicked]
 * runs when a suggestion starts a search. Shared by the phone's music search and TV search.
 */
@Composable
internal fun MusicSearchBody(
    viewModel: MusicSearchViewModel,
    onTrackClick: (MusicTrack, List<MusicTrack>, String?) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onChannelClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onMoodClick: (MoodAndGenres.Item) -> Unit,
    onMoodsSeeAll: () -> Unit,
    dismissSearchInput: () -> Unit,
    onSuggestionPicked: () -> Unit,
) {
    val query by viewModel.query.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.matchingHistory.collectAsStateWithLifecycle()
    val moods by viewModel.moods.collectAsStateWithLifecycle()
    val musicMenus = LocalMusicMenus.current

    fun menuActionFor(item: YTItem): (() -> Unit)? =
        when (item) {
            is SongItem -> ({ musicMenus.openSong(convertSongToMusicTrack(item)) })
            is AlbumItem, is PlaylistItem -> ({ item.toCollectionActionItem()?.let(musicMenus::openCollection) })
            else -> null
        }

    fun isDownloaded(item: YTItem): Boolean = (item as? SongItem)?.let { uiState.downloadedTrackIds.contains(it.id) } ?: false

    // A song from search plays alone, as YouTube Music does: the service seeds its radio from it,
    // so Up Next fills with related songs instead of the other results for the same title.
    fun openItem(
        item: YTItem,
        source: String,
    ) {
        dismissSearchInput()
        when (item) {
            is SongItem -> {
                val track = convertSongToMusicTrack(item)
                onTrackClick(track, listOf(track), source)
            }

            is ArtistItem -> {
                if (item.isProfile) onChannelClick(item.id) else onArtistClick(item.id)
            }

            is AlbumItem -> {
                onAlbumClick(item.id)
            }

            is PlaylistItem -> {
                onPlaylistClick(item.id)
            }
        }
    }

    fun playArtistTracks(
        artist: ArtistItem,
        shuffle: Boolean,
        source: String,
    ) {
        viewModel.getArtistTracks(artist.id) { tracks ->
            val musicTracks = tracks.filterIsInstance<SongItem>().map(::convertSongToMusicTrack)
            if (musicTracks.isNotEmpty()) {
                val queue = if (shuffle) musicTracks.shuffled() else musicTracks
                dismissSearchInput()
                onTrackClick(queue.first(), queue, source)
            }
        }
    }

    if (!uiState.isSearching) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = khepiBottomContentPadding()),
        ) {
            searchHistoryItems(
                query = query,
                history = history,
                onSelect = { item ->
                    viewModel.performSearch(item.query)
                    dismissSearchInput()
                },
                onDeleteHistoryItem = viewModel::deleteHistoryItem,
                onClearHistory = viewModel::clearHistory,
            )
            items(uiState.recommendedItems, key = { it.stableLazyKey("recommended") }) { item ->
                MusicCollectionRow(
                    item = item,
                    onClick = { openItem(item, RECOMMENDED_SOURCE) },
                    onMenuClick = menuActionFor(item),
                    onLongClick = menuActionFor(item),
                    isDownloaded = isDownloaded(item),
                )
            }
            items(uiState.suggestions, key = { it }) { suggestion ->
                SearchSuggestionRow(
                    suggestion = suggestion,
                    onClick = {
                        viewModel.performSearch(suggestion)
                        onSuggestionPicked()
                    },
                )
            }
            if (query.isBlank()) {
                item(key = "moods") {
                    MusicMoodsShelf(
                        moods = moods,
                        onMoodClick = { mood ->
                            dismissSearchInput()
                            onMoodClick(mood)
                        },
                        onSeeAll = {
                            dismissSearchInput()
                            onMoodsSeeAll()
                        },
                    )
                }
            }
        }
    } else {
        val filterParams = uiState.searchSummary?.filterParams.orEmpty()
        SearchFilterChips(
            filters = remember(filterParams) { musicSearchFilters(filterParams) },
            activeFilter = uiState.activeFilter,
            onFilterClick = viewModel::applyFilter,
        )

        val searchSource = stringResource(R.string.search_source_template).format(query)
        val artistSourceTemplate = stringResource(R.string.artist_source_template)

        val summaries = uiState.searchSummary?.summaries
        val hasResults =
            if (uiState.activeFilter == null) {
                summaries?.any { it.items.isNotEmpty() } == true
            } else {
                uiState.filteredResults.isNotEmpty()
            }

        val error = uiState.error
        if (uiState.isLoading) {
            KhepiLoadingIndicator()
        } else if (!hasResults && error != null) {
            KhepiErrorState(
                error = error,
                onRetry = {
                    val filter = uiState.activeFilter
                    if (filter == null) viewModel.performSearch(query) else viewModel.applyFilter(filter)
                },
            )
        } else if (!hasResults) {
            KhepiEmptyState(
                title = stringResource(R.string.music_search_no_results, query),
                icon = Icons.Rounded.SearchOff,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = khepiBottomContentPadding()),
            ) {
                if (uiState.activeFilter == null && summaries != null) {
                    summaries.forEachIndexed { index, summary ->
                        item(key = "summary_header_$index") {
                            MusicSectionHeader(title = searchSummaryTitle(summary))
                        }

                        val isTopResult = summary.kind == SearchSummaryKind.TOP_RESULT
                        if (isTopResult) {
                            val topItem = summary.items.first()
                            item(key = "top_result") {
                                TopResultCard(
                                    item = topItem,
                                    onClick = { openItem(topItem, searchSource) },
                                    onShuffleClick = {
                                        if (topItem is ArtistItem) {
                                            playArtistTracks(topItem, shuffle = true, artistSourceTemplate.format(topItem.title))
                                        }
                                    },
                                    onRadioClick = {
                                        if (topItem is ArtistItem) {
                                            playArtistTracks(topItem, shuffle = false, artistSourceTemplate.format(topItem.title))
                                        }
                                    },
                                    onLongClick = menuActionFor(topItem),
                                    onMenuClick = menuActionFor(topItem),
                                )
                            }
                        }

                        items(
                            items = if (isTopResult) summary.items.drop(1) else summary.items,
                            key = { it.stableLazyKey("summary_${summary.kind}_${summary.title}") },
                        ) { item ->
                            MusicCollectionRow(
                                showPlayCount = true,
                                item = item,
                                onClick = { openItem(item, searchSource) },
                                onMenuClick = menuActionFor(item),
                                onLongClick = menuActionFor(item),
                                isDownloaded = isDownloaded(item),
                            )
                        }
                    }
                } else {
                    items(uiState.filteredResults, key = { it.stableLazyKey("filtered") }) { item ->
                        MusicCollectionRow(
                            showPlayCount = true,
                            item = item,
                            onClick = { openItem(item, searchSource) },
                            onMenuClick = menuActionFor(item),
                            onLongClick = menuActionFor(item),
                            isDownloaded = isDownloaded(item),
                        )
                    }
                }

                if (uiState.continuation != null) {
                    item(key = "continuation") {
                        LaunchedEffect(Unit) {
                            viewModel.loadMore()
                        }
                        if (uiState.isMoreLoading) {
                            KhepiFeedProgress()
                        }
                    }
                }
            }
        }
    }
}
