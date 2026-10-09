package com.healthtrack.app.data.model

import java.util.UUID

data class NotificationRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "Reminder" // "Water Alert", "Medication Reminder", "System Test"
)
