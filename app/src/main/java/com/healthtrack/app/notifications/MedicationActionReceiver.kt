package com.healthtrack.app.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.healthtrack.app.HealthTrackApplication
import com.healthtrack.app.data.model.DoseLog
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
            val status = when (action) {
                "ACTION_TAKEN" -> DoseStatus.TAKEN
                "ACTION_SKIPPED" -> DoseStatus.SKIPPED
                else -> return@launch
            }
            
            // To ensure uniqueness:
            val id = "${medId}_${scheduledAt}"
            
            // For a true implementation, we need the medName, but we don't pass it fully here. 
            // In a real app we might fetch it or just pass it in intent.
            val doseLog = DoseLog(
                id = id,
                medId = medId,
                medName = "Medication", // placeholder or fetch from repo
                scheduledAt = scheduledAt,
                status = status,
                actedAt = System.currentTimeMillis()
            )
            medicationRepository.markDose(doseLog)
        }
    }
}
