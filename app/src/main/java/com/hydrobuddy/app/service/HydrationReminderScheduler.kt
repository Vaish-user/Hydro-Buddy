package com.hydrobuddy.app.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hydrobuddy.app.R
import com.hydrobuddy.app.data.datastore.HydroDataStore
import com.hydrobuddy.app.data.model.AppDayOfWeek
import com.hydrobuddy.app.data.model.ReminderSettings
import com.hydrobuddy.app.receiver.HydrationReminderReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object HydrationReminderScheduler {

    private const val TAG = "HydrationScheduler"
    const val PERMISSION_CHANNEL_ID = "pip_permission_channel"
    const val PERMISSION_NOTIFICATION_ID = 1002
    const val REMINDER_CHANNEL_ID = "pip_hydration_reminder_channel"
    const val REMINDER_NOTIFICATION_ID = 2002
    const val REQUEST_CODE_REMINDER = 2001

    fun ensurePermissionChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pip Mascot Setup"
            val descriptionText = "Important permissions to display Pip over other apps"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(PERMISSION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun ensureReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Prompts you to drink water and check in with Pip"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(REMINDER_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleNextReminder(context: Context, settings: ReminderSettings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, HydrationReminderReceiver::class.java).apply {
            action = "com.hydrobuddy.app.ACTION_HYDRATION_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!settings.isEnabled || !settings.isRunning) {
            alarmManager.cancel(pendingIntent)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dataStore = HydroDataStore(context)
                    dataStore.saveNextReminderAt(0L)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving 0 to nextReminderAt: ${e.message}")
                }
            }
            Log.d(TAG, "Reminders disabled or paused; alarm cancelled")
            return
        }

        val triggerAtMillis = calculateNextTriggerTime(settings)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataStore = HydroDataStore(context)
                dataStore.saveNextReminderAt(triggerAtMillis)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving nextReminderAt: ${e.message}")
            }
        }

        Log.d(TAG, "Scheduling next alarm for: $triggerAtMillis (${(triggerAtMillis - System.currentTimeMillis()) / 1000}s from now)")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException on exact alarm, falling back: ${e.message}")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, HydrationReminderReceiver::class.java).apply {
            action = "com.hydrobuddy.app.ACTION_HYDRATION_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled pending alarm intent")
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataStore = HydroDataStore(context)
                dataStore.saveNextReminderAt(0L)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving 0 to nextReminderAt: ${e.message}")
            }
        }
    }

    fun calculateNextTriggerTime(settings: ReminderSettings): Long {
        val now = java.time.LocalTime.now()
        val calendar = Calendar.getInstance()

        val start = ReminderSettings.parseTime(settings.startTime)
        val end = ReminderSettings.parseTime(settings.endTime)

        if (settings.isWithinActiveWindow(now)) {
            // Inside active window: add interval
            calendar.add(Calendar.MINUTE, settings.intervalMinutes)
            val nextTime = java.time.LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            if (!settings.isWithinActiveWindow(nextTime)) {
                // Next time steps outside active window: advance to next window start
                calendar.set(Calendar.HOUR_OF_DAY, start.hour)
                calendar.set(Calendar.MINUTE, start.minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        } else {
            // Outside active window: schedule for next window start
            calendar.set(Calendar.HOUR_OF_DAY, start.hour)
            calendar.set(Calendar.MINUTE, start.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        var safetyLoop = 0
        while (safetyLoop < 7 && !isDayActive(calendar, settings.activeDays)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, start.hour)
            calendar.set(Calendar.MINUTE, start.minute)
            safetyLoop++
        }

        return calendar.timeInMillis
    }

    private fun isDayActive(calendar: Calendar, activeDays: Set<AppDayOfWeek>): Boolean {
        if (activeDays.isEmpty()) return true
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> AppDayOfWeek.MON
            Calendar.TUESDAY -> AppDayOfWeek.TUE
            Calendar.WEDNESDAY -> AppDayOfWeek.WED
            Calendar.THURSDAY -> AppDayOfWeek.THU
            Calendar.FRIDAY -> AppDayOfWeek.FRI
            Calendar.SATURDAY -> AppDayOfWeek.SAT
            Calendar.SUNDAY -> AppDayOfWeek.SUN
            else -> AppDayOfWeek.MON
        }
        return activeDays.contains(dayOfWeek)
    }

    fun showOverlayPermissionNotification(context: Context) {
        ensurePermissionChannel(context)

        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            PERMISSION_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, PERMISSION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Enable Floating Pip Mascot")
            .setContentText("Tap to allow Pip to appear over your apps when it's time to drink water.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "HydroBuddy needs 'Appear on top' permission so Pip can walk onto your screen over other apps. Tap to grant permission."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(PERMISSION_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission missing for permission alert: ${e.message}")
        }
    }

    fun showHydrationReminderNotification(context: Context, reminderMessage: String) {
        ensureReminderChannel(context)

        val intent = Intent(context, com.hydrobuddy.app.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("trigger_reminder", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REMINDER_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = reminderMessage.ifBlank { "Time to drink water! Tap to check in with Pip." }
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time to Hydrate")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission missing for reminder notification: ${e.message}")
        }
    }
}
