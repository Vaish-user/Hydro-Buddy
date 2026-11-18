package com.hydrobuddy.app.ui.mascot

import android.util.Log
import com.google.android.filament.Engine
import com.google.android.filament.RenderableManager
import com.google.android.filament.gltfio.FilamentAsset
import io.github.sceneview.model.ModelInstance
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 9 Distinct Emotional Expressions for Pip V2.
 */
enum class PipExpression(val displayName: String) {
    NEUTRAL("Neutral"),
    HAPPY("Happy"),
    SAD("Sad"),
    ANGRY("Angry"),
    CURIOUS("Curious"),
    SLEEPY("Sleepy"),
    SURPRISED("Surprised"),
    MISCHIEVOUS("Mischievous"),
    PROUD("Proud")
}

/**
 * Controller for runtime morph-target facial expressions and natural blinking in Pip V2.
 * Directly drives Google Filament's RenderableManager without blocking the render thread.
 */
class PipExpressionController(
    private val engine: Engine,
    private val modelInstance: ModelInstance,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "PipExprController"

        // Verified morph target names from pip_v2.glb
        const val EYE_BLINK_L = "Eye_Blink_L"
        const val EYE_BLINK_R = "Eye_Blink_R"
        const val EYE_SQUINT_L = "Eye_Squint_L"
        const val EYE_SQUINT_R = "Eye_Squint_R"
        const val EYE_SMILE_L = "Eye_Smile_L"
        const val EYE_SMILE_R = "Eye_Smile_R"
        const val BROW_ANGRY_L = "Brow_Angry_L"
        const val BROW_ANGRY_R = "Brow_Angry_R"
        const val BROW_SURPRISED_L = "Brow_Surprised_L"
        const val BROW_SURPRISED_R = "Brow_Surprised_R"
        const val MOUTH_OPEN_WIDE = "Mouth_Open_Wide"
        const val MOUTH_SMIRK_R = "Mouth_Smirk_R"
        const val SMILE_HAPPY = "Smile_Happy"
        const val POUT_SAD = "Pout_Sad"
        const val MOUTH_OPEN = "Mouth_Open"
        const val EYES_CRESCENT_HAPPY = "Eyes_Crescent_Happy"
    }

    private data class MorphEntity(
        val entity: Int,
        val nodeName: String,
        val rInstance: Int,
        val targetMap: Map<String, Int>,
        val targetCount: Int
    )

    private val rm: RenderableManager = engine.renderableManager
    private val morphEntities = mutableListOf<MorphEntity>()

    // Current expression state
    private val _currentExpression = MutableStateFlow(PipExpression.NEUTRAL)
    val currentExpression: StateFlow<PipExpression> = _currentExpression.asStateFlow()

    private val _expressionIntensity = MutableStateFlow(1.0f)
    val expressionIntensity: StateFlow<Float> = _expressionIntensity.asStateFlow()

    private val _isBlinkingEnabled = MutableStateFlow(true)
    val isBlinkingEnabled: StateFlow<Boolean> = _isBlinkingEnabled.asStateFlow()

    // Active morph target weights (targetName -> currentWeight)
    private val activeWeights = mutableMapOf<String, Float>()
    private var blinkWeight = 0.0f

    private var transitionJob: Job? = null
    private var blinkJob: Job? = null
    private var isDisposed = false

    init {
        discoverMorphEntities()
        startBlinkingRoutine()
    }

    /**
     * Inspects the Filament model instance and maps all renderables with morph targets.
     */
    private fun discoverMorphEntities() {
        val asset: FilamentAsset = modelInstance.asset
        val entities: IntArray = modelInstance.entities

        for (entity in entities) {
            val nodeName = asset.getName(entity) ?: continue
            if (!rm.hasComponent(entity)) continue

            val rInstance = rm.getInstance(entity)
            val targetCount = rm.getMorphTargetCount(rInstance)
            if (targetCount <= 0) continue

            // Build deterministic target index mapping based on verified GLB order
            val targetMap = mutableMapOf<String, Int>()
            when (nodeName) {
                "Pip_Body" -> {
                    targetMap[SMILE_HAPPY] = 0
                    targetMap[POUT_SAD] = 1
                    targetMap[MOUTH_OPEN_WIDE] = 2
                    targetMap[MOUTH_SMIRK_R] = 3
                }
                "Pip_Eye_L" -> {
                    targetMap[EYES_CRESCENT_HAPPY] = 0
                    targetMap[EYE_BLINK_L] = 1
                    targetMap[EYE_SQUINT_L] = 2
                    targetMap[EYE_SMILE_L] = 3
                }
                "Pip_Eye_R" -> {
                    targetMap[EYES_CRESCENT_HAPPY] = 0
                    targetMap[EYE_BLINK_R] = 1
                    targetMap[EYE_SQUINT_R] = 2
                    targetMap[EYE_SMILE_R] = 3
                }
                "Pip_Brow_L" -> {
                    targetMap[BROW_ANGRY_L] = 0
                    targetMap[BROW_SURPRISED_L] = 1
                }
                "Pip_Brow_R" -> {
                    targetMap[BROW_ANGRY_R] = 0
                    targetMap[BROW_SURPRISED_R] = 1
                }
                "Pip_Beak_Lower" -> {
                    targetMap[MOUTH_OPEN] = 0
                    targetMap[MOUTH_OPEN_WIDE] = 1
                    targetMap[MOUTH_SMIRK_R] = 2
                }
            }

            morphEntities.add(
                MorphEntity(
                    entity = entity,
                    nodeName = nodeName,
                    rInstance = rInstance,
                    targetMap = targetMap,
                    targetCount = targetCount
                )
            )
            Log.d(TAG, "Discovered morph entity '$nodeName' with $targetCount targets")
        }
    }

    /**
     * Resolves target weights for any defined emotional expression.
     */
    private fun getExpressionTargetWeights(expression: PipExpression, intensity: Float): Map<String, Float> {
        val factor = intensity.coerceIn(0f, 1f)
        return when (expression) {
            PipExpression.NEUTRAL -> emptyMap()
            PipExpression.HAPPY -> mapOf(
                EYE_SMILE_L to 1.0f * factor,
                EYE_SMILE_R to 1.0f * factor,
                SMILE_HAPPY to 1.0f * factor
            )
            PipExpression.SAD -> mapOf(
                POUT_SAD to 1.0f * factor,
                BROW_ANGRY_L to 0.25f * factor,
                BROW_ANGRY_R to 0.25f * factor
            )
            PipExpression.ANGRY -> mapOf(
                BROW_ANGRY_L to 1.0f * factor,
                BROW_ANGRY_R to 1.0f * factor,
                EYE_SQUINT_L to 0.75f * factor,
                EYE_SQUINT_R to 0.75f * factor
            )
            PipExpression.CURIOUS -> mapOf(
                BROW_SURPRISED_L to 0.85f * factor,
                BROW_ANGRY_R to 0.35f * factor,
                EYE_SQUINT_R to 0.40f * factor
            )
            PipExpression.SLEEPY -> mapOf(
                EYE_SQUINT_L to 0.85f * factor,
                EYE_SQUINT_R to 0.85f * factor,
                POUT_SAD to 0.20f * factor
            )
            PipExpression.SURPRISED -> mapOf(
                BROW_SURPRISED_L to 1.0f * factor,
                BROW_SURPRISED_R to 1.0f * factor,
                MOUTH_OPEN_WIDE to 0.85f * factor
            )
            PipExpression.MISCHIEVOUS -> mapOf(
                EYE_SQUINT_L to 0.85f * factor,
                MOUTH_SMIRK_R to 1.0f * factor,
                BROW_ANGRY_L to 0.40f * factor
            )
            PipExpression.PROUD -> mapOf(
                EYE_SMILE_L to 0.75f * factor,
                EYE_SMILE_R to 0.75f * factor,
                MOUTH_SMIRK_R to 0.50f * factor,
                SMILE_HAPPY to 0.60f * factor
            )
        }
    }

    /**
     * Sets the active expression with optional transition duration.
     */
    fun setExpression(
        expression: PipExpression,
        intensity: Float = 1.0f,
        transitionDurationMs: Long = 200L
    ) {
        if (isDisposed) return
        _currentExpression.value = expression
        _expressionIntensity.value = intensity.coerceIn(0f, 1f)

        val targetWeights = getExpressionTargetWeights(expression, _expressionIntensity.value)

        transitionJob?.cancel()
        if (transitionDurationMs <= 0) {
            synchronized(activeWeights) {
                activeWeights.clear()
                activeWeights.putAll(targetWeights)
            }
            applyWeightsToFilament()
        } else {
            transitionJob = scope.launch {
                val startWeights = synchronized(activeWeights) { HashMap(activeWeights) }
                val allKeys = startWeights.keys + targetWeights.keys
                val steps = (transitionDurationMs / 16).toInt().coerceAtLeast(1)

                for (step in 1..steps) {
                    val progress = step.toFloat() / steps
                    // Smooth hermite ease-in-out curve
                    val t = progress * progress * (3f - 2f * progress)

                    synchronized(activeWeights) {
                        for (key in allKeys) {
                            val start = startWeights[key] ?: 0f
                            val target = targetWeights[key] ?: 0f
                            activeWeights[key] = start + (target - start) * t
                        }
                    }
                    applyWeightsToFilament()
                    delay(16)
                }
            }
        }
    }

    fun setIntensity(intensity: Float) {
        setExpression(_currentExpression.value, intensity, transitionDurationMs = 120L)
    }

    fun setBlinkingEnabled(enabled: Boolean) {
        _isBlinkingEnabled.value = enabled
        if (!enabled) {
            blinkWeight = 0f
            applyWeightsToFilament()
        }
    }

    /**
     * Non-blocking coroutine loop simulating natural, sporadic blinking.
     */
    private fun startBlinkingRoutine() {
        blinkJob?.cancel()
        blinkJob = scope.launch {
            try {
                while (isActive && !isDisposed) {
                    // Random human-like interval between blinks: 2.5s to 5.5s
                    val delayMs = Random.nextLong(2500, 5500)
                    delay(delayMs)

                    if (!_isBlinkingEnabled.value) continue
                    // Do not blink if eyes are already closed/sleepy
                    if (_currentExpression.value == PipExpression.SLEEPY) continue

                    performBlink()

                    // 10% chance of an immediate double-blink
                    if (Random.nextFloat() < 0.10f) {
                        delay(120)
                        performBlink()
                    }
                }
            } catch (_: CancellationException) {
                // Expected when scope is destroyed
            }
        }
    }

    private suspend fun performBlink() {
        // Fast eyelid close: ~60ms
        val closeSteps = 3
        for (i in 1..closeSteps) {
            blinkWeight = i.toFloat() / closeSteps
            applyWeightsToFilament()
            delay(20)
        }
        delay(40) // Brief closed hold
        // Eyelid open: ~70ms
        val openSteps = 4
        for (i in openSteps downTo 0) {
            blinkWeight = i.toFloat() / openSteps
            applyWeightsToFilament()
            delay(18)
        }
        blinkWeight = 0f
        applyWeightsToFilament()
    }

    /**
     * Applies effective weights to all registered Filament renderable entities.
     */
    private fun applyWeightsToFilament() {
        if (isDisposed) return
        val weightsSnapshot = synchronized(activeWeights) { HashMap(activeWeights) }

        // Inject blink weight on eye lids
        if (blinkWeight > 0f) {
            val currentL = weightsSnapshot[EYE_BLINK_L] ?: 0f
            val currentR = weightsSnapshot[EYE_BLINK_R] ?: 0f
            weightsSnapshot[EYE_BLINK_L] = maxOf(currentL, blinkWeight)
            weightsSnapshot[EYE_BLINK_R] = maxOf(currentR, blinkWeight)
        }

        for (entity in morphEntities) {
            if (!rm.hasComponent(entity.entity)) continue
            val weightsArray = FloatArray(entity.targetCount)

            for ((targetName, index) in entity.targetMap) {
                if (index < entity.targetCount) {
                    weightsArray[index] = weightsSnapshot[targetName] ?: 0f
                }
            }
            try {
                rm.setMorphWeights(entity.rInstance, weightsArray, 0)
            } catch (e: Exception) {
                Log.w(TAG, "Failed setting morph weights for ${entity.nodeName}: ${e.message}")
            }
        }
    }

    /**
     * Resets all morph target weights back to 0.0 baseline.
     */
    fun resetToNeutral() {
        setExpression(PipExpression.NEUTRAL, intensity = 1.0f, transitionDurationMs = 150L)
    }

    fun dispose() {
        isDisposed = true
        transitionJob?.cancel()
        blinkJob?.cancel()
        synchronized(activeWeights) {
            activeWeights.clear()
        }
        blinkWeight = 0f
        for (entity in morphEntities) {
            if (rm.hasComponent(entity.entity)) {
                try {
                    rm.setMorphWeights(entity.rInstance, FloatArray(entity.targetCount), 0)
                } catch (_: Exception) {}
            }
        }
        morphEntities.clear()
    }
}
