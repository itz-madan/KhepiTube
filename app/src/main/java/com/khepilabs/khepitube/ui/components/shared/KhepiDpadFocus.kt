package com.khepilabs.khepitube.ui.components.shared

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

val LocalDpadNavigation = staticCompositionLocalOf { false }

object FocusLiftDefaults {
    @Suppress("ktlint:standard:property-naming")
    const val Scale = 1.04f
    const val SMALL_SCALE = 1.04f
}

fun Modifier.dpadFocusGroup(): Modifier = focusGroup()

fun Modifier.skipDpadFocus(): Modifier = focusProperties { canFocus = false }

fun dpadFocusOnEnter(focusRequester: FocusRequester? = null): Modifier = Modifier

fun Modifier.dpadFocusOnEnter(focusRequester: FocusRequester? = null): Modifier = this

fun Modifier.dpadDownTo(focusRequester: FocusRequester): Modifier = focusProperties { down = focusRequester }

fun Modifier.dpadShelfRow(
    focusRequester: Any? = null,
    hasFocus: (() -> Boolean)? = null,
): Modifier = this

fun Modifier.onMenuKey(onMenu: () -> Unit): Modifier =
    onKeyEvent { event ->
        if (event.type == KeyEventType.KeyUp &&
            (event.key == Key.Menu || event.key == Key.Guide || event.key == Key.C)
        ) {
            onMenu()
            true
        } else {
            false
        }
    }

fun KeyEvent.isPress(key: Key): Boolean = type == KeyEventType.KeyDown && this.key == key

@Composable
fun rememberFocusScale(interactionSource: MutableInteractionSource): State<Float> {
    val isFocused by interactionSource.collectIsFocusedAsState()
    return animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = spring(),
        label = "focusScale",
    )
}

fun Modifier.focusLift(
    interactionSource: MutableInteractionSource? = null,
    liftScale: Float = 1.04f,
): Modifier =
    composed {
        val source = interactionSource ?: remember { MutableInteractionSource() }
        val isFocused by source.collectIsFocusedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isFocused) liftScale else 1f,
            animationSpec = spring(),
            label = "focusLiftScale",
        )
        this.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    }
