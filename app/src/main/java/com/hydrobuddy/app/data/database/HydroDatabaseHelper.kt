package com.hydrobuddy.app.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * SQLite local database helper for HydroBuddy water logs.
 * Provides ACID storage for all water intakes, date/time timestamps,
 * volume in ml, and titles.
 */
class HydroDatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    private val _dbChangeTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }
    val dbChangeTrigger: SharedFlow<Unit> = _dbChangeTrigger.asSharedFlow()

    private fun notifyDataChanged() {
        _dbChangeTrigger.tryEmit(Unit)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_WATER_LOGS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_TIME TEXT NOT NULL,
                $COLUMN_AMOUNT_ML INTEGER NOT NULL,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_water_logs_date ON $TABLE_WATER_LOGS($COLUMN_DATE);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Incremental migration steps when schema updates
    }

    /**
     * Insert a new water intake entry into SQLite.
     * Returns the assigned row ID, or -1 on error.
     */
    fun insertLog(
        date: String,
        time: String,
        amountMl: Int,
        title: String,
        timestamp: Long = System.currentTimeMillis()
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, date)
            put(COLUMN_TIME, time)
            put(COLUMN_AMOUNT_ML, amountMl)
            put(COLUMN_TITLE, title)
            put(COLUMN_TIMESTAMP, timestamp)
        }
        val id = db.insert(TABLE_WATER_LOGS, null, values)
        if (id != -1L) {
            notifyDataChanged()
        }
        return id
    }

    /**
     * Restore an entry with specific ID (for Undo operation).
     */
    fun restoreLog(entity: WaterLogEntity): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            if (entity.id > 0) {
                put(COLUMN_ID, entity.id)
            }
            put(COLUMN_DATE, entity.date)
            put(COLUMN_TIME, entity.time)
            put(COLUMN_AMOUNT_ML, entity.amountMl)
            put(COLUMN_TITLE, entity.title)
            put(COLUMN_TIMESTAMP, entity.timestamp)
        }
        val id = db.insertWithOnConflict(
            TABLE_WATER_LOGS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        if (id != -1L) {
            notifyDataChanged()
        }
        return id
    }

    /**
     * Delete an entry by its primary key ID.
     */
    fun deleteLog(id: Long): Boolean {
        val db = writableDatabase
        val affected = db.delete(TABLE_WATER_LOGS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        if (affected > 0) {
            notifyDataChanged()
            return true
        }
        return false
    }

    /**
     * Query all logs recorded on a specific date ("yyyy-MM-dd"), ordered chronologically.
     */
    fun getLogsForDate(date: String): List<WaterLogEntity> {
        val list = mutableListOf<WaterLogEntity>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_WATER_LOGS,
            null,
            "$COLUMN_DATE = ?",
            arrayOf(date),
            null,
            null,
            "$COLUMN_TIMESTAMP ASC"
        )
        cursor.use { c ->
            val idIndex = c.getColumnIndexOrThrow(COLUMN_ID)
            val dateIndex = c.getColumnIndexOrThrow(COLUMN_DATE)
            val timeIndex = c.getColumnIndexOrThrow(COLUMN_TIME)
            val amountIndex = c.getColumnIndexOrThrow(COLUMN_AMOUNT_ML)
            val titleIndex = c.getColumnIndexOrThrow(COLUMN_TITLE)
            val tsIndex = c.getColumnIndexOrThrow(COLUMN_TIMESTAMP)

            while (c.moveToNext()) {
                list.add(
                    WaterLogEntity(
                        id = c.getLong(idIndex),
                        date = c.getString(dateIndex),
                        time = c.getString(timeIndex),
                        amountMl = c.getInt(amountIndex),
                        title = c.getString(titleIndex),
                        timestamp = c.getLong(tsIndex)
                    )
                )
            }
        }
        return list
    }

    /**
     * Calculate sum of amount_ml for a given date.
     */
    fun getTotalMlForDate(date: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_AMOUNT_ML) FROM $TABLE_WATER_LOGS WHERE $COLUMN_DATE = ?",
            arrayOf(date)
        )
        cursor.use { c ->
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getInt(0)
            }
        }
        return 0
    }

    /**
     * Get daily totals for a list of dates.
     */
    fun getDailyTotals(dates: List<String>): Map<String, Int> {
        if (dates.isEmpty()) return emptyMap()
        val resultMap = dates.associateWith { 0 }.toMutableMap()
        val placeholders = dates.joinToString(",") { "?" }
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT $COLUMN_DATE, SUM($COLUMN_AMOUNT_ML) 
            FROM $TABLE_WATER_LOGS 
            WHERE $COLUMN_DATE IN ($placeholders)
            GROUP BY $COLUMN_DATE
            """.trimIndent(),
            dates.toTypedArray()
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val d = c.getString(0)
                val sum = c.getInt(1)
                resultMap[d] = sum
            }
        }
        return resultMap
    }

    /**
     * Returns total logs count across all time.
     */
    fun getTotalLogsCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_WATER_LOGS", null)
        cursor.use { c ->
            if (c.moveToFirst()) return c.getInt(0)
        }
        return 0
    }

    companion object {
        const val DATABASE_NAME = "hydrobuddy_water_logs.db"
        const val DATABASE_VERSION = 1

        const val TABLE_WATER_LOGS = "water_logs"
        const val COLUMN_ID = "id"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"
        const val COLUMN_AMOUNT_ML = "amount_ml"
        const val COLUMN_TITLE = "title"
        const val COLUMN_TIMESTAMP = "timestamp"

        @Volatile
        private var instance: HydroDatabaseHelper? = null

        fun getInstance(context: Context): HydroDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: HydroDatabaseHelper(context).also { instance = it }
            }
        }
    }
}
