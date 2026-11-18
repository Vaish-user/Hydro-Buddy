package com.hydrobuddy.app.data.database

import com.hydrobuddy.app.data.model.LoggedIntakeItem

/**
 * Immutable entity representing a single water intake log in SQLite.
 */
data class WaterLogEntity(
    val id: Long = 0L,
    val date: String,       // ISO format: "yyyy-MM-dd"
    val time: String,       // "HH:mm" (24h)
    val amountMl: Int,      // Volume in milliliters
    val title: String,      // Label e.g. "Glass of water", "Bottle"
    val timestamp: Long     // Milliseconds since epoch
) {
    fun toLoggedIntakeItem(): LoggedIntakeItem {
        return LoggedIntakeItem(
            id = id,
            date = date,
            amountMl = amountMl,
            timeFormatted = time,
            title = title,
            timestamp = timestamp
        )
    }
}
