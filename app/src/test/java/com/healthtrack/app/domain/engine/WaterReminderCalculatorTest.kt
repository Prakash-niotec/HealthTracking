package com.healthtrack.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WaterReminderCalculatorTest {
    private val timeZone = ZoneId.of("UTC")

    @Test
    fun testInsideWindow() {
        val now = LocalDate.of(2023, 1, 1).atTime(10, 0).atZone(timeZone).toInstant().toEpochMilli()
        val lastDrink = now - (30 * 60 * 1000L) // 30 mins ago
        
        // Interval 60 mins -> next should be lastDrink + 60m = 10:30
        val next = WaterReminderCalculator.calculateNextReminder(
            lastDrinkTimestamp = lastDrink,
            intervalMinutes = 60,
            windowStartHour = 8,
            windowEndHour = 22,
            nowTimestamp = now,
            timeZone = timeZone
        )
        
        val expected = LocalDate.of(2023, 1, 1).atTime(10, 30).atZone(timeZone).toInstant().toEpochMilli()
        assertEquals(expected, next)
    }

    @Test
    fun testPastTargetPushedToNow() {
        val now = LocalDate.of(2023, 1, 1).atTime(10, 0).atZone(timeZone).toInstant().toEpochMilli()
        val lastDrink = now - (120 * 60 * 1000L) // 2 hours ago
        
        // Interval 60 mins -> next should be lastDrink + 60m = 09:00 (which is past)
        // Expected is now + 1s
        val next = WaterReminderCalculator.calculateNextReminder(
            lastDrinkTimestamp = lastDrink,
            intervalMinutes = 60,
            windowStartHour = 8,
            windowEndHour = 22,
            nowTimestamp = now,
            timeZone = timeZone
        )
        
        assertEquals(now + 1000L, next)
    }

    @Test
    fun testPushToNextWindow() {
        val now = LocalDate.of(2023, 1, 1).atTime(21, 30).atZone(timeZone).toInstant().toEpochMilli()
        val lastDrink = now
        
        // Interval 60 mins -> next is 22:30. Window ends at 22:00.
        // Should push to 08:00 next day.
        val next = WaterReminderCalculator.calculateNextReminder(
            lastDrinkTimestamp = lastDrink,
            intervalMinutes = 60,
            windowStartHour = 8,
            windowEndHour = 22,
            nowTimestamp = now,
            timeZone = timeZone
        )
        
        val expected = LocalDate.of(2023, 1, 2).atTime(8, 0).atZone(timeZone).toInstant().toEpochMilli()
        assertEquals(expected, next)
    }

    @Test
    fun testCrossesMidnight() {
        val now = LocalDate.of(2023, 1, 1).atTime(14, 0).atZone(timeZone).toInstant().toEpochMilli()
        val lastDrink = now
        
        // Window 22:00 to 06:00. Now is 14:00 (outside).
        // Next should be pushed to 22:00 today.
        val next = WaterReminderCalculator.calculateNextReminder(
            lastDrinkTimestamp = lastDrink,
            intervalMinutes = 60,
            windowStartHour = 22,
            windowEndHour = 6,
            nowTimestamp = now,
            timeZone = timeZone
        )
        
        val expected = LocalDate.of(2023, 1, 1).atTime(22, 0).atZone(timeZone).toInstant().toEpochMilli()
        assertEquals(expected, next)
    }
}
