package com.hydrobuddy.app.data.model

data class LoggedIntakeItem(
    val id: Long = 0L,
    val title: String,
    val amountMl: Int,
    val timeFormatted: String,
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
