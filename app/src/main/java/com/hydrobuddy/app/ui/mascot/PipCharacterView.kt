package com.hydrobuddy.app.ui.mascot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrobuddy.app.data.model.AnimationSpeed
import com.hydrobuddy.app.data.model.CharacterSize
import com.hydrobuddy.app.ui.theme.BubbleShape
import com.hydrobuddy.app.ui.theme.MintPastelSecondary
import com.hydrobuddy.app.ui.theme.PureWhiteSurface
import com.hydrobuddy.app.ui.theme.SpruceTextPrimary
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlinx.coroutines.delay

/**
 * Flagship interactive companion view for Pip.
 * Supports both Production Pip (models/pip.glb) and Pip V2 (models/pip_v2.glb)
 * with independent facial expressions, modular equipment toggling, and natural blinking.
 */
@Composable
fun PipCharacterView(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    characterSize: CharacterSize = CharacterSize.MEDIUM,
    animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    animationState: PipAnimationState = PipAnimationState.A_IDLE,
    loopOverride: Boolean? = null,
    autoResetToIdle: Boolean = true,
    speechBubbleText: String? = null,
    prefer3D: Boolean = true,
    modelAssetPath: String = "models/pip.glb",
    expression: PipExpression = PipExpression.NEUTRAL,
    expressionIntensity: Float = 1.0f,
    enableBlinking: Boolean = true,
    equippedAccessories: Set<PipAccessory> = emptySet(),
    animationSpeedMultiplier: Float = 1.0f,
    scaleMultiplier: Float = 1.0f,
    rotationY: Float = 0f,
    rotationX: Float = 0f,
    onClick: (() -> Unit)? = null,
    onAnimationFinished: (() -> Unit)? = null
) {
    var has3DError by remember { mutableStateOf(!prefer3D) }
    var currentAnimState by remember(animationState) { mutableStateOf(animationState) }

    // If an action animation was triggered (non-looping), automatically return to IDLE after playback if autoResetToIdle is true
    LaunchedEffect(currentAnimState, autoResetToIdle, loopOverride) {
        if (autoResetToIdle && !currentAnimState.isLooping && loopOverride != true) {
            val duration = currentAnimState.defaultDurationMs
            val scaledDuration = when (animationSpeed) {
                AnimationSpeed.GENTLE -> (duration * 1.4f).toLong()
                AnimationSpeed.NORMAL -> duration
                AnimationSpeed.BOUNCY -> (duration * 0.75f).toLong()
            }
            delay(scaledDuration)
            onAnimationFinished?.invoke()
            currentAnimState = PipAnimationState.A_IDLE
        }
    }

    val actualSize = size * characterSize.scaleFactor * scaleMultiplier

    Column(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Bubble
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

        // Mascot Render Box
        Box(
            modifier = Modifier.size(actualSize),
            contentAlignment = Alignment.Center
        ) {
            if (!has3DError) {
                Pip3DView(
                    actualSize = actualSize,
                    animationState = currentAnimState,
                    loopOverride = loopOverride,
                    modelAssetPath = modelAssetPath,
                    expression = expression,
                    expressionIntensity = expressionIntensity,
                    enableBlinking = enableBlinking,
                    equippedAccessories = equippedAccessories,
                    animationSpeedMultiplier = animationSpeedMultiplier,
                    rotationY = rotationY,
                    rotationX = rotationX,
                    onError = { has3DError = true }
                )
            } else {
                PipSpriteView(
                    size = size,
                    characterSize = characterSize,
                    animationSpeed = animationSpeed,
                    animationState = currentAnimState,
                    loopOverride = loopOverride
                )
            }
        }
    }
}

