package com.hydrobuddy.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Phase 5 Material 3 Shape System.
 * Unified one corner radius (12dp) across cards, buttons, dialogs, and sheets.
 */
val AppCornerRadius = 12.dp

val CardShape = RoundedCornerShape(12.dp)
val ButtonShape = RoundedCornerShape(12.dp)
val ChipShape = RoundedCornerShape(12.dp)
val SheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
val BubbleShape = RoundedCornerShape(12.dp)
val PillShape = RoundedCornerShape(12.dp)

val HydroShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(12.dp)
)
