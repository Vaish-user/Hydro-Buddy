package com.hydrobuddy.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hydrobuddy.app.data.datastore.HydroDataStore
import com.hydrobuddy.app.service.HydrationReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-registers scheduled alarms with AlarmManager upon system boot completion.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dataStore = HydroDataStore(context)
                    val reminderSettings = dataStore.reminderSettingsFlow.first()
                    HydrationReminderScheduler.scheduleNextReminder(context, reminderSettings)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
