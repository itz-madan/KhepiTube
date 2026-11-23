package com.khepilabs.khepitube.ui.components.layout.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khepilabs.khepitube.R
import com.khepilabs.khepitube.ui.components.glass.GlassSurface

private const val MAX_BAR_TABS = 4

/**
 * Modern floating liquid pill navigation bar with a central floating Flame (Trending Refresh) button.
 */
@Composable
internal fun KhepiNavigationBar(
    tabs: List<KhepiTab>,
    selectedTab: KhepiTab?,
    onTabSelected: (KhepiTab) -> Unit,
    modifier: Modifier = Modifier,
    onTrendingClick: (() -> Unit)? = null,
) {
    val displayTabs = tabs.take(MAX_BAR_TABS)
    val overflowTabs = tabs.drop(MAX_BAR_TABS)

    val leftCount = when (displayTabs.size) {
        3 -> 1 // e.g. Home on left, Media & Library on right when Shorts is off
        else -> displayTabs.size / 2
    }
    val leftTabs = displayTabs.take(leftCount)
    val rightTabs = displayTabs.drop(leftCount)

    var isFlamePressed by remember { mutableStateOf(false) }
    val flameScale by animateFloatAsState(
        targetValue = if (isFlamePressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        finishedListener = { isFlamePressed = false },
        label = "flame_scale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Dark liquid glass floating pill container
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color.Black.copy(alpha = 0.35f),
                    ambientColor = Color.Black.copy(alpha = 0.2f),
                )
                .clip(RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            containerColor = Color(0x99121212),
            borderColor = Color.White,
            highlightColor = Color.White,
            borderWidth = 1.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leftTabs.forEach { tab ->
                        NavTabItem(
                            tab = tab,
                            selected = tab == selectedTab,
                            onClick = { onTabSelected(tab) },
                        )
                    }
                }

                Spacer(modifier = Modifier.width(64.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    rightTabs.forEach { tab ->
                        NavTabItem(
                            tab = tab,
                            selected = tab == selectedTab,
                            onClick = { onTabSelected(tab) },
                        )
                    }

                    if (overflowTabs.isNotEmpty()) {
                        OverflowNavItem(
                            tabs = overflowTabs,
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                        )
                    }
                }
            }
        }

        // Floating Center Flame (Trending Refresh) Button
        Box(
            modifier = Modifier
                .offset(y = (-8).dp)
                .size(52.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFF4DAAF8).copy(alpha = 0.5f),
                )
                .clip(CircleShape)
                .background(Color(0xFF4DAAF8)) // Bright cyan-blue circle
                .graphicsLayer {
                    scaleX = flameScale
                    scaleY = flameScale
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        isFlamePressed = true
                        onTabSelected(KhepiTab.Home)
                        onTrendingClick?.invoke()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Whatshot,
                contentDescription = "Trending Refresh",
                tint = Color.Black,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun NavTabItem(
    tab: KhepiTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeColor = Color(0xFF4DAAF8)
    val inactiveColor = Color.White.copy(alpha = 0.65f)
    val contentColor = if (selected) activeColor else inactiveColor

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "tab_item_scale",
    )

    Column(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = tab.icon(selected),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(tab.labelRes),
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun OverflowNavItem(
    tabs: List<KhepiTab>,
    selectedTab: KhepiTab?,
    onTabSelected: (KhepiTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val isSelected = selectedTab in tabs
    val activeColor = Color(0xFF4DAAF8)
    val inactiveColor = Color.White.copy(alpha = 0.65f)
    val contentColor = if (isSelected) activeColor else inactiveColor

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .selectable(
                    selected = isSelected,
                    onClick = { expanded = true },
                    role = Role.Tab,
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.MoreHoriz,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.nav_more),
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
            )
        }

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