@Composable
private fun Pip3DView(
    actualSize: Dp,
    animationState: PipAnimationState,
    loopOverride: Boolean? = null,
    modelAssetPath: String = "models/pip.glb",
    expression: PipExpression = PipExpression.NEUTRAL,
    expressionIntensity: Float = 1.0f,
    enableBlinking: Boolean = true,
    equippedAccessories: Set<PipAccessory> = emptySet(),
    animationSpeedMultiplier: Float = 1.0f,
    rotationY: Float = 0f,
    rotationX: Float = 0f,
    onError: () -> Unit
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val coroutineScope = rememberCoroutineScope()

    val cameraNode = rememberCameraNode(engine).apply {
        // Front-facing, framed on Pip's full body
        position = Float3(x = 0f, y = 0.50f, z = 3.2f)
        lookAt(Float3(x = 0f, y = 0.50f, z = 0f))
    }
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 90_000.0f
    }

    var modelNode by remember { mutableStateOf<ModelNode?>(null) }
    var expressionController by remember { mutableStateOf<PipExpressionController?>(null) }
    var equipmentManager by remember { mutableStateOf<PipEquipmentManager?>(null) }

    LaunchedEffect(engine, modelLoader, modelAssetPath) {
        try {
            val instance = modelLoader.createModelInstance(modelAssetPath)
            val node = ModelNode(
                modelInstance = instance,
                scaleToUnits = 1.0f
            ).apply {
                position = Float3(x = 0f, y = 0f, z = 0f)
                rotation = Float3(x = 0f, y = 0f, z = 0f)
            }
            modelNode = node

            // Initialize controllers if running Pip asset
            if (modelAssetPath.contains("pip")) {
                val exprCtrl = PipExpressionController(engine, instance, coroutineScope)
                exprCtrl.setExpression(expression, expressionIntensity, transitionDurationMs = 0L)
                exprCtrl.setBlinkingEnabled(enableBlinking)
                expressionController = exprCtrl

                val equipMgr = PipEquipmentManager(engine, instance, equippedAccessories)
                equipmentManager = equipMgr
            } else {
                expressionController = null
                equipmentManager = null
            }
        } catch (e: Exception) {
            onError()
        }
    }

    // Reactively update 3D model rotation (yaw and pitch)
    LaunchedEffect(modelNode, rotationY, rotationX) {
        modelNode?.let { node ->
            node.rotation = Float3(x = rotationX, y = rotationY, z = 0f)
        }
    }

    // Clean up controllers when this view leaves composition
    DisposableEffect(Unit) {
        onDispose {
            expressionController?.dispose()
            equipmentManager?.dispose()
        }
    }

    // Reactively update expression and intensity
    LaunchedEffect(expressionController, expression, expressionIntensity) {
        expressionController?.setExpression(expression, expressionIntensity, transitionDurationMs = 180L)
    }

    // Reactively update blinking
    LaunchedEffect(expressionController, enableBlinking) {
        expressionController?.setBlinkingEnabled(enableBlinking)
    }

    // Reactively update equipment
    LaunchedEffect(equipmentManager, equippedAccessories) {
        equipmentManager?.setEquippedAccessories(equippedAccessories)
    }

    // Reactively update 3D animation clip when animationState or speed changes
    LaunchedEffect(modelNode, animationState, loopOverride, animationSpeedMultiplier) {
        val node = modelNode ?: return@LaunchedEffect
        // In SceneView 2.2.1, non-looping animators trigger a ConcurrentModificationException
        // inside ModelNode.applyAnimations() when animator.isDone causes a concurrent map mutation.
        // Keeping the 3D node animation looping ensures SceneView never attempts concurrent removal,
        // while our Compose state machines deterministically govern state duration and transitions.
        val shouldLoop = true
        val speed = animationSpeedMultiplier.coerceIn(0.25f, 3.0f)
        try {
            node.playAnimation(
                animationIndex = animationState.animationIndex,
                speed = speed,
                loop = shouldLoop
            )
        } catch (e: Exception) {
            // Fallback to name if index fails
            try {
                node.playAnimation(
                    animationName = animationState.clipName,
                    speed = speed,
                    loop = shouldLoop
                )
            } catch (ignored: Exception) {}
        }
    }

    val currentModelNode = modelNode
    if (currentModelNode != null) {
        Scene(
            modifier = Modifier.size(actualSize),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            cameraManipulator = null,
            mainLightNode = mainLightNode,
            isOpaque = false,
            childNodes = rememberNodes {
                add(currentModelNode)
            }
        )
    }
}
