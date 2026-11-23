package com.khepilabs.khepitube.ui.components.videoplayer.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.ui.components.glass.GlassSurface
import com.khepilabs.khepitube.ui.theme.PlayerScrimAffordance
import com.khepilabs.khepitube.ui.theme.PlayerScrimContent

/**
 * A round icon button resting on video.
 *
 * [FilledIconButton] rather than [androidx.compose.material3.IconButton] because the player's
 * buttons are laid out against fixed pill and row heights: `IconButton` reserves a 48dp minimum
 * interactive size, which would grow a 28dp pill's slot and move everything beside it.
 */
@Composable
internal fun PlayerPillIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    buttonSize: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    containerColor: Color = PlayerScrimAffordance,
    contentColor: Color = PlayerScrimContent,
    haptic: HapticFeedbackType? = HapticFeedbackType.ContextClick,
    interactionSource: MutableInteractionSource? = null,
) {
    val haptics = LocalHapticFeedback.current
    GlassSurface(
        modifier =
            modifier
                .size(buttonSize)
                .clickable(
                    interactionSource = interactionSource ?: remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        haptic?.let(haptics::performHapticFeedback)
                        onClick()
                    },
                ),
        shape = CircleShape,
        containerColor = containerColor.copy(alpha = 0.76f),
        borderColor = contentColor.copy(alpha = 0.28f),
        borderWidth = 0.8.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(iconSize),
            )
        }
    }}
