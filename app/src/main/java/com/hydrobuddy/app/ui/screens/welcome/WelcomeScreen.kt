package com.hydrobuddy.app.ui.screens.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.ui.theme.MintSoftBackground
import com.hydrobuddy.app.ui.theme.MintTealPrimary
import com.hydrobuddy.app.ui.theme.PureWhiteSurface
import com.hydrobuddy.app.ui.theme.SageTextSecondary
import com.hydrobuddy.app.ui.theme.SpruceTextPrimary

/**
 * First-Time Welcome Screen for HydroBuddy.
 * Displayed exclusively on first app launch until legal agreements are accepted.
 */
@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var termsAgreed by remember { mutableStateOf(false) }
    var privacyAgreed by remember { mutableStateOf(false) }
    val isContinueEnabled = termsAgreed && privacyAgreed

    // Smooth subtle entrance animation
    val entranceAlpha = remember { Animatable(0f) }
    val entranceOffset = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        entranceOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
    }

    // Ambient floating hover transition for companion mascot
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_companion_hover")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover_float"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MintSoftBackground),
        contentAlignment = Alignment.Center
    ) {
        // 1. Top Graphic: Resting Companion Mascot with Ambient Breathing
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(entranceAlpha.value)
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            // Mascot sits comfortably in the upper half above the onboarding card
            val centerY = canvasH * 0.35f + hoverOffset

            drawCompanionMascot(
                centerX = centerX,
                centerY = centerY
            )
        }

        // 2. Onboarding UI: Heading, Subtitle, Checkboxes, Continue Button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .alpha(entranceAlpha.value)
                .offset(y = entranceOffset.value.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to HydroBuddy",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SpruceTextPrimary,
                    fontSize = 26.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your smart companion for mindful hydration.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SageTextSecondary,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Checkbox 1: Terms & Conditions (Default false)
            LegalCheckboxRow(
                text = "I have read and agree to HydroBuddy's Terms & Conditions",
                checked = termsAgreed,
                onCheckedChange = { termsAgreed = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Checkbox 2: Privacy Policy (Default false)
            LegalCheckboxRow(
                text = "I have read and agree to HydroBuddy's Privacy Policy",
                checked = privacyAgreed,
                onCheckedChange = { privacyAgreed = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Continue Button (Enabled only when both agreements are checked)
            Button(
                onClick = {
                    if (isContinueEnabled) {
                        onContinue()
                    }
                },
                enabled = isContinueEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpruceTextPrimary,
                    disabledContainerColor = SpruceTextPrimary.copy(alpha = 0.30f),
                    contentColor = PureWhiteSurface,
                    disabledContentColor = PureWhiteSurface.copy(alpha = 0.60f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Modern legal checkbox row with accessible tap target and custom checkbox design.
 */
@Composable
private fun LegalCheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) MintTealPrimary else Color(0xFFE2EBE8)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Checked",
                    tint = PureWhiteSurface,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                color = SpruceTextPrimary.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )
        )
    }
}

/**
 * Draws the glossy, fluid water droplet companion character with specular highlights.
 */
private fun DrawScope.drawCompanionMascot(
    centerX: Float,
    centerY: Float
) {
    val baseW = 190f
    val baseH = 210f

    // 1. Soft droplet ambient contact shadow
    drawOval(
        color = Color(0x221A332E),
        topLeft = Offset(centerX - baseW * 0.45f, centerY + baseH * 0.42f),
        size = Size(baseW * 0.9f, 26f)
    )

    // 2. Teardrop body path
    val dropletPath = Path().apply {
        moveTo(centerX, centerY - baseH * 0.58f)
        cubicTo(
            centerX + baseW * 0.25f, centerY - baseH * 0.28f,
            centerX + baseW * 0.58f, centerY + baseH * 0.12f,
            centerX + baseW * 0.52f, centerY + baseH * 0.38f
        )
        cubicTo(
            centerX + baseW * 0.45f, centerY + baseH * 0.56f,
            centerX - baseW * 0.45f, centerY + baseH * 0.56f,
            centerX - baseW * 0.52f, centerY + baseH * 0.38f
        )
        cubicTo(
            centerX - baseW * 0.58f, centerY + baseH * 0.12f,
            centerX - baseW * 0.25f, centerY - baseH * 0.28f,
            centerX, centerY - baseH * 0.58f
        )
        close()
    }

    // Fill with water gradient
    drawPath(
        path = dropletPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF48CAE4), // Crisp sky aqua
                Color(0xFF0096C7), // Azure core
                Color(0xFF023E8A)  // Deep ocean refraction
            ),
            startY = centerY - baseH * 0.6f,
            endY = centerY + baseH * 0.6f
        ),
        style = Fill
    )

    // 3. Specular water gloss highlight
    val highlightPath = Path().apply {
        moveTo(centerX - baseW * 0.18f, centerY - baseH * 0.42f)
        cubicTo(
            centerX - baseW * 0.40f, centerY - baseH * 0.15f,
            centerX - baseW * 0.44f, centerY + baseH * 0.12f,
            centerX - baseW * 0.38f, centerY + baseH * 0.24f
        )
        cubicTo(
            centerX - baseW * 0.30f, centerY + baseH * 0.10f,
            centerX - baseW * 0.28f, centerY - baseH * 0.15f,
            centerX - baseW * 0.15f, centerY - baseH * 0.35f
        )
        close()
    }

    drawPath(
        path = highlightPath,
        color = Color(0x66FFFFFF),
        style = Fill
    )

    // 4. Mascot Facial Features
    drawCompanionFace(
        centerX = centerX,
        centerY = centerY + baseH * 0.08f
    )
}

/**
 * Draws the friendly expressive companion face.
 */
private fun DrawScope.drawCompanionFace(
    centerX: Float,
    centerY: Float
) {
    val eyeRadius = 18f
    val eyeSpacing = 42f
    val pupilGleamRadius = 6.5f

    // Left Eye
    drawCircle(
        color = Color(0xFF1B262C),
        radius = eyeRadius,
        center = Offset(centerX - eyeSpacing, centerY)
    )
    drawCircle(
        color = Color.White,
        radius = pupilGleamRadius,
        center = Offset(centerX - eyeSpacing - 5f, centerY - 5f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = pupilGleamRadius * 0.45f,
        center = Offset(centerX - eyeSpacing + 6f, centerY + 6f)
    )

    // Right Eye
    drawCircle(
        color = Color(0xFF1B262C),
        radius = eyeRadius,
        center = Offset(centerX + eyeSpacing, centerY)
    )
    drawCircle(
        color = Color.White,
        radius = pupilGleamRadius,
        center = Offset(centerX + eyeSpacing - 5f, centerY - 5f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = pupilGleamRadius * 0.45f,
        center = Offset(centerX + eyeSpacing + 6f, centerY + 6f)
    )

    // Rosy Cheeks
    drawCircle(
        color = Color(0x44FF85A1),
        radius = 14f,
        center = Offset(centerX - eyeSpacing - 14f, centerY + 18f)
    )
    drawCircle(
        color = Color(0x44FF85A1),
        radius = 14f,
        center = Offset(centerX + eyeSpacing + 14f, centerY + 18f)
    )

    // Cute Smile Arc
    val smilePath = Path().apply {
        moveTo(centerX - 16f, centerY + 14f)
        cubicTo(
            centerX - 8f, centerY + 28f,
            centerX + 8f, centerY + 28f,
            centerX + 16f, centerY + 14f
        )
    }
    drawPath(
        path = smilePath,
        color = Color(0xFF1B262C),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 5.5f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    )
}
