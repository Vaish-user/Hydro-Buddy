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
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.ui.theme.FigmaBackground
import com.hydrobuddy.app.ui.theme.FigmaBlue
import com.hydrobuddy.app.ui.theme.FigmaNavy
import com.hydrobuddy.app.ui.theme.FigmaOrange
import com.hydrobuddy.app.ui.theme.FigmaSlate

/**
 * 6. BOTTOM SHEET: SETTINGS (Figma Node 1:528)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    dailyTargetMl: Int,
    onDismissRequest: () -> Unit,
    onOpenSystemOverlaySettings: () -> Unit,
    onOpenPersonalitySettings: (() -> Unit)? = null,
    onOpenPipV2Test: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                        text = "PREFERENCES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FigmaSlate,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "HydroBuddy Settings",
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

            // Settings Rows List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Daily Target Goal
                SettingRowItem(
                    label = "Daily Target Goal",
                    value = "%,d ml".format(dailyTargetMl),
                    valueColor = FigmaBlue
                )

                // 2. Measurement Units
                SettingRowItem(
                    label = "Measurement Units",
                    value = "Milliliters (ml)",
                    valueColor = FigmaBlue
                )

                // 3. Pip's Sarcasm Level
                SettingRowItem(
                    label = "Pip's Sarcasm Level",
                    value = "High (Spicy)",
                    valueColor = FigmaOrange
                )

                // 4. Pip Personality & Messages Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEBF5FB))
                        .border(1.dp, FigmaBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onOpenPersonalitySettings?.invoke() }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pip Personality & Messages",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaNavy
                        )
                        Text(
                            text = "Custom reminder dialogue & YES/NO responses",
                            fontSize = 10.sp,
                            color = FigmaSlate
                        )
                    }
                    Text(
                        text = "Customize",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FigmaBlue
                    )
                }

                // 5. Floating Pip Overlay Permission
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(FigmaBackground)
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenSystemOverlaySettings
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overlay Permission",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaNavy
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "System Settings",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FigmaBlue
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                            tint = FigmaBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // 6. Pip V2 3D Test Lab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.dp, Color(0xFF81C784).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onOpenPipV2Test?.invoke() }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pip V2 3D Test Lab",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaNavy
                        )
                        Text(
                            text = "Test 12 expressions, animations & 6 accessories",
                            fontSize = 10.sp,
                            color = FigmaSlate
                        )
                    }
                    Text(
                        text = "Open Lab",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            // Close Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F5F9))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close Settings",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaNavy
                )
            }
        }
    }
}

@Composable
private fun SettingRowItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FigmaBackground)
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = FigmaNavy
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}
