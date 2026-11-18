package com.hydrobuddy.app.ui.screens.appearance

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.model.AnimationSpeed
import com.hydrobuddy.app.data.model.CharacterPosition
import com.hydrobuddy.app.data.model.CharacterSize
import com.hydrobuddy.app.ui.components.HydroCard
import com.hydrobuddy.app.ui.components.HydroFilterChip
import com.hydrobuddy.app.ui.components.HydroSectionHeader
import com.hydrobuddy.app.ui.components.HydroSwitchRow
import com.hydrobuddy.app.ui.components.MascotIllustration
import com.hydrobuddy.app.ui.components.MascotMood
import com.hydrobuddy.app.ui.mascot.PipAnimationState
import com.hydrobuddy.app.ui.mascot.PipCharacterView
import com.hydrobuddy.app.ui.mascot.PipModelComparisonCard
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
fun AppearanceSettingsScreen(
    viewModel: HydroViewModel,
    modifier: Modifier = Modifier
) {
    val appearanceSettings by viewModel.appearanceSettings.collectAsState()
    val characterCustomization by viewModel.characterCustomization.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MintSoftBackground),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. LIVE APPEARANCE PREVIEW CARD
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HydroSectionHeader(
                        title = "Visual Appearance",
                        subtitle = "Previewing scale and float speed",
                        badgeText = "${appearanceSettings.characterSize.label} • ${appearanceSettings.animationSpeed.label}"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive Pip Character View reacting to size & speed
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SoftMintContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        PipCharacterView(
                            size = 90.dp,
                            characterSize = appearanceSettings.characterSize,
                            animationSpeed = appearanceSettings.animationSpeed,
                            animationState = PipAnimationState.A_IDLE
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Position: ${appearanceSettings.characterPosition.label}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // 1B. 3D MASCOT MODEL PROTOTYPE COMPARISON (STAGE 1 BLENDER INTEGRATION)
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSectionHeader(
                    title = "3D Mascot Model Prototype",
                    subtitle = "Compare Production Pip and custom Blender Stage 1",
                    badgeText = "Stage 1 Prototype"
                )
                Spacer(modifier = Modifier.height(14.dp))
                PipModelComparisonCard(
                    characterSize = appearanceSettings.characterSize
                )
            }
        }

        // 2. CHARACTER SIZE SELECTION
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSectionHeader(
                    title = "Character Size",
                    subtitle = "Scale factor for in-app and future overlay display"
                )

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CharacterSize.values().forEach { sizeOption ->
                        val isSelected = appearanceSettings.characterSize == sizeOption
                        HydroFilterChip(
                            label = sizeOption.label,
                            selected = isSelected,
                            onClick = { viewModel.updateCharacterSize(sizeOption) }
                        )
                    }
                }
            }
        }

        // 3. OVERLAY POSITION SELECTION
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSectionHeader(
                    title = "Default Screen Position",
                    subtitle = "Where ${characterCustomization.characterName} will dock when overlay is enabled"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CharacterPosition.values().forEach { positionOption ->
                        val isSelected = appearanceSettings.characterPosition == positionOption
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) SoftMintContainer else PureWhiteSurface)
                                .border(
                                    width = 1.2.dp,
                                    color = if (isSelected) MintTealPrimary else BorderSubtle,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.updateCharacterPosition(positionOption) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MintTealPrimary else BorderSubtle.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.OpenWith,
                                        contentDescription = null,
                                        tint = if (isSelected) PureWhiteSurface else SageTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = positionOption.label,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = SpruceTextPrimary
                                        )
                                    )
                                    Text(
                                        text = positionOption.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SageTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = MintTealDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. ANIMATION SPEED SELECTION
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSectionHeader(
                    title = "Animation Speed",
                    subtitle = "Breathing, blinking, and movement rhythm"
                )

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimationSpeed.values().forEach { speedOption ->
                        val isSelected = appearanceSettings.animationSpeed == speedOption
                        HydroFilterChip(
                            label = speedOption.label,
                            selected = isSelected,
                            onClick = { viewModel.updateAnimationSpeed(speedOption) }
                        )
                    }
                }
            }
        }

        // 5. SOUND EFFECTS TOGGLE
        item {
            HydroCard(backgroundColor = PureWhiteSurface) {
                HydroSwitchRow(
                    title = "Companion Sound Effects",
                    subtitle = "Play cute bubbling chirps on tap and feedback",
                    checked = appearanceSettings.soundEffectsEnabled,
                    onCheckedChange = { viewModel.toggleSoundEffects(it) },
                    icon = Icons.AutoMirrored.Rounded.VolumeUp
                )
            }
        }
    }
}

