package com.khepilabs.khepitube.ui.components.shared

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import com.khepilabs.khepitube.ui.components.glass.GlassSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import java.util.function.Consumer

private val HeaderSpacing = 16.dp
private val ButtonSpacing = 8.dp

object KhepiDialogDefaults {
    val ContentPadding = 24.dp
    val BottomPadding = 16.dp
    val ActionsSpacing = 8.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhepiAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest, modifier = modifier, properties = properties) {
        val windowBlurEnabled = rememberKhepiWindowBlurEnabled()
        GlassSurface(
            shape = AlertDialogDefaults.shape,
            containerColor = if (windowBlurEnabled) AlertDialogDefaults.containerColor.copy(alpha = 0.72f) else AlertDialogDefaults.containerColor.copy(alpha = 0.90f),
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        start = KhepiDialogDefaults.ContentPadding,
                        top = KhepiDialogDefaults.ContentPadding,
                        end = KhepiDialogDefaults.ContentPadding,
                        bottom = KhepiDialogDefaults.BottomPadding,
                    ),
            ) {
                icon?.let {
                    ProvideStyle(AlertDialogDefaults.iconContentColor, null) {
                        Box(Modifier.padding(bottom = HeaderSpacing).align(Alignment.CenterHorizontally)) { it() }
                    }
                }
                title?.let {
                    ProvideStyle(AlertDialogDefaults.titleContentColor, MaterialTheme.typography.headlineSmall) {
                        Box(
                            Modifier
                                .padding(bottom = HeaderSpacing)
                                .align(if (icon == null) Alignment.Start else Alignment.CenterHorizontally),
                        ) { it() }
                    }
                }
                text?.let {
                    ProvideStyle(AlertDialogDefaults.textContentColor, MaterialTheme.typography.bodyMedium) {
                        Box(
                            Modifier
                                .weight(weight = 1f, fill = false)
                                .padding(bottom = KhepiDialogDefaults.ActionsSpacing)
                                .align(Alignment.Start),
                        ) { it() }
                    }
                }
                Box(modifier = Modifier.align(Alignment.End)) {
                    ProvideStyle(MaterialTheme.colorScheme.primary, MaterialTheme.typography.labelLarge) {
                        DialogButtons(confirmButton = confirmButton, dismissButton = dismissButton)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogButtons(
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?,
) {
    val direction = LocalLayoutDirection.current
    val flipped = if (direction == LayoutDirection.Ltr) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides flipped) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(ButtonSpacing),
            verticalArrangement = Arrangement.spacedBy(ButtonSpacing),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                confirmButton()
                dismissButton?.invoke()
            }
        }
    }
}

@Composable
private fun ProvideStyle(
    color: Color,
    style: TextStyle?,
    content: @Composable () -> Unit,
) {
    val merged = if (style == null) LocalTextStyle.current else LocalTextStyle.current.merge(style)
    CompositionLocalProvider(LocalContentColor provides color, LocalTextStyle provides merged, content = content)
}


@Composable
internal fun rememberKhepiWindowBlurEnabled(): Boolean {
    val context = LocalContext.current
    val density = LocalDensity.current
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window
    val windowManager = remember(context) { context.getSystemService(WindowManager::class.java) }
    var blurEnabled by remember(window, windowManager) {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                windowManager?.isCrossWindowBlurEnabled == true,
        )
    }

    DisposableEffect(window, windowManager, density) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || window == null || windowManager == null) {
            return@DisposableEffect onDispose {}
        }

        val originalDimAmount = window.attributes.dimAmount
        val blurRadius = with(density) { 20.dp.roundToPx() }
        val updateBlur: (Boolean) -> Unit = { enabled ->
            val attributes = window.attributes
            attributes.setBlurBehindRadius(if (enabled) blurRadius else 0)
            window.attributes = attributes
            if (enabled) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.setDimAmount(0.14f)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.setDimAmount(0.36f)
            }
            blurEnabled = enabled
        }
        val listener = Consumer<Boolean> { enabled -> updateBlur(enabled) }

        updateBlur(windowManager.isCrossWindowBlurEnabled)
        windowManager.addCrossWindowBlurEnabledListener(context.mainExecutor, listener)

        onDispose {
            windowManager.removeCrossWindowBlurEnabledListener(listener)
            window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            val attributes = window.attributes
            attributes.setBlurBehindRadius(0)
            window.attributes = attributes
            window.setDimAmount(originalDimAmount)
        }
    }

    return blurEnabled
}
