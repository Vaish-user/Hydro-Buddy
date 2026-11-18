package com.hydrobuddy.app.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.model.AppDayOfWeek
import com.hydrobuddy.app.data.model.ReminderSettings
import com.hydrobuddy.app.ui.components.HydroCard
import com.hydrobuddy.app.ui.components.HydroDayChip
import com.hydrobuddy.app.ui.components.HydroFilterChip
import com.hydrobuddy.app.ui.components.HydroSectionHeader
import com.hydrobuddy.app.ui.components.HydroSwitchRow
import com.hydrobuddy.app.ui.theme.BorderSubtle
import com.hydrobuddy.app.ui.theme.MintPastelSecondary
import com.hydrobuddy.app.ui.theme.MintSoftBackground
import com.hydrobuddy.app.ui.theme.MintTealDark
import com.hydrobuddy.app.ui.theme.MintTealPrimary
import com.hydrobuddy.app.ui.theme.PillShape
import com.hydrobuddy.app.ui.theme.PureWhiteSurface
import com.hydrobuddy.app.ui.theme.SageTextSecondary
import com.hydrobuddy.app.ui.theme.SoftMintContainer
import com.hydrobuddy.app.ui.theme.SoftYellowContainer
import com.hydrobuddy.app.ui.theme.SpruceTextPrimary
import com.hydrobuddy.app.ui.theme.SunshineAccent
import com.hydrobuddy.app.viewmodel.HydroViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderSettingsScreen(
    viewModel: HydroViewModel,
    modifier: Modifier = Modifier
) {
    val reminderSettings by viewModel.reminderSettings.collectAsState()

    var showTimeDialogFor by remember { mutableStateOf<String?>(null) } // "start" or "end"

    val sampleTimeOptions = listOf(
        "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM",
        "06:00 PM", "07:00 PM", "08:00 PM", "09:00 PM", "10:00 PM", "11:00 PM"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MintSoftBackground),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. MASTER TOGGLE
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSwitchRow(
                    title = "Enable Hydration Reminders",
                    subtitle = if (reminderSettings.isEnabled) "Reminders are turned on" else "Reminders are disabled",
                    checked = reminderSettings.isEnabled,
                    onCheckedChange = { viewModel.toggleReminderEnabled(it) },
                    icon = Icons.Rounded.NotificationsActive
                )
            }
        }

        // 2. PRESET & CUSTOM INTERVALS
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSectionHeader(
                    title = "Reminder Interval",
                    subtitle = "How often Dewey should nudge you",
                    badgeText = ReminderSettings.formatInterval(reminderSettings.intervalMinutes)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Presets
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SageTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ReminderSettings.PRESET_INTERVALS.forEach { minutes ->
                        val isSelected = !reminderSettings.isCustomInterval && reminderSettings.intervalMinutes == minutes
                        HydroFilterChip(
                            label = ReminderSettings.formatInterval(minutes),
                            selected = isSelected,
                            onClick = { viewModel.setPresetInterval(minutes) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Custom Interval Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custom Interval",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = SpruceTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "${reminderSettings.intervalMinutes} min",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MintTealDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Slider(
                    value = reminderSettings.intervalMinutes.toFloat(),
                    onValueChange = { viewModel.setCustomInterval(it.toInt()) },
                    valueRange = 10f..180f,
                    steps = 16,
                    colors = SliderDefaults.colors(
                        thumbColor = MintTealPrimary,
                        activeTrackColor = MintTealPrimary,
                        inactiveTrackColor = SoftMintContainer
                    )
                )

                Text(
                    text = "Slide from 10 minutes up to 3 hours",
                    style = MaterialTheme.typography.bodySmall.copy(color = SageTextSecondary, fontSize = 11.sp)
                )
            }
        }

        // 3. SCHEDULE WINDOW (START & END TIME)
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSectionHeader(
                    title = "Active Schedule",
                    subtitle = "Set quiet hours when you don't want reminders"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Time Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(SoftMintContainer)
                            .border(1.dp, MintPastelSecondary, PillShape)
                            .clickable { showTimeDialogFor = "start" }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Starts At",
                                style = MaterialTheme.typography.bodySmall.copy(color = SageTextSecondary, fontSize = 11.sp)
                            )
                            Text(
                                text = reminderSettings.startTime,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpruceTextPrimary,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    // End Time Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(SoftMintContainer)
                            .border(1.dp, MintPastelSecondary, PillShape)
                            .clickable { showTimeDialogFor = "end" }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Ends At",
                                style = MaterialTheme.typography.bodySmall.copy(color = SageTextSecondary, fontSize = 11.sp)
                            )
                            Text(
                                text = reminderSettings.endTime,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpruceTextPrimary,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. ACTIVE DAYS OF WEEK
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSectionHeader(
                    title = "Active Days",
                    subtitle = "Days when reminders should repeat"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppDayOfWeek.values().forEach { day ->
                        val isSelected = reminderSettings.activeDays.contains(day)
                        HydroDayChip(
                            dayInitial = day.shortName,
                            selected = isSelected,
                            onClick = { viewModel.toggleActiveDay(day) }
                        )
                    }
                }
            }
        }

        // 5. ALERT PREFERENCES (SOUND & VIBRATION)
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSectionHeader(
                    title = "Feedback & Notification",
                    subtitle = "Tone and haptic response"
                )

                Spacer(modifier = Modifier.height(8.dp))

                HydroSwitchRow(
                    title = "Play Sound",
                    subtitle = "Gentle water droplet chime",
                    checked = reminderSettings.soundEnabled,
                    onCheckedChange = { viewModel.toggleSound(it) },
                    icon = Icons.AutoMirrored.Rounded.VolumeUp
                )

                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

                HydroSwitchRow(
                    title = "Vibration",
                    subtitle = "Subtle tactile pulse",
                    checked = reminderSettings.vibrationEnabled,
                    onCheckedChange = { viewModel.toggleVibration(it) },
                    icon = Icons.Rounded.Vibration
                )
            }
        }

        // 6. FLOATING MASCOT PERMISSION CARD
        item {
            val context = LocalContext.current
            val hasOverlayPermission = Settings.canDrawOverlays(context)

            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSectionHeader(
                    title = "Floating Mascot (Draw Over Other Apps)",
                    subtitle = "Allows Pip to walk onto your screen over Instagram, YouTube & WhatsApp",
                    badgeText = if (hasOverlayPermission) "Active" else "Action Needed"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasOverlayPermission) "Overlay Enabled" else "Permission Required",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasOverlayPermission) MintTealDark else Color(0xFFC79522)
                            )
                        )
                        Text(
                            text = if (hasOverlayPermission)
                                "Pip will appear over any active app when it's time to drink."
                            else
                                "Tap below to allow HydroBuddy to appear on top of other apps.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SageTextSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (!hasOverlayPermission) {
                        Button(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC79522)),
                            shape = PillShape
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Grant",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhiteSurface
                                )
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Permission Granted",
                            tint = MintTealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 7. TEST & STATUS CARD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhiteSurface, PillShape)
                    .border(1.dp, MintPastelSecondary, PillShape)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive Reminder",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpruceTextPrimary
                            )
                        )
                        Text(
                            text = "Test Pip's walk-in, dialogue & responses",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SageTextSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { viewModel.triggerReminderInteraction() },
                        colors = ButtonDefaults.buttonColors(containerColor = MintTealPrimary),
                        shape = PillShape
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test Walk-in",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PureWhiteSurface
                            )
                        )
                    }
                }
            }
        }
    }

    // Time Selection Dialog
    if (showTimeDialogFor != null) {
        val isStart = showTimeDialogFor == "start"
        AlertDialog(
            onDismissRequest = { showTimeDialogFor = null },
            confirmButton = {
                TextButton(onClick = { showTimeDialogFor = null }) {
                    Text("Done", color = MintTealDark, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = if (isStart) "Select Start Time" else "Select End Time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleTimeOptions.forEach { time ->
                        val currentVal = if (isStart) reminderSettings.startTime else reminderSettings.endTime
                        val isSelected = currentVal == time
                        HydroFilterChip(
                            label = time,
                            selected = isSelected,
                            onClick = {
                                if (isStart) viewModel.setStartTime(time) else viewModel.setEndTime(time)
                                showTimeDialogFor = null
                            }
                        )
                    }
                }
            },
            containerColor = PureWhiteSurface
        )
    }
}

