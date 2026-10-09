package com.healthtrack.app.data.fake

import com.healthtrack.app.data.model.WaterLog
import com.healthtrack.app.data.repository.WaterRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FakeWaterRepository : WaterRepository {
    private val logs = MutableStateFlow<List<WaterLog>>(emptyList())

    override fun getLogsForDate(dateKey: String): Flow<List<WaterLog>> {
        return logs.map { list -> list.filter { it.dateKey == dateKey } }
    }

    override suspend fun addLog(amountMl: Int, dateKey: String): Result<Unit> {
        val newLog = WaterLog(
            id = UUID.randomUUID().toString(),
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            dateKey = dateKey
        )
        logs.value = logs.value + newLog
        return Result.Success(Unit)
    }

    override suspend fun undoLastLog(dateKey: String): Result<Unit> {
        val currentLogs = logs.value.filter { it.dateKey == dateKey }.sortedByDescending { it.timestamp }
        if (currentLogs.isEmpty()) {
            return Result.Error("No logs to undo for today")
        }
        val logToRemove = currentLogs.first()
        logs.value = logs.value.filter { it.id != logToRemove.id }
        return Result.Success(Unit)
    }
}
