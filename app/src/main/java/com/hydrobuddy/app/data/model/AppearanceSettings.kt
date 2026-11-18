package com.hydrobuddy.app.data.model

enum class CharacterSize(val label: String, val scaleFactor: Float, val iconSizeDp: Int) {
    SMALL("Small", 0.75f, 72),
    MEDIUM("Medium", 1.0f, 96),
    LARGE("Large", 1.25f, 120),
    EXTRA_LARGE("XL", 1.5f, 140)
}

enum class CharacterPosition(val label: String, val description: String) {
    TOP_RIGHT("Top Right", "Docked in top corner"),
    CENTER_RIGHT("Center Right", "Floating at middle edge"),
    BOTTOM_RIGHT("Bottom Right", "Near screen bottom"),
    FLOATING_BUBBLE("Free Floating", "Draggable screen companion")
}

enum class AnimationSpeed(val label: String, val durationMs: Int) {
    GENTLE("Gentle", 2200),
    NORMAL("Normal", 1500),
    BOUNCY("Playful", 900)
}

data class AppearanceSettings(
    val characterSize: CharacterSize = CharacterSize.MEDIUM,
    val pipScale: Float = 1.0f, // 0.5f (50%) to 1.5f (150%), default 1.0f (100%)
    val characterPosition: CharacterPosition = CharacterPosition.BOTTOM_RIGHT,
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    val soundEffectsEnabled: Boolean = true
) {
    val effectiveScale: Float
        get() = pipScale.coerceIn(0.5f, 1.5f)
}
