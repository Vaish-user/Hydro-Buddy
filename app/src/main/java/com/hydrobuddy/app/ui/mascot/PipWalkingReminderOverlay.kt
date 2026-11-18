package com.hydrobuddy.app.ui.mascot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.model.CharacterSize
import com.hydrobuddy.app.data.model.PipPersonality
import com.hydrobuddy.app.ui.theme.MidnightNavy
import com.hydrobuddy.app.ui.theme.MintPastelSecondary
import com.hydrobuddy.app.ui.theme.MintTealDark
import com.hydrobuddy.app.ui.theme.MintTealPrimary
import com.hydrobuddy.app.ui.theme.PillShape
import com.hydrobuddy.app.ui.theme.PureWhiteSurface
import com.hydrobuddy.app.ui.theme.SageTextSecondary
import com.hydrobuddy.app.ui.theme.SoftMintContainer
import com.hydrobuddy.app.viewmodel.HydroViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Calibrated Pip walk speed factor relative to on-screen height:
 * Stride per step = 0.3 x height
 * Speed = stride * 2 steps / 1.2s ≈ 0.5 to 0.8 x height per second.
 * Tweakable constant.
 */
const val PIP_WALK_SPEED_FACTOR = 0.55f

/**
 * Speech bubble shape with a downward-pointing tail centered at the bottom,
 * aimed directly at Pip's head below. Single contiguous path for clean borders & shadows.
 */
class SpeechBubbleShape(
    private val cornerRadius: Dp = 20.dp,
    private val tailWidth: Dp = 16.dp,
    private val tailHeight: Dp = 10.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { cornerRadius.toPx() }
        val tw = with(density) { tailWidth.toPx() }
        val th = with(density) { tailHeight.toPx() }
        val w = size.width
        val h = (size.height - th).coerceAtLeast(r * 2)
        val midX = w / 2f

        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(w - r, 0f)
            arcTo(Rect(w - 2 * r, 0f, w, 2 * r), 270f, 90f, false)
            lineTo(w, h - r)
            arcTo(Rect(w - 2 * r, h - 2 * r, w, h), 0f, 90f, false)
            // Bottom edge with downward triangle tail pointing toward Pip
            lineTo(midX + tw / 2f, h)
            lineTo(midX, h + th)
            lineTo(midX - tw / 2f, h)
            lineTo(r, h)
            arcTo(Rect(0f, h - 2 * r, 2 * r, h), 90f, 90f, false)
            lineTo(0f, r)
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Animated typewriter text effect that reveals characters sequentially.
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    textAlign: TextAlign = TextAlign.Center,
    charDelayMs: Long = 26L,
    onComplete: (() -> Unit)? = null
) {
    var displayedLength by remember(text) { mutableStateOf(0) }

    LaunchedEffect(text) {
        displayedLength = 0
        for (i in 1..text.length) {
            displayedLength = i
            delay(charDelayMs)
        }
        onComplete?.invoke()
    }

    Text(
        text = text.take(displayedLength),
        style = style,
        textAlign = textAlign,
        modifier = modifier
    )
}

/**
 * Fluttering multi-color confetti particle burst for celebration.
 */
