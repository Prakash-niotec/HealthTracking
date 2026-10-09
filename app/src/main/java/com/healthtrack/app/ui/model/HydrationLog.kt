package com.healthtrack.app.ui.model


data class HydrationLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val label: String = "Water"
)
