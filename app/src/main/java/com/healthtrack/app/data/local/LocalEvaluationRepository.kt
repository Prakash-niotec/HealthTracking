package com.healthtrack.app.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthtrack.app.data.model.Evaluation
import com.healthtrack.app.data.repository.EvaluationRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class LocalEvaluationRepository(
    private val localCache: LocalCache,
    private val gson: Gson = Gson()
) : EvaluationRepository {

    private val KEY_EVALUATIONS = "evaluations"
    private val evaluationsFlow = MutableStateFlow<List<Evaluation>>(emptyList())

    init {
        val json = localCache.getString(KEY_EVALUATIONS)
        if (json != null) {
            val type = object : TypeToken<List<Evaluation>>() {}.type
            val storedEvals: List<Evaluation> = gson.fromJson(json, type) ?: emptyList()
            evaluationsFlow.value = storedEvals
        }
    }

    private fun saveEvaluations(list: List<Evaluation>) {
        evaluationsFlow.value = list
        localCache.putString(KEY_EVALUATIONS, gson.toJson(list))
    }

    override fun getEvaluations(): Flow<List<Evaluation>> {
        return evaluationsFlow.map { list -> list.sortedByDescending { it.timestamp } }
    }

    override suspend fun addEvaluation(evaluation: Evaluation): Result<Unit> {
        val updated = evaluationsFlow.value + evaluation
        saveEvaluations(updated)
        return Result.Success(Unit)
    }

    override suspend fun deleteEvaluation(id: String): Result<Unit> {
        val updated = evaluationsFlow.value.filter { it.id != id }
        saveEvaluations(updated)
        return Result.Success(Unit)
    }

    override suspend fun clearAll(): Result<Unit> {
        saveEvaluations(emptyList())
        return Result.Success(Unit)
    }
}
