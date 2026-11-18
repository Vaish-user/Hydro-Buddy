package com.hydrobuddy.app.ui.overlay

import android.graphics.Rect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.datastore.HydroDataStore
import com.hydrobuddy.app.data.model.AppearanceSettings
import com.hydrobuddy.app.data.model.CharacterCustomization
import com.hydrobuddy.app.data.model.CharacterSize
import com.hydrobuddy.app.data.model.PipPersonality
import com.hydrobuddy.app.ui.mascot.ConfettiCanvas
import com.hydrobuddy.app.ui.mascot.PIP_WALK_SPEED_FACTOR
import com.hydrobuddy.app.ui.mascot.PipAccessory
import com.hydrobuddy.app.ui.mascot.PipAnimationState
import com.hydrobuddy.app.ui.mascot.PipCharacterView
import com.hydrobuddy.app.ui.mascot.PipExpression
import com.hydrobuddy.app.ui.mascot.PipInteractionState
import com.hydrobuddy.app.ui.mascot.SpeechBubbleShape
import com.hydrobuddy.app.ui.mascot.TypewriterText
import com.hydrobuddy.app.ui.theme.MidnightNavy
import com.hydrobuddy.app.ui.theme.MintPastelSecondary
import com.hydrobuddy.app.ui.theme.MintTealDark
import com.hydrobuddy.app.ui.theme.MintTealPrimary
import com.hydrobuddy.app.ui.theme.PillShape
import com.hydrobuddy.app.ui.theme.PureWhiteSurface
import com.hydrobuddy.app.ui.theme.SageTextSecondary
import com.hydrobuddy.app.ui.theme.SoftMintContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Standalone floating Pip overlay rendered directly into WindowManager.
 *
 * Full theatrical performance flow:
 * 1. ENTER: Pip walks in from screen edge holding the glass, stops at center, settles to idle, waits 0.3s.
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
fun FloatingPipView(
    characterCustomization: CharacterCustomization,
    appearanceSettings: AppearanceSettings,
    dataStore: HydroDataStore,
    onBoundsChanged: (List<Rect>) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current

    var interactionState by remember { mutableStateOf(PipInteractionState.WALKING_IN) }
    var activePipAnimation by remember { mutableStateOf(PipAnimationState.B_WALKING_IN) }
    var activeExpression by remember { mutableStateOf(PipExpression.NEUTRAL) }
    var equippedAccessories by remember { mutableStateOf<Set<PipAccessory>>(setOf(PipAccessory.WATER_GLASS)) }
    var walkSpeedMultiplier by remember { mutableStateOf(1.0f) }

    var isDialogueVisible by remember { mutableStateOf(false) }
    var bubbleMessage by remember { mutableStateOf("") }
    var showActionButtons by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    val speechBubbleShape = remember { SpeechBubbleShape(cornerRadius = 20.dp, tailWidth = 16.dp, tailHeight = 10.dp) }

    // Hit-testing rects
    var cardBounds by remember { mutableStateOf<Rect?>(null) }
    var pipBounds by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(cardBounds, pipBounds) {
        val list = mutableListOf<Rect>()
        cardBounds?.let { list.add(it) }
        pipBounds?.let { list.add(it) }
        onBoundsChanged(list)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val offscreenRightPx = screenWidthPx * 1.05f
        val offscreenLeftPx = -screenWidthPx * 1.05f
        val scaledPipSize = (180.dp * appearanceSettings.effectiveScale).coerceIn(90.dp, 270.dp)
        val pipHeightPx = with(density) { scaledPipSize.toPx() }
        val baseWalkSpeedPxPerSec = pipHeightPx * PIP_WALK_SPEED_FACTOR

        val horizontalOffset = remember { Animatable(offscreenRightPx) }

        // State Machine Animation Orchestration
        LaunchedEffect(interactionState) {
            when (interactionState) {
                PipInteractionState.WALKING_IN -> {
                    // Step 1: ENTER
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

                    // Step 2: ASK
                    activePipAnimation = PipAnimationState.C_REMINDER
                    activeExpression = PipExpression.CURIOUS
                    bubbleMessage = characterCustomization.safeReminder
                    showActionButtons = true
                    isDialogueVisible = true
                    interactionState = PipInteractionState.IDLE_REMINDER
                }

                PipInteractionState.IDLE_REMINDER -> {
                    activeExpression = PipExpression.CURIOUS
                    equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                    bubbleMessage = characterCustomization.safeReminder
                    showActionButtons = true
                    isDialogueVisible = true
                    // Auto-dismiss after 16 seconds if untouched
                    delay(16000)
                    if (interactionState == PipInteractionState.IDLE_REMINDER) {
                        interactionState = PipInteractionState.WALKING_OUT
                    }
                }

                PipInteractionState.HAPPY_RESPONSE -> {
                    // Step 3: YES - Log intake and Bubble disappears, plays G_DRINKING
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            dataStore.logIntake(250, "Glass of water")
                        } catch (ignored: Exception) {}
                    }
                    showActionButtons = false
                    isDialogueVisible = false
                    cardBounds = null
                    activeExpression = PipExpression.HAPPY
                    equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                    activePipAnimation = PipAnimationState.G_DRINKING
                    delay(2400)

                    // Then plays F_CELEBRATING with confetti and encouraging message
                    activePipAnimation = PipAnimationState.F_CELEBRATING
                    showConfetti = true
                    bubbleMessage = characterCustomization.safeHappyEncouragement
                    isDialogueVisible = true
                    delay(2500)

                    showConfetti = false
                    isDialogueVisible = false
                    cardBounds = null
                    interactionState = PipInteractionState.WALKING_OUT
                }

                PipInteractionState.SAD_RESPONSE -> {
                    // Step 4: NO - Check personality
                    showActionButtons = false
                    when (characterCustomization.personality) {
                        PipPersonality.FRIENDLY -> {
                            equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                            activeExpression = PipExpression.NEUTRAL
                            activePipAnimation = PipAnimationState.A_IDLE
                            bubbleMessage = characterCustomization.safeNoResp
                            isDialogueVisible = true
                            delay(2000)
                            isDialogueVisible = false
                            cardBounds = null
                            walkSpeedMultiplier = 1.0f
                            interactionState = PipInteractionState.WALKING_OUT
                        }

                        PipPersonality.SAD -> {
                            equippedAccessories = setOf(PipAccessory.WATER_GLASS)
                            activeExpression = PipExpression.SAD
                            activePipAnimation = PipAnimationState.E_SAD
                            bubbleMessage = characterCustomization.safeNoResp
                            isDialogueVisible = true
                            delay(2500)
                            isDialogueVisible = false
                            cardBounds = null
                            walkSpeedMultiplier = 0.65f // Slow walk out
                            interactionState = PipInteractionState.WALKING_OUT
                        }

                        PipPersonality.RUDE -> {
                            equippedAccessories = setOf(PipAccessory.CARTOON_KNIFE)
                            activeExpression = PipExpression.ANGRY
                            activePipAnimation = PipAnimationState.E_SAD
                            bubbleMessage = characterCustomization.safeMischievousKnife
                            isDialogueVisible = true
                            delay(2800)
                            isDialogueVisible = false
                            cardBounds = null
                            walkSpeedMultiplier = 1.0f
                            interactionState = PipInteractionState.WALKING_OUT
                        }
                    }
                }

                PipInteractionState.WALKING_OUT -> {
                    isDialogueVisible = false
                    showActionButtons = false
                    showConfetti = false
                    cardBounds = null
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
                    interactionState = PipInteractionState.HIDDEN
                    onDismiss()
                }

                PipInteractionState.HIDDEN -> {
                    isDialogueVisible = false
                    cardBounds = null
                    onDismiss()
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
                enter = fadeIn(tween(250)) + scaleIn(tween(300)),
                exit = fadeOut(tween(200)) + scaleOut(tween(200))
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(min = 230.dp, max = 320.dp)
                        .shadow(16.dp, shape = speechBubbleShape)
                        .border(1.5.dp, MintPastelSecondary, shape = speechBubbleShape)
                        .onGloballyPositioned { coordinates ->
                            val b = coordinates.boundsInWindow()
                            cardBounds = Rect(b.left.toInt(), b.top.toInt(), b.right.toInt(), b.bottom.toInt())
                        },
                    shape = speechBubbleShape,
                    color = PureWhiteSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header with character badge & close button
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
                                onClick = { interactionState = PipInteractionState.WALKING_OUT },
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // Typewriter Animated Text Message
                        TypewriterText(
                            text = bubbleMessage,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MidnightNavy,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        // Yes / No interactive buttons during ASK phase
                        if (showActionButtons) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { interactionState = PipInteractionState.SAD_RESPONSE },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
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
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                Button(
                                    onClick = { interactionState = PipInteractionState.HAPPY_RESPONSE },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(40.dp),
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
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Animated Pip Mascot Container
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = horizontalOffset.value.roundToInt(), y = 0) }
                    .size(scaledPipSize)
                    .onGloballyPositioned { coordinates ->
                        val b = coordinates.boundsInWindow()
                        pipBounds = Rect(b.left.toInt(), b.top.toInt(), b.right.toInt(), b.bottom.toInt())
                    },
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
