package com.khepilabs.khepitube.data.video

import com.khepilabs.khepitube.data.model.Video
import com.khepilabs.khepitube.innertube.models.response.PlayerResponse
import com.khepilabs.khepitube.player.state.SubtitleOption
import com.khepilabs.khepitube.player.stream.CaptionTrackResolver
import com.khepilabs.khepitube.player.stream.InnerTubeVideoStreamExtractor
import com.khepilabs.khepitube.player.stream.StreamProcessor
import com.khepilabs.khepitube.player.stream.StreamSizeEstimator
import com.khepilabs.khepitube.player.stream.durationMs
import com.khepilabs.khepitube.player.stream.playableAudioFormats
import com.khepilabs.khepitube.player.stream.playableVideoFormats
import com.khepilabs.khepitube.utils.PerformanceDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

private const val EXTRACT_TIMEOUT_MS = 8_000L

/** Everything the download dialogs need to offer one video: its formats and each choice's size. */
data class VideoDownloadOptions(
    val video: Video,
    val videoFormats: List<PlayerResponse.StreamingData.Format>,
    val audioFormats: List<PlayerResponse.StreamingData.Format>,
    val streamSizes: Map<String, Long>,
    val subtitles: List<SubtitleOption> = emptyList(),
)

/**
 * Loads the formats for a video that is not playing, so a card can open the same download dialog
 * the player and Shorts open, with the same quality, codec and style settings applied.
 */
class VideoDownloadOptionsLoader
    @Inject
    constructor() {
        suspend fun load(video: Video): VideoDownloadOptions? =
            withContext(PerformanceDispatcher.networkIO) {
                val result =
                    withTimeoutOrNull(EXTRACT_TIMEOUT_MS) {
                        runCatching { InnerTubeVideoStreamExtractor.extract(video.id) }.getOrNull()
                    } ?: return@withContext null
                val videoFormats = result.playableVideoFormats()
                if (videoFormats.isEmpty()) return@withContext null
                val audioFormats = result.playableAudioFormats()
                VideoDownloadOptions(
                    video = video,
                    videoFormats = videoFormats,
                    audioFormats = audioFormats,
                    streamSizes = StreamSizeEstimator.fromInnerTubeFormats(videoFormats, audioFormats, result.durationMs() ?: 0L),
                    subtitles =
                        StreamProcessor.toSubtitleOptions(
                            StreamProcessor.processCaptions(CaptionTrackResolver.resolve(result.playerResponse)),
                        ),
                )
            }
    }
