package com.healthtrack.app.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.healthtrack.app.HealthTrackApplication
import com.healthtrack.app.data.model.DoseLog
import kotlinx.coroutines.flow.first
import com.healthtrack.app.data.model.DoseStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val medId = intent.getStringExtra("MED_ID") ?: return
        val scheduledAt = intent.getLongExtra("SCHEDULED_AT", 0L)

        // Dismiss notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(medId.hashCode())

        val app = context.applicationContext as HealthTrackApplication
        val medicationRepository = app.container.medicationRepository

        CoroutineScope(Dispatchers.IO).launch {
            if (action == "ACTION_SNOOZE") {
                val medName = intent.getStringExtra("MED_NAME") ?: return@launch
                val medDosage = intent.getStringExtra("MED_DOSAGE") ?: ""
                val settings = app.container.settingsRepository.notificationSettings.first()
                val snoozeMs = settings.snoozeDurationMinutes * 60 * 1000L
                val nextTrigger = System.currentTimeMillis() + snoozeMs

                val snoozeIntent = Intent(context, MedicationReceiver::class.java).apply {
                    putExtra("MED_ID", medId)
                    putExtra("MED_NAME", medName)
                    putExtra("MED_DOSAGE", medDosage)
                    putExtra("SCHEDULED_AT", scheduledAt)
                }

                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    (medId.hashCode() * 31) + scheduledAt.hashCode(),
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger,
                    snoozePendingIntent
                )
                return@launch
            }

            val status = when (action) {
                "ACTION_TAKEN" -> DoseStatus.TAKEN
                "ACTION_SKIPPED" -> DoseStatus.SKIPPED
                else -> return@launch
            }
            
            // To ensure uniqueness:
            val id = "${medId}_${scheduledAt}"
            
            val doseLog = DoseLog(
                id = id,
                medId = medId,
                medName = intent.getStringExtra("MED_NAME") ?: "Medication",
                medDosage = intent.getStringExtra("MED_DOSAGE") ?: "",
                scheduledAt = scheduledAt,
                status = status,
                actedAt = System.currentTimeMillis()
            )
            medicationRepository.markDose(doseLog)
        }
    }
}
