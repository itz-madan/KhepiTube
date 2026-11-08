package com.khepilabs.khepitube.ui.screens.library

import com.khepilabs.khepitube.ui.components.library.LibrarySection

/** Saved Shorts goes with the Shorts content switch as well as its own. */
internal fun visibleContentSections(
    hidden: Set<LibrarySection>,
    separatePlaylistKinds: Boolean,
    shortsEnabled: Boolean,
): List<LibrarySection> =
    LibrarySection
        .content(separatePlaylistKinds)
        .filter { it !in hidden && (shortsEnabled || it != LibrarySection.SAVED_SHORTS) }

internal fun visibleSettingsSections(hidden: Set<LibrarySection>): List<LibrarySection> =
    LibrarySection.settingsAndData.filter { it !in hidden }
