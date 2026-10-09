package com.healthtrack.app.data.local

import com.google.gson.Gson
import com.healthtrack.app.data.repository.NotificationSettings
import com.healthtrack.app.data.repository.SettingsRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalSettingsRepository(
    private val localCache: LocalCache,
    private val gson: Gson = Gson()
) : SettingsRepository {

    private val KEY_SETTINGS = "notification_settings"
    
    private val _settings = MutableStateFlow(NotificationSettings())
    override val notificationSettings: Flow<NotificationSettings> = _settings.asStateFlow()

    init {
        val json = localCache.getString(KEY_SETTINGS)
        if (json != null) {
            val settings = gson.fromJson(json, NotificationSettings::class.java)
            if (settings != null) {
                _settings.value = settings
            }
        }
    }

    override suspend fun updateSettings(settings: NotificationSettings): Result<Unit> {
        _settings.value = settings
        localCache.putString(KEY_SETTINGS, gson.toJson(settings))
        return Result.Success(Unit)
    }
}
