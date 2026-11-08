package com.khepilabs.khepitube.ui.screens.settings.index

import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.ui.components.settings.SettingEntry
import com.khepilabs.khepitube.ui.components.settings.SettingsDestination
import com.khepilabs.khepitube.widget.core.KhepiWidgetEntry
import com.khepilabs.khepitube.widget.core.KhepiWidgets

internal object WidgetsIndex {
    fun entryFor(widget: KhepiWidgetEntry) =
        SettingEntry(
            key = "widgets.${widget.id}",
            title = widget.label,
            summary = widget.description,
            section = R.string.settings_widgets_add_header,
            keywords = R.string.settings_keywords_widgets,
            destination = SettingsDestination.WIDGETS,
        )

    val all = KhepiWidgets.catalog.map(::entryFor)
}
