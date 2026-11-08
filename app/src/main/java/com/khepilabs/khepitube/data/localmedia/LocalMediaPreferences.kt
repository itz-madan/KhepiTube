package com.khepilabs.khepitube.data.localmedia

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.khepilabs.khepitube.data.local.safePreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.localMediaStore by safePreferencesDataStore(name = "local_media")

/** What the local library hides, and how it is laid out. Defaults match NextPlayer conventions. */
data class LocalMediaSettings(
    /** Voice notes, recordings and chat app media, found by their folder names. */
    val hideAppAudio: Boolean = true,
    /** Songs shorter than this are hidden; 0 shows every length. */
    val minAudioSeconds: Int = DEFAULT_MIN_AUDIO_SECONDS,
    /** Videos shorter than this are hidden; 0 shows every length. */
    val minVideoSeconds: Int = 0,
    /** Folders the viewer hid, by MediaStore bucket id. */
    val hiddenFolderIds: Set<String> = emptySet(),
    /** Null follows the window: a list on phones, a grid on wider windows. */
    val videosAsGrid: Boolean? = null,
    /** Show file extension suffix (.mp4, .mkv, .mp3) in list titles. */
    val showFileExtensions: Boolean = false,
    /** Show file sizes under items. */
    val showFileSizes: Boolean = true,
    /** Resume local playback from last position. */
    val resumeLocalPlayback: Boolean = true,
    /** Auto-play next item in folder. */
    val autoPlayNextLocal: Boolean = true,
    /** Show continue watching shelf for local media. */
    val showContinueWatching: Boolean = true,
) {
    companion object {
        const val DEFAULT_MIN_AUDIO_SECONDS = 60
        val MIN_AUDIO_CHOICES = listOf(0, 10, 30, 60, 120, 300)
        val MIN_VIDEO_CHOICES = listOf(0, 5, 10, 30, 60)
    }
}

@Singleton
class LocalMediaPreferences
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        private object Keys {
            val HIDE_APP_AUDIO = booleanPreferencesKey("hide_app_audio")
            val MIN_AUDIO_SECONDS = intPreferencesKey("min_audio_seconds")
            val MIN_VIDEO_SECONDS = intPreferencesKey("min_video_seconds")
            val HIDDEN_FOLDERS = stringSetPreferencesKey("hidden_folder_ids")
            val VIDEOS_AS_GRID = booleanPreferencesKey("videos_as_grid")
            val SHOW_FILE_EXTENSIONS = booleanPreferencesKey("show_file_extensions")
            val SHOW_FILE_SIZES = booleanPreferencesKey("show_file_sizes")
            val RESUME_LOCAL_PLAYBACK = booleanPreferencesKey("resume_local_playback")
            val AUTOPLAY_NEXT_LOCAL = booleanPreferencesKey("autoplay_next_local")
            val SHOW_CONTINUE_WATCHING = booleanPreferencesKey("show_continue_watching")
        }

        val settings: Flow<LocalMediaSettings> =
            context.localMediaStore.data
                .map { prefs ->
                    LocalMediaSettings(
                        hideAppAudio = prefs[Keys.HIDE_APP_AUDIO] ?: true,
                        minAudioSeconds = prefs[Keys.MIN_AUDIO_SECONDS] ?: LocalMediaSettings.DEFAULT_MIN_AUDIO_SECONDS,
                        minVideoSeconds = prefs[Keys.MIN_VIDEO_SECONDS] ?: 0,
                        hiddenFolderIds = prefs[Keys.HIDDEN_FOLDERS].orEmpty(),
                        videosAsGrid = prefs[Keys.VIDEOS_AS_GRID],
                        showFileExtensions = prefs[Keys.SHOW_FILE_EXTENSIONS] ?: false,
                        showFileSizes = prefs[Keys.SHOW_FILE_SIZES] ?: true,
                        resumeLocalPlayback = prefs[Keys.RESUME_LOCAL_PLAYBACK] ?: true,
                        autoPlayNextLocal = prefs[Keys.AUTOPLAY_NEXT_LOCAL] ?: true,
                        showContinueWatching = prefs[Keys.SHOW_CONTINUE_WATCHING] ?: true,
                    )
                }.distinctUntilChanged()

        suspend fun setHideAppAudio(hide: Boolean) = context.localMediaStore.edit { it[Keys.HIDE_APP_AUDIO] = hide }

        suspend fun setMinAudioSeconds(seconds: Int) = context.localMediaStore.edit { it[Keys.MIN_AUDIO_SECONDS] = seconds }

        suspend fun setMinVideoSeconds(seconds: Int) = context.localMediaStore.edit { it[Keys.MIN_VIDEO_SECONDS] = seconds }

        suspend fun setFolderHidden(
            folderId: String,
            hidden: Boolean,
        ) = context.localMediaStore.edit {
            val current = it[Keys.HIDDEN_FOLDERS].orEmpty()
            it[Keys.HIDDEN_FOLDERS] = if (hidden) current + folderId else current - folderId
        }

        suspend fun setVideosAsGrid(grid: Boolean) = context.localMediaStore.edit { it[Keys.VIDEOS_AS_GRID] = grid }

        suspend fun setShowFileExtensions(show: Boolean) = context.localMediaStore.edit { it[Keys.SHOW_FILE_EXTENSIONS] = show }

        suspend fun setShowFileSizes(show: Boolean) = context.localMediaStore.edit { it[Keys.SHOW_FILE_SIZES] = show }

        suspend fun setResumeLocalPlayback(resume: Boolean) = context.localMediaStore.edit { it[Keys.RESUME_LOCAL_PLAYBACK] = resume }

        suspend fun setAutoPlayNextLocal(autoPlay: Boolean) = context.localMediaStore.edit { it[Keys.AUTOPLAY_NEXT_LOCAL] = autoPlay }

        suspend fun setShowContinueWatching(show: Boolean) = context.localMediaStore.edit { it[Keys.SHOW_CONTINUE_WATCHING] = show }
    }
