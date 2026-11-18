package com.hydrobuddy.app.ui.screens.test

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.hydrobuddy.app.ui.mascot.PipAccessory
import com.hydrobuddy.app.ui.mascot.PipAnimationState
import com.hydrobuddy.app.ui.mascot.PipCharacterView
import com.hydrobuddy.app.ui.mascot.PipExpression
import com.hydrobuddy.app.ui.theme.FigmaNavy
import com.hydrobuddy.app.ui.theme.MintSoftBackground
import com.hydrobuddy.app.ui.theme.MintTealPrimary
import com.hydrobuddy.app.ui.theme.PureWhiteSurface
import com.hydrobuddy.app.ui.theme.SageTextSecondary
import com.hydrobuddy.app.ui.theme.SoftMintContainer
import com.hydrobuddy.app.ui.theme.SpruceTextPrimary

/**
 * Interactive Developer Testing Screen for Pip V2.
 * Allows full real-time permutation testing of:
 * 1. 8 skeletal animation clips
 * 2. 9 emotional facial expressions
 * 3. 6 modular equipment accessories (with mutual exclusivity rules)
 * 4. Expression intensity slider (0.0 to 1.0)
 * 5. Animation playback speed (0.5x, 1.0x, 1.5x, 2.0x)
 * 6. Natural blinking toggle
 * 7. Live model asset switcher (Production pip.glb vs Pip V2 pip_v2.glb)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PipV2TestScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Model Selection
    var selectedModelPath by remember { mutableStateOf("models/pip_v2.glb") }
    val isV2Model = selectedModelPath.contains("pip_v2")

    // Animation State
    var activeAnimation by remember { mutableStateOf(PipAnimationState.A_IDLE) }
    var animationSpeedMultiplier by remember { mutableFloatStateOf(1.0f) }

    // Facial Expression State
    var activeExpression by remember { mutableStateOf(PipExpression.NEUTRAL) }
    var expressionIntensity by remember { mutableFloatStateOf(1.0f) }
    var enableBlinking by remember { mutableStateOf(true) }

    // Equipment State
    var equippedAccessories by remember { mutableStateOf(setOf<PipAccessory>()) }

    // Viewport background
    var isDarkViewport by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhiteSurface)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = FigmaNavy
                )
            }
            Text(
                text = "Pip V2 Interactive Test Lab",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = FigmaNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                // Reset all to clean defaults
                activeAnimation = PipAnimationState.A_IDLE
                animationSpeedMultiplier = 1.0f
                activeExpression = PipExpression.NEUTRAL
                expressionIntensity = 1.0f
                enableBlinking = true
                equippedAccessories = emptySet()
            }) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Reset All",
                    tint = MintTealPrimary
                )
            }
        }

        // Live 3D Viewport Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(230.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDarkViewport) Color(0xFF142B42) else SoftMintContainer)
                .border(1.5.dp, if (isDarkViewport) Color(0xFF2E4B6A) else Color(0xFFE0F2F1), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            PipCharacterView(
                size = 190.dp,
                modelAssetPath = selectedModelPath,
                animationState = activeAnimation,
                animationSpeedMultiplier = animationSpeedMultiplier,
                expression = activeExpression,
                expressionIntensity = expressionIntensity,
                enableBlinking = enableBlinking,
                equippedAccessories = equippedAccessories,
                loopOverride = true,
                autoResetToIdle = false
            )

            // Top-right Viewport Controls (Dark/Light toggle)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isDarkViewport) PureWhiteSurface else FigmaNavy)
                        .clickable { isDarkViewport = !isDarkViewport }
                )
            }

            // Bottom-left info pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${if (isV2Model) "Pip V2 (925 KB)" else "Production Pip (586 KB)"} • ${activeAnimation.clipName}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Controls Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 0. Model Asset Selector
            Text(
                text = "3D Asset Version",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = FigmaNavy
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedModelPath == com.hydrobuddy.app.ui.mascot.PipModelManager.MODEL_PATH_V2,
                    onClick = {
                        selectedModelPath = com.hydrobuddy.app.ui.mascot.PipModelManager.MODEL_PATH_V2
                        com.hydrobuddy.app.ui.mascot.PipModelManager.setModelVersion(com.hydrobuddy.app.ui.mascot.PipModelManager.ModelVersion.V2)
                    },
                    label = { Text("Pip V2 (Modular & Expressive)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MintTealPrimary,
                        selectedLabelColor = PureWhiteSurface
                    )
                )
                FilterChip(
                    selected = selectedModelPath == com.hydrobuddy.app.ui.mascot.PipModelManager.MODEL_PATH_PRODUCTION,
                    onClick = {
                        selectedModelPath = com.hydrobuddy.app.ui.mascot.PipModelManager.MODEL_PATH_PRODUCTION
                        com.hydrobuddy.app.ui.mascot.PipModelManager.setModelVersion(com.hydrobuddy.app.ui.mascot.PipModelManager.ModelVersion.PRODUCTION)
                    },
                    label = { Text("Production Pip (Old)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FigmaNavy,
                        selectedLabelColor = PureWhiteSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Quick Presets Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MintSoftBackground)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Combinations Presets",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpruceTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip("Thirsty Pip") {
                            activeAnimation = PipAnimationState.G_DRINKING
                            activeExpression = PipExpression.HAPPY
                            equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                        }
                        PresetChip("Sarcastic Chef") {
                            activeAnimation = PipAnimationState.A_IDLE
                            activeExpression = PipExpression.MISCHIEVOUS
                            equippedAccessories = setOf(PipAccessory.CHEF_HAT, PipAccessory.CARTOON_KNIFE)
                        }
                        PresetChip("Party Pip") {
                            activeAnimation = PipAnimationState.F_CELEBRATING
                            activeExpression = PipExpression.SURPRISED
                            equippedAccessories = setOf(PipAccessory.PARTY_HAT, PipAccessory.SUNGLASSES)
                        }
                        PresetChip("Winter Walk") {
                            activeAnimation = PipAnimationState.B_WALKING_IN
                            activeExpression = PipExpression.CURIOUS
                            equippedAccessories = setOf(PipAccessory.SCARF, PipAccessory.SUNGLASSES)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Animation Clips Selector (8 Clips)
            Text(
                text = "1. Skeletal Animation Clips (${PipAnimationState.values().size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = FigmaNavy
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PipAnimationState.values().forEach { anim ->
                    FilterChip(
                        selected = activeAnimation == anim,
                        onClick = { activeAnimation = anim },
                        label = { Text(anim.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintTealPrimary,
                            selectedLabelColor = PureWhiteSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Speed Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Speed: ${"%.1f".format(animationSpeedMultiplier)}x",
                    style = MaterialTheme.typography.bodySmall.copy(color = SageTextSecondary)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { spd ->
                        FilterChip(
                            selected = animationSpeedMultiplier == spd,
                            onClick = { animationSpeedMultiplier = spd },
                            label = { Text("${spd}x", fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Facial Expressions Selector (9 Expressions)
            Text(
                text = "2. Facial Expressions (${PipExpression.values().size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = FigmaNavy
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PipExpression.values().forEach { expr ->
                    FilterChip(
                        selected = activeExpression == expr,
                        onClick = { activeExpression = expr },
                        label = { Text(expr.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FigmaNavy,
                            selectedLabelColor = PureWhiteSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Intensity Slider & Blinking Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intensity: ${(expressionIntensity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(color = SageTextSecondary),
                    modifier = Modifier.width(90.dp)
                )
                Slider(
                    value = expressionIntensity,
                    onValueChange = { expressionIntensity = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MintTealPrimary,
                        activeTrackColor = MintTealPrimary
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Natural Eyelid Blinking (Coroutines)",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SpruceTextPrimary)
                )
                Switch(
                    checked = enableBlinking,
                    onCheckedChange = { enableBlinking = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhiteSurface, checkedTrackColor = MintTealPrimary)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Equipment Accessories Selector (6 Accessories)
            Text(
                text = "3. Modular Equipment Accessories (${PipAccessory.values().size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = FigmaNavy
                )
            )
            Text(
                text = "Note: Chef Hat & Party Hat are mutually exclusive (HEAD category)",
                style = MaterialTheme.typography.labelSmall.copy(color = SageTextSecondary, fontSize = 11.sp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PipAccessory.values().forEach { acc ->
                    val isEquipped = equippedAccessories.contains(acc)
                    FilterChip(
                        selected = isEquipped,
                        onClick = {
                            val newSet = equippedAccessories.toMutableSet()
                            if (isEquipped) {
                                newSet.remove(acc)
                            } else {
                                // Enforce category mutual exclusivity
                                newSet.removeAll { it.category == acc.category }
                                newSet.add(acc)
                            }
                            equippedAccessories = newSet
                        },
                        label = { Text(acc.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00897B),
                            selectedLabelColor = PureWhiteSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(PureWhiteSurface)
            .border(1.dp, Color(0xFFB2DFDB), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = FigmaNavy,
                fontSize = 12.sp
            )
        )
    }
}
