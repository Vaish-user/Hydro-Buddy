package com.hydrobuddy.app.ui.mascot

/**
 * State machine governing Pip's interactive hydration reminder lifecycle.
 *
 * Sequence:
 * HIDDEN -> WALKING_IN -> IDLE_REMINDER -> (HAPPY_RESPONSE | SAD_RESPONSE) -> WALKING_OUT -> HIDDEN
 */
enum class PipInteractionState {
    HIDDEN,
    WALKING_IN,
    IDLE_REMINDER,
    HAPPY_RESPONSE,
    SAD_RESPONSE,
    WALKING_OUT;

    val isVisible: Boolean
        get() = this != HIDDEN

    val isWalking: Boolean
        get() = this == WALKING_IN || this == WALKING_OUT
}