@Composable
fun ConfettiCanvas(
    modifier: Modifier = Modifier
) {
    val confettiColors = remember {
        listOf(
            Color(0xFF10B981),
            Color(0xFF38BDF8),
            Color(0xFFFBBF24),
            Color(0xFFF43F5E),
            Color(0xFFA855F7),
            Color(0xFF34D399)
        )
    }

    class Particle(
        val xRatio: Float,
        val startY: Float,
        val size: Float,
        val color: Color,
        val speedY: Float,
        val wobbleSpeed: Float,
        val rotationSpeed: Float
    )

    val particles = remember {
        List(42) {
            Particle(
                xRatio = Random.nextFloat(),
                startY = Random.nextFloat() * -250f,
                size = Random.nextFloat() * 8f + 6f,
                color = confettiColors.random(),
                speedY = Random.nextFloat() * 180f + 320f,
                wobbleSpeed = Random.nextFloat() * 4f + 2f,
                rotationSpeed = Random.nextFloat() * 360f + 160f
            )
        }
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier) {
        val t = progress.value
        val totalTimeSec = t * 2.5f
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { p ->
            val y = p.startY + p.speedY * totalTimeSec
            if (y in -50f..canvasHeight + 50f) {
                val x = (p.xRatio * canvasWidth) + (sin(totalTimeSec * p.wobbleSpeed) * 32f)
                val rot = (totalTimeSec * p.rotationSpeed) % 360f
                val alpha = (1f - (y / canvasHeight).coerceIn(0f, 1f)).coerceIn(0f, 1f)

                rotate(degrees = rot, pivot = Offset(x, y)) {
                    drawRoundRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - p.size / 2, y - p.size / 4),
                        size = Size(p.size, p.size * 0.6f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }
            }
        }
    }
}

/**
 * Interactive reminder overlay for Pip.
 *
 * Full theatrical performance flow:
 * 1. ENTER: Pip walks in from screen edge (B_WALKING_IN at calibrated speed) holding glass,
 *    stops at center, settles to idle (A_IDLE), waits 0.3s.
 * 2. ASK: Speech bubble appears ABOVE Pip's head (tail pointing down at him) saying
 *    "Did you drink water?" (typewriter text) with Yes / No buttons. Pip plays C_REMINDER with curious face.
 * 3. YES: Bubble disappears, Pip plays G_DRINKING (glass raised to beak) with happy closed-eye face,
 *    then F_CELEBRATING with confetti and encouraging message in the bubble, then H_WALKING_OUT off screen.
 * 4. NO:
 *    - Sad personality: plays E_SAD, sad face, sad message, walks out slowly.
 *    - Rude personality: shows knife (Prop_Knife), angry face, comedic threat message ("Drink up or else..."),
 *      then walks out brandishing knife.
 */
