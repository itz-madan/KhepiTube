package com.khepilabs.khepitube.ui.components.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.ui.theme.GlassTier
import com.khepilabs.khepitube.ui.theme.LocalGlassTokens

/**
 * MeeTube-style glass container foundation.
 *
 * This composable intentionally does not pretend to sample the page behind it: true live blur and
 * refraction require a GlassSource host and are added only at call sites that can provide one.
 * Until then it renders a stable tinted surface with a restrained specular edge.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = Color.Unspecified,
    borderColor: Color = Color.Unspecified,
    highlightColor: Color = Color.Unspecified,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val tokens = LocalGlassTokens.current
    val actualContainer = if (containerColor != Color.Unspecified) containerColor else tokens.containerColor
    val actualBorder = if (borderColor != Color.Unspecified) borderColor else tokens.borderColor
    val actualHighlight = if (highlightColor != Color.Unspecified) highlightColor else tokens.highlightColor
    val strokeWidth = if (borderWidth == 1.dp) tokens.borderWidth else borderWidth
    val stroke = if (tokens.tier == GlassTier.FALLBACK || tokens.highlightAlpha <= 0f) {
        BorderStroke(strokeWidth, actualBorder.copy(alpha = tokens.borderAlpha))
    } else {
        BorderStroke(
            strokeWidth,
            Brush.verticalGradient(
                colors = listOf(
                    actualHighlight.copy(alpha = tokens.highlightAlpha),
                    actualBorder.copy(alpha = tokens.borderAlpha),
                    actualBorder.copy(alpha = tokens.borderAlpha * 0.72f),
                ),
            ),
        )
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = actualContainer.copy(alpha = tokens.fillAlpha),
        border = stroke,
    ) {
        Box(content = content)
    }
}

/** Interactive card container with clean solid surfaces. */
@Composable
fun GlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit,
) {
    GlassSurface(
        shape = shape,
        containerColor = containerColor,
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        content = content,
    )
}
