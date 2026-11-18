package com.hydrobuddy.app.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.model.DailyActivityRecord
import com.hydrobuddy.app.ui.theme.FigmaBackground
import com.hydrobuddy.app.ui.theme.FigmaBlue
import com.hydrobuddy.app.ui.theme.FigmaNavy
import com.hydrobuddy.app.ui.theme.FigmaSlate
import com.hydrobuddy.app.ui.theme.FigmaTeal

/**
 * 5. BOTTOM SHEET: STATS & WEEKLY VIEW (Figma Node 1:554)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsBottomSheet(
    streakDays: Int,
    consistencyPercentage: Int,
    weeklyActivity: List<DailyActivityRecord>,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val averageDailyIntake = if (weeklyActivity.isNotEmpty()) {
        weeklyActivity.map { it.intakeMl }.average().toInt()
    } else 0

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 48.dp, height = 6.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFFE2E8F0))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Tag + Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "INSIGHTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FigmaTeal,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Hydration Statistics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaNavy
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismissRequest
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = FigmaSlate,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Weekly Bar Chart Mock
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FigmaBackground)
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                    .padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "This Week",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaNavy
                    )
                    Text(
                        text = "Avg: %,d ml/day".format(averageDailyIntake),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FigmaBlue
                    )
                }

                // Bar Chart Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxMl = maxOf(
                        (weeklyActivity.maxOfOrNull { it.targetMl } ?: 2000).toFloat(),
                        (weeklyActivity.maxOfOrNull { it.intakeMl } ?: 0).toFloat(),
                        1000f
                    )

                    weeklyActivity.forEach { dayRecord ->
                        val isToday = dayRecord.dayLabel == "Today"
                        val fraction = (dayRecord.intakeMl.toFloat() / maxMl).coerceIn(0.06f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            // The Bar
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        if (dayRecord.isGoalAchieved) {
                                            Brush.verticalGradient(listOf(FigmaBlue, FigmaTeal))
                                        } else {
                                            Brush.verticalGradient(listOf(Color(0xFFBCE0FD), Color(0xFFD6EEFF)))
                                        }
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isToday) "T" else dayRecord.dayLabel.take(1),
                                fontSize = 10.sp,
                                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isToday) FigmaNavy else FigmaSlate
                            )
                        }
                    }
                }
            }

            // Quick Achievements (Two cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                        .padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = FigmaBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$streakDays Days",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaNavy
                    )
                    Text(
                        text = "Best Hydration Streak",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = FigmaSlate
                    )
                }

                // Completion Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                        .padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TrackChanges,
                        contentDescription = null,
                        tint = FigmaBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$consistencyPercentage%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaNavy
                    )
                    Text(
                        text = "Monthly Goal Completion",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = FigmaSlate
                    )
                }
            }
        }
    }
}
