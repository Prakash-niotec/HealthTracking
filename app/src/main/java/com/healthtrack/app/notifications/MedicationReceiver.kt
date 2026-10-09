package com.healthtrack.app.notifications


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.healthtrack.app.MainActivity

class MedicationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getStringExtra("MED_ID") ?: return
        val medName = intent.getStringExtra("MED_NAME") ?: return
        val scheduledAt = intent.getLongExtra("SCHEDULED_AT", 0L)

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

        // Action: Taken
        val takenIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = "ACTION_TAKEN"
            putExtra("MED_ID", medId)
            putExtra("SCHEDULED_AT", scheduledAt)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (medId.hashCode() * 31) + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Skipped
        val skippedIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = "ACTION_SKIPPED"
            putExtra("MED_ID", medId)
            putExtra("SCHEDULED_AT", scheduledAt)
        }
        val skippedPendingIntent = PendingIntent.getBroadcast(
            context,
            (medId.hashCode() * 31) + 2,
            skippedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_MEDICATION)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder
            .setContentTitle("Time for your medication")
            .setContentText("It is time to take $medName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "Taken", takenPendingIntent)
            .addAction(0, "Skip", skippedPendingIntent)
            .build()

        notificationManager.notify(medId.hashCode(), notification)
    }
}
