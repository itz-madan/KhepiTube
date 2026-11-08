package com.khepilabs.khepitube.ui.components.layout.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.local.DEFAULT_NAV_TAB_ORDER

/**
 * A root destination of the app's primary navigation.
 */
enum class KhepiTab(
    val id: Int,
    val route: String,
    @StringRes val labelRes: Int,
) {
    Home(0, "home", R.string.nav_home),
    Shorts(1, "shorts", R.string.nav_shorts),
    Music(2, "music", R.string.nav_music),
    LocalMedia(3, "localMedia", R.string.settings_local_media_title),
    Subscriptions(4, "subscriptions", R.string.nav_subs),
    Library(5, "library", R.string.nav_library),
    Search(6, "search", R.string.nav_search),
    Explore(7, "categories", R.string.nav_explore),
    ;

    companion object {
        fun fromId(id: Int): KhepiTab? = entries.firstOrNull { it.id == id }
    }
}

@Composable
fun KhepiTab.icon(selected: Boolean): ImageVector =
    when (this) {
        KhepiTab.Home -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        KhepiTab.Shorts -> ImageVector.vectorResource(if (selected) R.drawable.ic_shorts_filled else R.drawable.ic_shorts)
        KhepiTab.Music -> if (selected) Icons.Filled.MusicNote else Icons.Outlined.MusicNote
        KhepiTab.LocalMedia -> if (selected) Icons.Filled.Folder else Icons.Outlined.Folder
        KhepiTab.Subscriptions -> if (selected) Icons.Filled.Subscriptions else Icons.Outlined.Subscriptions
        KhepiTab.Library -> if (selected) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary
        KhepiTab.Search -> if (selected) Icons.Filled.Search else Icons.Outlined.Search
        KhepiTab.Explore -> if (selected) Icons.Filled.Explore else Icons.Outlined.Explore
    }

data class NavigationVisibility(
    val home: Boolean = true,
    val shorts: Boolean = true,
    val music: Boolean = false,
    val search: Boolean = false,
    val categories: Boolean = false,
)

fun visibleKhepiTabs(
    order: List<Int>,
    visibility: NavigationVisibility,
): List<KhepiTab> {
    val enabled =
        buildSet {
            if (visibility.home) add(KhepiTab.Home)
            if (visibility.shorts) add(KhepiTab.Shorts)
            add(KhepiTab.LocalMedia)
            add(KhepiTab.Library)
            if (visibility.search) add(KhepiTab.Search)
            if (visibility.categories) add(KhepiTab.Explore)
        }
    return (order + DEFAULT_NAV_TAB_ORDER)
        .distinct()
        .mapNotNull(KhepiTab::fromId)
        .filter(enabled::contains)
}

fun resolveDefaultKhepiTab(
    preferredId: Int,
    order: List<Int>,
    visibility: NavigationVisibility,
): KhepiTab {
    val visible = visibleKhepiTabs(order, visibility)
    return visible.firstOrNull { it.id == preferredId } ?: visible.first()
}
