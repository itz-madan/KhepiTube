package com.khepilabs.khepitube.ui.components.layout.navigation

import com.khepilabs.khepitube.data.local.DEFAULT_NAV_TAB_ORDER
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KhepiTabTest {
    @Test
    fun hiddenHomeFallsBackToFirstVisibleDestination() {
        val visibility = NavigationVisibility(home = false, shorts = false, music = false)

        val resolved =
            resolveDefaultKhepiTab(
                preferredId = KhepiTab.Home.id,
                order = listOf(0, 4, 3, 1, 2, 5, 6),
                visibility = visibility,
            )

        assertEquals(KhepiTab.Library, resolved)
        assertFalse(visibleKhepiTabs(listOf(0, 4, 3), visibility).contains(KhepiTab.Home))
    }

    @Test
    fun reEnabledHomeRestoresAHomeDefault() {
        val resolved =
            resolveDefaultKhepiTab(
                preferredId = KhepiTab.Home.id,
                order = listOf(3, 0, 4),
                visibility = NavigationVisibility(home = true),
            )

        assertEquals(KhepiTab.Home, resolved)
    }

    @Test
    fun tabsFollowTheSavedOrder() {
        val tabs =
            visibleKhepiTabs(
                order = listOf(4, 3, 2, 1, 0),
                visibility = NavigationVisibility(),
            )

        assertEquals(
            listOf(KhepiTab.Library, KhepiTab.Subscriptions, KhepiTab.Music, KhepiTab.Shorts, KhepiTab.Home),
            tabs,
        )
    }

    @Test
    fun aSavedOrderMissingNewerTabsAppendsThemInDefaultOrder() {
        val tabs =
            visibleKhepiTabs(
                order = listOf(3, 0),
                visibility = NavigationVisibility(search = true, categories = true),
            )

        assertEquals(
            listOf(
                KhepiTab.Subscriptions,
                KhepiTab.Home,
                KhepiTab.Shorts,
                KhepiTab.Music,
                KhepiTab.Library,
                KhepiTab.Search,
                KhepiTab.Explore,
            ),
            tabs,
        )
    }

    @Test
    fun unknownPersistedIdsAreIgnored() {
        val tabs = visibleKhepiTabs(order = listOf(42, 4, -1, 3), visibility = NavigationVisibility())

        assertEquals(KhepiTab.Library, tabs.first())
        assertEquals(tabs.distinct(), tabs)
    }

    @Test
    fun persistedIdsNeverChange() {
        assertEquals(DEFAULT_NAV_TAB_ORDER, KhepiTab.entries.map(KhepiTab::id))
        KhepiTab.entries.forEach { tab -> assertEquals(tab, KhepiTab.fromId(tab.id)) }
    }

    /**
     * Settings and Notifications live in the top bar of every root destination, which only works if
     * a root destination always exists. Subscriptions and Library are unconditional in
     * [visibleKhepiTabs]; this pins that down so hiding tabs can never orphan those screens.
     */
    @Test
    fun everyVisibilityCombinationKeepsAnUnhideableRootDestination() {
        val orders =
            listOf(
                DEFAULT_NAV_TAB_ORDER,
                listOf(6, 5, 4, 3, 2, 1, 0),
                listOf(4, 3),
                emptyList(),
            )

        for (bits in 0 until 32) {
            val visibility =
                NavigationVisibility(
                    home = bits and 1 != 0,
                    shorts = bits and 2 != 0,
                    music = bits and 4 != 0,
                    search = bits and 8 != 0,
                    categories = bits and 16 != 0,
                )

            for (order in orders) {
                val visible = visibleKhepiTabs(order, visibility)
                assertTrue(
                    "no unhideable root destination for $visibility / $order",
                    visible.contains(KhepiTab.Subscriptions) || visible.contains(KhepiTab.Library),
                )

                val resolved = resolveDefaultKhepiTab(KhepiTab.Home.id, order, visibility)
                assertTrue(
                    "resolved default $resolved is not visible for $visibility / $order",
                    visible.contains(resolved),
                )
            }
        }
    }
}
