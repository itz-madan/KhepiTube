package com.khepilabs.khepitube.ui

import com.khepilabs.khepitube.ui.components.layout.navigation.KhepiTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationDestinationsTest {
    @Test
    fun tabRootsMapToTheirTab() {
        KhepiTab.entries.filter { it != KhepiTab.Shorts }.forEach { tab ->
            assertEquals(tab, khepiTabForDestination(tab.route, shortsSourceArg = null))
        }
    }

    @Test
    fun onlyTheShortsFeedIsTheShortsTab() {
        assertEquals(KhepiTab.Shorts, khepiTabForDestination(SHORTS_ROUTE_PATTERN, shortsSourceArg = null))
        assertEquals(KhepiTab.Shorts, khepiTabForDestination(SHORTS_ROUTE_PATTERN, shortsSourceArg = "feed"))
        assertNull(khepiTabForDestination(SHORTS_ROUTE_PATTERN, shortsSourceArg = "feed:dQw4w9WgXcQ"))
        assertNull(khepiTabForDestination(SHORTS_ROUTE_PATTERN, shortsSourceArg = "saved:"))
    }

    @Test
    fun aVideoOpenedFromShortsPlaysOverTheFirstOtherTab() {
        assertEquals("music", shortsExitRoute(listOf(KhepiTab.Shorts, KhepiTab.Music, KhepiTab.Library)))
        assertEquals("home", shortsExitRoute(listOf(KhepiTab.Home, KhepiTab.Shorts)))
        assertEquals("home", shortsExitRoute(listOf(KhepiTab.Shorts)))
        assertEquals("home", shortsExitRoute(emptyList()))
    }

    @Test
    fun detailScreensAreNotTabRoots() {
        listOf("settings", "settings/content", "playlists", "playlist/{playlistId}", "onboarding", null).forEach { route ->
            assertNull(khepiTabForDestination(route, shortsSourceArg = null))
        }
    }

    @Test
    fun routesFromOutsideTheGraphResolveToTheirTab() {
        assertEquals(KhepiTab.Search, khepiTabForRoute("search"))
        assertEquals(KhepiTab.Music, khepiTabForRoute("music"))
        listOf("downloads", "history", "musicRecognize", "musicPlayer/abc", "settings").forEach { route ->
            assertNull(khepiTabForRoute(route))
        }
    }

    @Test
    fun searchIsTheOnlyTabWithoutTheBar() {
        KhepiTab.entries.forEach { tab -> assertEquals(tab != KhepiTab.Search, tab.showsNavigationBar()) }
        assertFalse((null as KhepiTab?).showsNavigationBar())
        assertTrue(KhepiTab.Home.showsNavigationBar())
    }

    @Test
    fun onlyChannelIdsAreBrowsedDirectly() {
        assertEquals("UCXuqSBlHAE6Xw-yeJA0Tunw", youtubeChannelBrowseId("UCXuqSBlHAE6Xw-yeJA0Tunw"))
        assertEquals("UCXuqSBlHAE6Xw-yeJA0Tunw", youtubeChannelBrowseId("https://m.youtube.com/channel/UCXuqSBlHAE6Xw-yeJA0Tunw?si=abc"))
        assertNull(youtubeChannelBrowseId("@LinusTechTips"))
        assertNull(youtubeChannelBrowseId("https://www.youtube.com/@LinusTechTips"))
        assertNull(youtubeChannelBrowseId("https://www.youtube.com/c/LinusTechTips"))
        assertNull(youtubeChannelBrowseId(" "))
    }

    @Test
    fun channelHandlesUseHandleUrls() {
        assertEquals("https://www.youtube.com/@flow", youtubeChannelUrl("@flow"))
        assertEquals("https://www.youtube.com/@flow", youtubeChannelUrl("flow"))
        assertEquals(
            "https://www.youtube.com/channel/UC123",
            youtubeChannelUrl("UC123"),
        )
    }

    @Test
    fun malformedHandleChannelUrlsAreRepaired() {
        assertEquals(
            "https://www.youtube.com/@flow",
            youtubeChannelUrl("https://youtube.com/channel/@flow"),
        )
        assertEquals(
            "https://www.youtube.com/@flow",
            youtubeChannelUrl("https://m.youtube.com/@flow/videos"),
        )
    }

    @Test
    fun channelRoutesEncodeCanonicalUrls() {
        assertEquals(
            "channel?url=https%3A%2F%2Fwww.youtube.com%2F%40flow",
            youtubeChannelRoute("@flow"),
        )
    }
}
