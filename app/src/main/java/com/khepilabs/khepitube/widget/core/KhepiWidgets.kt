package com.khepilabs.khepitube.widget.core

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.khepilabs.khepitube.R

data class KhepiWidgetEntry(
    val id: String,
    @StringRes val label: Int,
    @StringRes val description: Int,
    val receiver: Class<*>? = null,
    @DrawableRes val icon: Int = R.drawable.ic_launcher_foreground,
)

object KhepiWidgets {
    val catalog: List<KhepiWidgetEntry> =
        listOf(
            KhepiWidgetEntry("search", R.string.search, R.string.search_hint),
        )

    suspend fun observeThemeChanges(context: Context) {}
}
