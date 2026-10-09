package com.healthtrack.app.data.repository

import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow

data class NotificationSettings(
    val medicationEnabled: Boolean = true,
    val waterEnabled: Boolean = true,
    val waterIntervalMinutes: Int = 60, // Valid range 15..480
    val waterWindowStartHour: Int = 8,
    val waterWindowEndHour: Int = 22,
    
    // M3: configurable windows
    val snoozeDurationMinutes: Int = 10,
    val missedWindowMinutes: Int = 120,
    val onTimeWindowMinutes: Int = 60,

    // W3: Hydration factors by drink type
    val hydrationFactors: Map<String, Float> = mapOf(
        "Water" to 1.0f,
        "Milk" to 1.0f,
        "Tea" to 0.8f,
        "Coffee" to 0.8f,
        "Juice" to 1.0f,
        "Other" to 1.0f
    )
)

interface SettingsRepository {
    val notificationSettings: Flow<NotificationSettings>
    suspend fun updateSettings(settings: NotificationSettings): Result<Unit>
}
