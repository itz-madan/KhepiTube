package com.khepilabs.khepitube.ui.components.shared

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale

fun Modifier.pressScale(
    pressedScale: Float = 0.96f,
    interactionSource: MutableInteractionSource? = null,
): Modifier =
    composed {
        val source = interactionSource ?: remember { MutableInteractionSource() }
        val isPressed by source.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) pressedScale else 1f,
            label = "pressScale",
        )
        this.scale(scale)
    }

fun Modifier.pressScale(
    interactionSource: MutableInteractionSource?,
    pressedScale: Float = 0.96f,
): Modifier = pressScale(pressedScale, interactionSource)

fun Modifier.pressScale(scale: Float): Modifier = pressScale(pressedScale = scale)

fun Modifier.dismissKeyboardOnPress(arg: Any? = null): Modifier = this

fun Modifier.animateMediaGridItem(): Modifier = this

fun LazyGridItemScope.animateMediaGridItem(): Modifier = Modifier

fun Modifier.animateMediaListItem(): Modifier = this

fun LazyItemScope.animateMediaListItem(): Modifier = Modifier

fun Modifier.thumbnailGradientOverlay(): Modifier = this
