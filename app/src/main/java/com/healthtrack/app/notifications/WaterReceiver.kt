package com.healthtrack.app.notifications

import android.R
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.healthtrack.app.MainActivity
import com.healthtrack.app.HealthTrackApplication
import com.healthtrack.app.data.model.WaterLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class WaterReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HealthTrackApplication
        val settingsRepo = app.container.settingsRepository
        val waterRepo = app.container.waterRepository
        
        if (intent.action == "ACTION_ADD_WATER_250") {
            CoroutineScope(Dispatchers.IO).launch {
                val now = System.currentTimeMillis()
                val dateKey = LocalDate.now().toString()
                
                val log = WaterLog(
                    id = UUID.randomUUID().toString(),
                    amountMl = 250,
                    timestamp = now,
                    dateKey = dateKey,
                    drinkType = "Water",
                    effectiveMl = 250
                )
                waterRepo.addLog(log)
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(1001)
                
                val settings = settingsRepo.notificationSettings.first()
                val currentLogs = waterRepo.getLogsForDate(dateKey).first()
                val currentTotal = currentLogs.sumOf { it.effectiveMl }
                val dailyGoal = waterRepo.getGoalForDate(dateKey).first()?.goalMl ?: 2500
                
                val alarmScheduler = AlarmSchedulerImpl(context)
                alarmScheduler.scheduleWaterAlarms(settings, dailyGoal, currentTotal, now)
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val settings = settingsRepo.notificationSettings.first()
            if (!settings.waterEnabled) return@launch
            
            val nowHour = LocalTime.now().hour
            if (nowHour < settings.waterWindowStartHour || nowHour >= settings.waterWindowEndHour) {
                return@launch
            }

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

            val addWaterIntent = Intent(context, WaterReceiver::class.java).apply {
                action = "ACTION_ADD_WATER_250"
            }
            val addWaterPendingIntent = PendingIntent.getBroadcast(
                context,
                1002,
                addWaterIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_WATER)
                .setSmallIcon(R.drawable.ic_dialog_info)
                .setContentTitle("Stay Hydrated!")
                .setContentText("It's time to drink some water.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(0, "Add 250 ml", addWaterPendingIntent)
                .build()

            notificationManager.notify(1001, notification)
            NotificationHelper.recordNotification(context, "Stay Hydrated!", "It's time to drink some water.", "Water Alert")

            val dateKey = LocalDate.now().toString()
            val dailyGoal = waterRepo.getGoalForDate(dateKey).first()?.goalMl ?: 2500
            val currentLogs = waterRepo.getLogsForDate(dateKey).first()
            val currentTotal = currentLogs.sumOf { it.effectiveMl }
            val lastDrink = currentLogs.maxByOrNull { it.timestamp }?.timestamp ?: 0L

            val alarmScheduler = AlarmSchedulerImpl(context)
            alarmScheduler.scheduleWaterAlarms(settings, dailyGoal, currentTotal, lastDrink)
        }
    }
}
