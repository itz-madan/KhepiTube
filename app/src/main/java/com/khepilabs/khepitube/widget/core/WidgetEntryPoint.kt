package com.khepilabs.khepitube.widget.core

import android.content.Context
import com.khepilabs.khepitube.widget.core.refresh.WidgetContentSync
import com.khepilabs.khepitube.widget.downloads.DownloadsSource
import com.khepilabs.khepitube.widget.onrepeat.OnRepeatSource
import com.khepilabs.khepitube.widget.playlist.PlaylistWidgetSource
import com.khepilabs.khepitube.widget.recent.RecentlyPlayedSource
import com.khepilabs.khepitube.widget.week.WeekSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Glance widgets can't use constructor injection (the framework instantiates them),
 * so Hilt singletons are reached through this entry point.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun recentlyPlayedSource(): RecentlyPlayedSource

    fun downloadsSource(): DownloadsSource

    fun onRepeatSource(): OnRepeatSource

    fun playlistWidgetSource(): PlaylistWidgetSource

    fun weekSource(): WeekSource

    fun widgetContentSync(): WidgetContentSync
}

fun widgetEntryPoint(context: Context): WidgetEntryPoint =
    EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
