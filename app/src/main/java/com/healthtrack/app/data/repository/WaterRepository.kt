package com.healthtrack.app.data.repository

import com.healthtrack.app.data.model.WaterLog
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow

interface WaterRepository {
    fun getLogsForDate(dateKey: String): Flow<List<WaterLog>>
    suspend fun addLog(amountMl: Int, dateKey: String): Result<Unit>
    suspend fun undoLastLog(dateKey: String): Result<Unit>
}
