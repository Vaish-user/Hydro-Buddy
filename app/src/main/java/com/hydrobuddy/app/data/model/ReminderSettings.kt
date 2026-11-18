package com.hydrobuddy.app.data.model

import android.content.Context
import android.text.format.DateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AppDayOfWeek(val shortName: String, val fullName: String) {
    MON("M", "Monday"),
    TUE("T", "Tuesday"),
    WED("W", "Wednesday"),
    THU("T", "Thursday"),
    FRI("F", "Friday"),
    SAT("S", "Saturday"),
    SUN("S", "Sunday")
}

data class ReminderSettings(
    val isEnabled: Boolean = true,
    val intervalMinutes: Int = 45,
    val isCustomInterval: Boolean = false,
    val startTime: String = "08:00",
    val endTime: String = "22:00",
    val activeDays: Set<AppDayOfWeek> = AppDayOfWeek.values().toSet(),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isRunning: Boolean = true
) {
    val crossesMidnight: Boolean
        get() {
            val start = parseTime(startTime)
            val end = parseTime(endTime)
            return start.isAfter(end)
        }

    fun isWithinActiveHours(time: LocalTime = LocalTime.now()): Boolean {
        if (!isEnabled || !isRunning) return false
        val start = parseTime(startTime)
        val end = parseTime(endTime)
        return if (!crossesMidnight) {
            !time.isBefore(start) && !time.isAfter(end)
        } else {
            !time.isBefore(start) || !time.isAfter(end)
        }
    }

    fun isWithinActiveWindow(time: LocalTime = LocalTime.now()): Boolean = isWithinActiveHours(time)

    companion object {
        val PRESET_INTERVALS = listOf(15, 30, 45, 60, 90, 120, 180)

        fun parseTime(timeStr: String): LocalTime {
            return try {
                if (timeStr.contains("AM", ignoreCase = true) || timeStr.contains("PM", ignoreCase = true)) {
                    LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
                } else {
                    LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("HH:mm"))
                }
            } catch (e: Exception) {
                LocalTime.of(8, 0)
            }
        }

        fun formatDisplayTime(timeStr: String): String {
            val t = parseTime(timeStr)
            return t.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        fun formatTimeForDevice(context: Context, timeStr: String): String {
            val t = parseTime(timeStr)
            val is24Hour = DateFormat.is24HourFormat(context)
            val pattern = if (is24Hour) "HH:mm" else "h:mm a"
            return t.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
        }

        fun formatActiveHoursRange(context: Context, startTime: String, endTime: String): String {
            val startFormatted = formatTimeForDevice(context, startTime)
            val endFormatted = formatTimeForDevice(context, endTime)
            return "$startFormatted to $endFormatted"
        }

        fun formatInterval(minutes: Int): String {
            return when {
                minutes < 60 -> "$minutes min"
                minutes == 60 -> "1 hour"
                minutes % 60 == 0 -> "${minutes / 60} hours"
                else -> "${minutes / 60}h ${minutes % 60}m"
            }
        }
    }
}
