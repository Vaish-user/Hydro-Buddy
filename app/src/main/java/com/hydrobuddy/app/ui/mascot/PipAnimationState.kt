package com.hydrobuddy.app.ui.mascot

/**
 * Defines all 8 production animation clips for Pip (pip_001),
 * synchronized across the 3D GLB model and 2D sprite-sheet atlas.
 */
enum class PipAnimationState(
    val clipName: String,
    val animationIndex: Int,
    val rowIndex: Int,
    val displayName: String,
    val isLooping: Boolean,
    val defaultDurationMs: Long = 1000L
) {
    A_IDLE(
        clipName = "A_IDLE",
        animationIndex = 0,
        rowIndex = 0,
        displayName = "Idle Breathing",
        isLooping = true,
        defaultDurationMs = 1200L
    ),
    B_WALKING_IN(
        clipName = "B_WALKING_IN",
        animationIndex = 1,
        rowIndex = 1,
        displayName = "Walking In",
        isLooping = false,
        defaultDurationMs = 1000L
    ),
    C_REMINDER(
        clipName = "C_REMINDER",
        animationIndex = 2,
        rowIndex = 2,
        displayName = "Hydration Reminder",
        isLooping = false,
        defaultDurationMs = 1000L
    ),
    D_HAPPY(
        clipName = "D_HAPPY",
        animationIndex = 3,
        rowIndex = 3,
        displayName = "Happy Waddle",
        isLooping = false,
        defaultDurationMs = 800L
    ),
    E_SAD(
        clipName = "E_SAD",
        animationIndex = 4,
        rowIndex = 4,
        displayName = "Sad Droop",
        isLooping = false,
        defaultDurationMs = 1000L
    ),
    F_CELEBRATING(
        clipName = "F_CELEBRATING",
        animationIndex = 5,
        rowIndex = 5,
        displayName = "Celebrating",
        isLooping = false,
        defaultDurationMs = 1200L
    ),
    G_DRINKING(
        clipName = "G_DRINKING",
        animationIndex = 6,
        rowIndex = 6,
        displayName = "Drinking Water",
        isLooping = false,
        defaultDurationMs = 1500L
    ),
    H_WALKING_OUT(
        clipName = "H_WALKING_OUT",
        animationIndex = 7,
        rowIndex = 7,
        displayName = "Walking Out",
        isLooping = false,
        defaultDurationMs = 1000L
    );

    companion object {
        fun fromClipName(name: String): PipAnimationState? {
            return entries.firstOrNull { it.clipName.equals(name, ignoreCase = true) }
        }
    }
}

