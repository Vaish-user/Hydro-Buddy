package com.hydrobuddy.app.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.model.AppearanceSettings
import com.hydrobuddy.app.data.model.CharacterSize
import com.hydrobuddy.app.data.model.ReminderSettings
import com.hydrobuddy.app.ui.components.FloatingBottomDock
import com.hydrobuddy.app.ui.mascot.PipAnimationState
import com.hydrobuddy.app.ui.mascot.PipCharacterView
import com.hydrobuddy.app.ui.mascot.PipInteractionState
import com.hydrobuddy.app.ui.mascot.PipWalkingReminderOverlay
import com.hydrobuddy.app.ui.navigation.NavRoute
import com.hydrobuddy.app.ui.sheets.CustomWaterBottomSheet
import com.hydrobuddy.app.ui.sheets.PipTone
import com.hydrobuddy.app.ui.sheets.PipWardrobeBottomSheet
import com.hydrobuddy.app.ui.sheets.ReminderSettingsSheet
import com.hydrobuddy.app.ui.sheets.SettingsBottomSheet
import com.hydrobuddy.app.ui.sheets.StatsBottomSheet
import com.hydrobuddy.app.ui.sheets.WaterHistoryBottomSheet
import com.hydrobuddy.app.ui.theme.FigmaBackground
import com.hydrobuddy.app.ui.theme.FigmaBlue
import com.hydrobuddy.app.ui.theme.FigmaBorder
import com.hydrobuddy.app.ui.theme.FigmaDarkBlue
import com.hydrobuddy.app.ui.theme.FigmaGreen
import com.hydrobuddy.app.ui.theme.FigmaGreenContainer
import com.hydrobuddy.app.ui.theme.FigmaGreenDot
import com.hydrobuddy.app.ui.theme.FigmaLightSlate
import com.hydrobuddy.app.ui.theme.FigmaNavy
import com.hydrobuddy.app.ui.theme.FigmaOrange
import com.hydrobuddy.app.ui.theme.FigmaOrangeContainer
import com.hydrobuddy.app.ui.theme.FigmaOrangeText
import com.hydrobuddy.app.ui.theme.FigmaPipAtmosphericGlow
import com.hydrobuddy.app.ui.theme.FigmaPopularButtonGradient
import com.hydrobuddy.app.ui.theme.FigmaPrimaryGradient
import com.hydrobuddy.app.ui.theme.FigmaProgressTrack
import com.hydrobuddy.app.ui.theme.FigmaSlate
import com.hydrobuddy.app.ui.theme.FigmaSoftBlue
import com.hydrobuddy.app.ui.theme.FigmaSurfaceWhite
import com.hydrobuddy.app.ui.theme.FigmaTeal
import com.hydrobuddy.app.ui.theme.PillShape
import com.hydrobuddy.app.viewmodel.HydroViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Flagship Home Screen for HydroBuddy.
 * Rebuilt from the official Figma design specification (Frame 1:527):
 * - Header with gradient identity, dynamic greeting, current date, and settings button
 * - Pip Hero Presentation with speech bubble pointer, atmospheric glow, 3D mascot, and "Tap Pip" badge
 * - Today's Hydration card with metric numbers, remaining goal, milestone chips, and circular progress ring
 * - Quick Water Logging 4-card grid (+100ml Sip, emphasized +250ml Glass with POPULAR pill, +500ml Bottle, Custom Input)
 * - Today's Activity timeline with intake history and timestamps
 * - Smart Reminder Card with active toggle pill and scheduled time
 */
