package com.healthtrack.app.ui.model

import java.util.UUID

data class DoseTime(
    val time: String,
    val mealTag: String = "Take with food"
)

data class MedicationHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val action: String = "Taken", // "Taken", "Skipped", "Unmarked"
    val note: String = ""
)

data class MedicationItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dosage: String = "10 mg",
    val timing: String = "8:00 AM",
    val frequency: String = "Once Daily",
    val formFactor: String = "Pill/Tablet",
    val isTaken: Boolean = false,
    val nextDoseTime: String = "Today, 8:00 AM",
    val strength: String = "10",
    val unit: String = "mg",
    val doseTimes: List<DoseTime> = listOf(DoseTime(time = timing, mealTag = "Take with breakfast")),
    val activeDays: List<String> = listOf("M", "T", "W", "T", "F", "S", "S"),
    val refillReminder: Boolean = true,
    val stockCount: Int = 30,
    val specialInstructions: String = "",
    val history: List<MedicationHistoryEntry> = listOf(
        MedicationHistoryEntry(
            timestamp = System.currentTimeMillis() - 86400000,
            action = "Taken",
            note = "Dose taken on schedule"
        )
    )
)
