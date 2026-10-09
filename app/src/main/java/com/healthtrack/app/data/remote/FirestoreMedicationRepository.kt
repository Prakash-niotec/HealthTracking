package com.healthtrack.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthtrack.app.data.local.LocalCache
import com.healthtrack.app.data.model.DoseLog
import com.healthtrack.app.data.model.DoseStatus
import com.healthtrack.app.data.model.FrequencyType
import com.healthtrack.app.data.model.Medication
import com.healthtrack.app.data.repository.MedicationRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreMedicationRepository(
    private val localCache: LocalCache,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val gson: Gson = Gson()
) : MedicationRepository {

    private val KEY_MEDS = "firestore_meds"
    private val KEY_DOSE_LOGS = "firestore_dose_logs"

    private val medsFlow = MutableStateFlow<List<Medication>>(emptyList())
    private val doseLogsFlow = MutableStateFlow<List<DoseLog>>(emptyList())

    private var medsListener: ListenerRegistration? = null
    private var doseLogsListener: ListenerRegistration? = null

    init {
        val medsJson = localCache.getString(KEY_MEDS)
        if (medsJson != null) {
            val type = object : TypeToken<List<Medication>>() {}.type
            medsFlow.value = gson.fromJson(medsJson, type) ?: emptyList()
        }

        val doseLogsJson = localCache.getString(KEY_DOSE_LOGS)
        if (doseLogsJson != null) {
            val type = object : TypeToken<List<DoseLog>>() {}.type
            doseLogsFlow.value = gson.fromJson(doseLogsJson, type) ?: emptyList()
        }

        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            medsListener?.remove()
            doseLogsListener?.remove()

            if (user != null) {
                medsListener = firestore.collection("users").document(user.uid)
                    .collection("medications")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val meds = snapshot.documents.mapNotNull { doc ->
                                val id = doc.id
                                val name = doc.getString("name") ?: return@mapNotNull null
                                val dosage = doc.getString("dosage") ?: return@mapNotNull null
                                val freqStr = doc.getString("frequencyType") ?: FrequencyType.DAILY.name
                                val freq = runCatching { FrequencyType.valueOf(freqStr) }.getOrDefault(FrequencyType.DAILY)
                                val times = doc.get("times") as? List<String> ?: emptyList()
                                val daysList = doc.get("days") as? List<Long> ?: emptyList()
                                val startDate = doc.getLong("startDate") ?: System.currentTimeMillis()
                                val endDate = doc.getLong("endDate")
                                val isActive = doc.getBoolean("isActive") ?: true

                                Medication(id, name, dosage, freq, times, daysList.map { it.toInt() }.toSet(), startDate, endDate, isActive)
                            }
                            saveMedsToCache(meds)
                        }
                    }

                doseLogsListener = firestore.collection("users").document(user.uid)
                    .collection("doseLogs")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val doseLogs = snapshot.documents.mapNotNull { doc ->
                                val id = doc.id
                                val medId = doc.getString("medId") ?: return@mapNotNull null
                                val medName = doc.getString("medName") ?: return@mapNotNull null
                                val medDosage = doc.getString("medDosage") ?: ""
                                val scheduledAt = doc.getLong("scheduledAt") ?: return@mapNotNull null
                                val statusStr = doc.getString("status") ?: return@mapNotNull null
                                val status = runCatching { DoseStatus.valueOf(statusStr) }.getOrDefault(DoseStatus.PENDING)
                                val actedAt = doc.getLong("actedAt")

                                DoseLog(id, medId, medName, medDosage, scheduledAt, status, actedAt)
                            }
                            saveDoseLogsToCache(doseLogs)
                        }
                    }
            } else {
                saveMedsToCache(emptyList())
                saveDoseLogsToCache(emptyList())
            }
        }
    }

    private fun saveMedsToCache(list: List<Medication>) {
        medsFlow.value = list
        localCache.putString(KEY_MEDS, gson.toJson(list))
    }

    private fun saveDoseLogsToCache(list: List<DoseLog>) {
        doseLogsFlow.value = list
        localCache.putString(KEY_DOSE_LOGS, gson.toJson(list))
    }

    override fun getMedications(): Flow<List<Medication>> = medsFlow

    override suspend fun addMedication(medication: Medication): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        saveMedsToCache(medsFlow.value + medication)
        return try {
            val map = mapOf(
                "name" to medication.name,
                "dosage" to medication.dosage,
                "frequencyType" to medication.frequencyType.name,
                "times" to medication.times,
                "days" to medication.days.toList(),
                "startDate" to medication.startDate,
                "endDate" to medication.endDate,
                "isActive" to medication.isActive
            )
            firestore.collection("users").document(user.uid)
                .collection("medications").document(medication.id)
                .set(map).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error adding medication", e)
        }
    }

    override suspend fun updateMedication(medication: Medication): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        saveMedsToCache(medsFlow.value.map { if (it.id == medication.id) medication else it })
        return try {
            val map = mapOf(
                "name" to medication.name,
                "dosage" to medication.dosage,
                "frequencyType" to medication.frequencyType.name,
                "times" to medication.times,
                "days" to medication.days.toList(),
                "startDate" to medication.startDate,
                "endDate" to medication.endDate,
                "isActive" to medication.isActive
            )
            firestore.collection("users").document(user.uid)
                .collection("medications").document(medication.id)
                .update(map).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error updating medication", e)
        }
    }

    override suspend fun deleteMedication(medId: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        saveMedsToCache(medsFlow.value.filter { it.id != medId })
        return try {
            firestore.collection("users").document(user.uid)
                .collection("medications").document(medId)
                .delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error deleting medication", e)
        }
    }

    override fun getDoseLogsForDate(dateKey: String): Flow<List<DoseLog>> {
        return doseLogsFlow.map { list -> list.filter { it.id.contains(dateKey) } }
    }

    override suspend fun markDose(doseLog: DoseLog): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        val existing = doseLogsFlow.value.find { it.id == doseLog.id }
        val updatedList = if (existing != null) {
            doseLogsFlow.value.map { if (it.id == doseLog.id) doseLog else it }
        } else {
            doseLogsFlow.value + doseLog
        }
        saveDoseLogsToCache(updatedList)

        return try {
            val map = mapOf(
                "medId" to doseLog.medId,
                "medName" to doseLog.medName,
                "medDosage" to doseLog.medDosage,
                "scheduledAt" to doseLog.scheduledAt,
                "status" to doseLog.status.name,
                "actedAt" to doseLog.actedAt
            )
            firestore.collection("users").document(user.uid)
                .collection("doseLogs").document(doseLog.id)
                .set(map).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error marking dose", e)
        }
    }
}
