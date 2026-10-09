package com.healthtrack.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthtrack.app.data.local.LocalCache
import com.healthtrack.app.data.model.DailyWaterGoal
import com.healthtrack.app.data.model.WaterLog
import com.healthtrack.app.data.repository.WaterRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreWaterRepository(
    private val localCache: LocalCache,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val gson: Gson = Gson()
) : WaterRepository {

    private val KEY_WATER_LOGS = "firestore_water_logs"
    private val KEY_DAILY_GOALS = "firestore_daily_goals"
    private val logsFlow = MutableStateFlow<List<WaterLog>>(emptyList())
    private val goalsFlow = MutableStateFlow<List<DailyWaterGoal>>(emptyList())
    
    private var logsListenerRegistration: ListenerRegistration? = null
    private var goalsListenerRegistration: ListenerRegistration? = null

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

        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            logsListenerRegistration?.remove()
            goalsListenerRegistration?.remove()
            
            if (user != null) {
                logsListenerRegistration = firestore.collection("users")
                    .document(user.uid)
                    .collection("waterLogs")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val newLogs = snapshot.documents.mapNotNull { doc ->
                                val id = doc.id
                                val amountMl = doc.getLong("amountMl")?.toInt() ?: return@mapNotNull null
                                val effectiveMl = doc.getLong("effectiveMl")?.toInt() ?: amountMl
                                val drinkType = doc.getString("drinkType") ?: "Water"
                                val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                                val dateKey = doc.getString("dateKey") ?: return@mapNotNull null
                                WaterLog(id, amountMl, timestamp, dateKey, drinkType, effectiveMl)
                            }
                            saveLogsToCache(newLogs)
                        }
                    }

                goalsListenerRegistration = firestore.collection("users")
                    .document(user.uid)
                    .collection("waterGoals")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val newGoals = snapshot.documents.mapNotNull { doc ->
                                val dateKey = doc.id
                                val goalMl = doc.getLong("goalMl")?.toInt() ?: return@mapNotNull null
                                DailyWaterGoal(dateKey, goalMl)
                            }
                            saveGoalsToCache(newGoals)
                        }
                    }
            } else {
                saveLogsToCache(emptyList())
                saveGoalsToCache(emptyList())
            }
        }
    }

    private fun saveLogsToCache(logs: List<WaterLog>) {
        logsFlow.value = logs
        localCache.putString(KEY_WATER_LOGS, gson.toJson(logs))
    }

    private fun saveGoalsToCache(goals: List<DailyWaterGoal>) {
        goalsFlow.value = goals
        localCache.putString(KEY_DAILY_GOALS, gson.toJson(goals))
    }

    override fun getLogsForDate(dateKey: String): Flow<List<WaterLog>> {
        return logsFlow.map { list -> list.filter { it.dateKey == dateKey } }
    }

    override suspend fun addLog(log: WaterLog): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        
        val updatedList = (listOf(log) + logsFlow.value).sortedByDescending { it.timestamp }
        saveLogsToCache(updatedList)

        return try {
            val logMap = mapOf(
                "amountMl" to log.amountMl,
                "effectiveMl" to log.effectiveMl,
                "drinkType" to log.drinkType,
                "timestamp" to log.timestamp,
                "dateKey" to log.dateKey
            )
            firestore.collection("users").document(user.uid)
                .collection("waterLogs").document(log.id)
                .set(logMap).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add log", e)
        }
    }

    override suspend fun updateLog(log: WaterLog): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        
        val updatedList = logsFlow.value.map { if (it.id == log.id) log else it }.sortedByDescending { it.timestamp }
        saveLogsToCache(updatedList)

        return try {
            val logMap = mapOf(
                "amountMl" to log.amountMl,
                "effectiveMl" to log.effectiveMl,
                "drinkType" to log.drinkType,
                "timestamp" to log.timestamp,
                "dateKey" to log.dateKey
            )
            firestore.collection("users").document(user.uid)
                .collection("waterLogs").document(log.id)
                .update(logMap).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update log", e)
        }
    }

    override suspend fun deleteLog(id: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        
        val updatedList = logsFlow.value.filter { it.id != id }
        saveLogsToCache(updatedList)

        return try {
            firestore.collection("users").document(user.uid)
                .collection("waterLogs").document(id)
                .delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete log", e)
        }
    }

    override suspend fun undoLastLog(dateKey: String): Result<Unit> {
        val currentLogs = logsFlow.value.filter { it.dateKey == dateKey }.sortedByDescending { it.timestamp }
        if (currentLogs.isEmpty()) {
            return Result.Error("No logs to undo for today")
        }
        val logToRemove = currentLogs.first()
        return deleteLog(logToRemove.id)
    }

    override fun getGoalForDate(dateKey: String): Flow<DailyWaterGoal?> {
        return goalsFlow.map { list -> list.find { it.dateKey == dateKey } }
    }

    override suspend fun setGoalForDate(dailyGoal: DailyWaterGoal): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        val existing = goalsFlow.value.find { it.dateKey == dailyGoal.dateKey }
        val updatedList = if (existing != null) {
            goalsFlow.value.map { if (it.dateKey == dailyGoal.dateKey) dailyGoal else it }
        } else {
            goalsFlow.value + dailyGoal
        }
        saveGoalsToCache(updatedList)

        return try {
            val map = mapOf("goalMl" to dailyGoal.goalMl)
            firestore.collection("users").document(user.uid)
                .collection("waterGoals").document(dailyGoal.dateKey)
                .set(map).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update daily goal", e)
        }
    }
}
