package com.khepilabs.khepitube.ui.screens.settings.content

import com.khepilabs.khepitube.data.local.PlayerPreferences
import com.khepilabs.khepitube.ui.components.music.section.MusicHomeShelf
import com.khepilabs.khepitube.ui.screens.settings.SettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MusicHomeSettingsViewModel
    @Inject
    constructor(
        private val preferences: PlayerPreferences,
    ) : SettingsViewModel() {
        val hidden = preferences.hiddenMusicHomeShelves.map(MusicHomeShelf::fromStored).asState(emptySet())

        fun setShown(
            shelf: MusicHomeShelf,
            shown: Boolean,
        ) = write { preferences.setMusicHomeShelfHidden(shelf.name, hidden = !shown) }
    }
