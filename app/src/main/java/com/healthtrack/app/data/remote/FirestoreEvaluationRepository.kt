package com.healthtrack.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthtrack.app.data.local.LocalCache
import com.healthtrack.app.data.model.Evaluation
import com.healthtrack.app.data.model.EvaluationResult
import com.healthtrack.app.data.repository.EvaluationRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreEvaluationRepository(
    private val localCache: LocalCache,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val gson: Gson = Gson()
) : EvaluationRepository {

    private val KEY_EVALS = "firestore_evaluations"
    private val evalsFlow = MutableStateFlow<List<Evaluation>>(emptyList())
    private var evalsListener: ListenerRegistration? = null

    init {
        val json = localCache.getString(KEY_EVALS)
        if (json != null) {
            val type = object : TypeToken<List<Evaluation>>() {}.type
            evalsFlow.value = gson.fromJson(json, type) ?: emptyList()
        }

        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            evalsListener?.remove()

            if (user != null) {
                evalsListener = firestore.collection("users").document(user.uid)
                    .collection("evaluations")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val evals = snapshot.documents.mapNotNull { doc ->
                                val id = doc.id
                                val ingredient = doc.getString("ingredient") ?: return@mapNotNull null
                                val value = doc.getDouble("value")?.toFloat() ?: return@mapNotNull null
                                val unit = doc.getString("unit") ?: return@mapNotNull null
                                val condition = doc.getString("condition") ?: return@mapNotNull null
                                val ruleId = doc.getString("ruleId")
                                val threshold = doc.getDouble("threshold")?.toFloat()
                                val thresholdUnit = doc.getString("thresholdUnit")
                                val resultStr = doc.getString("result")
                                val result = if (resultStr != null) runCatching { EvaluationResult.valueOf(resultStr) }.getOrNull() else null
                                val reason = doc.getString("reason") ?: return@mapNotNull null
                                val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null

                                Evaluation(id, ingredient, value, unit, condition, ruleId, threshold, thresholdUnit, result, reason, timestamp)
                            }
                            saveToCache(evals)
                        }
                    }
            } else {
                saveToCache(emptyList())
            }
        }
    }

    private fun saveToCache(list: List<Evaluation>) {
        evalsFlow.value = list
        localCache.putString(KEY_EVALS, gson.toJson(list))
    }

    override fun getEvaluations(): Flow<List<Evaluation>> {
        return evalsFlow.map { list -> list.sortedByDescending { it.timestamp } }
    }

    override suspend fun addEvaluation(evaluation: Evaluation): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        saveToCache((listOf(evaluation) + evalsFlow.value).sortedByDescending { it.timestamp })
        
        return try {
            val map = mapOf(
                "ingredient" to evaluation.ingredient,
                "value" to evaluation.value,
                "unit" to evaluation.unit,
                "condition" to evaluation.condition,
                "ruleId" to evaluation.ruleId,
                "threshold" to evaluation.threshold,
                "thresholdUnit" to evaluation.thresholdUnit,
                "result" to evaluation.result?.name,
                "reason" to evaluation.reason,
                "timestamp" to evaluation.timestamp
            )
            firestore.collection("users").document(user.uid)
                .collection("evaluations").document(evaluation.id)
                .set(map).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save evaluation", e)
        }
    }

    override suspend fun deleteEvaluation(id: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        saveToCache(evalsFlow.value.filter { it.id != id })
        
        return try {
            firestore.collection("users").document(user.uid)
                .collection("evaluations").document(id)
                .delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete evaluation", e)
        }
    }

    override suspend fun clearAll(): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        saveToCache(emptyList())
        
        return try {
            val docs = firestore.collection("users").document(user.uid).collection("evaluations").get().await()
            firestore.runBatch { batch ->
                for (doc in docs.documents) {
                    batch.delete(doc.reference)
                }
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to clear evaluations", e)
        }
    }
}
