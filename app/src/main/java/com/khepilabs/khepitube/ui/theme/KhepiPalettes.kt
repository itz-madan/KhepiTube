package com.khepilabs.khepitube.ui.theme

import androidx.compose.ui.graphics.Color

object KhepiPalettes {
    val classicDark =
        PalettePair(
            id = "classic-dark",
            light =
                PaletteSeed(
                    primary = Color(0xFF1E88E5),
                    onPrimary = Color(0xFFFFFFFF),
                    secondary = Color(0xFF1565C0),
                    background = Color(0xFFF8F9FA),
                    surface = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF1F1F1F),
                    onSurfaceVariant = Color(0xFF757575),
                    outline = Color(0xFFE0E0E0),
                    error = Color(0xFFD32F2F),
                ),
            dark =
                PaletteSeed(
                    primary = Color(0xFF64B5F6),
                    onPrimary = Color(0xFF000000),
                    secondary = Color(0xFF90CAF9),
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    onSurface = Color(0xFFE0E0E0),
                    onSurfaceVariant = Color(0xFF9E9E9E),
                    outline = Color(0xFF333333),
                    error = Color(0xFFCF6679),
                ),
        )

    val default: PalettePair = classicDark

    fun forMode(mode: ThemeMode): PalettePair = classicDark
}
