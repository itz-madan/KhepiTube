package com.khepilabs.khepitube.ui.screens.settings.taste

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khepilabs.khepitube.data.recommendation.music.FavouriteArtistCatalog
import com.khepilabs.khepitube.data.recommendation.music.FavouriteArtistPicker
import com.khepilabs.khepitube.data.recommendation.music.FavouriteArtistsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class FavouriteArtistsViewModel
    @Inject
    constructor(
        store: FavouriteArtistsStore,
        catalog: FavouriteArtistCatalog,
    ) : ViewModel() {
        val picker = FavouriteArtistPicker(viewModelScope, store, catalog)
    }
