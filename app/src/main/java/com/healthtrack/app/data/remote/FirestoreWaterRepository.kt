package com.healthtrack.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthtrack.app.data.local.LocalCache
import com.healthtrack.app.data.model.WaterLog
import com.healthtrack.app.data.repository.WaterRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreWaterRepository(
    private val localCache: LocalCache,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val gson: Gson = Gson()
) : WaterRepository {

    private val KEY_WATER_LOGS = "firestore_water_logs"
    private val logsFlow = MutableStateFlow<List<WaterLog>>(emptyList())
    private var listenerRegistration: ListenerRegistration? = null

    init {
        // Hydrate from LocalCache instantly
        val json = localCache.getString(KEY_WATER_LOGS)
        if (json != null) {
            val type = object : TypeToken<List<WaterLog>>() {}.type
            val storedLogs: List<WaterLog> = gson.fromJson(json, type) ?: emptyList()
            logsFlow.value = storedLogs
        }

        // Attach listener for real-time Firestore updates
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            listenerRegistration?.remove()
            
            if (user != null) {
                listenerRegistration = firestore.collection("users")
                    .document(user.uid)
                    .collection("waterLogs")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val newLogs = snapshot.documents.mapNotNull { doc ->
                                val id = doc.id
                                val amountMl = doc.getLong("amountMl")?.toInt() ?: return@mapNotNull null
                                val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                                val dateKey = doc.getString("dateKey") ?: return@mapNotNull null
                                WaterLog(id, amountMl, timestamp, dateKey)
                            }
                            saveToCache(newLogs)
                        }
                    }
            } else {
                saveToCache(emptyList())
            }
        }
    }

    private fun saveToCache(logs: List<WaterLog>) {
        logsFlow.value = logs
        localCache.putString(KEY_WATER_LOGS, gson.toJson(logs))
    }

    override fun getLogsForDate(dateKey: String): Flow<List<WaterLog>> {
        return logsFlow.map { list -> list.filter { it.dateKey == dateKey } }
    }

    override suspend fun addLog(amountMl: Int, dateKey: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        val id = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        
        val newLog = WaterLog(id, amountMl, timestamp, dateKey)
        
        // Write-through to cache instantly
        val updatedList = (listOf(newLog) + logsFlow.value).sortedByDescending { it.timestamp }
        saveToCache(updatedList)

        // Then to Firestore
        return try {
            val logMap = mapOf(
                "amountMl" to amountMl,
                "timestamp" to timestamp,
                "dateKey" to dateKey
            )
            firestore.collection("users").document(user.uid)
                .collection("waterLogs").document(id)
                .set(logMap).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add log", e)
        }
    }

    override suspend fun undoLastLog(dateKey: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        val currentLogs = logsFlow.value.filter { it.dateKey == dateKey }.sortedByDescending { it.timestamp }
        if (currentLogs.isEmpty()) {
            return Result.Error("No logs to undo for today")
        }
        val logToRemove = currentLogs.first()
        
        // Write-through to cache instantly
        val updatedList = logsFlow.value.filter { it.id != logToRemove.id }
        saveToCache(updatedList)

        // Delete from Firestore
        return try {
            firestore.collection("users").document(user.uid)
                .collection("waterLogs").document(logToRemove.id)
                .delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to undo log", e)
        }
    }
}
