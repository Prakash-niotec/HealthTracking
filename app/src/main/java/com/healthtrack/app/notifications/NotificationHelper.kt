package com.healthtrack.app.notifications

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.healthtrack.app.MainActivity
import com.healthtrack.app.data.local.LocalCache
import com.healthtrack.app.data.local.LocalNotificationHistoryRepository
import com.healthtrack.app.data.model.NotificationRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {

    const val CHANNEL_ID_MEDICATION = "medication_reminders"
    const val CHANNEL_ID_WATER = "water_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val medChannel = NotificationChannel(
                CHANNEL_ID_MEDICATION,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alerts for scheduled medications"
            }

            val waterChannel = NotificationChannel(
                CHANNEL_ID_WATER,
                "Water Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic reminders to stay hydrated"
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(medChannel)
            notificationManager.createNotificationChannel(waterChannel)
        }
    }

    fun recordNotification(context: Context, title: String, message: String, category: String) {
        val repo = LocalNotificationHistoryRepository(LocalCache(context))
        CoroutineScope(Dispatchers.IO).launch {
            repo.addRecord(NotificationRecord(title = title, message = message, category = category))
        }
    }

    fun sendTestNotification(context: Context) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WATER)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("HealthNexa Reminder Test")
            .setContentText("Your water and medication reminders are active and working perfectly!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(9999, notification)

        recordNotification(context, "HealthNexa Reminder Test", "Your water and medication reminders are active and working perfectly!", "System Test")
    }
}
