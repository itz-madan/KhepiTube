package com.khepilabs.khepitube.ui.screens.settings.appearance

import android.net.Uri
import com.khepilabs.khepitube.data.local.AppFontPreferences
import com.khepilabs.khepitube.data.local.AppFontSelection
import com.khepilabs.khepitube.data.local.FontImportResult
import com.khepilabs.khepitube.ui.screens.settings.SettingsViewModel
import com.khepilabs.khepitube.ui.theme.AppFont
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FontViewModel
    @Inject
    constructor(
        private val preferences: AppFontPreferences,
    ) : SettingsViewModel() {
        val selection = preferences.selection.asState(AppFontSelection())

        private val _importResult = MutableStateFlow<FontImportResult?>(null)

        /** What the last file pick did, until the page has said so. */
        val importResult: StateFlow<FontImportResult?> = _importResult.asStateFlow()

        fun select(font: AppFont) = write { preferences.setFont(font) }

        fun import(uri: Uri) =
            write {
                val result = preferences.customFonts.import(uri)
                if (result is FontImportResult.Imported) preferences.setCustomFont(result.displayName)
                _importResult.value = result
            }

        fun consumeImportResult() {
            _importResult.value = null
        }
    }
