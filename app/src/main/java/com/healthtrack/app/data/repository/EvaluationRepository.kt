package com.healthtrack.app.data.repository

import com.healthtrack.app.data.model.Evaluation
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {
    fun getEvaluations(): Flow<List<Evaluation>>
    suspend fun addEvaluation(evaluation: Evaluation): Result<Unit>
    suspend fun deleteEvaluation(id: String): Result<Unit>
    suspend fun clearAll(): Result<Unit>
}
