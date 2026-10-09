package com.healthtrack.app.domain.engine

import com.healthtrack.app.data.model.DoseLog
import com.healthtrack.app.data.model.DoseStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdherenceCalculatorTest {

    private val hourMs = 60 * 60 * 1000L
    private val minuteMs = 60 * 1000L

    @Test
    fun testAllTakenOnTime() {
        val now = 1000000L
        val logs = listOf(
            DoseLog("1", "m1", "M", now - hourMs, DoseStatus.TAKEN, actedAt = now - hourMs + 10 * minuteMs), // +10m
            DoseLog("2", "m2", "M", now - 2 * hourMs, DoseStatus.TAKEN, actedAt = now - 2 * hourMs + 59 * minuteMs) // +59m
        )
        
        assertEquals(100, AdherenceCalculator.calculateAdherencePercentage(logs, now))
        
        val stats = AdherenceCalculator.calculateStats(logs, now)
        assertEquals(2, stats.onTimeCount)
        assertEquals(0, stats.lateCount)
    }

    @Test
    fun testMixed() {
        val now = 1000000L
        val logs = listOf(
            DoseLog("1", "m1", "M", now - 3 * hourMs, DoseStatus.TAKEN, actedAt = now - 3 * hourMs + 10 * minuteMs), // On time
            DoseLog("2", "m2", "M", now - 2 * hourMs, DoseStatus.TAKEN, actedAt = now - 2 * hourMs + 61 * minuteMs), // Late (>60m)
            DoseLog("3", "m3", "M", now - 4 * hourMs, DoseStatus.SKIPPED, actedAt = now - 4 * hourMs), // Skipped
            DoseLog("4", "m4", "M", now + 1 * hourMs, DoseStatus.PENDING, actedAt = null) // Future (ignored)
        )
        
        // 1 on time / 3 due = 33%
        assertEquals(33, AdherenceCalculator.calculateAdherencePercentage(logs, now))

        val stats = AdherenceCalculator.calculateStats(logs, now)
        assertEquals(1, stats.onTimeCount)
        assertEquals(1, stats.lateCount)
        assertEquals(1, stats.skippedCount)
        assertEquals(0, stats.missedCount)
    }

    @Test
    fun testNoneDue() {
        val now = 1000000L
        val logs = listOf(
            DoseLog("4", "m4", "M", now + 1 * hourMs, DoseStatus.PENDING, actedAt = null) // Future (ignored)
        )
        
        assertNull(AdherenceCalculator.calculateAdherencePercentage(logs, now))
    }

    @Test
    fun testMissedThreshold() {
        val now = 1000000L
        val logs = listOf(
            // 2.5 hours ago, still pending -> auto calculated as missed
            DoseLog("1", "m1", "M", now - (2 * hourMs + 30 * minuteMs), DoseStatus.PENDING, actedAt = null)
        )
        
        val stats = AdherenceCalculator.calculateStats(logs, now)
        assertEquals(0, stats.onTimeCount)
        assertEquals(0, stats.lateCount)
        assertEquals(1, stats.missedCount)
    }
}
