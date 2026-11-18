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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.ui.theme.FigmaBlue
import com.hydrobuddy.app.ui.theme.FigmaNavy
import com.hydrobuddy.app.ui.theme.FigmaOrange
import com.hydrobuddy.app.ui.theme.FigmaSlate
import com.hydrobuddy.app.ui.theme.FigmaTeal

data class AccessoryItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val isLocked: Boolean = false,
    val lockRequirement: String? = null
)

/**
 * 3. BOTTOM SHEET: PIP CUSTOMIZATION (Wardrobe) (Figma Node 1:661)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipWardrobeBottomSheet(
    characterName: String,
    streakDays: Int,
    onDismissRequest: () -> Unit,
    onApplyOutfit: (handAccessory: String, headGear: String) -> Unit,
    onOpenPersonalitySettings: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val handAccessories = listOf(
        AccessoryItem("knife", "Butter Knife", Icons.Rounded.Restaurant),
        AccessoryItem("straw", "Tiny Straw", Icons.Rounded.LocalDrink),
        AccessoryItem("trident", "Ice Trident", Icons.Rounded.FlashOn, isLocked = true, lockRequirement = "Reach 30 Days")
    )

    val headGears = listOf(
        AccessoryItem("tufts", "Signature Tufts", Icons.Rounded.AutoAwesome),
        AccessoryItem("cap", "Aqua Cap", Icons.Rounded.Face),
        AccessoryItem("crown", "Water Crown", Icons.Rounded.Star, isLocked = true, lockRequirement = "Goal 100%")
    )

    var selectedHand by remember { mutableStateOf("knife") }
    var selectedHead by remember { mutableStateOf("tufts") }

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
                        text = "MASCOT WARDROBE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FigmaBlue,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Customize Pip",
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

            // Mascot Avatar Hero
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(6.dp, CircleShape, spotColor = FigmaTeal.copy(alpha = 0.2f))
                        .clip(CircleShape)
                        .background(Color(0xFFEBF5FB))
                        .border(1.5.dp, FigmaTeal.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Face,
                        contentDescription = null,
                        tint = FigmaTeal,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Text(
                    text = "$characterName the Penguin",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FigmaNavy
                )
                Text(
                    text = "Level 4 Companion • $streakDays Day Hydration Streak",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = FigmaSlate
                )
            }

            // Section 1: Hand Accessories
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Hand Accessories",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaNavy
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    handAccessories.forEach { item ->
                        val isSelected = selectedHand == item.id
                        AccessoryCard(
                            item = item,
                            isSelected = isSelected,
                            onSelect = {
                                if (!item.isLocked) {
                                    selectedHand = item.id
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section 2: Head Gear
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Head Gear",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaNavy
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    headGears.forEach { item ->
                        val isSelected = selectedHead == item.id
                        AccessoryCard(
                            item = item,
                            isSelected = isSelected,
                            onSelect = {
                                if (!item.isLocked) {
                                    selectedHead = item.id
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Apply Outfit Gradient Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = FigmaBlue.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(FigmaBlue, FigmaTeal)
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onApplyOutfit(selectedHand, selectedHead)
                            onDismissRequest()
                        }
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Apply Outfit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            // Customize Personality & Messages
            if (onOpenPersonalitySettings != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onDismissRequest()
                                onOpenPersonalitySettings()
                            }
                        )
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Customize Personality & Dialogue",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaNavy
                    )
                }
            }
        }
    }
}

@Composable
private fun AccessoryCard(
    item: AccessoryItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = when {
        isSelected -> Color(0xFFEBF5FB)
        item.isLocked -> Color(0xFFF8FAFC)
        else -> Color.White
    }

    val cardBorder = when {
        isSelected -> FigmaBlue
        item.isLocked -> Color(0xFFCBD5E1)
        else -> Color(0xFFE2E8F0)
    }

    Box(
        modifier = modifier
            .alpha(if (item.isLocked) 0.65f else 1f)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = cardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = if (isSelected) FigmaTeal else FigmaSlate,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = item.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FigmaNavy,
                maxLines = 1
            )
            Text(
                text = when {
                    isSelected -> "EQUIPPED"
                    item.isLocked -> item.lockRequirement ?: "Locked"
                    else -> "Unlocked"
                },
                fontSize = 9.sp,
                fontWeight = if (isSelected || item.isLocked) FontWeight.ExtraBold else FontWeight.Normal,
                color = when {
                    isSelected -> FigmaBlue
                    item.isLocked -> FigmaOrange
                    else -> FigmaSlate
                }
            )
        }
    }
}
