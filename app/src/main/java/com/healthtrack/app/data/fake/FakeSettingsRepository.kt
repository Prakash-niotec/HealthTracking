package com.healthtrack.app.data.fake

import com.healthtrack.app.data.repository.NotificationSettings
import com.healthtrack.app.data.repository.SettingsRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(NotificationSettings())
    override val notificationSettings: Flow<NotificationSettings> = _settings.asStateFlow()

    override suspend fun updateSettings(settings: NotificationSettings): Result<Unit> {
        _settings.value = settings
        return Result.Success(Unit)
    }
}
