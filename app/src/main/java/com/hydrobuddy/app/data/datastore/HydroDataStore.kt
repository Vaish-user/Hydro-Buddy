package com.hydrobuddy.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hydrobuddy.app.data.model.AnimationSpeed
import com.hydrobuddy.app.data.model.AppearanceSettings
import com.hydrobuddy.app.data.model.AppThemeMode
import com.hydrobuddy.app.data.model.CharacterCustomization
import com.hydrobuddy.app.data.model.CharacterPosition
import com.hydrobuddy.app.data.model.CharacterSize
import com.hydrobuddy.app.data.model.PipPersonality
import com.hydrobuddy.app.data.model.ReminderSettings
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hydro_preferences")

class HydroDataStore(private val context: Context) {

    object PreferencesKeys {
        // Daily Goal
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val GOAL_UNIT = stringPreferencesKey("goal_unit") // "ml" or "litres"

        // Reminder Engine & Schedule
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val INTERVAL_MINUTES = intPreferencesKey("interval_minutes")
        val START_TIME = stringPreferencesKey("start_time")
        val END_TIME = stringPreferencesKey("end_time")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val IS_RUNNING = booleanPreferencesKey("is_running")
        val NEXT_REMINDER_AT = longPreferencesKey("next_reminder_at")

        // Pip Mascot & Personality
        val CHAR_NAME = stringPreferencesKey("char_name")
        val PERSONALITY = stringPreferencesKey("pip_personality")
        val REMINDER_MSG = stringPreferencesKey("reminder_msg")
        val YES_BTN_TEXT = stringPreferencesKey("yes_btn_text")
        val NO_BTN_TEXT = stringPreferencesKey("no_btn_text")
        val YES_MSG = stringPreferencesKey("yes_msg")
        val HAPPY_MSG = stringPreferencesKey("happy_msg")
        val NO_MSG = stringPreferencesKey("no_msg")
        val MISCHIEVOUS_KNIFE_MSG = stringPreferencesKey("mischievous_knife_msg")
        val ENTRANCE_MSG = stringPreferencesKey("entrance_msg")
        val EXIT_MSG = stringPreferencesKey("exit_msg")

        // Intake & Streak
        val TODAY_INTAKE_ML = intPreferencesKey("today_intake_ml")
        val CURRENT_STREAK_DAYS = intPreferencesKey("current_streak_days")
        val LAST_LOGGED_DATE = stringPreferencesKey("last_logged_date")
        val TODAY_LOGS_JSON = stringPreferencesKey("today_logs_json")

        // Pip Appearance & Scale
        val PIP_SCALE = floatPreferencesKey("pip_scale")
        val CHAR_SIZE = stringPreferencesKey("char_size")
        val CHAR_POSITION = stringPreferencesKey("char_position")
        val ANIMATION_SPEED = stringPreferencesKey("animation_speed")
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val themeModeFlow: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        val modeStr = prefs[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        try {
            AppThemeMode.valueOf(modeStr)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    suspend fun saveThemeMode(themeMode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    // Daily Goal Flow (Default 2000 ml, clamped 500..6000)
    val dailyGoalMlFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[PreferencesKeys.DAILY_GOAL_ML] ?: 2000).coerceIn(500, 6000)
    }

    val goalUnitFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.GOAL_UNIT] ?: "ml"
    }

