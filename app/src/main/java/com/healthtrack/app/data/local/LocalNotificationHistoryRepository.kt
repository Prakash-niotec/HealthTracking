package com.healthtrack.app.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthtrack.app.data.model.NotificationRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalNotificationHistoryRepository(
    private val localCache: LocalCache,
    private val gson: Gson = Gson()
) {
    private val KEY_NOTIF_HISTORY = "notification_history_logs"
    private val _historyFlow = MutableStateFlow<List<NotificationRecord>>(emptyList())
    val historyFlow: Flow<List<NotificationRecord>> = _historyFlow.asStateFlow()

    init {
        loadFromCache()
    }

    private fun loadFromCache() {
        val json = localCache.getString(KEY_NOTIF_HISTORY)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<NotificationRecord>>() {}.type
            val list: List<NotificationRecord> = gson.fromJson(json, type) ?: emptyList()
            _historyFlow.value = list
        }
    }

    suspend fun addRecord(record: NotificationRecord) {
        val updated = listOf(record) + _historyFlow.value
        _historyFlow.value = updated
        localCache.putString(KEY_NOTIF_HISTORY, gson.toJson(updated))
    }

    suspend fun clearHistory() {
        _historyFlow.value = emptyList()
        localCache.remove(KEY_NOTIF_HISTORY)
    }
}
