package com.healthtrack.app.domain.engine

import com.healthtrack.app.data.model.DoseLog
import com.healthtrack.app.data.model.DoseStatus
import com.healthtrack.app.data.model.WaterLog
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StreakCalculatorTest {

    private val timeZone = ZoneId.of("UTC")
    private val waterGoal = 2000

    private fun waterLog(date: String, amount: Int): WaterLog {
        return WaterLog("w", amount, 0L, date)
    }

    private fun doseLog(date: String, status: DoseStatus): DoseLog {
        val ts = LocalDate.parse(date).atStartOfDay(timeZone).toInstant().toEpochMilli()
        return DoseLog("d", "m1", "M", ts, status, ts)
    }

    @Test
    fun testLongStreak() {
        val today = LocalDate.parse("2023-01-05")
        
        val wLogs = listOf(
            waterLog("2023-01-05", 2000), // Today met
            waterLog("2023-01-04", 2500),
            waterLog("2023-01-03", 2000),
            waterLog("2023-01-02", 2000)
        )
        val dLogs = listOf(
            doseLog("2023-01-05", DoseStatus.TAKEN),
            doseLog("2023-01-04", DoseStatus.TAKEN),
            doseLog("2023-01-03", DoseStatus.TAKEN)
            // No med on 02, still satisfied
        )

        assertEquals(4, StreakCalculator.calculateStreak(wLogs, dLogs, waterGoal, today, timeZone))
    }

    @Test
    fun testBrokenStreak() {
        val today = LocalDate.parse("2023-01-05")
        
        val wLogs = listOf(
            waterLog("2023-01-05", 2000),
            waterLog("2023-01-04", 1999), // Short by 1ml -> breaks streak!
            waterLog("2023-01-03", 2000)
        )
        
        // Only 05 is valid continuously. Streak = 1.
        assertEquals(1, StreakCalculator.calculateStreak(wLogs, emptyList(), waterGoal, today, timeZone))
    }

    @Test
    fun testTodayIncompleteButYesterdayMet() {
        val today = LocalDate.parse("2023-01-05")
        
        val wLogs = listOf(
            waterLog("2023-01-05", 1000), // Incomplete today
            waterLog("2023-01-04", 2000),
            waterLog("2023-01-03", 2000)
        )
        val dLogs = emptyList<DoseLog>()

        // 04 and 03 are met. Streak = 2.
        assertEquals(2, StreakCalculator.calculateStreak(wLogs, dLogs, waterGoal, today, timeZone))
    }

    @Test
    fun testNoMedicationDaysCountAsSatisfied() {
        val today = LocalDate.parse("2023-01-05")
        
        val wLogs = listOf(
            waterLog("2023-01-05", 2000),
            waterLog("2023-01-04", 2000)
        )
        val dLogs = emptyList<DoseLog>()

        assertEquals(2, StreakCalculator.calculateStreak(wLogs, dLogs, waterGoal, today, timeZone))
    }
}
