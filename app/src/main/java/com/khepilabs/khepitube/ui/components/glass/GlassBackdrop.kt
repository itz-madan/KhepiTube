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
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
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
 * Hosts ordinary screen content, a separate optional backdrop source, and floating glass chrome.
 *
 * Put scrolling content and the screen body in [content]. When individual cards inside that body
 * need live blur, provide a background-only [contentBackdrop] layer. That layer is recorded
 * separately and exposed only while [content] draws, so embedded GlassSurface instances cannot
 * recursively sample their own text or siblings. Put floating chrome in [glassOverlay]; it samples
 * the foreground content instead.
 *
 * Keep [contentBackdrop] limited to visuals genuinely behind the embedded glass surfaces. On
 * Android versions without supported render effects, GlassSurface keeps its readable tinted fallback.
 */
@Composable
fun GlassScreenHost(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    glassOverlay: @Composable BoxScope.() -> Unit,
    contentBackdrop: (@Composable BoxScope.() -> Unit)? = null,
) {
    val source = rememberGraphicsLayer()
    val contentSource = rememberGraphicsLayer()
    val backdrop = remember(source) { GlassBackdropState(source) }
    val contentBackdropState = remember(contentSource) { GlassBackdropState(contentSource) }

    Box(
        modifier = modifier.onGloballyPositioned {
            val origin = it.positionInRoot()
            backdrop.originInRoot = origin
            contentBackdropState.originInRoot = origin
        },
    ) {
        if (contentBackdrop != null) {
            // A separate, non-recursive backdrop source for glass surfaces embedded in scrolling
            // content. It is recorded independently from the foreground, so rows never sample
            // themselves or their own text while drawing.
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            contentSource.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(contentSource)
                        },
                content = contentBackdrop,
            )
            CompositionLocalProvider(LocalGlassBackdrop provides contentBackdropState) {
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
            }
        } else {
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
        }
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
