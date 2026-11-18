package com.hydrobuddy.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.ui.navigation.NavRoute

private data class FloatingDockItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

private val dockItems = listOf(
    FloatingDockItem(NavRoute.HOME, "Home", Icons.Rounded.Home),
    FloatingDockItem(NavRoute.STATISTICS, "Stats", Icons.Rounded.BarChart),
    FloatingDockItem(NavRoute.CUSTOMIZE, "Wardrobe", Icons.Rounded.Checkroom),
    FloatingDockItem(NavRoute.SETTINGS, "Settings", Icons.Rounded.Settings)
)

/**
 * Cheap rounded floating nav bar (Part B).
 * - No blur or backdrop effects.
 * - Solid surface color from theme with 1dp border.
 * - Modest 4dp elevation.
 * - Selected-tab state updates immediately on tap.
 * - Fast 120ms color transition.
 */
@Composable
fun FloatingBottomDock(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(24.dp)
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = pillShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, outlineColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dockItems.forEach { item ->
                    val isSelected = currentRoute == item.route ||
                            (item.route == NavRoute.SETTINGS &&
                                    (currentRoute == NavRoute.REMINDER_SETTINGS || currentRoute == NavRoute.APPEARANCE_SETTINGS))

                    DockItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                onNavigate(item.route)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DockItem(
    item: FloatingDockItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 120),
        label = "dock_icon_tint"
    )

    val pillBg by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else Color.Transparent,
        animationSpec = tween(durationMillis = 120),
        label = "dock_pill_bg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(pillBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = iconTint
            )
        }
    }
}
