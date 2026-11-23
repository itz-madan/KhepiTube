package com.khepilabs.khepitube.widget.week

import androidx.glance.appwidget.GlanceAppWidget
import com.khepilabs.khepitube.data.stats.VideoStatsRecorder
import com.khepilabs.khepitube.widget.core.refresh.WidgetContentKey
import com.khepilabs.khepitube.widget.core.refresh.WidgetContentSource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class WidgetWeek(val days: List<WeekDay>, val topArtist: String?, val topArtistImage: String?) {
    val totalMs: Long get() = days.sumOf { it.ms }
}

data class WeekDay(val date: LocalDate, val ms: Long)

/** Local video-watch activity widget; no recap or online-music dependency. */
@Singleton
class WeekSource @Inject constructor(
    private val videoStats: VideoStatsRecorder,
) : WidgetContentSource {
    override val key = WidgetContentKey.WEEK
    override val widget: GlanceAppWidget get() = WeekWidget()
    override fun changes(): Flow<Unit> = videoStats.changes

    suspend fun load(today: LocalDate = LocalDate.now()): WidgetWeek =
        WidgetWeek(
            days = (0 until DAYS).map { offset -> WeekDay(today.minusDays((DAYS - 1L - offset)), 0L) },
            topArtist = null,
            topArtistImage = null,
        )

    override suspend fun signature(): String = load().days.joinToString(",") { "${it.date}:${it.ms / 60_000}" }

    companion object { const val DAYS = 7 }
}
