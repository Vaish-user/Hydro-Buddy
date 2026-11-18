package com.hydrobuddy.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.model.AnimationSpeed
import com.hydrobuddy.app.data.model.CharacterSize
import com.hydrobuddy.app.ui.theme.BubbleShape
import com.hydrobuddy.app.ui.theme.MintPastelSecondary
import com.hydrobuddy.app.ui.theme.MintTealDark
import com.hydrobuddy.app.ui.theme.MintTealPrimary
import com.hydrobuddy.app.ui.theme.PureWhiteSurface
import com.hydrobuddy.app.ui.theme.SoftCoralAccent
import com.hydrobuddy.app.ui.theme.SpruceTextPrimary

enum class MascotMood {
    HAPPY,
    EXCITED,
    THIRSTY,
    WAVING
}

@Composable
fun MascotIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    characterSize: CharacterSize = CharacterSize.MEDIUM,
    animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    mood: MascotMood = MascotMood.HAPPY,
    speechBubbleText: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_breathing")
    val duration = animationSpeed.durationMs

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )

    val eyeSquint by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration * 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eye_squint"
    )

    val actualSize = size * characterSize.scaleFactor

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!speechBubbleText.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(PureWhiteSurface, BubbleShape)
                    .border(1.5.dp, MintPastelSecondary, BubbleShape)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = speechBubbleText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SpruceTextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Box(
            modifier = Modifier
                .size(actualSize)
                .offset(y = floatOffset.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(actualSize)) {
                drawDeweyMascot(
                    mood = mood,
                    eyeSquint = eyeSquint
                )
            }
        }
    }
}

private fun DrawScope.drawDeweyMascot(
    mood: MascotMood,
    eyeSquint: Float
) {
    val width = size.width
    val height = size.height

    // 1. Soft droplet drop shadow (elliptical bottom shadow)
    drawOval(
        color = Color(0x1F253B36),
        topLeft = Offset(width * 0.2f, height * 0.88f),
        size = Size(width * 0.6f, height * 0.1f)
    )

    // 2. Main Water Droplet Body
    val bodyPath = Path().apply {
        // Top tip of water droplet
        moveTo(width * 0.5f, height * 0.14f)
        // Right shoulder curve
        cubicTo(
            width * 0.76f, height * 0.38f,
            width * 0.88f, height * 0.62f,
            width * 0.86f, height * 0.74f
        )
        // Bottom curve
        cubicTo(
            width * 0.84f, height * 0.92f,
            width * 0.16f, height * 0.92f,
            width * 0.14f, height * 0.74f
        )
        // Left shoulder curve
        cubicTo(
            width * 0.12f, height * 0.62f,
            width * 0.24f, height * 0.38f,
            width * 0.5f, height * 0.14f
        )
        close()
    }

    // Gradient fill: smooth pastel mint to teal
    drawPath(
        path = bodyPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                MintPastelSecondary,
                MintTealPrimary,
                MintTealDark
            ),
            startY = height * 0.1f,
            endY = height * 0.95f
        )
    )

    // Subtle outline
    drawPath(
        path = bodyPath,
        color = PureWhiteSurface.copy(alpha = 0.5f),
        style = Stroke(width = width * 0.02f)
    )

    // 3. Sprout on top
    val sproutPath = Path().apply {
        moveTo(width * 0.5f, height * 0.14f)
        cubicTo(
            width * 0.55f, height * 0.04f,
            width * 0.68f, height * 0.06f,
            width * 0.66f, height * 0.14f
        )
        cubicTo(
            width * 0.62f, height * 0.18f,
            width * 0.53f, height * 0.16f,
            width * 0.5f, height * 0.14f
        )
        close()
    }
    drawPath(
        path = sproutPath,
        color = Color(0xFF98DE8A)
    )
    drawPath(
        path = sproutPath,
        color = Color(0xFF67B958),
        style = Stroke(width = width * 0.012f)
    )

    // 4. Highlight sheen on top-left of droplet
    val sheenPath = Path().apply {
        moveTo(width * 0.38f, height * 0.34f)
        cubicTo(
            width * 0.30f, height * 0.42f,
            width * 0.24f, height * 0.54f,
            width * 0.26f, height * 0.68f
        )
        cubicTo(
            width * 0.23f, height * 0.58f,
            width * 0.26f, height * 0.44f,
            width * 0.36f, height * 0.32f
        )
        close()
    }
    drawPath(
        path = sheenPath,
        color = PureWhiteSurface.copy(alpha = 0.55f)
    )

    // Small shine spot
    drawCircle(
        color = PureWhiteSurface.copy(alpha = 0.75f),
        radius = width * 0.025f,
        center = Offset(width * 0.32f, height * 0.37f)
    )

    // 5. Cute Eyes
    val eyeY = height * 0.56f
    val leftEyeX = width * 0.41f
    val rightEyeX = width * 0.59f
    val eyeRadius = width * 0.045f

    when (mood) {
        MascotMood.EXCITED -> {
            // Cute happy arched eyes (^^)
            val leftArch = Path().apply {
                moveTo(leftEyeX - eyeRadius, eyeY)
                quadraticTo(leftEyeX, eyeY - eyeRadius * 1.5f, leftEyeX + eyeRadius, eyeY)
            }
            val rightArch = Path().apply {
                moveTo(rightEyeX - eyeRadius, eyeY)
                quadraticTo(rightEyeX, eyeY - eyeRadius * 1.5f, rightEyeX + eyeRadius, eyeY)
            }
            drawPath(leftArch, SpruceTextPrimary, style = Stroke(width = width * 0.03f, cap = StrokeCap.Round))
            drawPath(rightArch, SpruceTextPrimary, style = Stroke(width = width * 0.03f, cap = StrokeCap.Round))
        }
        else -> {
            // Round cute sparkling eyes
            drawCircle(
                color = SpruceTextPrimary,
                radius = eyeRadius,
                center = Offset(leftEyeX, eyeY)
            )
            drawCircle(
                color = SpruceTextPrimary,
                radius = eyeRadius,
                center = Offset(rightEyeX, eyeY)
            )
            // Left eye twinkle
            drawCircle(
                color = PureWhiteSurface,
                radius = eyeRadius * 0.42f,
                center = Offset(leftEyeX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f)
            )
            // Right eye twinkle
            drawCircle(
                color = PureWhiteSurface,
                radius = eyeRadius * 0.42f,
                center = Offset(rightEyeX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f)
            )
        }
    }

    // 6. Rosy Cheeks
    val cheekY = height * 0.63f
    val cheekRadiusX = width * 0.055f
    val cheekRadiusY = height * 0.035f

    drawOval(
        color = SoftCoralAccent.copy(alpha = 0.7f),
        topLeft = Offset(width * 0.27f, cheekY - cheekRadiusY),
        size = Size(cheekRadiusX * 2, cheekRadiusY * 2)
    )
    drawOval(
        color = SoftCoralAccent.copy(alpha = 0.7f),
        topLeft = Offset(width * 0.62f, cheekY - cheekRadiusY),
        size = Size(cheekRadiusX * 2, cheekRadiusY * 2)
    )

    // 7. Sweet Smile
    val smilePath = Path().apply {
        moveTo(width * 0.46f, height * 0.64f)
        quadraticTo(
            width * 0.50f,
            height * 0.71f,
            width * 0.54f,
            height * 0.64f
        )
    }
    drawPath(
        path = smilePath,
        color = SpruceTextPrimary,
        style = Stroke(
            width = width * 0.025f,
            cap = StrokeCap.Round
        )
    )
}

