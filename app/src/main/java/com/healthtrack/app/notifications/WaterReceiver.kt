package com.healthtrack.app.notifications


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.healthtrack.app.MainActivity
import com.healthtrack.app.HealthTrackApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime

class WaterReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HealthTrackApplication
        val settingsRepo = app.container.settingsRepository
        val waterRepo = app.container.waterRepository
        
        CoroutineScope(Dispatchers.IO).launch {
            val settings = settingsRepo.notificationSettings.first()
            if (!settings.waterEnabled) return@launch
            
            // Check active window
            val nowHour = LocalTime.now().hour
            if (nowHour < settings.waterWindowStartHour || nowHour >= settings.waterWindowEndHour) {
                return@launch
            }

            // Optional: Check if goal is already met today. If so, return.
            // Simplified for receiver: always show if in window.

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_WATER)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder
                .setContentTitle("Stay Hydrated!")
                .setContentText("It's time to drink some water.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(1001, notification)

            // Reschedule next one automatically
            val alarmScheduler = AlarmSchedulerImpl(context)
            alarmScheduler.scheduleWaterAlarms(settings, 2000, 0) // placeholders
        }
    }
}
