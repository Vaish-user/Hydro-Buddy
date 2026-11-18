package com.hydrobuddy.app.ui

import android.app.Activity
import android.os.Bundle
import com.hydrobuddy.app.data.datastore.HydroDataStore
import com.hydrobuddy.app.service.HydrationReminderScheduler
import com.hydrobuddy.app.ui.overlay.FloatingPipOverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Transparent 1-millisecond activity triggered by launcher App Shortcut
 * ("Remind me now") to trigger Pip directly onto the home screen without opening the app.
 */
class ManualTriggerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.Main).launch {
            val dataStore = HydroDataStore(applicationContext)
            val settings = dataStore.reminderSettingsFlow.first()

            // Cancel pending alarms and schedule next reminder interval
            HydrationReminderScheduler.cancelReminders(applicationContext)
            HydrationReminderScheduler.scheduleNextReminder(applicationContext, settings)

            if (FloatingPipOverlayManager.canDrawOverlays(applicationContext)) {
                FloatingPipOverlayManager.showFloatingOverlay(applicationContext)
            } else {
                val launchIntent = intent.setClass(applicationContext, com.hydrobuddy.app.MainActivity::class.java).apply {
                    putExtra("trigger_reminder", true)
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(launchIntent)
            }
            finish()
            overridePendingTransition(0, 0)
        }
    }
}
