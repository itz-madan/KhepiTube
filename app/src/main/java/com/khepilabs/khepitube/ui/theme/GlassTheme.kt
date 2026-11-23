package com.khepilabs.khepitube.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Hardware capability tier for the MeeTube-compatible glass renderer.
 *
 * ADVANCED means RuntimeShader/refraction is available (API 33+); STANDARD means RenderNode and
 * RenderEffect backdrop sampling are available (API 31-32); FALLBACK is the opaque, accessible
 * surface used on older Android versions. The tier describes platform capability, not whether a
 * particular composable has been connected to a backdrop source yet.
 */
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
): GlassTokens {
    val tier = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> GlassTier.ADVANCED
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> GlassTier.STANDARD
        else -> GlassTier.FALLBACK
    }
    return GlassTokens(
        tier = tier,
        // Until a composable is wired to a GlassSource, keep its surface fully readable.
        fillAlpha = 1f,
        borderAlpha = if (tier == GlassTier.FALLBACK) 0.18f else 0.34f,
        highlightAlpha = if (tier == GlassTier.FALLBACK) 0f else 0.22f,
        blurRadius = 0.dp,
        borderWidth = 1.dp,
        containerColor = containerColor,
        borderColor = borderColor,
        highlightColor = Color.White,
    )
}