@Composable
fun PipWalkingReminderOverlay(
    viewModel: HydroViewModel,
    modifier: Modifier = Modifier
) {
    val interactionState by viewModel.pipInteractionState.collectAsState()
    val characterCustomization by viewModel.characterCustomization.collectAsState()
    val appearanceSettings by viewModel.appearanceSettings.collectAsState()

    if (interactionState == PipInteractionState.HIDDEN) {
        return
    }

    val density = LocalDensity.current

    // Internal choreography state variables
    var activePipAnimation by remember { mutableStateOf(PipAnimationState.B_WALKING_IN) }
    var activeExpression by remember { mutableStateOf(PipExpression.NEUTRAL) }
    var equippedAccessories by remember { mutableStateOf<Set<PipAccessory>>(setOf(PipAccessory.WATER_GLASS)) }
    var walkSpeedMultiplier by remember { mutableStateOf(1.0f) }

    var isDialogueVisible by remember { mutableStateOf(false) }
    var bubbleMessage by remember { mutableStateOf("") }
    var showActionButtons by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    val speechBubbleShape = remember { SpeechBubbleShape(cornerRadius = 20.dp, tailWidth = 16.dp, tailHeight = 10.dp) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.40f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // If user clicks outside while Pip is asking, treat as dismiss
                if (interactionState == PipInteractionState.IDLE_REMINDER) {
                    viewModel.dismissPipInteraction()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val offscreenRightPx = screenWidthPx * 0.95f
        val offscreenLeftPx = -screenWidthPx * 0.95f
        val scaledPipSize = (180.dp * appearanceSettings.effectiveScale).coerceIn(90.dp, 270.dp)
        val pipHeightPx = with(density) { scaledPipSize.toPx() }
        val baseWalkSpeedPxPerSec = pipHeightPx * PIP_WALK_SPEED_FACTOR

        val horizontalOffset = remember { Animatable(offscreenRightPx) }

        // State Machine Animation Orchestration
        LaunchedEffect(interactionState) {
            android.util.Log.d("PipWalkingOverlay", "interactionState changed to: $interactionState")
            when (interactionState) {
                PipInteractionState.HIDDEN -> {
                    horizontalOffset.snapTo(offscreenRightPx)
                    isDialogueVisible = false
                    showConfetti = false
                    showActionButtons = false
                    activeExpression = PipExpression.NEUTRAL
                    equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                }

                PipInteractionState.WALKING_IN -> {
                    // Step 1: ENTER - Pip walks in from edge holding the glass
                    isDialogueVisible = false
                    showConfetti = false
                    showActionButtons = false
                    activeExpression = PipExpression.NEUTRAL
                    equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                    walkSpeedMultiplier = 1.0f
                    activePipAnimation = PipAnimationState.B_WALKING_IN

                    horizontalOffset.snapTo(offscreenRightPx)
                    val walkInDurationMs = ((offscreenRightPx / baseWalkSpeedPxPerSec) * 1000f).roundToInt().coerceAtLeast(1200)
                    horizontalOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = walkInDurationMs,
                            easing = CubicBezierEasing(0.35f, 0.05f, 0.25f, 1.0f)
                        )
                    )

                    // Pip stops, settles to idle, waits 0.3s
                    activePipAnimation = PipAnimationState.A_IDLE
                    delay(300)

                    // Step 2: ASK - Curious face, speech bubble above head with Yes/No
                    activePipAnimation = PipAnimationState.C_REMINDER
                    activeExpression = PipExpression.CURIOUS
                    bubbleMessage = characterCustomization.safeReminder
                    showActionButtons = true
                    isDialogueVisible = true
                    viewModel.onPipReachedPosition()
                }

                PipInteractionState.IDLE_REMINDER -> {
                    // Settled asking state
                    activeExpression = PipExpression.CURIOUS
                    equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                    bubbleMessage = characterCustomization.safeReminder
                    showActionButtons = true
                    isDialogueVisible = true
                }

                PipInteractionState.HAPPY_RESPONSE -> {
                    // Step 3: YES - Bubble disappears, plays G_DRINKING (glass to beak) with happy closed-eye face
                    showActionButtons = false
                    isDialogueVisible = false
                    activeExpression = PipExpression.HAPPY
                    equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                    activePipAnimation = PipAnimationState.G_DRINKING
                    delay(2400)

                    // Then plays F_CELEBRATING with confetti and encouraging message in the bubble
                    activePipAnimation = PipAnimationState.F_CELEBRATING
                    showConfetti = true
                    bubbleMessage = characterCustomization.safeHappyEncouragement
                    isDialogueVisible = true
                    delay(2500)

                    // Then walks out
                    showConfetti = false
                    isDialogueVisible = false
                    viewModel.startPipWalkOut()
                }

                PipInteractionState.SAD_RESPONSE -> {
                    // Step 4: NO - Check personality
                    showActionButtons = false
                    when (characterCustomization.personality) {
                        PipPersonality.FRIENDLY -> {
                            // Friendly: polite acknowledgement, gentle walk out
                            equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                            activeExpression = PipExpression.NEUTRAL
                            activePipAnimation = PipAnimationState.A_IDLE
                            bubbleMessage = characterCustomization.safeNoResp
                            isDialogueVisible = true
                            delay(2000)
                            isDialogueVisible = false
                            walkSpeedMultiplier = 1.0f
                            viewModel.startPipWalkOut()
                        }

                        PipPersonality.SAD -> {
                            // Sad personality: E_SAD, sad face, sad message, walks out slowly
                            equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                            activeExpression = PipExpression.SAD
                            activePipAnimation = PipAnimationState.E_SAD
                            bubbleMessage = characterCustomization.safeNoResp
                            isDialogueVisible = true
                            delay(2500)
                            isDialogueVisible = false
                            walkSpeedMultiplier = 0.65f // Slow walk out
                            viewModel.startPipWalkOut()
                        }

                        PipPersonality.RUDE -> {
                            // Rude personality: show knife, angry face, comedic threat message, walks out
                            equippedAccessories = setOf(PipAccessory.CARTOON_KNIFE)
                            activeExpression = PipExpression.ANGRY
                            activePipAnimation = PipAnimationState.E_SAD
                            bubbleMessage = characterCustomization.safeMischievousKnife
                            isDialogueVisible = true
                            delay(2800)
                            isDialogueVisible = false
                            walkSpeedMultiplier = 1.0f
                            viewModel.startPipWalkOut()
                        }
                    }
                }

                PipInteractionState.WALKING_OUT -> {
                    // Walk off-screen
                    isDialogueVisible = false
                    showActionButtons = false
                    showConfetti = false
                    activePipAnimation = PipAnimationState.H_WALKING_OUT
                    val effectiveSpeed = baseWalkSpeedPxPerSec * walkSpeedMultiplier
                    val walkOutDistance = Math.abs(offscreenLeftPx - horizontalOffset.value)
                    val walkOutDurationMs = ((walkOutDistance / effectiveSpeed) * 1000f).roundToInt().coerceAtLeast(1200)

                    horizontalOffset.animateTo(
                        targetValue = offscreenLeftPx,
                        animationSpec = tween(
                            durationMillis = walkOutDurationMs,
                            easing = CubicBezierEasing(0.35f, 0.05f, 0.25f, 1.0f)
                        )
                    )
                    viewModel.onPipWalkOutFinished()
                }
            }
        }

        // Full screen Confetti Overlay during celebration
        if (showConfetti) {
            ConfettiCanvas(modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Speech Bubble placed directly ABOVE Pip's head with downward tail
            AnimatedVisibility(
                visible = isDialogueVisible &&
                        interactionState != PipInteractionState.WALKING_IN &&
                        interactionState != PipInteractionState.WALKING_OUT &&
                        interactionState != PipInteractionState.HIDDEN,
                enter = fadeIn(tween(280)) + scaleIn(tween(300)),
                exit = fadeOut(tween(220)) + scaleOut(tween(220))
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(min = 240.dp, max = 340.dp)
                        .shadow(12.dp, shape = speechBubbleShape)
                        .border(1.5.dp, MintPastelSecondary, shape = speechBubbleShape),
                    shape = speechBubbleShape,
                    color = PureWhiteSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header with character badge & dismiss button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(SoftMintContainer)
                                    .border(1.dp, MintPastelSecondary, PillShape)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.WaterDrop,
                                        contentDescription = null,
                                        tint = MintTealPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = characterCustomization.safeName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MintTealDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.dismissPipInteraction() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = SageTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Typewriter Animated Text Message
                        TypewriterText(
                            text = bubbleMessage,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 15.5.sp,
                                lineHeight = 21.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        // Yes / No interactive buttons during ASK phase
                        if (showActionButtons) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.onUserReminderResponse(false) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    shape = PillShape,
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        width = 1.dp,
                                        brush = androidx.compose.ui.graphics.SolidColor(MintPastelSecondary)
                                    )
                                ) {
                                    Text(
                                        text = characterCustomization.safeNoBtn,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = SageTextSecondary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    )
                                }

                                Button(
                                    onClick = { viewModel.onUserReminderResponse(true) },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(42.dp),
                                    shape = PillShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MintTealPrimary
                                    )
                                ) {
                                    Text(
                                        text = characterCustomization.safeYesBtn,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = PureWhiteSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Pip Mascot Container (moves horizontally across screen)
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = horizontalOffset.value.roundToInt(), y = 0) }
                    .size(scaledPipSize),
                contentAlignment = Alignment.Center
            ) {
                PipCharacterView(
                    size = scaledPipSize,
                    characterSize = CharacterSize.MEDIUM,
                    scaleMultiplier = 1.0f,
                    animationSpeed = appearanceSettings.animationSpeed,
                    animationState = activePipAnimation,
                    expression = activeExpression,
                    equippedAccessories = equippedAccessories,
                    loopOverride = if (interactionState.isWalking) true else null,
                    autoResetToIdle = !interactionState.isWalking,
                    prefer3D = true,
                    modelAssetPath = "models/pip.glb"
                )
            }
        }
    }
}
