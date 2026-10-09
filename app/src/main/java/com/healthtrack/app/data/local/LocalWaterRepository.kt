package com.healthtrack.app.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthtrack.app.data.model.WaterLog
import com.healthtrack.app.data.repository.WaterRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class LocalWaterRepository(
    private val localCache: LocalCache,
    private val gson: Gson = Gson()
) : WaterRepository {

    private val KEY_WATER_LOGS = "water_logs"
    private val logsFlow = MutableStateFlow<List<WaterLog>>(emptyList())

    init {
        val json = localCache.getString(KEY_WATER_LOGS)
        if (json != null) {
            val type = object : TypeToken<List<WaterLog>>() {}.type
            val storedLogs: List<WaterLog> = gson.fromJson(json, type) ?: emptyList()
            logsFlow.value = storedLogs
        }
    }

    private fun saveLogs(logs: List<WaterLog>) {
        logsFlow.value = logs
        localCache.putString(KEY_WATER_LOGS, gson.toJson(logs))
    }

    override fun getLogsForDate(dateKey: String): Flow<List<WaterLog>> {
        return logsFlow.map { list -> list.filter { it.dateKey == dateKey } }
    }

    override suspend fun addLog(amountMl: Int, dateKey: String): Result<Unit> {
        val newLog = WaterLog(
            id = UUID.randomUUID().toString(),
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            dateKey = dateKey
        )
        val updatedList = logsFlow.value + newLog
        saveLogs(updatedList)
        return Result.Success(Unit)
    }

    override suspend fun undoLastLog(dateKey: String): Result<Unit> {
        val currentLogs = logsFlow.value.filter { it.dateKey == dateKey }.sortedByDescending { it.timestamp }
        if (currentLogs.isEmpty()) {
            return Result.Error("No logs to undo for today")
        }
        val logToRemove = currentLogs.first()
        val updatedList = logsFlow.value.filter { it.id != logToRemove.id }
        saveLogs(updatedList)
        return Result.Success(Unit)
    }
}
