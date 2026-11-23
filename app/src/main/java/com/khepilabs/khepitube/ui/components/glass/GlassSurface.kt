package com.khepilabs.khepitube.ui.components.glass

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect as PlatformRenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawLayer
import androidx.compose.ui.graphics.drawscope.drawPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.ui.theme.GlassTier
import com.khepilabs.khepitube.ui.theme.LocalGlassTokens

/**
 * MeeTube-style glass container foundation.
 *
 * Inside [GlassBackdrop], this replays the recorded Compose content through a hardware blur on
 * Android 12+. Outside a backdrop host it remains a stable tinted surface. Refraction is a later
 * rendering pass; this implementation does not claim to provide it yet.
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

    val backdrop = LocalGlassBackdrop.current
    val effectLayer = rememberGraphicsLayer()
    val density = LocalDensity.current
    var positionInRoot by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val blurEffect = remember(density) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val saturation = ColorMatrix().apply { setSaturation(1.5f) }
            val saturatedBackdrop =
                PlatformRenderEffect.createColorFilterEffect(ColorMatrixColorFilter(saturation))
            PlatformRenderEffect.createBlurEffect(
                with(density) { 8.dp.toPx() },
                with(density) { 8.dp.toPx() },
                saturatedBackdrop,
                Shader.TileMode.CLAMP,
            ).asComposeRenderEffect()
        } else {
            null
        }
    }
    val canSampleBackdrop = backdrop != null && blurEffect != null
    val fillColor = actualContainer.copy(alpha = actualContainer.alpha * tokens.fillAlpha)

    Surface(
        modifier = modifier,
        shape = shape,
        color = if (canSampleBackdrop) Color.Transparent else fillColor,
        border = null,
    ) {
        Box(
            modifier =
                Modifier
                    .clip(shape)
                    .onGloballyPositioned { positionInRoot = it.positionInRoot() }
                    .drawWithContent {
                        if (backdrop != null && blurEffect != null) {
                            effectLayer.renderEffect = blurEffect
                            effectLayer.record {
                                translate(
                                    left = -(positionInRoot.x - backdrop.originInRoot.x),
                                    top = -(positionInRoot.y - backdrop.originInRoot.y),
                                ) {
                                    drawLayer(backdrop.source)
                                }
                            }
                            drawLayer(effectLayer)
                            drawRect(fillColor)
                        }
                        drawContent()
                        val outline = shape.createOutline(size, layoutDirection, this)
                        val borderPath = when (outline) {
                            is Outline.Generic -> outline.path
                            is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
                            is Outline.Rectangle -> Path().apply { addRect(Rect(androidx.compose.ui.geometry.Offset.Zero, size)) }
                        }
                        drawPath(
                            path = borderPath,
                            brush = stroke.brush,
                            style = Stroke(width = strokeWidth.toPx() * 2f),
                        )
                    },
            content = content,
        )
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
