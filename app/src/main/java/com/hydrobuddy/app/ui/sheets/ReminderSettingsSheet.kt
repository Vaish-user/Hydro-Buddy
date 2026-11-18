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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.model.ReminderSettings
import com.hydrobuddy.app.ui.theme.FigmaBackground
import com.hydrobuddy.app.ui.theme.FigmaBlue
import com.hydrobuddy.app.ui.theme.FigmaNavy
import com.hydrobuddy.app.ui.theme.FigmaOrange
import com.hydrobuddy.app.ui.theme.FigmaSlate
import com.hydrobuddy.app.ui.theme.FigmaTeal

enum class PipTone(val title: String, val icon: ImageVector, val message: String) {
    SARCASTIC("Sarcastic & Knife-wielding", Icons.Rounded.Warning, "\"Oi! You forgot to drink water again?\""),
    CHEERFUL("Cheerful & Encouraging", Icons.Rounded.WaterDrop, "\"Time for a refreshing sip of water!\"")
}

/**
 * 2. BOTTOM SHEET: REMINDER SETTINGS (Figma Node 1:724)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsSheet(
    currentIntervalMinutes: Int,
    currentReminderTone: PipTone = PipTone.SARCASTIC,
    startTime: String = "08:00",
    endTime: String = "22:00",
    onDismissRequest: () -> Unit,
    onSavePreferences: (intervalMinutes: Int, tone: PipTone) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedInterval by remember { mutableIntStateOf(currentIntervalMinutes) }
    var selectedTone by remember { mutableStateOf(currentReminderTone) }

    val intervalOptions = listOf(
        Pair(45, "Every 45m"),
        Pair(90, "Every 1.5h"),
        Pair(120, "Every 2h")
    )

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
                        text = "SMART SCHEDULE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FigmaOrange,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Hydration Reminders",
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

            // 1. Reminder Interval Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FigmaBackground)
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Reminder Interval",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaNavy
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    intervalOptions.forEach { (minutes, label) ->
                        val isSelected = selectedInterval == minutes
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) FigmaBlue else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) FigmaBlue else Color(0xFFE2E8F0),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { selectedInterval = minutes }
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else FigmaSlate,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 2. Active Hours Range Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FigmaBackground)
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Active Window",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaNavy
                    )
                    Text(
                        text = "Silent during sleep",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = FigmaSlate
                    )
                }

                Text(
                    text = ReminderSettings.formatActiveHoursRange(LocalContext.current, startTime, endTime),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FigmaBlue
                )
            }

            // 3. Pip's Reminder Tone Selector Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FigmaBackground)
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pip's Reminder Tone",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaNavy
                )
                Text(
                    text = "Choose how Pip pesters you",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = FigmaSlate
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PipTone.values().forEach { tone ->
                        val isSelected = selectedTone == tone
                        val cardBorder = if (isSelected) FigmaTeal else Color(0xFFF1F5F9)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { selectedTone = tone }
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = tone.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) FigmaTeal else FigmaSlate,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = tone.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) FigmaNavy else FigmaSlate
                                )
                            }

                            // Custom Radio Button
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) FigmaBlue else Color(0xFFCBD5E1),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(FigmaBlue)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Save Reminder Preferences Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FigmaNavy)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onSavePreferences(selectedInterval, selectedTone)
                            onDismissRequest()
                        }
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Save Reminder Preferences",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}
