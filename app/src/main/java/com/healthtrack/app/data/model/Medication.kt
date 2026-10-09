package com.healthtrack.app.data.model

enum class FrequencyType {
    DAILY,
    SPECIFIC_DAYS,
    CUSTOM_INTERVAL
}

data class Medication(
    val id: String,
    val name: String,
    val dosage: String,
    val frequencyType: FrequencyType,
    val times: List<String>, // "HH:mm"
    val days: Set<Int>, // 1 (Mon) to 7 (Sun) or similar, for SPECIFIC_DAYS
    val startDate: Long, // timestamp
    val endDate: Long?, // optional timestamp
    val isActive: Boolean
)
