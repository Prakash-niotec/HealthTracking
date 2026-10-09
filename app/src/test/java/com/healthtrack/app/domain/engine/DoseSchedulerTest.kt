package com.healthtrack.app.domain.engine

import com.healthtrack.app.data.model.FrequencyType
import com.healthtrack.app.data.model.Medication
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DoseSchedulerTest {

    private val scheduler = DoseScheduler()
    private val timeZone = ZoneId.of("UTC")

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long {
        return LocalDate.of(year, month, day).atTime(hour, minute).atZone(timeZone).toInstant().toEpochMilli()
    }

    @Test
    fun testDaily() {
        val med = Medication(
            id = "1",
            name = "Aspirin",
            dosage = "100mg",
            frequencyType = FrequencyType.DAILY,
            times = listOf("08:00", "20:00"),
            days = emptySet(),
            startDate = millis(2023, 1, 1),
            endDate = null,
            isActive = true
        )

        val targetDate = LocalDate.of(2023, 1, 5)
        val doses = scheduler.generateScheduledDoses(listOf(med), targetDate, timeZone)

        assertEquals(2, doses.size)
        assertEquals(millis(2023, 1, 5, 8, 0), doses[0].scheduledAt)
        assertEquals(millis(2023, 1, 5, 20, 0), doses[1].scheduledAt)
    }

    @Test
    fun testSpecificDays() {
        val targetDate = LocalDate.of(2023, 1, 2) // Monday
        val med = Medication(
            id = "1",
            name = "Meds",
            dosage = "10mg",
            frequencyType = FrequencyType.SPECIFIC_DAYS,
            times = listOf("10:00"),
            days = setOf(1, 3), // Monday, Wednesday
            startDate = millis(2023, 1, 1),
            endDate = null,
            isActive = true
        )

        val dosesMonday = scheduler.generateScheduledDoses(listOf(med), targetDate, timeZone)
        assertEquals(1, dosesMonday.size)

        val dosesTuesday = scheduler.generateScheduledDoses(listOf(med), targetDate.plusDays(1), timeZone)
        assertEquals(0, dosesTuesday.size)
    }

    @Test
    fun testCustomInterval() {
        val targetDate = LocalDate.of(2023, 1, 1)
        val med = Medication(
            id = "1",
            name = "Meds",
            dosage = "10mg",
            frequencyType = FrequencyType.CUSTOM_INTERVAL,
            times = listOf("10:00"),
            days = setOf(3), // Every 3 days
            startDate = millis(2023, 1, 1),
            endDate = null,
            isActive = true
        )

        assertEquals(1, scheduler.generateScheduledDoses(listOf(med), LocalDate.of(2023, 1, 1), timeZone).size)
        assertEquals(0, scheduler.generateScheduledDoses(listOf(med), LocalDate.of(2023, 1, 2), timeZone).size)
        assertEquals(0, scheduler.generateScheduledDoses(listOf(med), LocalDate.of(2023, 1, 3), timeZone).size)
        assertEquals(1, scheduler.generateScheduledDoses(listOf(med), LocalDate.of(2023, 1, 4), timeZone).size)
    }

    @Test
    fun testStartEndDatesStrict() {
        val targetDate = LocalDate.of(2023, 1, 1)
        val med = Medication(
            id = "1",
            name = "Meds",
            dosage = "10mg",
            frequencyType = FrequencyType.DAILY,
            times = listOf("08:00"),
            days = emptySet(),
            startDate = millis(2023, 1, 1, 12, 0), // Starts noon
            endDate = millis(2023, 1, 3, 12, 0),   // Ends noon
            isActive = true
        )

        // Jan 1: 08:00 is BEFORE start time
        val jan1 = scheduler.generateScheduledDoses(listOf(med), LocalDate.of(2023, 1, 1), timeZone)
        assertEquals(0, jan1.size)

        // Jan 2: 08:00 is valid
        val jan2 = scheduler.generateScheduledDoses(listOf(med), LocalDate.of(2023, 1, 2), timeZone)
        assertEquals(1, jan2.size)

        // Jan 3: 08:00 is BEFORE end time, valid
        val jan3 = scheduler.generateScheduledDoses(listOf(med), LocalDate.of(2023, 1, 3), timeZone)
        assertEquals(1, jan3.size)

        // Jan 4: Entirely after
        val jan4 = scheduler.generateScheduledDoses(listOf(med), LocalDate.of(2023, 1, 4), timeZone)
        assertEquals(0, jan4.size)
    }
}
