package com.khepilabs.khepitube.widget.core.theme

import android.R
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProviders
import androidx.glance.unit.ColorProvider

@Composable
fun KhepiGlanceTheme(
    colors: ColorProviders,
    content: @Composable () -> Unit,
) {
    GlanceTheme(colors = colors, content = content)
}

@Composable
fun GlanceModifier.widgetSurface(color: ColorProvider = GlanceTheme.colors.widgetBackground): GlanceModifier {
    val withBackground =
        this
            .appWidgetBackground()
            .background(color)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        withBackground.cornerRadius(R.dimen.system_app_widget_background_radius)
    } else {
        withBackground
    }
}
