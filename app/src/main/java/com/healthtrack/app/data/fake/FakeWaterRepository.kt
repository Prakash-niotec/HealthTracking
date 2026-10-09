package com.healthtrack.app.data.fake

import com.healthtrack.app.data.model.DailyWaterGoal
import com.healthtrack.app.data.model.WaterLog
import com.healthtrack.app.data.repository.WaterRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWaterRepository : WaterRepository {
    private val logs = MutableStateFlow<List<WaterLog>>(emptyList())
    private val goals = MutableStateFlow<List<DailyWaterGoal>>(emptyList())

    override fun getLogsForDate(dateKey: String): Flow<List<WaterLog>> {
        return logs.map { list -> list.filter { it.dateKey == dateKey } }
    }

    override suspend fun addLog(log: WaterLog): Result<Unit> {
        logs.value = logs.value + log
        return Result.Success(Unit)
    }

    override suspend fun updateLog(log: WaterLog): Result<Unit> {
        logs.value = logs.value.map { if (it.id == log.id) log else it }
        return Result.Success(Unit)
    }

    override suspend fun deleteLog(id: String): Result<Unit> {
        logs.value = logs.value.filter { it.id != id }
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

    override fun getGoalForDate(dateKey: String): Flow<DailyWaterGoal?> {
        return goals.map { list -> list.find { it.dateKey == dateKey } }
    }

    override suspend fun setGoalForDate(dailyGoal: DailyWaterGoal): Result<Unit> {
        val existing = goals.value.find { it.dateKey == dailyGoal.dateKey }
        if (existing != null) {
            goals.value = goals.value.map { if (it.dateKey == dailyGoal.dateKey) dailyGoal else it }
        } else {
            goals.value = goals.value + dailyGoal
        }
        return Result.Success(Unit)
    }
}
