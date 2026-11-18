package com.hydrobuddy.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hydrobuddy.app.data.datastore.HydroDataStore
import com.hydrobuddy.app.data.model.CharacterCustomization
import com.hydrobuddy.app.service.HydrationForegroundTracker
import com.hydrobuddy.app.service.HydrationReminderScheduler
import com.hydrobuddy.app.ui.overlay.FloatingPipOverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by AlarmManager at scheduled hydration intervals.
 *
 * Routing Rules:
 * 1. If HydroBuddy is open and in foreground: Trigger in-app Pip interaction.
 * 2. If HydroBuddy is in background (over Instagram, YouTube, WhatsApp, launcher, etc.):
 *    - If SYSTEM_ALERT_WINDOW granted: Directly display floating Pip WindowManager overlay!
 *    - If not granted: Prompt user with permission onboarding notification.
 * 3. Never brings HydroBuddy to the foreground.
 * 4. Reschedules next alarm window.
 */
class HydrationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("HydrationReceiver", "onReceive triggered with intent: $intent")
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataStore = HydroDataStore(context)
                val reminderSettings = dataStore.reminderSettingsFlow.first()
                val force = intent?.getBooleanExtra("force", false) == true

                val resetTexts = intent?.getBooleanExtra("reset_texts", false) == true
                if (resetTexts) {
                    val p = dataStore.characterCustomizationFlow.first().personality
                    dataStore.saveCharacterCustomization(CharacterCustomization(personality = p))
                    Log.d("HydrationReceiver", "Reset customization to prompt defaults")
                }

                val personalityExtra = intent?.getStringExtra("personality")
                if (!personalityExtra.isNullOrBlank()) {
                    val p = com.hydrobuddy.app.data.model.PipPersonality.values().find {
                        it.name.equals(personalityExtra, ignoreCase = true) || it.id.equals(personalityExtra, ignoreCase = true)
                    }
                    if (p != null) {
                        val currentCustom = dataStore.characterCustomizationFlow.first()
                        dataStore.saveCharacterCustomization(currentCustom.copy(personality = p))
                        Log.d("HydrationReceiver", "Updated personality via intent: $p")
                    }
                }

                Log.d("HydrationReceiver", "Settings: isEnabled=${reminderSettings.isEnabled}, isRunning=${reminderSettings.isRunning}, force=$force, isWithinActive=${reminderSettings.isWithinActiveWindow()}")

                if (!force && (!reminderSettings.isEnabled || !reminderSettings.isRunning || !reminderSettings.isWithinActiveWindow())) {
                    Log.d("HydrationReceiver", "Outside active hours or reminders disabled; rescheduling and skipping trigger")
                    HydrationReminderScheduler.scheduleNextReminder(context, reminderSettings)
                    return@launch
                }

                // Check if HydroBuddy is already visible in foreground
                val handledInForeground = HydrationForegroundTracker.notifyReminderTriggered()
                Log.d("HydrationReceiver", "handledInForeground: $handledInForeground (inForeground=${HydrationForegroundTracker.isAppInForeground})")

                if (!handledInForeground) {
                    // Over another app or launcher: show floating Pip overlay!
                    if (FloatingPipOverlayManager.canDrawOverlays(context)) {
                        Log.d("HydrationReceiver", "Displaying floating Pip overlay over current app")
                        FloatingPipOverlayManager.showFloatingOverlay(context)
                    } else {
                        Log.d("HydrationReceiver", "Overlay permission not granted; posting reminder notification to open in-app Pip")
                        val custom = dataStore.characterCustomizationFlow.first()
                        HydrationReminderScheduler.showHydrationReminderNotification(context, custom.safeReminder)
                    }
                }

                // Reschedule next reminder window
                HydrationReminderScheduler.scheduleNextReminder(context, reminderSettings)
            } catch (e: Exception) {
                Log.e("HydrationReceiver", "Error in reminder receiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
