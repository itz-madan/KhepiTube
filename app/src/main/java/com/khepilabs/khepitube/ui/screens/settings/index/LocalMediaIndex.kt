package com.khepilabs.khepitube.ui.screens.settings.index

import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.ui.components.settings.SettingEntry
import com.khepilabs.khepitube.ui.components.settings.SettingsDestination

internal object LocalMediaIndex {
    private val page = SettingsDestination.LOCAL_MEDIA

    private fun entry(
        key: String,
        title: Int,
        summary: Int? = null,
    ) = SettingEntry(
        key = "local_media.$key",
        title = title,
        summary = summary,
        section = R.string.settings_local_media_title,
        keywords = R.string.settings_local_media_keywords,
        destination = page,
    )

    val hideAppAudio = entry("hide_app_audio", R.string.local_setting_hide_app_audio, R.string.local_setting_hide_app_audio_desc)
    val minAudio = entry("min_audio", R.string.local_setting_min_audio)
    val minVideo = entry("min_video", R.string.local_setting_min_video)
    val showExtensions = entry("show_extensions", R.string.local_setting_show_extensions, R.string.local_setting_show_extensions_desc)
    val showFileSizes = entry("show_file_sizes", R.string.local_setting_show_file_sizes, R.string.local_setting_show_file_sizes_desc)
    val resumePlayback = entry("resume_playback", R.string.local_setting_resume_playback, R.string.local_setting_resume_playback_desc)
    val autoPlayNext = entry("autoplay_next", R.string.local_setting_autoplay_next, R.string.local_setting_autoplay_next_desc)
    val showContinueWatching =
        entry(
            "show_continue_watching",
            R.string.local_setting_show_continue_watching,
            R.string.local_setting_show_continue_watching_desc,
        )
    val hiddenFolders = entry("hidden_folders", R.string.local_setting_hidden_folders)

    val all =
        listOf(
            hideAppAudio,
            minAudio,
            minVideo,
            showExtensions,
            showFileSizes,
            resumePlayback,
            autoPlayNext,
            showContinueWatching,
            hiddenFolders,
        )
}
