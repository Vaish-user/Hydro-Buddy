package com.hydrobuddy.app.data.model

data class DailyActivityRecord(
    val dayLabel: String, // e.g. "Mon"
    val fullDay: String,  // e.g. "Monday"
    val dateString: String = "", // "yyyy-MM-dd"
    val intakeMl: Int,
    val targetMl: Int,
    val remindersResponded: Int = 0,
    val remindersTotal: Int = 0
) {
    val progressFraction: Float
        get() = if (targetMl > 0) (intakeMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1.5f) else 0f

    val isGoalAchieved: Boolean
        get() = targetMl > 0 && intakeMl >= targetMl
}

data class HydrationStatistics(
    val totalRemindersToday: Int = 0,
    val yesResponses: Int = 0,
    val noResponses: Int = 0,
    val dailyConsistencyPercentage: Int = 100,
    val currentStreakDays: Int = 0,
    val todayIntakeMl: Int = 0,
    val dailyTargetMl: Int = 2000,
    val isMockData: Boolean = false,
    val weeklyActivity: List<DailyActivityRecord> = emptyList()
)

