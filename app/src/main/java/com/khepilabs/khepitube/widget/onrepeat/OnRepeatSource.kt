package com.khepilabs.khepitube.widget.onrepeat

import androidx.glance.appwidget.GlanceAppWidget
import com.khepilabs.khepitube.data.music.model.MusicTrack
import com.khepilabs.khepitube.widget.core.refresh.WidgetContentKey
import com.khepilabs.khepitube.widget.core.refresh.WidgetContentSource
import javax.inject.Inject
import javax.inject.Singleton

/** Retained widget shell; recommendations are intentionally disabled. */
@Singleton
class OnRepeatSource @Inject constructor() : WidgetContentSource {
    override val key = WidgetContentKey.ON_REPEAT
    override val widget: GlanceAppWidget get() = OnRepeatWidget()
    suspend fun tracks(): List<MusicTrack> = emptyList()
    override suspend fun signature(): String = ""
    companion object { const val MAX_ITEMS = 8 }
}
