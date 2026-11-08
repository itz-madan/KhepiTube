package com.khepilabs.khepitube.ui.components.layout.navigation

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.khepilabs.khepitube.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp")
class KhepiNavigationBarTest {
    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val selections = mutableListOf<KhepiTab>()

    private fun label(tab: KhepiTab) = context.getString(tab.labelRes)

    private fun setBar(
        tabs: List<KhepiTab>,
        selected: KhepiTab,
    ) {
        rule.setContent {
            MaterialTheme {
                KhepiNavigationBar(tabs = tabs, selectedTab = selected, onTabSelected = { selections += it })
            }
        }
    }

    @Test
    fun `the selected tab is announced as selected and the others are not`() {
        setBar(KhepiTab.entries.take(5), selected = KhepiTab.Music)

        rule.onNodeWithText(label(KhepiTab.Music)).assertIsSelected()
        rule.onNodeWithText(label(KhepiTab.Home)).assertIsNotSelected()
    }

    @Test
    fun `each tab is announced once`() {
        setBar(KhepiTab.entries.take(5), selected = KhepiTab.Home)

        rule
            .onNodeWithText(label(KhepiTab.Home))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
    }

    @Test
    fun `five tabs fit the bar without an overflow`() {
        setBar(KhepiTab.entries.take(5), selected = KhepiTab.Home)

        rule.onAllNodesWithText(context.getString(R.string.nav_more)).assertCountEquals(0)
    }

    @Test
    fun `tabs past the fourth move behind More and still navigate`() {
        setBar(KhepiTab.entries, selected = KhepiTab.Home)

        rule.onAllNodesWithText(label(KhepiTab.Library)).assertCountEquals(0)
        rule.onNodeWithText(context.getString(R.string.nav_more)).performClick()
        rule.onNodeWithText(label(KhepiTab.Explore)).performClick()

        assertThat(selections).containsExactly(KhepiTab.Explore)
    }

    @Test
    fun `More reads as selected while an overflow tab is current`() {
        setBar(KhepiTab.entries, selected = KhepiTab.Search)

        rule.onNodeWithText(context.getString(R.string.nav_more)).assertIsSelected()
    }
}
