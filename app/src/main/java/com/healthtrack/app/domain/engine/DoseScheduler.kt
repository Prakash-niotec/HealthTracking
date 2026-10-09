package com.healthtrack.app.domain.engine

import com.healthtrack.app.data.model.Medication
import com.healthtrack.app.data.model.FrequencyType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class ScheduledDose(
    val medId: String,
    val medName: String,
    val scheduledAt: Long // timestamp
)

class DoseScheduler {
    fun generateScheduledDoses(
        medications: List<Medication>,
        targetDate: LocalDate,
        timeZone: ZoneId = ZoneId.systemDefault()
    ): List<ScheduledDose> {
        val results = mutableListOf<ScheduledDose>()
        val targetStartOfDay = targetDate.atStartOfDay(timeZone).toInstant().toEpochMilli()
        val targetEndOfDay = targetDate.plusDays(1).atStartOfDay(timeZone).toInstant().toEpochMilli() - 1

        for (med in medications) {
            if (!med.isActive) continue

            // Check boundaries
            if (med.startDate > targetEndOfDay) continue
            if (med.endDate != null && med.endDate < targetStartOfDay) continue

            val shouldGenerate = when (med.frequencyType) {
                FrequencyType.DAILY -> true
                FrequencyType.SPECIFIC_DAYS -> {
                    // map java.time.DayOfWeek (1=Mon) to our domain (1=Mon)
                    val dayOfWeek = targetDate.dayOfWeek.value
                    med.days.contains(dayOfWeek)
                }
                FrequencyType.CUSTOM_INTERVAL -> {
                    // calculate days between start date and target date
                    val startLocalDate = Instant.ofEpochMilli(med.startDate).atZone(timeZone).toLocalDate()
                    val daysBetween = ChronoUnit.DAYS.between(startLocalDate, targetDate)
                    if (daysBetween >= 0) {
                        val interval = med.days.firstOrNull() ?: 1 // In custom interval, 'days' set holds the interval N
                        daysBetween % interval == 0L
                    } else {
                        false
                    }
                }
            }

            if (shouldGenerate) {
                for (timeStr in med.times) {
                    val parts = timeStr.split(":")
                    if (parts.size == 2) {
                        val hour = parts[0].toIntOrNull() ?: 0
                        val minute = parts[1].toIntOrNull() ?: 0
                        val scheduledDateTime = LocalDateTime.of(targetDate, LocalTime.of(hour, minute))
                        val scheduledTimestamp = scheduledDateTime.atZone(timeZone).toInstant().toEpochMilli()

                        // Ensure the specific dose is within the start/end date bounds strictly
                        if (scheduledTimestamp >= med.startDate && (med.endDate == null || scheduledTimestamp <= med.endDate)) {
                            results.add(
                                ScheduledDose(
                                    medId = med.id,
                                    medName = med.name,
                                    scheduledAt = scheduledTimestamp
                                )
                            )
                        }
                    }
                }
            }
        }
        return results.sortedBy { it.scheduledAt }
    }
}
