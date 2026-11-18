package com.hydrobuddy.app.data.config

import com.hydrobuddy.app.data.model.PipPersonality

/**
 * Structured messages configuration for Pip reminder performances by personality.
 * Contains clean, plain text with no emojis.
 */
data class PersonalityMessageConfig(
    val reminderQuestion: String,
    val yesButtonLabel: String,
    val noButtonLabel: String,
    val yesResponseMessage: String,
    val celebrationEncouragement: String,
    val noResponseMessage: String,
    val alternativeNotice: String
)

object PipPersonalityConfig {

    val FRIENDLY = PersonalityMessageConfig(
        reminderQuestion = "Did you drink water?",
        yesButtonLabel = "Yes",
        noButtonLabel = "No",
        yesResponseMessage = "Great job staying hydrated. Keep it up.",
        celebrationEncouragement = "Goal progress on track. Stay refreshed.",
        noResponseMessage = "No problem. Remember to take a sip when you can.",
        alternativeNotice = "Hydration goal updated."
    )

    val SAD = PersonalityMessageConfig(
        reminderQuestion = "Did you drink water?",
        yesButtonLabel = "Yes",
        noButtonLabel = "No",
        yesResponseMessage = "Thank you for drinking water.",
        celebrationEncouragement = "I feel much better now that you drank water.",
        noResponseMessage = "I am disappointed that you have not had water.",
        alternativeNotice = "Hydration reminder recorded."
    )

    val RUDE = PersonalityMessageConfig(
        reminderQuestion = "Did you drink water?",
        yesButtonLabel = "Yes",
        noButtonLabel = "No",
        yesResponseMessage = "Finally. About time you drank some water.",
        celebrationEncouragement = "Do not wait until you are parched next time.",
        noResponseMessage = "Drink water right now or else.",
        alternativeNotice = "Drink water right now or else."
    )

    fun getConfig(personality: PipPersonality): PersonalityMessageConfig {
        return when (personality) {
            PipPersonality.FRIENDLY -> FRIENDLY
            PipPersonality.SAD -> SAD
            PipPersonality.RUDE -> RUDE
        }
    }
}
