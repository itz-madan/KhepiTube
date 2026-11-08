package com.khepilabs.khepitube.ui.screens.settings.appearance

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.local.AppFontSelection
import com.khepilabs.khepitube.data.local.HomeFeedColumns
import com.khepilabs.khepitube.data.local.HomeViewMode
import com.khepilabs.khepitube.ui.components.settings.SettingsDestination
import com.khepilabs.khepitube.ui.components.settings.SettingsPage
import com.khepilabs.khepitube.ui.components.settings.SettingsTarget
import com.khepilabs.khepitube.ui.components.settings.nav
import com.khepilabs.khepitube.ui.components.settings.switch
import com.khepilabs.khepitube.ui.components.settings.toggleGroup
import com.khepilabs.khepitube.ui.components.shared.KhepiToggleOption
import com.khepilabs.khepitube.ui.screens.settings.index.AppearanceIndex
import com.khepilabs.khepitube.ui.screens.settings.index.DestinationIndex
import com.khepilabs.khepitube.ui.theme.AppFont
import com.khepilabs.khepitube.ui.theme.GridItemSize
import com.khepilabs.khepitube.ui.theme.ThemeCatalog
import com.khepilabs.khepitube.ui.theme.ThemeMode

/** Everything about how KhepiTube looks: theme, layout, and sub-pages for finer control. */
@Composable
internal fun AppearanceScreen(
    onBack: (() -> Unit)?,
    highlight: String?,
    onNavigate: (SettingsTarget) -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val themeVariant by viewModel.themeVariant.collectAsStateWithLifecycle()
    val customThemeName by viewModel.customThemeName.collectAsStateWithLifecycle()
    val font by viewModel.font.collectAsStateWithLifecycle()
    val homeViewMode by viewModel.homeViewMode.collectAsStateWithLifecycle()
    val homeColumns by viewModel.homeColumns.collectAsStateWithLifecycle()
    val gridItemSize by viewModel.gridItemSize.collectAsStateWithLifecycle()
    val libraryPreviews by viewModel.libraryPreviews.collectAsStateWithLifecycle()
    val separatePlaylists by viewModel.separatePlaylists.collectAsStateWithLifecycle()
    val appLogo by viewModel.appLogo.collectAsStateWithLifecycle()
    val groupBadges by viewModel.groupBadges.collectAsStateWithLifecycle()
    val cardLikeButtons by viewModel.cardLikeButtons.collectAsStateWithLifecycle()
    val cardMarkWatched by viewModel.cardMarkWatched.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val themeSummary =
        if (themeMode == ThemeMode.SYSTEM) {
            stringResource(R.string.theme_name_system_default)
        } else {
            stringResource(
                R.string.settings_value_pair,
                customThemeName.takeIf { themeMode == ThemeMode.CUSTOM } ?: stringResource(ThemeCatalog.nameRes(themeMode)),
                stringResource(themeVariantLabel(themeVariant)),
            )
        }
    val fontSummary = fontLabel(font)
    val viewModeOptions =
        listOf(
            KhepiToggleOption(HomeViewMode.GRID, stringResource(R.string.content_settings_layout_grid), Icons.Outlined.GridView),
            KhepiToggleOption(
                HomeViewMode.LIST,
                stringResource(R.string.content_settings_layout_list),
                Icons.AutoMirrored.Outlined.ViewList,
            ),
        )
    val columnOptions =
        HomeFeedColumns.entries.map { columns ->
            KhepiToggleOption(
                columns,
                columns.fixedCount?.toString() ?: stringResource(R.string.content_settings_home_columns_auto),
            )
        }
    val artworkOptions =
        listOf(
            KhepiToggleOption(GridItemSize.BIG, stringResource(R.string.content_settings_grid_big_title)),
            KhepiToggleOption(GridItemSize.SMALL, stringResource(R.string.content_settings_grid_small_title)),
        )

    SettingsPage(
        title = stringResource(R.string.appearance_title),
        onBack = onBack,
        highlight = highlight,
        snackbarHostState = snackbarHostState,
    ) {
        group(key = "appearance.style", header = R.string.settings_section_style) {
            nav(
                DestinationIndex.entry(SettingsDestination.THEME),
                value = themeSummary,
                icon = Icons.Outlined.Palette,
                onClick = { onNavigate(SettingsTarget(SettingsDestination.THEME)) },
            )
            nav(
                DestinationIndex.entry(SettingsDestination.FONT),
                value = fontSummary,
                icon = Icons.Outlined.TextFields,
                onClick = { onNavigate(SettingsTarget(SettingsDestination.FONT)) },
            )
        }
        group(key = "appearance.layout", header = R.string.settings_section_layout) {
            toggleGroup(AppearanceIndex.homeViewMode, viewModeOptions, homeViewMode, viewModel::setHomeViewMode)
            if (homeViewMode == HomeViewMode.GRID) {
                toggleGroup(AppearanceIndex.homeColumns, columnOptions, homeColumns, viewModel::setHomeColumns)
            }
            toggleGroup(AppearanceIndex.musicArtworkSize, artworkOptions, gridItemSize, viewModel::setGridItemSize)
            switch(AppearanceIndex.libraryPreviews, libraryPreviews, viewModel::setLibraryPreviews)
            switch(AppearanceIndex.separatePlaylists, separatePlaylists, viewModel::setSeparatePlaylists)
            switch(AppearanceIndex.appLogo, appLogo, viewModel::setAppLogo)
            switch(AppearanceIndex.groupBadges, groupBadges, viewModel::setGroupBadges)
        }
        group(key = "appearance.cards", header = R.string.settings_section_video_cards) {
            switch(AppearanceIndex.cardLikeButtons, cardLikeButtons, viewModel::setCardLikeButtons)
            switch(AppearanceIndex.cardWatchProgress, viewModel.cardWatchProgress, viewModel::setCardWatchProgress)
            switch(AppearanceIndex.cardMarkWatched, cardMarkWatched, viewModel::setCardMarkWatched)
        }
        group(key = "appearance.more", header = R.string.settings_section_more) {
            listOf(
                SettingsDestination.NAVIGATION_BAR,
                SettingsDestination.DATE_TIME,
                SettingsDestination.PLAYER_APPEARANCE,
            ).forEach { destination ->
                nav(DestinationIndex.entry(destination), onClick = { onNavigate(SettingsTarget(destination)) })
            }
        }
    }
}

@Composable
private fun fontLabel(font: AppFontSelection): String =
    when (font.font) {
        AppFont.SYSTEM -> stringResource(R.string.font_system_default)
        AppFont.CONDENSED -> stringResource(R.string.font_condensed)
        AppFont.SERIF -> stringResource(R.string.font_serif)
        AppFont.CUSTOM -> font.customName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.font_custom)
    }
