package com.hydrobuddy.app.data.model

import com.hydrobuddy.app.data.config.PipPersonalityConfig

/**
 * Pip's reaction personality profile for hydration reminders.
 * Friendly is default. Rude is opt-in.
 */
enum class PipPersonality(val displayName: String, val id: String, val isOptInRequired: Boolean) {
    FRIENDLY("Friendly", "friendly", false),
    SAD("Sad", "sad", false),
    RUDE("Rude", "rude", true)
}

/**
 * Configurable personality and message customization model for Pip.
 * Encapsulates all dialogue strings with clean plain text (no emojis).
 */
data class CharacterCustomization(
    val characterName: String = DEFAULT_NAME,
    val personality: PipPersonality = PipPersonality.FRIENDLY,
    val waterReminderMessage: String = DEFAULT_REMINDER,
    val yesButtonText: String = DEFAULT_YES_BTN,
    val noButtonText: String = DEFAULT_NO_BTN,
    val yesResponseMessage: String = DEFAULT_YES_RESP,
    val happyEncouragementMessage: String = DEFAULT_HAPPY_ENCOURAGEMENT,
    val noResponseMessage: String = DEFAULT_NO_RESP,
    val mischievousKnifeMessage: String = DEFAULT_MISCHIEVOUS_KNIFE,
    val entranceMessage: String = DEFAULT_ENTRANCE,
    val exitMessage: String = DEFAULT_EXIT
) {
    // Backward compatibility aliases
    val greetingMessage: String
        get() = entranceMessage

    val sadEncouragementMessage: String
        get() = mischievousKnifeMessage

    val safeName: String
        get() = characterName.ifBlank { DEFAULT_NAME }

    val safeReminder: String
        get() = waterReminderMessage.ifBlank {
            PipPersonalityConfig.getConfig(personality).reminderQuestion
        }

    val safeYesBtn: String
        get() = yesButtonText.ifBlank {
            PipPersonalityConfig.getConfig(personality).yesButtonLabel
        }

    val safeNoBtn: String
        get() = noButtonText.ifBlank {
            PipPersonalityConfig.getConfig(personality).noButtonLabel
        }

    val safeYesResp: String
        get() = yesResponseMessage.ifBlank {
            PipPersonalityConfig.getConfig(personality).yesResponseMessage
        }

    val safeHappyEncouragement: String
        get() = happyEncouragementMessage.ifBlank {
            PipPersonalityConfig.getConfig(personality).celebrationEncouragement
        }

    val safeNoResp: String
        get() = noResponseMessage.ifBlank {
            PipPersonalityConfig.getConfig(personality).noResponseMessage
        }

    val safeMischievousKnife: String
        get() = mischievousKnifeMessage.ifBlank {
            PipPersonalityConfig.getConfig(personality).alternativeNotice
        }

    val safeEntrance: String
        get() = entranceMessage.ifBlank { DEFAULT_ENTRANCE }

    val safeExit: String
        get() = exitMessage.ifBlank { DEFAULT_EXIT }

    companion object {
        const val DEFAULT_NAME = "Pip"
        const val DEFAULT_REMINDER = "Did you drink water?"
        const val DEFAULT_YES_BTN = "Yes"
        const val DEFAULT_NO_BTN = "No"
        const val DEFAULT_YES_RESP = "Great job staying hydrated. Keep it up."
        const val DEFAULT_HAPPY_ENCOURAGEMENT = "Goal progress on track. Stay refreshed."
        const val DEFAULT_NO_RESP = "No problem. Remember to take a sip when you can."
        const val DEFAULT_MISCHIEVOUS_KNIFE = "Drink water right now or else."
        const val DEFAULT_ENTRANCE = "Ready to stay hydrated?"
        const val DEFAULT_EXIT = "See you at the next reminder."

        fun default(): CharacterCustomization = CharacterCustomization()
    }
}
