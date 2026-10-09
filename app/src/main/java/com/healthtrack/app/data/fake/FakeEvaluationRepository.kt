package com.healthtrack.app.data.fake

import com.healthtrack.app.data.model.Evaluation
import com.healthtrack.app.data.repository.EvaluationRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeEvaluationRepository : EvaluationRepository {
    private val evaluations = MutableStateFlow<List<Evaluation>>(emptyList())

    override fun getEvaluations(): Flow<List<Evaluation>> {
        return evaluations.map { list -> list.sortedByDescending { it.timestamp } }
    }

    override suspend fun addEvaluation(evaluation: Evaluation): Result<Unit> {
        evaluations.value = evaluations.value + evaluation
        return Result.Success(Unit)
    }

    override suspend fun deleteEvaluation(id: String): Result<Unit> {
        evaluations.value = evaluations.value.filter { it.id != id }
        return Result.Success(Unit)
    }

    override suspend fun clearAll(): Result<Unit> {
        evaluations.value = emptyList()
        return Result.Success(Unit)
    }
}
