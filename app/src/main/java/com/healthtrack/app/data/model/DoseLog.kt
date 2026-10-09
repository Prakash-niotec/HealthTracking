package com.healthtrack.app.data.model

enum class DoseStatus {
    PENDING,
    TAKEN,
    SKIPPED,
    MISSED
}

data class DoseLog(
    val id: String, // deterministic: medId_dateKey_HHmm
    val medId: String,
    val medName: String,
    val scheduledAt: Long,
    val status: DoseStatus,
    val actedAt: Long?
)
