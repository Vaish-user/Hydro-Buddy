package com.hydrobuddy.app.ui.mascot

import android.util.Log
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Robust controller for Pip V2 skeletal animation clips.
 * Enforces SceneView 2.2.1 concurrent-safe animation looping while managing
 * clip duration, transitions, and sequencing via deterministic Kotlin coroutines.
 */
class PipAnimationController(
    private val scope: CoroutineScope,
    private var modelNode: ModelNode? = null
) {
    companion object {
        private const val TAG = "PipAnimationController"
    }

    private val _currentAnimation = MutableStateFlow(PipAnimationState.A_IDLE)
    val currentAnimation: StateFlow<PipAnimationState> = _currentAnimation.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var animationJob: Job? = null
    private var isDisposed = false

    /**
     * Binds or updates the active ModelNode.
     */
    fun attachModelNode(node: ModelNode?) {
        this.modelNode = node
        if (node != null) {
            applyActiveAnimationToNode()
        }
    }

    /**
     * Sets global playback speed multiplier (0.5x to 2.0x).
     */
    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3.0f)
        _playbackSpeed.value = clamped
        applyActiveAnimationToNode()
    }

    /**
     * Plays a single animation clip with optional auto-return to IDLE after its natural duration.
     */
    fun playAnimation(
        animationState: PipAnimationState,
        speed: Float? = null,
        autoReturnToIdle: Boolean = true,
        onFinished: (() -> Unit)? = null
    ) {
        if (isDisposed) return
        animationJob?.cancel()

        if (speed != null) {
            _playbackSpeed.value = speed.coerceIn(0.25f, 3.0f)
        }

        _currentAnimation.value = animationState
        _isPlaying.value = true
        applyActiveAnimationToNode()

        // If it's a non-looping action or autoReturnToIdle is explicitly requested:
        if (autoReturnToIdle && !animationState.isLooping) {
            animationJob = scope.launch {
                try {
                    val rawDurationMs = animationState.defaultDurationMs
                    val effectiveDurationMs = (rawDurationMs / _playbackSpeed.value).toLong()
                    delay(effectiveDurationMs)

                    if (isActive && !isDisposed) {
                        onFinished?.invoke()
                        resetToIdle()
                    }
                } catch (_: CancellationException) {
                    // Interrupted by new animation or disposal
                }
            }
        }
    }

    /**
     * Plays an orchestrated sequence of animations sequentially.
     * Example: [B_WALKING_IN, C_REMINDER, G_DRINKING, D_HAPPY, H_WALKING_OUT]
     */
    fun playSequence(
        sequence: List<PipAnimationState>,
        speed: Float? = null,
        onSequenceComplete: (() -> Unit)? = null
    ) {
        if (isDisposed || sequence.isEmpty()) return
        animationJob?.cancel()

        if (speed != null) {
            _playbackSpeed.value = speed.coerceIn(0.25f, 3.0f)
        }

        animationJob = scope.launch {
            try {
                for (state in sequence) {
                    _currentAnimation.value = state
                    _isPlaying.value = true
                    applyActiveAnimationToNode()

                    val rawDurationMs = state.defaultDurationMs
                    val effectiveDurationMs = (rawDurationMs / _playbackSpeed.value).toLong()
                    delay(effectiveDurationMs)
                }

                if (isActive && !isDisposed) {
                    onSequenceComplete?.invoke()
                    resetToIdle()
                }
            } catch (_: CancellationException) {
                // Interrupted
            }
        }
    }

    /**
     * Smoothly resets playback back to baseline looping A_IDLE.
     */
    fun resetToIdle() {
        if (isDisposed) return
        animationJob?.cancel()
        _currentAnimation.value = PipAnimationState.A_IDLE
        _isPlaying.value = false
        applyActiveAnimationToNode()
    }

    /**
     * Dispatches animation command to the underlying SceneView ModelNode.
     * Preserves SceneView 2.2.1 workaround: always pass loop = true to node.playAnimation.
     */
    private fun applyActiveAnimationToNode() {
        val node = modelNode ?: return
        val state = _currentAnimation.value
        val speed = _playbackSpeed.value

        try {
            // In SceneView 2.2.1, setting loop = false triggers ConcurrentModificationException
            // inside ModelNode.applyAnimations() when animator.isDone concurrently modifies the map.
            // Keeping loop = true on the node while our coroutine timer manages state transitions
            // provides 100% thread safety and visual smoothness.
            node.playAnimation(
                animationIndex = state.animationIndex,
                speed = speed,
                loop = true
            )
        } catch (e: Exception) {
            try {
                node.playAnimation(
                    animationName = state.clipName,
                    speed = speed,
                    loop = true
                )
            } catch (err: Exception) {
                Log.w(TAG, "Failed playing animation '${state.clipName}': ${err.message}")
            }
        }
    }

    fun dispose() {
        isDisposed = true
        animationJob?.cancel()
        modelNode = null
    }
}
