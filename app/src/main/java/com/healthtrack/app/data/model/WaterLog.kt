package com.healthtrack.app.data.model

data class WaterLog(
    val id: String,
    val amountMl: Int,
    val timestamp: Long,
    val dateKey: String, // format "yyyy-MM-dd"
    val drinkType: String = "Water",
    val effectiveMl: Int = amountMl
)

data class DailyWaterGoal(
    val dateKey: String,
    val goalMl: Int
)
