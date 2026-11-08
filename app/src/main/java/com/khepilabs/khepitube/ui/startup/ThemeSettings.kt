package com.khepilabs.khepitube.ui.startup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
import com.khepilabs.khepitube.data.local.AppFontPreferences
import com.khepilabs.khepitube.data.local.LocalDataManager
import com.khepilabs.khepitube.ui.theme.CustomTheme
import com.khepilabs.khepitube.ui.theme.KhepiFontFamily
import com.khepilabs.khepitube.ui.theme.KhepiTheme
import com.khepilabs.khepitube.ui.theme.ThemeMode
import com.khepilabs.khepitube.ui.theme.ThemeVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Every stored theme choice the root theme needs, read as one value so the first frame has all of them. */
@Immutable
data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeVariant: ThemeVariant = ThemeVariant.DARK,
    val customTheme: CustomTheme? = null,
    val systemLightThemeMode: ThemeMode = ThemeMode.DARK,
    val systemDarkThemeMode: ThemeMode = ThemeMode.DARK,
    val systemDarkThemeVariant: ThemeVariant = ThemeVariant.DARK,
    val fontFamily: FontFamily = KhepiFontFamily,
)

fun LocalDataManager.themeSettings(fonts: AppFontPreferences): Flow<ThemeSettings> =
    combine(themeMode, themeVariant, activeCustomTheme, systemLightThemeMode, systemDarkThemeMode) { mode, variant, custom, light, dark ->
        ThemeSettings(mode, variant, custom, light, dark)
    }.combine(systemDarkThemeVariant) { settings, darkVariant -> settings.copy(systemDarkThemeVariant = darkVariant) }
        .combine(fonts.fontFamily) { settings, family -> settings.copy(fontFamily = family) }

@Composable
fun KhepiTheme(
    settings: ThemeSettings,
    content: @Composable () -> Unit,
) = KhepiTheme(
    themeMode = settings.themeMode,
    themeVariant = settings.themeVariant,
    customTheme = settings.customTheme,
    systemLightThemeMode = settings.systemLightThemeMode,
    systemDarkThemeMode = settings.systemDarkThemeMode,
    systemDarkThemeVariant = settings.systemDarkThemeVariant,
    fontFamily = settings.fontFamily,
    content = content,
)
