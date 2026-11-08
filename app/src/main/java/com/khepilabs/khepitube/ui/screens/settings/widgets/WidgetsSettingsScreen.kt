package com.khepilabs.khepitube.ui.screens.settings.widgets

import android.appwidget.AppWidgetManager
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.ui.components.settings.SettingsPage
import com.khepilabs.khepitube.ui.components.settings.nav
import com.khepilabs.khepitube.ui.components.settings.notice
import com.khepilabs.khepitube.ui.screens.settings.index.WidgetsIndex
import com.khepilabs.khepitube.widget.core.KhepiWidgets
import kotlinx.coroutines.launch

/** Every Flow widget, each one tap from the launcher's own add-widget dialog. */
@Composable
internal fun WidgetsSettingsScreen(
    onBack: (() -> Unit)?,
    highlight: String?,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val canPin = remember(context) { AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported }
    val unsupported = stringResource(R.string.settings_widgets_unsupported)

    SettingsPage(
        title = stringResource(R.string.settings_widgets_title),
        onBack = onBack,
        highlight = highlight,
        snackbarHostState = snackbarHostState,
    ) {
        if (!canPin) notice("widgets.unsupported", text = { unsupported })
        group(key = "widgets.add", header = R.string.settings_widgets_add_header, footer = R.string.settings_widgets_footer) {
            KhepiWidgets.catalog.forEach { widget ->
                nav(
                    WidgetsIndex.entryFor(widget),
                    onClick = {
                        scope.launch {
                            val receiver = widget.receiver ?: return@launch

                            @Suppress("UNCHECKED_CAST")
                            val requested =
                                GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                                    receiver as Class<out GlanceAppWidgetReceiver>,
                                )
                            if (!requested) snackbarHostState.showSnackbar(unsupported)
                        }
                    },
                    enabled = canPin,
                    showChevron = false,
                    iconRes = widget.icon,
                )
            }
        }
    }
}
