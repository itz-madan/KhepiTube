package com.khepilabs.khepitube.ui.screens.settings.index

import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.ui.components.settings.SettingAvailability
import com.khepilabs.khepitube.ui.components.settings.SettingEntry
import com.khepilabs.khepitube.ui.components.settings.SettingsDestination

/** Options that live directly on the settings list rather than on a page of their own. */
internal object HomeIndex {
    private val home = SettingsDestination.HOME

    val checkForUpdates =
        SettingEntry(
            key = "home.updates",
            title = R.string.check_for_updates,
            summary = R.string.check_for_updates_subtitle,
            section = R.string.settings_header_about,
            destination = home,
            availability = SettingAvailability.GithubOnly,
        )

    val all = listOf(checkForUpdates)
}
