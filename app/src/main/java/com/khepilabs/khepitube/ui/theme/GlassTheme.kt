package com.khepilabs.khepitube.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class GlassTier {
    ADVANCED,
    STANDARD,
    FALLBACK,
}

@Immutable
data class GlassTokens(
    val tier: GlassTier = GlassTier.FALLBACK,
    val fillAlpha: Float = 1f,
    val borderAlpha: Float = 0.15f,
    val highlightAlpha: Float = 0f,
    val blurRadius: Dp = 0.dp,
    val borderWidth: Dp = 1.dp,
    val containerColor: Color = Color.Unspecified,
    val borderColor: Color = Color.Unspecified,
    val highlightColor: Color = Color.Unspecified,
)

val LocalGlassTokens = staticCompositionLocalOf { GlassTokens() }

@Composable
fun rememberGlassTokens(
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
): GlassTokens =
    GlassTokens(
        tier = GlassTier.FALLBACK,
        fillAlpha = 1f,
        borderAlpha = 0.15f,
        highlightAlpha = 0f,
        blurRadius = 0.dp,
        borderWidth = 1.dp,
        containerColor = containerColor,
        borderColor = borderColor,
        highlightColor = Color.Transparent,
    )
