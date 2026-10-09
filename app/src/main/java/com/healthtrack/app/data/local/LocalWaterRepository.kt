package com.healthtrack.app.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthtrack.app.data.model.DailyWaterGoal
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
    private val KEY_DAILY_GOALS = "daily_goals"
    
    private val logsFlow = MutableStateFlow<List<WaterLog>>(emptyList())
    private val goalsFlow = MutableStateFlow<List<DailyWaterGoal>>(emptyList())

    init {
        val jsonLogs = localCache.getString(KEY_WATER_LOGS)
        if (jsonLogs != null) {
            val type = object : TypeToken<List<WaterLog>>() {}.type
            val storedLogs: List<WaterLog> = gson.fromJson(jsonLogs, type) ?: emptyList()
            logsFlow.value = storedLogs
        }

        val jsonGoals = localCache.getString(KEY_DAILY_GOALS)
        if (jsonGoals != null) {
            val type = object : TypeToken<List<DailyWaterGoal>>() {}.type
            val storedGoals: List<DailyWaterGoal> = gson.fromJson(jsonGoals, type) ?: emptyList()
            goalsFlow.value = storedGoals
        }
    }

    private fun saveLogs(logs: List<WaterLog>) {
        logsFlow.value = logs
        localCache.putString(KEY_WATER_LOGS, gson.toJson(logs))
    }

    private fun saveGoals(goals: List<DailyWaterGoal>) {
        goalsFlow.value = goals
        localCache.putString(KEY_DAILY_GOALS, gson.toJson(goals))
    }

    override fun getLogsForDate(dateKey: String): Flow<List<WaterLog>> {
        return logsFlow.map { list -> list.filter { it.dateKey == dateKey } }
    }

    override suspend fun addLog(log: WaterLog): Result<Unit> {
        val updatedList = logsFlow.value + log
        saveLogs(updatedList)
        return Result.Success(Unit)
    }

    override suspend fun updateLog(log: WaterLog): Result<Unit> {
        val updatedList = logsFlow.value.map { if (it.id == log.id) log else it }
        saveLogs(updatedList)
        return Result.Success(Unit)
    }

    override suspend fun deleteLog(id: String): Result<Unit> {
        val updatedList = logsFlow.value.filter { it.id != id }
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

    override fun getGoalForDate(dateKey: String): Flow<DailyWaterGoal?> {
        return goalsFlow.map { list -> list.find { it.dateKey == dateKey } }
    }

    override suspend fun setGoalForDate(dailyGoal: DailyWaterGoal): Result<Unit> {
        val existing = goalsFlow.value.find { it.dateKey == dailyGoal.dateKey }
        val updated = if (existing != null) {
            goalsFlow.value.map { if (it.dateKey == dailyGoal.dateKey) dailyGoal else it }
        } else {
            goalsFlow.value + dailyGoal
        }
        saveGoals(updated)
        return Result.Success(Unit)
    }
}
