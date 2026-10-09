package com.healthtrack.app.data.repository

import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow

data class NotificationSettings(
    val medicationEnabled: Boolean = true,
    val waterEnabled: Boolean = true,
    val waterIntervalMinutes: Int = 60, // 30, 60, 90, 120
    val waterWindowStartHour: Int = 8,
    val waterWindowEndHour: Int = 22
)

interface SettingsRepository {
    val notificationSettings: Flow<NotificationSettings>
    suspend fun updateSettings(settings: NotificationSettings): Result<Unit>
}
