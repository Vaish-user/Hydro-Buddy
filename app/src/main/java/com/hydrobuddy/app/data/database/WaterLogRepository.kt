package com.hydrobuddy.app.data.database

import android.content.Context
import com.hydrobuddy.app.data.datastore.HydroDataStore
import com.hydrobuddy.app.data.model.DailyActivityRecord
import com.hydrobuddy.app.data.model.LoggedIntakeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Repository providing single-source-of-truth access to SQLite water logs,
 * reactive flow streams for UI consumption, and streak calculations.
 */
class WaterLogRepository(
    private val context: Context,
    private val dataStore: HydroDataStore
) {
    private val db = HydroDatabaseHelper.getInstance(context)

    init {
        // Asynchronously check and migrate legacy DataStore logs if database is empty
        CoroutineScope(Dispatchers.IO).launch {
            migrateLegacyDataStoreLogsIfNeeded()
        }
    }

    private suspend fun migrateLegacyDataStoreLogsIfNeeded() {
        if (db.getTotalLogsCount() == 0) {
            val json = dataStore.todayLogsJsonFlow.firstOrNull() ?: return
            if (json.isNotBlank() && json != "[]") {
                try {
                    val array = JSONArray(json)
                    val today = LocalDate.now().toString()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val title = obj.optString("title", "Glass of water")
                        val amount = obj.optInt("amount", 250)
                        val time = obj.optString("time", "12:00")
                        db.insertLog(
                            date = today,
                            time = time,
                            amountMl = amount,
                            title = title
                        )
                    }
                } catch (e: Exception) {
                    // Silently ignore corrupt legacy JSON
                }
            }
        }
    }

    /**
     * Flow of today's water intake entries, ordered by timestamp ascending.
     */
    val todayLogsFlow: Flow<List<LoggedIntakeItem>> = db.dbChangeTrigger
        .map {
            val today = LocalDate.now().toString()
            db.getLogsForDate(today).map { it.toLoggedIntakeItem() }
        }
        .flowOn(Dispatchers.IO)

    /**
     * Flow of total milliliters logged today.
     */
    val todayTotalMlFlow: Flow<Int> = db.dbChangeTrigger
        .map {
            val today = LocalDate.now().toString()
            db.getTotalMlForDate(today)
        }
        .flowOn(Dispatchers.IO)

    /**
     * Flow of 7-day activity records (6 days prior + today),
     * dynamically linked with the user's editable daily goal.
     */
    fun get7DayActivityFlow(dailyGoalMlFlow: Flow<Int>): Flow<List<DailyActivityRecord>> =
        combine(db.dbChangeTrigger, dailyGoalMlFlow) { _, dailyGoal ->
            val today = LocalDate.now()
            val last7Dates = (6 downTo 0).map { today.minusDays(it.toLong()) }
            val dateStrings = last7Dates.map { it.toString() }
            val totalsMap = db.getDailyTotals(dateStrings)

            last7Dates.map { date ->
                val dateStr = date.toString()
                val isToday = date == today
                val shortLabel = if (isToday) {
                    "Today"
                } else {
                    date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                }
                val fullLabel = if (isToday) {
                    "Today (${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())})"
                } else {
                    date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                }
                val intake = totalsMap[dateStr] ?: 0

                DailyActivityRecord(
                    dayLabel = shortLabel,
                    fullDay = fullLabel,
                    dateString = dateStr,
                    intakeMl = intake,
                    targetMl = dailyGoal
                )
            }
        }.flowOn(Dispatchers.IO)

    /**
     * Computes the current streak of consecutive days where intake reached or exceeded daily goal.
     */
    fun getStreakFlow(dailyGoalMlFlow: Flow<Int>): Flow<Int> =
        combine(db.dbChangeTrigger, dailyGoalMlFlow) { _, dailyGoal ->
            computeCurrentStreak(dailyGoal)
        }.flowOn(Dispatchers.IO)

    private fun computeCurrentStreak(dailyGoal: Int): Int {
        if (dailyGoal <= 0) return 0
        val today = LocalDate.now()
        val todayTotal = db.getTotalMlForDate(today.toString())

        var streak = 0
        var checkDate = if (todayTotal >= dailyGoal) {
            streak = 1
            today.minusDays(1)
        } else {
            // Today not yet achieved, check if yesterday was achieved
            val yesterdayTotal = db.getTotalMlForDate(today.minusDays(1).toString())
            if (yesterdayTotal >= dailyGoal) {
                streak = 1
                today.minusDays(2)
            } else {
                return if (todayTotal > 0) 1 else 0
            }
        }

        // Walk backwards up to 365 days
        for (i in 0 until 365) {
            val total = db.getTotalMlForDate(checkDate.toString())
            if (total >= dailyGoal) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Insert water log into SQLite, update DataStore today count for legacy sync.
     */
    suspend fun logIntake(amountMl: Int, title: String): LoggedIntakeItem = withContext(Dispatchers.IO) {
        val today = LocalDate.now().toString()
        val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val nowMs = System.currentTimeMillis()

        val id = db.insertLog(
            date = today,
            time = time,
            amountMl = amountMl,
            title = title,
            timestamp = nowMs
        )

        // Also update DataStore total and streak
        val newTodayTotal = db.getTotalMlForDate(today)
        val dailyGoal = dataStore.dailyGoalMlFlow.firstOrNull() ?: 2000
        val currentStreak = computeCurrentStreak(dailyGoal)

        dataStore.logIntake(amountMl, title)

        LoggedIntakeItem(
            id = id,
            title = title,
            amountMl = amountMl,
            timeFormatted = time,
            date = today,
            timestamp = nowMs
        )
    }

    /**
     * Delete log entry by ID.
     */
    suspend fun deleteLog(id: Long): Boolean = withContext(Dispatchers.IO) {
        val success = db.deleteLog(id)
        if (success) {
            val today = LocalDate.now().toString()
            val newTodayTotal = db.getTotalMlForDate(today)
            // Synchronize with DataStore
            val allLogs = db.getLogsForDate(today)
            val jsonArray = JSONArray()
            for (item in allLogs) {
                val obj = org.json.JSONObject().apply {
                    put("title", item.title)
                    put("amount", item.amountMl)
                    put("time", item.time)
                }
                jsonArray.put(obj)
            }
            // Update preferences directly
            dataStore.saveTodayIntakeOverride(newTodayTotal, jsonArray.toString())
        }
        success
    }

    /**
     * Restore log entry (Undo support).
     */
    suspend fun restoreLog(item: LoggedIntakeItem): Long = withContext(Dispatchers.IO) {
        val entity = WaterLogEntity(
            id = item.id,
            date = if (item.date.isNotBlank()) item.date else LocalDate.now().toString(),
            time = item.timeFormatted,
            amountMl = item.amountMl,
            title = item.title,
            timestamp = item.timestamp
        )
        val id = db.restoreLog(entity)
        if (id != -1L) {
            val today = LocalDate.now().toString()
            val newTodayTotal = db.getTotalMlForDate(today)
            val allLogs = db.getLogsForDate(today)
            val jsonArray = JSONArray()
            for (l in allLogs) {
                val obj = org.json.JSONObject().apply {
                    put("title", l.title)
                    put("amount", l.amountMl)
                    put("time", l.time)
                }
                jsonArray.put(obj)
            }
            dataStore.saveTodayIntakeOverride(newTodayTotal, jsonArray.toString())
        }
        id
    }
}
