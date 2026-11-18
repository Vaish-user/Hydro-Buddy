package com.hydrobuddy.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.ui.theme.MintSoftBackground

/**
 * Original Animated Splash Entrance for HydroBuddy.
 * Exact original choreography:
 * - Scene 1 (0.0s - 1.0s): Fullscreen ocean azure liquid screen with friendly animated face.
 * - Scene 2 (1.0s - 1.33s): Fluid surface tension collapse into droplet mascot.
 * - Scene 3 (1.33s - 2.0s): Fluid squash and elastic anticipation.
 * - Scene 4 (2.0s - 3.0s): Rebound stretch and crystalline settling. Full Pip character appears.
 * - Scene 5 (3.0s - 4.0s): Pip glides and settles into position.
 * - Scene 6 (4.0s - 5.27s): Continuous ambient breathing hover and ready state.
 *
 * Automatically navigates to HomeScreen upon completion.
 */
@Composable
fun AnimatedSplashScreen(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animTime = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Runs across 5.27 seconds (316 frames at 60fps)
        animTime.animateTo(
            targetValue = 5.27f,
            animationSpec = tween(durationMillis = 5270, easing = LinearEasing)
        )
        onAnimationComplete()
    }

    val t = animTime.value

    // Idle breathing transition for resting state (t > 4.0s)
    val infiniteTransition = rememberInfiniteTransition(label = "splash_idle_hover")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover_float"
    )

    // Calculate stage parameters based on time
    val (collapseFraction, squashStretchX, squashStretchY, dropletOffsetY, greetingAlpha, greetingOffsetY) = calculateAnimationParameters(t, hoverOffset)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MintSoftBackground)
            .clickable {
                // Allows tapping to skip directly to Home if desired
                onAnimationComplete()
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. Animated Canvas (Full Screen Water Morph & Droplet Character)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f + dropletOffsetY

            // A. Draw full-screen water collapse layer
            if (collapseFraction < 1.0f) {
                val currentW = canvasW * (1f - collapseFraction)
                val currentH = canvasH * (1f - collapseFraction)

                val left = (canvasW - currentW) / 2f
                val top = (canvasH - currentH) / 2f

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00B4D8), Color(0xFF0077B6))
                    ),
                    topLeft = Offset(left, top),
                    size = Size(currentW, currentH),
                    cornerRadius = CornerRadius(collapseFraction * 200f, collapseFraction * 200f)
                )
            }

            // B. Draw Water Droplet Mascot
            if (t >= 0.8f) {
                val mascotScale = if (t < 1.33f) {
                    ((t - 0.8f) / 0.53f).coerceIn(0f, 1f)
                } else 1f

                drawWaterDropletMascot(
                    centerX = centerX,
                    centerY = centerY,
                    scaleX = squashStretchX * mascotScale,
                    scaleY = squashStretchY * mascotScale
                )
            } else {
                // When full screen is displayed, draw the face directly at center
                drawMascotFace(
                    centerX = centerX,
                    centerY = centerY,
                    scale = 1f
                )
            }
        }

        // 2. Final Entrance Greeting ("Welcome to HydroBuddy!")
        if (greetingAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Welcome to HydroBuddy!",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                        letterSpacing = (-0.3).sp,
                        color = Color(0xFF142B42),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .offset(y = (68 + greetingOffsetY).dp)
                        .alpha(greetingAlpha)
                )
            }
        }
    }
}

/**
 * Draws the glossy, fluid water droplet character with specular highlights.
 */
