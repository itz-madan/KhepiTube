package com.khepilabs.khepitube.ui.components.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.ui.theme.LocalGlassTokens

/**
 * A clean, solid, opaque surface container with subtle border strokes and zero blur/glass effects.
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

    Surface(
        modifier = modifier,
        shape = shape,
        color = actualContainer,
        border = BorderStroke(borderWidth, actualBorder.copy(alpha = 0.3f)),
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
    shape: Shape = MaterialTheme.shapes.large,
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
