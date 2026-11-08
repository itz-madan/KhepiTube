package com.khepilabs.khepitube.ui.components.videoplayer.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.ui.components.shared.KhepiBottomSheet
import com.khepilabs.khepitube.ui.components.shared.KhepiSegmentedGap
import com.khepilabs.khepitube.ui.components.shared.KhepiSheetHeader
import com.khepilabs.khepitube.ui.components.shared.defaultSheetExpandedHeight
import com.khepilabs.khepitube.ui.components.shared.khepiSegmentShape
import com.khepilabs.khepitube.ui.components.shared.rememberKhepiBottomSheetState
import com.khepilabs.khepitube.ui.components.shared.rememberMediaArtworkTint
import com.khepilabs.khepitube.utils.formatDurationMillis
import kotlinx.coroutines.delay
import org.schabi.newpipe.extractor.stream.StreamSegment

/** How long a tapped chapter keeps the highlight if the playhead never reports arriving. */
private const val CHAPTER_SELECTION_GRACE_MS = 1_500L

@Composable
fun KhepiChaptersBottomSheet(
    chapters: List<StreamSegment>,
    currentPosition: Long,
    durationMs: Long = 0L,
    onChapterClick: (Long) -> Unit,
    onDismiss: () -> Unit,
    thumbnailUrl: String = "",
    expandedHeight: Dp? = null,
    collapsedHeight: Dp = 0.dp,
    enableVerticalDismiss: Boolean = true,
    onSheetProgressChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberKhepiBottomSheetState()
    val tint = rememberMediaArtworkTint(thumbnailUrl)
    val positionChapterIndex =
        chapters
            .indexOfLast { currentPosition >= it.startTimeSeconds.toLong() * 1000L }
            .coerceAtLeast(0)

    var pendingChapterIndex by remember(chapters) { mutableStateOf<Int?>(null) }
    LaunchedEffect(pendingChapterIndex, positionChapterIndex) {
        val pending = pendingChapterIndex ?: return@LaunchedEffect
        if (positionChapterIndex == pending) {
            pendingChapterIndex = null
        } else {
            delay(CHAPTER_SELECTION_GRACE_MS)
            pendingChapterIndex = null
        }
    }
    val activeChapterIndex = pendingChapterIndex ?: positionChapterIndex
    val initialActiveChapterIndex = remember(chapters) { positionChapterIndex }
    val chaptersListState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = initialActiveChapterIndex,
        )

    LaunchedEffect(chapters, initialActiveChapterIndex) {
        if (chapters.isNotEmpty()) {
            chaptersListState.scrollToItem(initialActiveChapterIndex)
        }
    }

    KhepiBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
        state = sheetState,
        expandedHeight = expandedHeight ?: defaultSheetExpandedHeight(),
        collapsedHeight = collapsedHeight,
        dismissible = enableVerticalDismiss,
        dismissOnOutsideTap = false,
        shape = RectangleShape,
        containerColor = MaterialTheme.colorScheme.surface,
        onProgressChange = onSheetProgressChange,
        header = { dragModifier ->
            KhepiSheetHeader(
                inSidePane = !enableVerticalDismiss,
                title = stringResource(R.string.chapters),
                subtitle = chaptersSubtitle(chapters.size, durationMs),
                onClose = { sheetState.dismiss() },
                modifier = dragModifier,
            )
        },
    ) {
        LazyColumn(
            state = chaptersListState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(KhepiSegmentedGap),
        ) {
            itemsIndexed(
                chapters,
                key = { index, chapter ->
                    "${chapter.title}_${chapter.startTimeSeconds}_$index"
                },
            ) { index, chapter ->
                val startTimeMs = chapter.startTimeSeconds.toLong() * 1000L
                val nextChapter = chapters.getOrNull(index + 1)
                val endTimeMs =
                    nextChapter?.startTimeSeconds?.let { it.toLong() * 1000L }
                        ?: durationMs.takeIf { it > startTimeMs }
                val isCurrent = index == activeChapterIndex
                val hasPlayhead = currentPosition >= startTimeMs && (endTimeMs == null || currentPosition < endTimeMs)
                val progress =
                    if (isCurrent && hasPlayhead && endTimeMs != null && endTimeMs > startTimeMs) {
                        ((currentPosition - startTimeMs).toFloat() / (endTimeMs - startTimeMs).toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                val durationSeconds = endTimeMs?.takeIf { it > startTimeMs }?.let { ((it - startTimeMs) / 1000L).toInt() }

                ChapterItem(
                    chapter = chapter,
                    isCurrent = isCurrent,
                    progress = progress,
                    durationSeconds = durationSeconds,
                    thumbnailUrl = chapter.previewUrl?.takeIf { it.isNotBlank() } ?: thumbnailUrl,
                    shape = khepiSegmentShape(index, chapters.size),
                    tint = tint,
                    onClick = {
                        pendingChapterIndex = index
                        onChapterClick(startTimeMs)
                    },
                )
            }
        }
    }
}

@Composable
private fun chaptersSubtitle(
    count: Int,
    durationMs: Long,
): String {
    val chapters = pluralStringResource(R.plurals.chapters_count_template, count, count)
    if (durationMs <= 0L) return chapters
    return "$chapters ${stringResource(R.string.metadata_separator)} ${formatDurationMillis(durationMs)}"
}
