package com.healthtrack.app.domain.engine

import com.healthtrack.app.data.model.DoseLog
import com.healthtrack.app.data.model.DoseStatus
import com.healthtrack.app.data.model.WaterLog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object StreakCalculator {

    /**
     * Calculates the streak of consecutive days ending today (or yesterday if today is not finished) 
     * where BOTH:
     * - water total >= goal 
     * - every DUE dose that day was TAKEN
     * Days with no medications count as medication-satisfied.
     */
    fun calculateStreak(
        waterLogs: List<WaterLog>,
        doseLogs: List<DoseLog>,
        waterGoalMl: Int,
        targetDate: LocalDate, // Typically "today"
        timeZone: ZoneId = ZoneId.systemDefault()
    ): Int {
        var streak = 0
        var currentDate = targetDate

        val waterLogsByDate = waterLogs.groupBy { it.dateKey }
        // For doses, grouping by scheduledAt converted to LocalDate string "yyyy-MM-dd"
        val doseLogsByDate = doseLogs.groupBy { 
            Instant.ofEpochMilli(it.scheduledAt).atZone(timeZone).toLocalDate().toString()
        }

        // Check today first. If today is incomplete (broken streak), we still check if yesterday had a streak
        // "A streak ending today or yesterday".
        val isTodaySatisfied = isDaySatisfied(
            dateKey = currentDate.toString(),
            waterLogs = waterLogsByDate[currentDate.toString()] ?: emptyList(),
            doseLogs = doseLogsByDate[currentDate.toString()] ?: emptyList(),
            waterGoalMl = waterGoalMl
        )

        if (isTodaySatisfied) {
            streak++
            currentDate = currentDate.minusDays(1)
        } else {
            // Today not satisfied, check if yesterday was. 
            // If yesterday wasn't either, streak is 0.
            currentDate = currentDate.minusDays(1)
            val isYesterdaySatisfied = isDaySatisfied(
                dateKey = currentDate.toString(),
                waterLogs = waterLogsByDate[currentDate.toString()] ?: emptyList(),
                doseLogs = doseLogsByDate[currentDate.toString()] ?: emptyList(),
                waterGoalMl = waterGoalMl
            )
            if (!isYesterdaySatisfied) {
                return 0
            } else {
                streak++
                currentDate = currentDate.minusDays(1)
            }
        }

        // Count backwards continuously
        while (true) {
            val satisfied = isDaySatisfied(
                dateKey = currentDate.toString(),
                waterLogs = waterLogsByDate[currentDate.toString()] ?: emptyList(),
                doseLogs = doseLogsByDate[currentDate.toString()] ?: emptyList(),
                waterGoalMl = waterGoalMl
            )
            if (satisfied) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    private fun isDaySatisfied(
        dateKey: String,
        waterLogs: List<WaterLog>,
        doseLogs: List<DoseLog>,
        waterGoalMl: Int
    ): Boolean {
        // Water goal met?
        val totalWater = waterLogs.sumOf { it.amountMl }
        if (totalWater < waterGoalMl) return false

        // Doses satisfied? (All due doses taken). If no doses scheduled, it's satisfied.
        // Ignore PENDING doses in the future (though usually we are evaluating past days).
        val dueDoses = doseLogs.filter { it.status != DoseStatus.PENDING } // If it's evaluated for past, pending shouldn't really exist unless missed
        
        if (dueDoses.isNotEmpty()) {
            val allTaken = dueDoses.all { it.status == DoseStatus.TAKEN }
            if (!allTaken) return false
        }
        
        // Also if any missed/skipped, it's false
        val anyMissedOrSkipped = doseLogs.any { it.status == DoseStatus.MISSED || it.status == DoseStatus.SKIPPED }
        if (anyMissedOrSkipped) return false

        return true
    }
}