    suspend fun saveDailyGoal(goalMl: Int, unit: String = "ml") {
        val validated = goalMl.coerceIn(500, 6000)
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DAILY_GOAL_ML] = validated
            prefs[PreferencesKeys.GOAL_UNIT] = unit
        }
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    val reminderSettingsFlow: Flow<ReminderSettings> = context.dataStore.data.map { prefs ->
        ReminderSettings(
            isEnabled = prefs[PreferencesKeys.REMINDERS_ENABLED] ?: true,
            intervalMinutes = (prefs[PreferencesKeys.INTERVAL_MINUTES] ?: 45).coerceIn(15, 180),
            startTime = prefs[PreferencesKeys.START_TIME] ?: "08:00",
            endTime = prefs[PreferencesKeys.END_TIME] ?: "22:00",
            soundEnabled = prefs[PreferencesKeys.SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            isRunning = prefs[PreferencesKeys.IS_RUNNING] ?: true
        )
    }

    suspend fun saveReminderSettings(settings: ReminderSettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.REMINDERS_ENABLED] = settings.isEnabled
            prefs[PreferencesKeys.INTERVAL_MINUTES] = settings.intervalMinutes.coerceIn(15, 180)
            prefs[PreferencesKeys.START_TIME] = settings.startTime
            prefs[PreferencesKeys.END_TIME] = settings.endTime
            prefs[PreferencesKeys.SOUND_ENABLED] = settings.soundEnabled
            prefs[PreferencesKeys.VIBRATION_ENABLED] = settings.vibrationEnabled
            prefs[PreferencesKeys.IS_RUNNING] = settings.isRunning
        }
    }

    val nextReminderAtFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.NEXT_REMINDER_AT] ?: 0L
    }

    suspend fun saveNextReminderAt(timestampMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.NEXT_REMINDER_AT] = timestampMs
        }
    }

    val characterCustomizationFlow: Flow<CharacterCustomization> = context.dataStore.data.map { prefs ->
        val personalityName = prefs[PreferencesKeys.PERSONALITY]
        val personality = PipPersonality.values().find { it.name == personalityName || it.id == personalityName }
            ?: PipPersonality.FRIENDLY

        CharacterCustomization(
            characterName = prefs[PreferencesKeys.CHAR_NAME] ?: CharacterCustomization.DEFAULT_NAME,
            personality = personality,
            waterReminderMessage = prefs[PreferencesKeys.REMINDER_MSG] ?: CharacterCustomization.DEFAULT_REMINDER,
            yesButtonText = prefs[PreferencesKeys.YES_BTN_TEXT] ?: CharacterCustomization.DEFAULT_YES_BTN,
            noButtonText = prefs[PreferencesKeys.NO_BTN_TEXT] ?: CharacterCustomization.DEFAULT_NO_BTN,
            yesResponseMessage = prefs[PreferencesKeys.YES_MSG] ?: CharacterCustomization.DEFAULT_YES_RESP,
            happyEncouragementMessage = prefs[PreferencesKeys.HAPPY_MSG] ?: CharacterCustomization.DEFAULT_HAPPY_ENCOURAGEMENT,
            noResponseMessage = prefs[PreferencesKeys.NO_MSG] ?: CharacterCustomization.DEFAULT_NO_RESP,
            mischievousKnifeMessage = prefs[PreferencesKeys.MISCHIEVOUS_KNIFE_MSG] ?: CharacterCustomization.DEFAULT_MISCHIEVOUS_KNIFE,
            entranceMessage = prefs[PreferencesKeys.ENTRANCE_MSG] ?: CharacterCustomization.DEFAULT_ENTRANCE,
            exitMessage = prefs[PreferencesKeys.EXIT_MSG] ?: CharacterCustomization.DEFAULT_EXIT
        )
    }

    suspend fun saveCharacterCustomization(customization: CharacterCustomization) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CHAR_NAME] = customization.characterName
            prefs[PreferencesKeys.PERSONALITY] = customization.personality.name
            prefs[PreferencesKeys.REMINDER_MSG] = customization.waterReminderMessage
            prefs[PreferencesKeys.YES_BTN_TEXT] = customization.yesButtonText
            prefs[PreferencesKeys.NO_BTN_TEXT] = customization.noButtonText
            prefs[PreferencesKeys.YES_MSG] = customization.yesResponseMessage
            prefs[PreferencesKeys.HAPPY_MSG] = customization.happyEncouragementMessage
            prefs[PreferencesKeys.NO_MSG] = customization.noResponseMessage
            prefs[PreferencesKeys.MISCHIEVOUS_KNIFE_MSG] = customization.mischievousKnifeMessage
            prefs[PreferencesKeys.ENTRANCE_MSG] = customization.entranceMessage
            prefs[PreferencesKeys.EXIT_MSG] = customization.exitMessage
        }
    }

    suspend fun restoreDefaultCharacterCustomization() {
        saveCharacterCustomization(CharacterCustomization.default())
    }

    val appearanceSettingsFlow: Flow<AppearanceSettings> = context.dataStore.data.map { prefs ->
        val scale = prefs[PreferencesKeys.PIP_SCALE] ?: 1.0f
        AppearanceSettings(
            characterSize = CharacterSize.valueOf(prefs[PreferencesKeys.CHAR_SIZE] ?: CharacterSize.MEDIUM.name),
            pipScale = scale.coerceIn(0.5f, 1.5f),
            characterPosition = CharacterPosition.valueOf(prefs[PreferencesKeys.CHAR_POSITION] ?: CharacterPosition.BOTTOM_RIGHT.name),
            animationSpeed = AnimationSpeed.valueOf(prefs[PreferencesKeys.ANIMATION_SPEED] ?: AnimationSpeed.NORMAL.name),
            soundEffectsEnabled = prefs[PreferencesKeys.SOUND_EFFECTS_ENABLED] ?: true
        )
    }

    suspend fun saveAppearanceSettings(settings: AppearanceSettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PIP_SCALE] = settings.pipScale.coerceIn(0.5f, 1.5f)
            prefs[PreferencesKeys.CHAR_SIZE] = settings.characterSize.name
            prefs[PreferencesKeys.CHAR_POSITION] = settings.characterPosition.name
            prefs[PreferencesKeys.ANIMATION_SPEED] = settings.animationSpeed.name
            prefs[PreferencesKeys.SOUND_EFFECTS_ENABLED] = settings.soundEffectsEnabled
        }
    }

    suspend fun savePipScale(scale: Float) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PIP_SCALE] = scale.coerceIn(0.5f, 1.5f)
        }
    }

    val todayIntakeMlFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        val lastDate = prefs[PreferencesKeys.LAST_LOGGED_DATE] ?: ""
        val today = LocalDate.now().toString()
        if (lastDate == today) {
            prefs[PreferencesKeys.TODAY_INTAKE_ML] ?: 0
        } else {
            0
        }
    }

    val currentStreakDaysFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CURRENT_STREAK_DAYS] ?: 1
    }

    val todayLogsJsonFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val lastDate = prefs[PreferencesKeys.LAST_LOGGED_DATE] ?: ""
        val today = LocalDate.now().toString()
        if (lastDate == today) {
            prefs[PreferencesKeys.TODAY_LOGS_JSON] ?: "[]"
        } else {
            "[]"
        }
    }

    suspend fun logIntake(amountMl: Int, title: String) {
        val today = LocalDate.now().toString()
        val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        context.dataStore.edit { prefs ->
            val lastDate = prefs[PreferencesKeys.LAST_LOGGED_DATE] ?: ""
            val currentIntake = if (lastDate == today) prefs[PreferencesKeys.TODAY_INTAKE_ML] ?: 0 else 0
            val newIntake = currentIntake + amountMl
            prefs[PreferencesKeys.TODAY_INTAKE_ML] = newIntake
            prefs[PreferencesKeys.LAST_LOGGED_DATE] = today

            val currentLogsStr = if (lastDate == today) prefs[PreferencesKeys.TODAY_LOGS_JSON] ?: "[]" else "[]"
            val newEntry = "{\"title\":\"$title\",\"amount\":$amountMl,\"time\":\"$nowTime\"}"
            val updatedLogs = if (currentLogsStr == "[]" || currentLogsStr.isBlank()) {
                "[$newEntry]"
            } else {
                currentLogsStr.removeSuffix("]") + ",$newEntry]"
            }
            prefs[PreferencesKeys.TODAY_LOGS_JSON] = updatedLogs
        }
    }

    suspend fun saveTodayIntakeOverride(intakeMl: Int, logsJson: String) {
        val today = LocalDate.now().toString()
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.TODAY_INTAKE_ML] = intakeMl.coerceAtLeast(0)
            prefs[PreferencesKeys.TODAY_LOGS_JSON] = logsJson
            prefs[PreferencesKeys.LAST_LOGGED_DATE] = today
        }
    }
}
