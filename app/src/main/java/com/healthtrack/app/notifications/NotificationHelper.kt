package com.healthtrack.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

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
}
