package com.khepilabs.khepitube.ui.screens.settings.index

import com.khepilabs.khepitube.ui.components.settings.SettingEntry

/** Every option in Settings, in the order the pages show them. */
internal object SettingsIndex {
    val all: List<SettingEntry> by lazy {
        HomeIndex.all +
            DestinationIndex.all +
            AppearanceIndex.all +
            ThemeIndex.all +
            CustomThemeIndex.all +
            FontIndex.all +
            NavigationBarIndex.all +
            DateTimeIndex.all +
            PlayerAppearanceIndex.all +
            LanguageRegionIndex.all +
            PlaybackIndex.all +
            BufferIndex.all +
            QualityIndex.all +
            ContentIndex.all +
            MusicHomeIndex.all +
            LibrarySectionsIndex.all +
            IntegrationsIndex.all +
            DownloadsIndex.all +
            LocalMediaIndex.all +
            NotificationsIndex.all +
            HistoryIndex.all +
            AboutIndex.all
    }
}
