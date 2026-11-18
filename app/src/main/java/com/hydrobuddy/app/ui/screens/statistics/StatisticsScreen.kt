package com.hydrobuddy.app.ui.screens.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.ui.components.HydroCard
import com.hydrobuddy.app.ui.components.HydroSectionHeader
import com.hydrobuddy.app.ui.components.HydroStatCard
import com.hydrobuddy.app.viewmodel.HydroViewModel

@Composable
fun StatisticsScreen(
    viewModel: HydroViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.statistics.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TODAY'S HYDRATION OVERVIEW
        item {
            HydroCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WaterDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Today's intake",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "Target: ${stats.dailyTargetMl} ml",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        val progressPct = if (stats.dailyTargetMl > 0) {
                            ((stats.todayIntakeMl.toFloat() / stats.dailyTargetMl.toFloat()) * 100).toInt()
                        } else 0
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (progressPct >= 100) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$progressPct% reached",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (progressPct >= 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Large volume numbers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%,d".format(stats.todayIntakeMl),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 32.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ml",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 18.sp
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        val remainingMl = (stats.dailyTargetMl - stats.todayIntakeMl).coerceAtLeast(0)
                        Text(
                            text = if (remainingMl > 0) "$remainingMl ml left" else "Goal achieved!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (remainingMl > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }

        // 2. 7-DAY BAR CHART WITH DYNAMIC GOAL LINE
        item {
            HydroCard {
                HydroSectionHeader(
                    title = "7-day hydration history",
                    subtitle = "Daily volume vs ${stats.dailyTargetMl} ml goal",
                    badgeText = "Goal: ${stats.dailyTargetMl} ml"
                )

                Spacer(modifier = Modifier.height(16.dp))

                val targetGoal = stats.dailyTargetMl
                val maxLogged = stats.weeklyActivity.maxOfOrNull { it.intakeMl } ?: 0
                val chartMaxMl = remember(targetGoal, maxLogged) {
                    maxOf(targetGoal.toFloat() * 1.30f, maxLogged.toFloat() * 1.15f, 1500f)
                }
                val goalLineRatio = (targetGoal.toFloat() / chartMaxMl).coerceIn(0.15f, 0.90f)
                val primaryColor = MaterialTheme.colorScheme.primary

                // Chart Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    // Dashed Goal Line across the chart
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val bottomY = size.height - 24.dp.toPx()
                        val topY = 24.dp.toPx()
                        val availableH = bottomY - topY
                        val goalY = bottomY - (goalLineRatio * availableH)

                        drawLine(
                            color = primaryColor.copy(alpha = 0.55f),
                            start = Offset(0f, goalY),
                            end = Offset(size.width, goalY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }

                    // Bar Columns
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        stats.weeklyActivity.forEach { dayRecord ->
                            val heightFraction by animateFloatAsState(
                                targetValue = (dayRecord.intakeMl.toFloat() / chartMaxMl).coerceIn(0.04f, 1f),
                                label = "bar_height"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                            ) {
                                // Volume label on top of bar
                                Text(
                                    text = if (dayRecord.intakeMl >= 1000) {
                                        "%.1fL".format(dayRecord.intakeMl / 1000f)
                                    } else if (dayRecord.intakeMl > 0) {
                                        "${dayRecord.intakeMl}"
                                    } else {
                                        "-"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 9.sp,
                                        color = if (dayRecord.isGoalAchieved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (dayRecord.isGoalAchieved) FontWeight.Bold else FontWeight.Normal
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Vertical Bar
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            if (dayRecord.isGoalAchieved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Day Label
                                Text(
                                    text = dayRecord.dayLabel,
                                    maxLines = 1,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (dayRecord.dayLabel == "Today") FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (dayRecord.dayLabel == "Today") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Achieved",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Under goal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Goal (${stats.dailyTargetMl} ml)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // 3. STREAK & COMPANION HIGHLIGHT
        item {
            HydroCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${stats.currentStreakDays}-day streak",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = if (stats.currentStreakDays > 0) {
                                "Consistent hydration! Keep it going by hitting your ${stats.dailyTargetMl} ml goal today."
                            } else {
                                "Reach your ${stats.dailyTargetMl} ml goal today to start building your streak!"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        // 4. OVERVIEW METRICS GRID (REMINDERS, CONSISTENCY, YES, NO)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HydroStatCard(
                        title = "Reminders today",
                        value = "${stats.totalRemindersToday}",
                        icon = Icons.Rounded.Notifications,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    HydroStatCard(
                        title = "Consistency",
                        value = "${stats.dailyConsistencyPercentage}%",
                        icon = Icons.Rounded.Percent,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HydroStatCard(
                        title = "Yes responses",
                        value = "${stats.yesResponses}",
                        subtitle = "Water drank",
                        icon = Icons.Rounded.ThumbUp,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    HydroStatCard(
                        title = "No responses",
                        value = "${stats.noResponses}",
                        subtitle = "Snoozed or missed",
                        icon = Icons.Rounded.ThumbDown,
                        iconTint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
