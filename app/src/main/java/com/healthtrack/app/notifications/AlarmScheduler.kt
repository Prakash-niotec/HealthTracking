package com.healthtrack.app.notifications

import com.healthtrack.app.data.model.Medication
import com.healthtrack.app.data.repository.NotificationSettings
import java.time.LocalDate

interface AlarmScheduler {
    fun scheduleMedicationAlarms(medications: List<Medication>, date: LocalDate)
    fun scheduleWaterAlarms(settings: NotificationSettings, waterGoal: Int, currentWater: Int, lastDrinkTimestamp: Long)
    fun cancelAllAlarms()
}