private fun DrawScope.drawWaterDropletMascot(
    centerX: Float,
    centerY: Float,
    scaleX: Float,
    scaleY: Float
) {
    val baseW = 190f * scaleX
    val baseH = 210f * scaleY

    // 1. Soft droplet ambient contact shadow
    drawOval(
        color = Color(0x221A332E),
        topLeft = Offset(centerX - baseW * 0.45f, centerY + baseH * 0.42f),
        size = Size(baseW * 0.9f, 26f * scaleX)
    )

    // 2. Teardrop body path
    val dropletPath = Path().apply {
        // Top peak tip
        moveTo(centerX, centerY - baseH * 0.58f)
        // Right flank curve
        cubicTo(
            centerX + baseW * 0.25f, centerY - baseH * 0.28f,
            centerX + baseW * 0.58f, centerY + baseH * 0.12f,
            centerX + baseW * 0.52f, centerY + baseH * 0.38f
        )
        // Bottom belly curve
        cubicTo(
            centerX + baseW * 0.45f, centerY + baseH * 0.56f,
            centerX - baseW * 0.45f, centerY + baseH * 0.56f,
            centerX - baseW * 0.52f, centerY + baseH * 0.38f
        )
        // Left flank curve
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

    // 3. Specular water gloss highlight (crescent along upper-left shoulder)
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
    drawMascotFace(
        centerX = centerX,
        centerY = centerY + baseH * 0.08f,
        scale = scaleX
    )
}

/**
 * Draws the friendly expressive mascot face matching the reference animation.
 */
private fun DrawScope.drawMascotFace(
    centerX: Float,
    centerY: Float,
    scale: Float
) {
    val eyeRadius = 18f * scale
    val eyeSpacing = 42f * scale
    val pupilGleamRadius = 6.5f * scale

    // Left Eye
    drawCircle(
        color = Color(0xFF1B262C),
        radius = eyeRadius,
        center = Offset(centerX - eyeSpacing, centerY)
    )
    drawCircle(
        color = Color.White,
        radius = pupilGleamRadius,
        center = Offset(centerX - eyeSpacing - 5f * scale, centerY - 5f * scale)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = pupilGleamRadius * 0.45f,
        center = Offset(centerX - eyeSpacing + 6f * scale, centerY + 6f * scale)
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
        center = Offset(centerX + eyeSpacing - 5f * scale, centerY - 5f * scale)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = pupilGleamRadius * 0.45f,
        center = Offset(centerX + eyeSpacing + 6f * scale, centerY + 6f * scale)
    )

    // Rosy Cheeks
    drawCircle(
        color = Color(0x44FF85A1),
        radius = 14f * scale,
        center = Offset(centerX - eyeSpacing - 14f * scale, centerY + 18f * scale)
    )
    drawCircle(
        color = Color(0x44FF85A1),
        radius = 14f * scale,
        center = Offset(centerX + eyeSpacing + 14f * scale, centerY + 18f * scale)
    )

    // Cute Smile Arc
    val smilePath = Path().apply {
        moveTo(centerX - 16f * scale, centerY + 14f * scale)
        cubicTo(
            centerX - 8f * scale, centerY + 28f * scale,
            centerX + 8f * scale, centerY + 28f * scale,
            centerX + 16f * scale, centerY + 14f * scale
        )
    }
    drawPath(
        path = smilePath,
        color = Color(0xFF1B262C),
        style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round)
    )
}

/**
 * Calculates smooth frame-by-frame interpolation parameters.
 */
private data class AnimationParams(
    val collapseFraction: Float,
    val squashStretchX: Float,
    val squashStretchY: Float,
    val dropletOffsetY: Float,
    val greetingAlpha: Float,
    val greetingOffsetY: Float
)

private fun calculateAnimationParameters(t: Float, hoverOffset: Float): AnimationParams {
    // 1. Collapse Fraction: 0.0s - 1.0s = 0, 1.0s - 1.33s = 0 -> 1
    val collapse = when {
        t < 1.0f -> 0f
        t < 1.33f -> ((t - 1.0f) / 0.33f).coerceIn(0f, 1f)
        else -> 1f
    }

    // 2. Squash & Stretch:
    // 1.33s - 1.66s: Squash (scaleX: 1.0 -> 1.25, scaleY: 1.0 -> 0.75)
    // 1.66s - 2.33s: Rebound (scaleX: 1.25 -> 0.88, scaleY: 0.75 -> 1.25)
    // 2.33s - 3.00s: Settle back to (1.0, 1.0)
    val (sx, sy) = when {
        t < 1.33f -> Pair(1.0f, 1.0f)
        t < 1.66f -> {
            val p = (t - 1.33f) / 0.33f
            Pair(1.0f + 0.25f * p, 1.0f - 0.25f * p)
        }
        t < 2.33f -> {
            val p = (t - 1.66f) / 0.67f
            Pair(1.25f - 0.37f * p, 0.75f + 0.50f * p)
        }
        t < 3.0f -> {
            val p = (t - 2.33f) / 0.67f
            Pair(0.88f + 0.12f * p, 1.25f - 0.25f * p)
        }
        else -> Pair(1.0f, 1.0f)
    }

    // 3. Droplet vertical offset:
    // 3.0s - 4.0s: Floats upward (offset: 0 -> -160px)
    // > 4.0s: hoverOffset applied
    val offsetY = when {
        t < 3.0f -> 0f
        t < 4.0f -> {
            val p = ((t - 3.0f) / 1.0f).coerceIn(0f, 1f)
            val eased = FastOutSlowInEasing.transform(p)
            -160f * eased
        }
        else -> -160f + hoverOffset * 3f
    }

    // 4. Final Entrance Greeting ("Welcome to HydroBuddy!"):
    // Synchronized with Pip's upward movement (t >= 3.0s)
    val (gAlpha, gOffsetY) = when {
        t < 3.0f -> Pair(0f, 20f)
        t < 3.8f -> {
            val p = ((t - 3.0f) / 0.8f).coerceIn(0f, 1f)
            val eased = FastOutSlowInEasing.transform(p)
            Pair(eased, 20f * (1f - eased))
        }
        else -> Pair(1f, 0f)
    }

    return AnimationParams(
        collapseFraction = collapse,
        squashStretchX = sx,
        squashStretchY = sy,
        dropletOffsetY = offsetY,
        greetingAlpha = gAlpha,
        greetingOffsetY = gOffsetY
    )
}
