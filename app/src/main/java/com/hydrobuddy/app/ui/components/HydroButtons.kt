package com.hydrobuddy.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.ui.theme.ButtonShape
import com.hydrobuddy.app.ui.theme.ChipShape
import com.hydrobuddy.app.ui.theme.FigmaBlue
import com.hydrobuddy.app.ui.theme.FigmaBorder
import com.hydrobuddy.app.ui.theme.FigmaNavy
import com.hydrobuddy.app.ui.theme.FigmaProgressTrack
import com.hydrobuddy.app.ui.theme.FigmaSlate
import com.hydrobuddy.app.ui.theme.FigmaSoftBlue
import com.hydrobuddy.app.ui.theme.FigmaSurfaceWhite

/**
 * Authoritative Primary Action Button.
 * 14.dp corner radius, 50.dp height, subtle ambient elevation, Plus Jakarta Sans typography.
 */
@Composable
fun HydroPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = FigmaBlue,
    contentColor: Color = Color.White,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(50.dp)
            .shadow(
                elevation = if (enabled) 2.dp else 0.dp,
                shape = ButtonShape,
                ambientColor = containerColor.copy(alpha = 0.25f),
                spotColor = containerColor.copy(alpha = 0.35f)
            ),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = FigmaBorder,
            disabledContentColor = FigmaSlate.copy(alpha = 0.6f)
        ),
        enabled = enabled
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
        }
    }
}

/**
 * Authoritative Secondary Action Button.
 * Clean white surface, 1.dp subtle border, navy text.
 */
@Composable
fun HydroSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = FigmaSurfaceWhite,
    contentColor: Color = FigmaNavy,
    borderColor: Color = FigmaBorder
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .shadow(
                elevation = 1.dp,
                shape = ButtonShape,
                ambientColor = Color(0x06142B42)
            ),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
        }
    }
}

/**
 * Authoritative Filter Chip.
 * 8.dp corner radius (ChipShape), clear active state, 1.dp border.
 */
@Composable
fun HydroFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(ChipShape)
            .background(
                if (selected) FigmaBlue else FigmaSurfaceWhite
            )
            .border(
                width = 1.dp,
                color = if (selected) FigmaBlue else FigmaBorder,
                shape = ChipShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (selected) Color.White else FigmaNavy,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        )
    }
}

/**
 * Authoritative Day Picker Chip.
 * Clean square with rounded corners (10.dp) for day-of-week toggling.
 */
@Composable
fun HydroDayChip(
    dayInitial: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) FigmaBlue else FigmaSurfaceWhite
            )
            .border(
                width = 1.dp,
                color = if (selected) FigmaBlue else FigmaBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayInitial,
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (selected) Color.White else FigmaNavy,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )
    }
}

/**
 * Authoritative Settings Switch Row.
 * Reusable row for toggle settings with optional icon and subtitle.
 */
@Composable
fun HydroSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FigmaSoftBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FigmaBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = FigmaNavy,
                    fontSize = 14.sp
                )
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = FigmaSlate,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FigmaBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = FigmaProgressTrack,
                uncheckedBorderColor = FigmaBorder
            )
        )
    }
}

/**
 * Authoritative Text Input Field.
 * 14.dp rounded corners, subtle 1.dp border, clean focus states, accessible typography.
 */
@Composable
fun HydroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = false,
    maxLines: Int = 3
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = FigmaNavy,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = FigmaSlate.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                )
            },
            shape = ButtonShape,
            singleLine = singleLine,
            maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = FigmaNavy,
                fontSize = 14.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FigmaSurfaceWhite,
                unfocusedContainerColor = FigmaSurfaceWhite,
                focusedBorderColor = FigmaBlue,
                unfocusedBorderColor = FigmaBorder,
                focusedTextColor = FigmaNavy,
                unfocusedTextColor = FigmaNavy
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

