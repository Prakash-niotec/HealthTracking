package com.healthtrack.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.healthtrack.app.HealthTrackApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as HealthTrackApplication
            val alarmScheduler = AlarmSchedulerImpl(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                // Reschedule Meds
                val meds = app.container.medicationRepository.getMedications().first()
                alarmScheduler.scheduleMedicationAlarms(meds, LocalDate.now())

                // Reschedule Water
                val settings = app.container.settingsRepository.notificationSettings.first()
                alarmScheduler.scheduleWaterAlarms(settings, 2000, 0)
            }
        }
    }
}
