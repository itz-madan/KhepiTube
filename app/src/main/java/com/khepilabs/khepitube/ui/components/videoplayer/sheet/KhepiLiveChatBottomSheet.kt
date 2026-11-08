package com.khepilabs.khepitube.ui.components.videoplayer.sheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.data.model.LiveChatMessage
import com.khepilabs.khepitube.ui.components.shared.KhepiBottomSheet
import com.khepilabs.khepitube.ui.components.shared.KhepiSheetHeader
import com.khepilabs.khepitube.ui.components.shared.defaultSheetExpandedHeight
import com.khepilabs.khepitube.ui.components.shared.rememberKhepiBottomSheetState

// Draggable live-chat bottom sheet (portrait)
@Composable
fun KhepiLiveChatBottomSheet(
    messages: List<LiveChatMessage>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    expandedHeight: Dp? = null,
    collapsedHeight: Dp = 0.dp,
    onSheetProgressChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberKhepiBottomSheetState()
    KhepiBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
        state = sheetState,
        expandedHeight = expandedHeight ?: defaultSheetExpandedHeight(),
        collapsedHeight = collapsedHeight,
        dismissOnOutsideTap = false,
        shape = RectangleShape,
        containerColor = MaterialTheme.colorScheme.surface,
        onProgressChange = onSheetProgressChange,
        header = { dragModifier ->
            KhepiSheetHeader(
                title = stringResource(R.string.live_chat),
                onClose = { sheetState.dismiss() },
                modifier = dragModifier,
            )
        },
    ) {
        LiveChatList(
            messages = messages,
            isLoading = isLoading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
        )
    }
}
