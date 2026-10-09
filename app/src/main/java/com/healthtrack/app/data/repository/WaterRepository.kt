package com.healthtrack.app.data.repository

import com.healthtrack.app.data.model.DailyWaterGoal
import com.healthtrack.app.data.model.WaterLog
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow

interface WaterRepository {
    fun getLogsForDate(dateKey: String): Flow<List<WaterLog>>
    suspend fun addLog(log: WaterLog): Result<Unit>
    suspend fun updateLog(log: WaterLog): Result<Unit>
    suspend fun deleteLog(id: String): Result<Unit>
    suspend fun undoLastLog(dateKey: String): Result<Unit>

    fun getGoalForDate(dateKey: String): Flow<DailyWaterGoal?>
    suspend fun setGoalForDate(dailyGoal: DailyWaterGoal): Result<Unit>
}
