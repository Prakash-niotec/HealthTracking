package com.healthtrack.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.healthtrack.app.data.model.Medication
import com.healthtrack.app.data.repository.NotificationSettings
import com.healthtrack.app.domain.engine.DoseScheduler
import com.healthtrack.app.domain.engine.WaterReminderCalculator
import java.time.LocalDate
import java.time.ZoneId

class AlarmSchedulerImpl(
    private val context: Context,
    private val doseScheduler: DoseScheduler = DoseScheduler()
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleMedicationAlarms(medications: List<Medication>, date: LocalDate) {
        val scheduledDoses = doseScheduler.generateScheduledDoses(medications, date)

        for (dose in scheduledDoses) {
            val intent = Intent(context, MedicationReceiver::class.java).apply {
                putExtra("MED_ID", dose.medId)
                putExtra("MED_NAME", dose.medName)
                putExtra("SCHEDULED_AT", dose.scheduledAt)
            }

            // Generate a stable request code based on medId and timestamp to avoid duplicates
            val requestCode = (dose.medId.hashCode() * 31) + dose.scheduledAt.hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Only schedule if time is in the future
            if (dose.scheduledAt > System.currentTimeMillis()) {
                scheduleExact(dose.scheduledAt, pendingIntent)
            }
        }
    }

    override fun scheduleWaterAlarms(settings: NotificationSettings, waterGoal: Int, currentWater: Int, lastDrinkTimestamp: Long) {
        // Cancel existing water alarms
        cancelWaterAlarms()

        if (!settings.waterEnabled || currentWater >= waterGoal) {
            return
        }

        val now = System.currentTimeMillis()
        val triggerAtMs = WaterReminderCalculator.calculateNextReminder(
            lastDrinkTimestamp = lastDrinkTimestamp,
            intervalMinutes = settings.waterIntervalMinutes,
            windowStartHour = settings.waterWindowStartHour,
            windowEndHour = settings.waterWindowEndHour,
            nowTimestamp = now
        )

        val intent = Intent(context, WaterReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WATER_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(triggerAtMs, pendingIntent)
    }

    private fun scheduleExact(timeMs: Long, pendingIntent: PendingIntent) {
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeMs,
                pendingIntent
            )
        } else {
            // Fallback
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeMs,
                pendingIntent
            )
        }
    }

    override fun cancelAllAlarms() {
        // Cancel medication alarms by re-generating and canceling
        // This is a naive approach; a better approach tracks scheduled IDs
        // But since PendingIntents match by Intent components, canceling the water alarm is easy
        cancelWaterAlarms()
    }

    private fun cancelWaterAlarms() {
        val intent = Intent(context, WaterReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WATER_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        const val WATER_ALARM_REQUEST_CODE = 9999
    }
}
