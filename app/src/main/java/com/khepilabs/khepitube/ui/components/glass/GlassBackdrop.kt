package com.khepilabs.khepitube.ui.components.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot

@Stable
internal class GlassBackdropState(
    val source: GraphicsLayer,
) {
    var originInRoot by mutableStateOf(Offset.Zero)
}

internal val LocalGlassBackdrop = staticCompositionLocalOf<GlassBackdropState?> { null }

/**
 * Records only [content] into a reusable Compose graphics layer, then draws [overlay] above it.
 *
 * Glass surfaces inside [overlay] can sample the recorded content without recursively capturing
 * themselves. This is a Compose-native backdrop source; it avoids View snapshots and bitmap readbacks.
 */
/**
 * Hosts a screen's ordinary content below a separate glass-overlay layer.
 *
 * Put scrolling content and the screen body in [content]. Put only floating chrome (for example,
 * a translucent top bar, search field, or floating action controls) in [glassOverlay]. Glass
 * surfaces in the overlay can sample the content underneath without recording themselves.
 *
 * This is deliberately a two-slot API: placing a GlassSurface inside [content] does not make it a
 * backdrop overlay. Migrate a screen's chrome into [glassOverlay] when that chrome needs live
 * backdrop sampling. On Android versions without supported render effects, GlassSurface keeps its
 * readable tinted fallback.
 */
@Composable
fun GlassScreenHost(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    glassOverlay: @Composable BoxScope.() -> Unit,
) {
    val source = rememberGraphicsLayer()
    val backdrop = remember(source) { GlassBackdropState(source) }

    Box(
        modifier = modifier.onGloballyPositioned {
            backdrop.originInRoot = it.positionInRoot()
        },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        source.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(source)
                    },
            content = content,
        )
        CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
            Box(
                modifier = Modifier.fillMaxSize(),
                content = glassOverlay,
            )
        }
    }
}

/** Internal adapter retained for the app-level navigation chrome host. */
@Composable
internal fun GlassBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    overlay: @Composable BoxScope.() -> Unit,
) {
    GlassScreenHost(
        modifier = modifier,
        content = content,
        glassOverlay = overlay,
    )
}
