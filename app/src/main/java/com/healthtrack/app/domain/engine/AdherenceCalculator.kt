package com.healthtrack.app.domain.engine

import com.healthtrack.app.data.model.DoseLog
import com.healthtrack.app.data.model.DoseStatus

object AdherenceCalculator {

    /**
     * Calculates adherence % = TAKEN-on-time doses / total due doses
     * (due = scheduled time already passed; exclude future PENDING).
     *
     * Returns null ("N/A") if there are zero due doses to avoid division by zero.
     */
    fun calculateAdherencePercentage(
        logs: List<DoseLog>,
        currentTimeMillis: Long,
        onTimeWindowMinutes: Int
    ): Int? {
        val dueDoses = logs.filter { it.scheduledAt <= currentTimeMillis }

        if (dueDoses.isEmpty()) {
            return null // "N/A"
        }

        val onTimeTakenCount = dueDoses.count { log ->
            log.status == DoseStatus.TAKEN &&
            log.actedAt != null &&
            (log.actedAt - log.scheduledAt) <= (onTimeWindowMinutes * 60 * 1000L)
        }

        return ((onTimeTakenCount.toFloat() / dueDoses.size.toFloat()) * 100f).toInt()
    }

    /**
     * Counts: on-time, late, skipped, missed
     */
    fun calculateStats(
        logs: List<DoseLog>, 
        currentTimeMillis: Long,
        onTimeWindowMinutes: Int,
        missedThresholdMinutes: Int
    ): AdherenceStats {
        val dueDoses = logs.filter { it.scheduledAt <= currentTimeMillis }
        
        var onTime = 0
        var late = 0
        var skipped = 0
        var missed = 0

        for (log in dueDoses) {
            when (log.status) {
                DoseStatus.TAKEN -> {
                    if (log.actedAt != null && (log.actedAt - log.scheduledAt) <= (onTimeWindowMinutes * 60 * 1000L)) {
                        onTime++
                    } else {
                        late++
                    }
                }
                DoseStatus.SKIPPED -> skipped++
                DoseStatus.MISSED -> missed++
                DoseStatus.PENDING -> {
                    // It's due, but still pending. Check if it should have auto-transitioned to missed
                    if (currentTimeMillis - log.scheduledAt >= (missedThresholdMinutes * 60 * 1000L)) {
                        missed++
                    } else {
                        // Technically late, hasn't crossed the missed threshold yet, but user hasn't acted.
                        // Wait for user action or threshold cross. Treat as late in progress for stats.
                        late++
                    }
                }
            }
        }
        return AdherenceStats(onTime, late, skipped, missed)
    }
}

data class AdherenceStats(
    val onTimeCount: Int,
    val lateCount: Int,
    val skippedCount: Int,
    val missedCount: Int
)
