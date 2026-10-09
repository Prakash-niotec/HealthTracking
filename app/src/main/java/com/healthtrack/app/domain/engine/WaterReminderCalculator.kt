package com.healthtrack.app.domain.engine

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object WaterReminderCalculator {

    /**
     * Calculates the next water reminder timestamp.
     * @param lastDrinkTimestamp The timestamp of the last logged drink, or 0 if none today.
     * @param intervalMinutes The reminder interval.
     * @param windowStartHour The start hour of the active window (0-23).
     * @param windowEndHour The end hour of the active window (0-23).
     * @param nowTimestamp The current time to evaluate from.
     * @param timeZone The user's timezone.
     * @return The timestamp for the next alarm, or null if no alarm should be scheduled.
     */
    fun calculateNextReminder(
        lastDrinkTimestamp: Long,
        intervalMinutes: Int,
        windowStartHour: Int,
        windowEndHour: Int,
        nowTimestamp: Long,
        timeZone: ZoneId = ZoneId.systemDefault()
    ): Long {
        val nowZdt = Instant.ofEpochMilli(nowTimestamp).atZone(timeZone)
        
        // Base next trigger is either last drink + interval, or now + interval if no last drink
        var nextTriggerMs = if (lastDrinkTimestamp > 0) {
            lastDrinkTimestamp + (intervalMinutes * 60 * 1000L)
        } else {
            nowTimestamp + (intervalMinutes * 60 * 1000L)
        }
        
        // If the calculated time is already in the past, schedule for now (it'll fire immediately)
        if (nextTriggerMs < nowTimestamp) {
            nextTriggerMs = nowTimestamp + 1000L
        }

        var triggerZdt = Instant.ofEpochMilli(nextTriggerMs).atZone(timeZone)
        val triggerTime = triggerZdt.toLocalTime()
        val start = LocalTime.of(windowStartHour, 0)
        val end = LocalTime.of(windowEndHour, 0)

        val crossesMidnight = start.isAfter(end)
        
        val isInsideWindow = if (crossesMidnight) {
            triggerTime.isAfter(start) || triggerTime.isBefore(end) || triggerTime == start
        } else {
            (triggerTime.isAfter(start) || triggerTime == start) && triggerTime.isBefore(end)
        }

        if (!isInsideWindow) {
            // Push it to the next available window start
            if (crossesMidnight) {
                // Window e.g. 22:00 to 06:00
                // If it's 14:00, push to 22:00 today.
                if (triggerTime.isAfter(end) && triggerTime.isBefore(start)) {
                    triggerZdt = triggerZdt.withHour(windowStartHour).withMinute(0).withSecond(0).withNano(0)
                }
            } else {
                // Window e.g. 08:00 to 22:00
                if (triggerTime.isBefore(start)) {
                    triggerZdt = triggerZdt.withHour(windowStartHour).withMinute(0).withSecond(0).withNano(0)
                } else if (triggerTime.isAfter(end) || triggerTime == end) {
                    triggerZdt = triggerZdt.plusDays(1).withHour(windowStartHour).withMinute(0).withSecond(0).withNano(0)
                }
            }
            nextTriggerMs = triggerZdt.toInstant().toEpochMilli()
            
            // If the shifted time is somehow in the past (e.g. today's start hour but it's already later), push to tomorrow
            if (nextTriggerMs <= nowTimestamp) {
                 nextTriggerMs = triggerZdt.plusDays(1).toInstant().toEpochMilli()
            }
        }

        return nextTriggerMs
    }
}