@Composable
fun HomeScreen(
    viewModel: HydroViewModel,
    onNavigateToSettings: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenCustomIntakeSheet: (() -> Unit)? = null,
    onOpenHistorySheet: (() -> Unit)? = null,
    onOpenReminderSheet: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val reminderSettings by viewModel.reminderSettings.collectAsState()
    val characterCustomization by viewModel.characterCustomization.collectAsState()
    val appearanceSettings by viewModel.appearanceSettings.collectAsState()
    val pipInteractionState by viewModel.pipInteractionState.collectAsState()
    val todayIntakeMl by viewModel.todayIntakeMl.collectAsState()
    val dailyGoalMl by viewModel.dailyGoalMl.collectAsState()
    val currentStreakDays by viewModel.currentStreakDays.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val lastDeletedLog by viewModel.lastDeletedLog.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val nextReminderAt by viewModel.nextReminderAt.collectAsState()

    LaunchedEffect(lastDeletedLog) {
        if (lastDeletedLog != null) {
            delay(4000L)
            viewModel.dismissUndoSnackbar()
        }
    }

    val coroutineScope = rememberCoroutineScope()

    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val progressFraction = if (dailyGoalMl > 0) {
        (todayIntakeMl.toFloat() / dailyGoalMl.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val percentageInt = (progressFraction * 100).toInt()
    val remainingMl = (dailyGoalMl - todayIntakeMl).coerceAtLeast(0)

    val isAwake = reminderSettings.isWithinActiveWindow()

    // Mascot interactive reaction state
    var mascotReactionState by remember { mutableStateOf(PipAnimationState.A_IDLE) }
    var speechMessage by remember(characterCustomization) {
        mutableStateOf(characterCustomization.safeEntrance)
    }

    // Dynamic greeting based on current hour
    val currentHour = LocalTime.now().hour
    val greetingText = when (currentHour) {
        in 5..11 -> "Good morning!"
        in 12..17 -> "Good afternoon!"
        else -> "Good evening!"
    }
    val formattedDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
    }

    // Check overlay permission
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // Active Bottom Sheet in Home Dashboard
    var activeBottomSheet by remember { mutableStateOf(HomeBottomSheet.NONE) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. HEADER SECTION (Node 1:856)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Logo + Brand Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WaterDrop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = "HydroBuddy",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-0.3).sp
                            )
                        )
                    }

                    // Greeting + Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = greetingText,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }

                // Settings Button (Clean Material 3 button)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable { onNavigateToSettings("settings") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Optional Overlay Permission Banner (if revoked)
            if (!hasOverlayPermission) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Pip floating reminders",
                                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Allow Pip to walk in over apps when it's time to drink",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Grant",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. PIP HERO PRESENTATION
            // ==========================================
            val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
            val currentSpeechText = if (!isAwake) {
                "Resting until ${ReminderSettings.formatTimeForDevice(context, reminderSettings.startTime)}"
            } else {
                speechMessage
            }

            PipHeroPresentation(
                isAwake = isAwake,
                isDarkTheme = isDarkTheme,
                appearanceSettings = appearanceSettings,
                mascotReactionState = mascotReactionState,
                pipInteractionState = pipInteractionState,
                speechText = currentSpeechText,
                onTap = {
                    if (!isAwake) {
                        speechMessage = "Zzz... Resting until ${ReminderSettings.formatTimeForDevice(context, reminderSettings.startTime)}"
                    } else {
                        viewModel.triggerReminderInteraction()
                    }
                }
            )

            // ==========================================
            // 3. HYDRATION PROGRESS CARD
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card Header: Target Label & Percentage Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Daily target",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Today's hydration",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Percentage Pill Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Text(
                                    text = "$percentageInt% reached",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }

                    // Metric Numbers & Circular Progress Ring
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Metric values & milestone badges
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%,d", todayIntakeMl),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "ml",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            // Sub-stats (Goal & Left)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Goal: ${String.format(Locale.getDefault(), "%,d", dailyGoalMl)} ml",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(text = "•", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                                Text(
                                    text = "Left: ${String.format(Locale.getDefault(), "%,d", remainingMl)} ml",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            // Mini Milestone Badges Row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                val milestoneBadgeText = when {
                                    todayIntakeMl >= dailyGoalMl -> "Goal met"
                                    progressFraction >= 0.5f -> "On track"
                                    todayIntakeMl > 0 -> "In progress"
                                    else -> "Starting out"
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (progressFraction >= 0.5f) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = milestoneBadgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (progressFraction >= 0.5f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${currentStreakDays}d streak",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        // Right: Circular Progress Ring
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val animatedProgress by animateFloatAsState(
                                targetValue = progressFraction,
                                animationSpec = tween(900, easing = FastOutSlowInEasing),
                                label = "progress_ring"
                            )

                            val primaryColor = MaterialTheme.colorScheme.primary
                            val trackColor = MaterialTheme.colorScheme.surfaceVariant

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 8.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                                val arcSize = Size(diameter, diameter)

                                // Track background ring
                                drawArc(
                                    color = trackColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Active solid progress arc (no gradient)
                                drawArc(
                                    color = primaryColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            // Center percentage label
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$percentageInt%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "hydrated",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. QUICK WATER LOGGING (Node 1:928)
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add water",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Tap to quickly log intake",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // 4-Card Quick Logging Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. +100 ml (Sip)
                    QuickLogCard(
                        modifier = Modifier.weight(1f),
                        amountText = "+100",
                        labelText = "Sip",
                        icon = Icons.Rounded.WaterDrop,
                        isEmphasized = false,
                        onClick = {
                            viewModel.logIntake(100, "Sip of water")
                            mascotReactionState = PipAnimationState.D_HAPPY
                        }
                    )

                    // 2. +250 ml (Glass) — EMPHASIZED POPULAR
                    QuickLogCard(
                        modifier = Modifier.weight(1f),
                        amountText = "+250",
                        labelText = "Glass",
                        icon = Icons.Rounded.LocalDrink,
                        isEmphasized = true,
                        popularBadge = "POPULAR",
                        onClick = {
                            viewModel.logIntake(250, "Glass of water")
                            mascotReactionState = PipAnimationState.G_DRINKING
                        }
                    )

                    // 3. +500 ml (Bottle)
                    QuickLogCard(
                        modifier = Modifier.weight(1f),
                        amountText = "+500",
                        labelText = "Bottle",
                        icon = Icons.Rounded.WaterDrop,
                        isEmphasized = false,
                        onClick = {
                            viewModel.logIntake(500, "Bottle of water")
                            mascotReactionState = PipAnimationState.D_HAPPY
                        }
                    )

                    // 4. Custom Input
                    QuickLogCustomCard(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            activeBottomSheet = HomeBottomSheet.CUSTOM_WATER
                            onOpenCustomIntakeSheet?.invoke()
                        }
                    )
                }
            }

            // ==========================================
            // 5. TODAY'S ACTIVITY (Node 1:977)
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Today's activity",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                activeBottomSheet = HomeBottomSheet.WATER_HISTORY
                                onOpenHistorySheet?.invoke()
                            }
                        ) {
                            Text(
                                text = "View all",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Activity Items List
                    if (todayLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No drinks logged yet today • Tap above to log intake",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            todayLogs.reversed().take(5).forEach { item ->
                                ActivityTimelineItem(
                                    title = item.title,
                                    subtitle = "Water log",
                                    amountText = "+${item.amountMl} ml",
                                    timeText = ReminderSettings.formatTimeForDevice(context, item.timeFormatted),
                                    onDelete = { viewModel.deleteWaterLog(item) }
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 6. NEXT REMINDER CARD (Node 1:1028)
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Bell Icon Container
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Schedule Details
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (!isAwake) "Active schedule" else "Hydration reminder",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    val badgeText = when {
                                        !reminderSettings.isRunning -> "Paused"
                                        !reminderSettings.isEnabled -> "Disabled"
                                        !isAwake -> "Sleeping"
                                        else -> "Active"
                                    }
                                    val badgeColor = when {
                                        !reminderSettings.isRunning || !reminderSettings.isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                                        !isAwake -> Color(0xFF6366F1)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                    val badgeBg = when {
                                        !reminderSettings.isRunning || !reminderSettings.isEnabled -> MaterialTheme.colorScheme.surfaceVariant
                                        !isAwake -> Color(0xFF6366F1).copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = badgeColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                if (!reminderSettings.isEnabled || !reminderSettings.isRunning) {
                                    Text(
                                        text = "Reminders paused",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                } else if (!isAwake) {
                                    Text(
                                        text = "Outside active hours, next at ${ReminderSettings.formatTimeForDevice(context, reminderSettings.startTime)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF6366F1),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "Active window: ${ReminderSettings.formatActiveHoursRange(context, reminderSettings.startTime, reminderSettings.endTime)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                } else {
                                    val remainingMillis = (nextReminderAt - currentTimeMillis).coerceAtLeast(0L)
                                    val totalSeconds = remainingMillis / 1000
                                    val minutes = totalSeconds / 60
                                    val seconds = totalSeconds % 60
                                    val countdownStr = if (remainingMillis <= 0L) {
                                        "Due now"
                                    } else if (minutes >= 60) {
                                        "Next reminder in ${minutes / 60}h ${minutes % 60}m"
                                    } else if (minutes > 0) {
                                        "Next reminder in ${minutes}m ${seconds}s"
                                    } else {
                                        "Next reminder in ${seconds}s"
                                    }

                                    Text(
                                        text = countdownStr,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Active window: ${ReminderSettings.formatActiveHoursRange(context, reminderSettings.startTime, reminderSettings.endTime)} (every ${ReminderSettings.formatInterval(reminderSettings.intervalMinutes)})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        // Toggle Pill Switch & Settings Shortcut
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Toggle Pill (44x24dp, 12dp radius)
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (reminderSettings.isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.toggleReminderRunning() }
                                    .padding(2.dp),
                                contentAlignment = if (reminderSettings.isRunning) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                )
                            }

                            // Reminder settings trigger
                            IconButton(
                                onClick = {
                                    onNavigateToSettings("settings")
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Reminder settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Remind Me Now Button
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.material3.Button(
                        onClick = { viewModel.triggerReminderNow() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        elevation = null
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remind me now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // ==========================================
        // FOREGROUND PIP REMINDER INTERACTIVE OVERLAY
        // ==========================================
        if (pipInteractionState.isVisible) {
            PipWalkingReminderOverlay(
                viewModel = viewModel
            )
        }

        // ==========================================
        // FIGMA BOTTOM SHEETS HOST
        // ==========================================
        when (activeBottomSheet) {
            HomeBottomSheet.CUSTOM_WATER -> {
                CustomWaterBottomSheet(
                    onDismissRequest = { activeBottomSheet = HomeBottomSheet.NONE },
                    onLogIntake = { amountMl, beverage ->
                        viewModel.logIntake(amountMl, "${beverage.label} intake")
                        mascotReactionState = PipAnimationState.D_HAPPY
                    }
                )
            }
            HomeBottomSheet.REMINDER_SETTINGS -> {
                ReminderSettingsSheet(
                    currentIntervalMinutes = reminderSettings.intervalMinutes,
                    startTime = reminderSettings.startTime,
                    endTime = reminderSettings.endTime,
                    onDismissRequest = { activeBottomSheet = HomeBottomSheet.NONE },
                    onSavePreferences = { intervalMinutes, tone ->
                        viewModel.setPresetInterval(intervalMinutes)
                        viewModel.updateWaterReminderMessage(tone.message)
                    }
                )
            }
            HomeBottomSheet.PIP_WARDROBE -> {
                PipWardrobeBottomSheet(
                    characterName = characterCustomization.characterName,
                    streakDays = currentStreakDays,
                    onDismissRequest = { activeBottomSheet = HomeBottomSheet.NONE },
                    onApplyOutfit = { _, _ ->
                        mascotReactionState = PipAnimationState.D_HAPPY
                    },
                    onOpenPersonalitySettings = {
                        activeBottomSheet = HomeBottomSheet.NONE
                        onNavigateToSettings("customize")
                    }
                )
            }
            HomeBottomSheet.WATER_HISTORY -> {
                WaterHistoryBottomSheet(
                    todayIntakeMl = todayIntakeMl,
                    todayLogs = todayLogs,
                    onDeleteLog = { viewModel.deleteWaterLog(it) },
                    onDismissRequest = { activeBottomSheet = HomeBottomSheet.NONE }
                )
            }
            HomeBottomSheet.STATS -> {
                StatsBottomSheet(
                    streakDays = currentStreakDays,
                    consistencyPercentage = statistics.dailyConsistencyPercentage,
                    weeklyActivity = statistics.weeklyActivity,
                    onDismissRequest = { activeBottomSheet = HomeBottomSheet.NONE }
                )
            }
            HomeBottomSheet.SETTINGS -> {
                SettingsBottomSheet(
                    dailyTargetMl = dailyGoalMl,
                    onDismissRequest = { activeBottomSheet = HomeBottomSheet.NONE },
                    onOpenSystemOverlaySettings = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    },
                    onOpenPersonalitySettings = {
                        activeBottomSheet = HomeBottomSheet.NONE
                        onNavigateToSettings("customize")
                    },
                    onOpenPipV2Test = {
                        activeBottomSheet = HomeBottomSheet.NONE
                        onNavigateToSettings("pip_v2_test")
                    }
                )
            }
            HomeBottomSheet.NONE -> Unit
        }

        // Undo Delete Notification Bar
        AnimatedVisibility(
            visible = lastDeletedLog != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Removed ${lastDeletedLog?.amountMl} ml ${lastDeletedLog?.title ?: "entry"}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.inverseOnSurface),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Undo",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.inversePrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewModel.undoDeleteLog() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.dismissUndoSnackbar() }
                    )
                }
            }
        }
    }
}

enum class HomeBottomSheet {
    NONE,
    CUSTOM_WATER,
    REMINDER_SETTINGS,
    PIP_WARDROBE,
    WATER_HISTORY,
    STATS,
    SETTINGS
}

/**
 * Standard Quick Logging Card (+100ml, +500ml, +250ml)
 */
@Composable
private fun QuickLogCard(
    modifier: Modifier = Modifier,
    amountText: String,
    labelText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEmphasized: Boolean,
    popularBadge: String? = null,
    onClick: () -> Unit
) {
    val cardBg = if (isEmphasized) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val cardBorder = if (isEmphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
    val contentColor = if (isEmphasized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val labelColor = if (isEmphasized) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isEmphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEmphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Amount text
            Text(
                text = amountText,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            )

            // Label text
            Text(
                text = labelText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = labelColor
                )
            )
        }

        // Floating "Popular" Pill
        if (popularBadge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-6).dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "Popular",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}

/**
 * Custom Input Button with clean outline
 */
@Composable
private fun QuickLogCustomCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "Custom",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = "Input",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Activity Timeline Item Row
 */
@Composable
private fun ActivityTimelineItem(
    title: String,
    subtitle: String,
    amountText: String,
    timeText: String,
    onDelete: (() -> Unit)? = null
) {
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
                    .size(28.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Isolated Pip Hero Presentation (Part B).
 * - Encapsulates 3D Pip Mascot, speech bubble, and touch rotation gestures.
 * - Draws static blue radial gradient via drawBehind (no blur, no animation, remembered Brush).
 * - Completely isolated from high frequency Home state (countdown timer, log updates).
 */
@Composable
fun PipHeroPresentation(
    isAwake: Boolean,
    isDarkTheme: Boolean,
    appearanceSettings: AppearanceSettings,
    mascotReactionState: PipAnimationState,
    pipInteractionState: PipInteractionState,
    speechText: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pipRotationY = remember { Animatable(0f) }
    val pipRotationX = remember { Animatable(0f) }
    var returnToFrontJob by remember { mutableStateOf<Job?>(null) }

    val glowBrush = remember(isDarkTheme) {
        val centerColor = if (isDarkTheme) {
            Color(0xFF38BDF8).copy(alpha = 0.22f) // Softer in dark
        } else {
            Color(0xFF0284C7).copy(alpha = 0.15f) // Lighter in light
        }
        Brush.radialGradient(
            colors = listOf(centerColor, Color.Transparent)
        )
    }

    val scaledHomeSize = (180.dp * appearanceSettings.effectiveScale).coerceIn(90.dp, 270.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Speech Bubble Floating above Pip
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (!isAwake) Icons.Rounded.Nightlight else Icons.Rounded.WaterDrop,
                            contentDescription = null,
                            tint = if (!isAwake) Color(0xFF6366F1) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = speechText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Speech Triangle Pointer
                Box(
                    modifier = Modifier
                        .offset(x = (-30).dp, y = (-5).dp)
                        .size(10.dp)
                        .rotate(45f)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                )
            }

            // 3D Pip Mascot Container with Cheap Static Blue Radial Glow
            Box(
                modifier = Modifier
                    .size(scaledHomeSize)
                    .drawBehind {
                        drawCircle(
                            brush = glowBrush,
                            radius = size.minDimension * 0.72f
                        )
                    }
                    .pointerInput(isAwake) {
                        val touchSlop = viewConfiguration.touchSlop
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            returnToFrontJob?.cancel()
                            coroutineScope.launch {
                                pipRotationY.stop()
                                pipRotationX.stop()
                            }

                            val velocityTracker = VelocityTracker()
                            velocityTracker.addPosition(down.uptimeMillis, down.position)

                            var totalDragX = 0f
                            var totalDragY = 0f
                            var isHorizontalDrag: Boolean? = null
                            var isPointerUp = false

                            while (!isPointerUp) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                if (change.changedToUpIgnoreConsumed()) {
                                    isPointerUp = true
                                    break
                                }

                                val dragAmount = change.position - change.previousPosition
                                totalDragX += dragAmount.x
                                totalDragY += dragAmount.y
                                velocityTracker.addPosition(change.uptimeMillis, change.position)

                                if (isHorizontalDrag == null) {
                                    val distSq = totalDragX * totalDragX + totalDragY * totalDragY
                                    if (distSq > touchSlop * touchSlop) {
                                        if (Math.abs(totalDragX) >= Math.abs(totalDragY)) {
                                            isHorizontalDrag = true
                                        } else {
                                            // Vertical drag: yield to parent verticalScroll
                                            isHorizontalDrag = false
                                            break
                                        }
                                    }
                                }

                                if (isHorizontalDrag == true) {
                                    change.consume()
                                    val deltaYaw = dragAmount.x * 0.45f
                                    val deltaPitch = -dragAmount.y * 0.15f

                                    coroutineScope.launch {
                                        pipRotationY.snapTo(pipRotationY.value + deltaYaw)
                                        pipRotationX.snapTo((pipRotationX.value + deltaPitch).coerceIn(-15f, 15f))
                                    }
                                }
                            }

                            if (isPointerUp) {
                                val velocity = velocityTracker.calculateVelocity()
                                val velX = velocity.x

                                coroutineScope.launch {
                                    if (isHorizontalDrag == true) {
                                        // Apply inertia if there is sufficient velocity
                                        if (Math.abs(velX) > 120f) {
                                            val angularVelocity = velX * 0.35f
                                            try {
                                                pipRotationY.animateDecay(
                                                    initialVelocity = angularVelocity,
                                                    animationSpec = exponentialDecay(frictionMultiplier = 2.2f)
                                                )
                                            } catch (_: Exception) {
                                                // Interrupted by new touch
                                            }
                                        }
                                    } else if (isHorizontalDrag == null) {
                                        // Tap interaction
                                        onTap()
                                    }

                                    // 3-second timer with no touch eases Pip back to front view
                                    returnToFrontJob?.cancel()
                                    returnToFrontJob = launch {
                                        delay(3000L)
                                        val currentY = pipRotationY.value
                                        val normalizedY = (((currentY % 360f) + 540f) % 360f) - 180f
                                        pipRotationY.snapTo(normalizedY)

                                        launch {
                                            pipRotationY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                                            )
                                        }
                                        launch {
                                            pipRotationX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (pipInteractionState == PipInteractionState.HIDDEN) {
                    PipCharacterView(
                        size = scaledHomeSize,
                        characterSize = CharacterSize.MEDIUM,
                        scaleMultiplier = 1.0f,
                        animationSpeed = appearanceSettings.animationSpeed,
                        animationState = mascotReactionState,
                        rotationY = pipRotationY.value,
                        rotationX = pipRotationX.value,
                        prefer3D = true,
                        modelAssetPath = "models/pip.glb"
                    )
                }

                // Interactive "Tap Pip" badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-4).dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (!isAwake) Color(0xFF6366F1) else MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Text(
                            text = if (!isAwake) "Sleeping" else "Tap Pip",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (!isAwake) Color(0xFF6366F1) else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
