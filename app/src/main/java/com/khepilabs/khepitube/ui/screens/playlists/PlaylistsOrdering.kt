package com.khepilabs.khepitube.ui.screens.playlists

import com.khepilabs.khepitube.data.model.PlaylistInfo
import com.khepilabs.khepitube.data.playlist.PlaylistListOrder
import com.khepilabs.khepitube.data.playlist.sortedFor
import com.khepilabs.khepitube.ui.components.library.PlaylistOwnershipFilter

/**
 * Owned playlists lead saved ones in every order except [PlaylistListOrder.CUSTOM], where the
 * viewer's own arrangement decides.
 */
internal fun orderedPlaylists(
    owned: List<PlaylistInfo>,
    saved: List<PlaylistInfo>,
    filter: PlaylistOwnershipFilter,
    order: PlaylistListOrder,
): List<PlaylistInfo> =
    if (order == PlaylistListOrder.CUSTOM) {
        filter.select(owned, saved).sortedFor(order)
    } else {
        filter.select(owned.sortedFor(order), saved.sortedFor(order))
    }
