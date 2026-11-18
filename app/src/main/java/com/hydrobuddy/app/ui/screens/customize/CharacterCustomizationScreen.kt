package com.hydrobuddy.app.ui.screens.customize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.model.CharacterCustomization
import com.hydrobuddy.app.ui.mascot.PipAccessory
import com.hydrobuddy.app.ui.mascot.PipAnimationState
import com.hydrobuddy.app.ui.mascot.PipCharacterView
import com.hydrobuddy.app.viewmodel.HydroViewModel
import com.hydrobuddy.app.viewmodel.PreviewMessageType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Stitch-to-Figma Implementation of Pip Personality & Messages (Node 4:2).
 * Faithful implementation of the authorized design without reinterpreting or redesigning.
 */
@Composable
fun CharacterCustomizationScreen(
    viewModel: HydroViewModel,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    val savedCustomization by viewModel.characterCustomization.collectAsState()
    val appearanceSettings by viewModel.appearanceSettings.collectAsState()
    val selectedTab by viewModel.selectedPreviewTab.collectAsState()
    var selectedPipAnim by remember { mutableStateOf<PipAnimationState?>(null) }

    // 10 Configurable DataStore personality fields
    var personalityInput by remember(savedCustomization) { mutableStateOf(savedCustomization.personality) }
    var nameInput by remember(savedCustomization) { mutableStateOf(savedCustomization.characterName) }
    var reminderInput by remember(savedCustomization) { mutableStateOf(savedCustomization.waterReminderMessage) }
    var yesBtnInput by remember(savedCustomization) { mutableStateOf(savedCustomization.yesButtonText) }
    var noBtnInput by remember(savedCustomization) { mutableStateOf(savedCustomization.noButtonText) }
    var yesRespInput by remember(savedCustomization) { mutableStateOf(savedCustomization.yesResponseMessage) }
    var encouragementInput by remember(savedCustomization) { mutableStateOf(savedCustomization.happyEncouragementMessage) }
    var noRespInput by remember(savedCustomization) { mutableStateOf(savedCustomization.noResponseMessage) }
    var mischievousInput by remember(savedCustomization) { mutableStateOf(savedCustomization.mischievousKnifeMessage) }
    var entranceInput by remember(savedCustomization) { mutableStateOf(savedCustomization.entranceMessage) }
    var exitInput by remember(savedCustomization) { mutableStateOf(savedCustomization.exitMessage) }

    // UI Feedback states
    var showSuccessBanner by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Map preview chips to Pip animations and message previews
    val (previewText, tabAnimation) = when (selectedTab) {
        PreviewMessageType.REMINDER -> Pair(reminderInput.ifBlank { CharacterCustomization.DEFAULT_REMINDER }, PipAnimationState.C_REMINDER)
        PreviewMessageType.YES_RESPONSE -> Pair(yesRespInput.ifBlank { CharacterCustomization.DEFAULT_YES_RESP }, PipAnimationState.G_DRINKING)
        PreviewMessageType.NO_RESPONSE -> Pair(noRespInput.ifBlank { CharacterCustomization.DEFAULT_NO_RESP }, PipAnimationState.E_SAD)
        PreviewMessageType.HAPPY -> Pair(encouragementInput.ifBlank { CharacterCustomization.DEFAULT_HAPPY_ENCOURAGEMENT }, PipAnimationState.F_CELEBRATING)
        PreviewMessageType.MISCHIEVOUS -> Pair(mischievousInput.ifBlank { CharacterCustomization.DEFAULT_MISCHIEVOUS_KNIFE }, PipAnimationState.E_SAD)
        PreviewMessageType.ENTRANCE, PreviewMessageType.GREETING -> Pair(entranceInput.ifBlank { CharacterCustomization.DEFAULT_ENTRANCE }, PipAnimationState.B_WALKING_IN)
        PreviewMessageType.EXIT, PreviewMessageType.SAD -> Pair(exitInput.ifBlank { CharacterCustomization.DEFAULT_EXIT }, PipAnimationState.H_WALKING_OUT)
    }

    val previewAccessories = when (selectedTab) {
        PreviewMessageType.MISCHIEVOUS, PreviewMessageType.NO_RESPONSE -> setOf(PipAccessory.CARTOON_KNIFE)
        PreviewMessageType.REMINDER, PreviewMessageType.YES_RESPONSE, PreviewMessageType.HAPPY -> setOf(PipAccessory.WATER_GLASS)
        else -> emptySet()
    }

    val activeAnimState = selectedPipAnim ?: tabAnimation

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Main scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SUCCESS BANNER ---
            if (showSuccessBanner) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = successMessage,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { showSuccessBanner = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- HERO SECTION: MEET PIP ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dynamic Speech Bubble with tail
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WaterDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = previewText,
                                    style = TextStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        letterSpacing = (-0.3).sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }

                        // Triangle tail pointing downwards to Pip
                        Box(
                            modifier = Modifier
                                .offset(y = (-6).dp)
                                .size(12.dp)
                                .rotate(45f)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }

                    // Circular Pip Avatar Graphic
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        PipCharacterView(
                            size = 120.dp,
                            characterSize = appearanceSettings.characterSize,
                            animationSpeed = appearanceSettings.animationSpeed,
                            animationState = activeAnimState,
                            equippedAccessories = previewAccessories,
                            speechBubbleText = null,
                            onClick = {
                                selectedPipAnim = PipAnimationState.G_DRINKING
                            },
                            onAnimationFinished = {
                                selectedPipAnim = null
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Character Identity Pill Tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = nameInput.ifBlank { CharacterCustomization.DEFAULT_NAME },
                                style = TextStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Penguin Buddy",
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Lvl 4",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 1: LIVE DIALOGUE PLAYGROUND ---
            item {
                FigmaCardContainer {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live dialogue playground",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        // "Interactive" Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.NorthEast,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Interactive",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Test how Pip talks and reacts on your lock screen & prompt notifications.",
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preview Speech Card Simulation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mini avatar icon
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WaterDrop,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${nameInput.ifBlank { CharacterCustomization.DEFAULT_NAME }} Companion",
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = "Preview",
                                            style = TextStyle(
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "\"$previewText\"",
                                        style = TextStyle(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Preview Quick Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Drank it! / YES Button
                                Button(
                                    onClick = {
                                        viewModel.setPreviewTab(PreviewMessageType.YES_RESPONSE)
                                        selectedPipAnim = PipAnimationState.G_DRINKING
                                    },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(38.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = yesBtnInput.ifBlank { CharacterCustomization.DEFAULT_YES_BTN },
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Snooze / NO Button
                                Surface(
                                    onClick = {
                                        viewModel.setPreviewTab(PreviewMessageType.NO_RESPONSE)
                                        selectedPipAnim = PipAnimationState.E_SAD
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = noBtnInput.ifBlank { CharacterCustomization.DEFAULT_NO_BTN },
                                            style = TextStyle(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Chips / State Switchers
                    val previewChips = listOf(
                        Pair(PreviewMessageType.REMINDER, "Reminder"),
                        Pair(PreviewMessageType.YES_RESPONSE, "Yes response"),
                        Pair(PreviewMessageType.NO_RESPONSE, "No response"),
                        Pair(PreviewMessageType.HAPPY, "Happy"),
                        Pair(PreviewMessageType.MISCHIEVOUS, "Mischievous"),
                        Pair(PreviewMessageType.ENTRANCE, "Entrance"),
                        Pair(PreviewMessageType.EXIT, "Exit")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(previewChips) { (tab, label) ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        viewModel.setPreviewTab(tab)
                                        selectedPipAnim = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 2: CHARACTER IDENTITY ---
            item {
                FigmaCardContainer {
                    FigmaCardHeader(
                        icon = Icons.Rounded.Person,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Character identity",
                        subtitle = "Name & persona identifiers",
                        badge = "Base Info",
                        badgeBg = MaterialTheme.colorScheme.surfaceVariant,
                        badgeTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FigmaInputField(
                        label = "Character name",
                        value = nameInput,
                        onValueChange = { if (it.length <= 20) nameInput = it },
                        maxLength = 20,
                        placeholder = "Pip",
                        helperText = "How your companion addresses itself inside reminders and home widget cards."
                    )
                }
            }

            // --- SECTION 3: REMINDER DIALOGUE ---
            item {
                FigmaCardContainer {
                    FigmaCardHeader(
                        icon = Icons.Rounded.WaterDrop,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Reminder dialogue",
                        subtitle = "Prompt text & quick actions",
                        actionText = "Reset",
                        onActionClick = {
                            val defs = CharacterCustomization.default()
                            reminderInput = defs.waterReminderMessage
                            yesBtnInput = defs.yesButtonText
                            noBtnInput = defs.noButtonText
                            successMessage = "Reminder texts reset to default"
                            showSuccessBanner = true
                            coroutineScope.launch {
                                delay(3000)
                                showSuccessBanner = false
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FigmaInputField(
                        label = "Water reminder message",
                        value = reminderInput,
                        onValueChange = { if (it.length <= 120) reminderInput = it },
                        maxLength = 120,
                        placeholder = CharacterCustomization.DEFAULT_REMINDER,
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FigmaInputField(
                            label = "Yes button",
                            value = yesBtnInput,
                            onValueChange = { if (it.length <= 20) yesBtnInput = it },
                            maxLength = 20,
                            placeholder = CharacterCustomization.DEFAULT_YES_BTN,
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        FigmaInputField(
                            label = "No button",
                            value = noBtnInput,
                            onValueChange = { if (it.length <= 20) noBtnInput = it },
                            maxLength = 20,
                            placeholder = CharacterCustomization.DEFAULT_NO_BTN,
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // --- SECTION 4: RESPONSE PERSONALITY ---
            item {
                FigmaCardContainer {
                    FigmaCardHeader(
                        icon = Icons.Rounded.AutoAwesome,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Response personality",
                        subtitle = "Mood variations and feedback",
                        badge = "Interactive",
                        badgeBg = MaterialTheme.colorScheme.surfaceVariant,
                        badgeTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Personality Mode Selector (Rude vs Sad)
                    Text(
                        text = "Reaction personality",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.hydrobuddy.app.data.model.PipPersonality.values().forEach { p ->
                            val isSelected = personalityInput == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        personalityInput = p
                                        viewModel.updatePersonality(p)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = p.displayName,
                                        style = TextStyle(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = when (p) {
                                            com.hydrobuddy.app.data.model.PipPersonality.FRIENDLY -> "Gentle & kind"
                                            com.hydrobuddy.app.data.model.PipPersonality.SAD -> "Guilt-trip"
                                            com.hydrobuddy.app.data.model.PipPersonality.RUDE -> "Sarcastic"
                                        },
                                        style = TextStyle(
                                            fontSize = 10.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. YES Response Message (Green dot)
                    FigmaInputField(
                        label = "Yes response message",
                        dotColor = Color(0xFF10B981),
                        value = yesRespInput,
                        onValueChange = { if (it.length <= 120) yesRespInput = it },
                        maxLength = 120,
                        placeholder = CharacterCustomization.DEFAULT_YES_RESP,
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Happy Encouragement Message (Yellow/Amber dot)
                    FigmaInputField(
                        label = "Happy encouragement message",
                        dotColor = Color(0xFFFBBF24),
                        value = encouragementInput,
                        onValueChange = { if (it.length <= 120) encouragementInput = it },
                        maxLength = 120,
                        placeholder = CharacterCustomization.DEFAULT_HAPPY_ENCOURAGEMENT,
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. NO Response Message (Red/Rose dot)
                    FigmaInputField(
                        label = "No response message",
                        dotColor = Color(0xFFFB7185),
                        value = noRespInput,
                        onValueChange = { if (it.length <= 120) noRespInput = it },
                        maxLength = 120,
                        placeholder = CharacterCustomization.DEFAULT_NO_RESP,
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Mischievous Message (Purple dot)
                    FigmaInputField(
                        label = "Mischievous message",
                        dotColor = Color(0xFFC084FC),
                        value = mischievousInput,
                        onValueChange = { if (it.length <= 120) mischievousInput = it },
                        maxLength = 120,
                        placeholder = CharacterCustomization.DEFAULT_MISCHIEVOUS_KNIFE,
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            // --- SECTION 5: ARRIVAL & DEPARTURE ---
            item {
                FigmaCardContainer {
                    FigmaCardHeader(
                        icon = Icons.Rounded.DirectionsWalk,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Arrival & departure",
                        subtitle = "App launch and background cues"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FigmaInputField(
                        label = "Entrance message",
                        value = entranceInput,
                        onValueChange = { if (it.length <= 120) entranceInput = it },
                        maxLength = 120,
                        placeholder = CharacterCustomization.DEFAULT_ENTRANCE,
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    FigmaInputField(
                        label = "Exit message",
                        value = exitInput,
                        onValueChange = { if (it.length <= 120) exitInput = it },
                        maxLength = 120,
                        placeholder = CharacterCustomization.DEFAULT_EXIT,
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
        }

        // --- STICKY BOTTOM ACTION BAR ---
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Restore Defaults Button
                    Surface(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Restore defaults",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Save Changes Button
                    Button(
                        onClick = {
                            val updated = CharacterCustomization(
                                characterName = nameInput.trim().ifBlank { CharacterCustomization.DEFAULT_NAME },
                                personality = personalityInput,
                                waterReminderMessage = reminderInput.trim().ifBlank { CharacterCustomization.DEFAULT_REMINDER },
                                yesButtonText = yesBtnInput.trim().ifBlank { CharacterCustomization.DEFAULT_YES_BTN },
                                noButtonText = noBtnInput.trim().ifBlank { CharacterCustomization.DEFAULT_NO_BTN },
                                yesResponseMessage = yesRespInput.trim().ifBlank { CharacterCustomization.DEFAULT_YES_RESP },
                                happyEncouragementMessage = encouragementInput.trim().ifBlank { CharacterCustomization.DEFAULT_HAPPY_ENCOURAGEMENT },
                                noResponseMessage = noRespInput.trim().ifBlank { CharacterCustomization.DEFAULT_NO_RESP },
                                mischievousKnifeMessage = mischievousInput.trim().ifBlank { CharacterCustomization.DEFAULT_MISCHIEVOUS_KNIFE },
                                entranceMessage = entranceInput.trim().ifBlank { CharacterCustomization.DEFAULT_ENTRANCE },
                                exitMessage = exitInput.trim().ifBlank { CharacterCustomization.DEFAULT_EXIT }
                            )
                            viewModel.saveFullCharacterCustomization(updated)
                            successMessage = "Changes saved and synced to Pip"
                            showSuccessBanner = true
                            coroutineScope.launch {
                                delay(3500)
                                showSuccessBanner = false
                            }
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save changes",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sync Feedback Text
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Changes sync automatically to Pip overlay & notifications",
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }

    // --- RESTORE DEFAULTS CONFIRMATION DIALOG ---
    if (showResetConfirmDialog) {
        AlertDialog(
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = "Restore default messages?",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            text = {
                Text(
                    text = "This will reset all Pip dialogue, reminder texts, and YES/NO button labels to their default values. Your hydration intake history, schedule, and settings will remain untouched.",
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreDefaultCharacterCustomization()
                        val defaults = CharacterCustomization.default()
                        nameInput = defaults.characterName
                        reminderInput = defaults.waterReminderMessage
                        yesBtnInput = defaults.yesButtonText
                        noBtnInput = defaults.noButtonText
                        yesRespInput = defaults.yesResponseMessage
                        encouragementInput = defaults.happyEncouragementMessage
                        noRespInput = defaults.noResponseMessage
                        mischievousInput = defaults.mischievousKnifeMessage
                        entranceInput = defaults.entranceMessage
                        exitInput = defaults.exitMessage

                        showResetConfirmDialog = false
                        successMessage = "Default messages restored"
                        showSuccessBanner = true
                        coroutineScope.launch {
                            delay(3500)
                            showSuccessBanner = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset defaults", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        )
    }
}

/**
 * Material 3 flat card container with 12dp radius and 1dp border.
 */
@Composable
private fun FigmaCardContainer(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Section Header matching Material 3 design:
 * Icon container (32dp, rounded 12dp) + Title & Subtitle + Optional Badge or Action Button
 */
@Composable
private fun FigmaCardHeader(
    icon: ImageVector,
    iconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeBg: Color = MaterialTheme.colorScheme.surfaceVariant,
    badgeTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badge,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = badgeTextColor
                    )
                )
            }
        } else if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Text field component matching Material 3 design:
 * - Label with optional status indicator dot on left
 * - Character count on right (e.g. "3 / 20" or "72 / 120")
 * - Rounded container with 12dp radius and 1dp outline
 * - Helper text below
 */
@Composable
private fun FigmaInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    dotColor: Color? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1,
    helperText: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Label and Counter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (dotColor != null) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = label,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Text(
                text = "${value.length} / $maxLength",
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Outlined Text Field
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (helperText != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = helperText,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            )
        }
    }
}
