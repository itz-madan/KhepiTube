package com.khepilabs.khepitube.ui.components.layout.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.ui.utils.LocalWindowSizeClass
import com.khepilabs.khepitube.ui.utils.isMediumWidth

private const val MAX_BAR_TABS = 5

/**
 * Clean, solid, opaque bottom navigation bar with high-contrast active indicator and vibrant tints.
 */
@Composable
internal fun KhepiNavigationBar(
    tabs: List<KhepiTab>,
    selectedTab: KhepiTab?,
    onTabSelected: (KhepiTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalItems = LocalWindowSizeClass.current.isMediumWidth
    val iconPosition = if (horizontalItems) NavigationItemIconPosition.Start else NavigationItemIconPosition.Top
    val barTabs = if (tabs.size <= MAX_BAR_TABS) tabs else tabs.take(MAX_BAR_TABS - 1)
    val overkhepiTabs = tabs.drop(barTabs.size)
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        color = colors.surfaceContainer,
        tonalElevation = 3.dp,
    ) {
        ShortNavigationBar(
            containerColor = colors.surfaceContainer,
            arrangement =
                if (horizontalItems) ShortNavigationBarArrangement.Centered else ShortNavigationBarArrangement.EqualWeight,
        ) {
            barTabs.forEach { tab ->
                val selected = tab == selectedTab
                ShortNavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon(selected),
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(tab.labelRes),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    },
                    iconPosition = iconPosition,
                    colors =
                        ShortNavigationBarItemDefaults.colors(
                            selectedIconColor = colors.primary,
                            selectedTextColorTopIconPosition = colors.primary,
                            selectedTextColorStartIconPosition = colors.primary,
                            selectedIndicatorColor = colors.primaryContainer,
                            unselectedIconColor = colors.onSurfaceVariant,
                            unselectedTextColor = colors.onSurfaceVariant,
                        ),
                )
            }
            if (overkhepiTabs.isNotEmpty()) {
                OverflowItem(
                    tabs = overkhepiTabs,
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    iconPosition = iconPosition,
                )
            }
        }
    }
}

@Composable
private fun OverflowItem(
    tabs: List<KhepiTab>,
    selectedTab: KhepiTab?,
    onTabSelected: (KhepiTab) -> Unit,
    iconPosition: NavigationItemIconPosition,
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Box(propagateMinConstraints = true) {
        ShortNavigationBarItem(
            selected = selectedTab in tabs,
            onClick = { expanded = true },
            icon = { Icon(imageVector = Icons.Filled.MoreHoriz, contentDescription = null) },
            label = { Text(text = stringResource(R.string.nav_more)) },
            iconPosition = iconPosition,
            colors =
                ShortNavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColorTopIconPosition = colors.primary,
                    selectedTextColorStartIconPosition = colors.primary,
                    selectedIndicatorColor = colors.primaryContainer,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant,
                ),
        )
        DropdownMenuPopup(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuGroup(shapes = MenuDefaults.groupShape(index = 0, count = 1)) {
                tabs.forEachIndexed { index, tab ->
                    DropdownMenuItem(
                        selected = tab == selectedTab,
                        onClick = {
                            expanded = false
                            onTabSelected(tab)
                        },
                        text = { Text(text = stringResource(tab.labelRes)) },
                        shapes = MenuDefaults.itemShape(index = index, count = tabs.size),
                        leadingIcon = {
                            Icon(
                                imageVector = tab.icon(selected = false),
                                contentDescription = null,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                            )
                        },
                        selectedLeadingIcon = {
                            Icon(
                                imageVector = tab.icon(selected = true),
                                contentDescription = null,
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                            )
                        },
                    )
                }
            }
        }
    }
}
