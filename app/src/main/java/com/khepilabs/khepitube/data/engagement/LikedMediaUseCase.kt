package com.khepilabs.khepitube.data.engagement

import com.khepilabs.khepitube.data.local.LikedVideoInfo
import com.khepilabs.khepitube.data.local.LikedVideosRepository
import com.khepilabs.khepitube.data.model.toMusicTrack
import javax.inject.Inject
import com.khepilabs.khepitube.data.music.PlaylistRepository as MusicLibrary

/**
 * Likes and unlikes from the Liked videos and Liked music pages. A liked song is also kept in the
 * music library's favorites, which the music recommendations read, so both change together.
 */
class LikedMediaUseCase
    @Inject
    constructor(
        private val likes: LikedVideosRepository,
        private val musicLibrary: MusicLibrary,
    ) {
        /** Unlikes [videoIds]; the result is what [restore] needs to undo it. */
        suspend fun unlike(videoIds: Collection<String>): List<LikedVideoInfo> {
            val taken = likes.takeLikes(videoIds)
            taken.filter { it.isMusic }.forEach {
                musicLibrary.removeFromFavorites(it.videoId)
            }
            return taken
        }

        suspend fun restore(taken: List<LikedVideoInfo>) {
            likes.restoreLikes(taken)
            taken.filter { it.isMusic }.map { it.toMusicTrack() }.forEach {
                musicLibrary.addToFavorites(it)
            }
        }
    }
