package com.healthtrack.app.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthtrack.app.data.model.DoseLog
import com.healthtrack.app.data.model.Medication
import com.healthtrack.app.data.repository.MedicationRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class LocalMedicationRepository(
    private val localCache: LocalCache,
    private val gson: Gson = Gson()
) : MedicationRepository {

    private val KEY_MEDICATIONS = "medications"
    private val KEY_DOSE_LOGS = "dose_logs"

    private val medicationsFlow = MutableStateFlow<List<Medication>>(emptyList())
    private val doseLogsFlow = MutableStateFlow<List<DoseLog>>(emptyList())

    init {
        val medsJson = localCache.getString(KEY_MEDICATIONS)
        if (medsJson != null) {
            val type = object : TypeToken<List<Medication>>() {}.type
            val storedMeds: List<Medication> = gson.fromJson(medsJson, type) ?: emptyList()
            medicationsFlow.value = storedMeds
        }

        val doseLogsJson = localCache.getString(KEY_DOSE_LOGS)
        if (doseLogsJson != null) {
            val type = object : TypeToken<List<DoseLog>>() {}.type
            val storedDoseLogs: List<DoseLog> = gson.fromJson(doseLogsJson, type) ?: emptyList()
            doseLogsFlow.value = storedDoseLogs
        }
    }

    private fun saveMedications(list: List<Medication>) {
        medicationsFlow.value = list
        localCache.putString(KEY_MEDICATIONS, gson.toJson(list))
    }

    private fun saveDoseLogs(list: List<DoseLog>) {
        doseLogsFlow.value = list
        localCache.putString(KEY_DOSE_LOGS, gson.toJson(list))
    }

    override fun getMedications(): Flow<List<Medication>> = medicationsFlow

    override suspend fun addMedication(medication: Medication): Result<Unit> {
        val updated = medicationsFlow.value + medication
        saveMedications(updated)
        return Result.Success(Unit)
    }

    override suspend fun updateMedication(medication: Medication): Result<Unit> {
        val updated = medicationsFlow.value.map { if (it.id == medication.id) medication else it }
        saveMedications(updated)
        return Result.Success(Unit)
    }

    override suspend fun deleteMedication(medId: String): Result<Unit> {
        val updated = medicationsFlow.value.filter { it.id != medId }
        saveMedications(updated)
        return Result.Success(Unit)
    }

    override fun getDoseLogsForDate(dateKey: String): Flow<List<DoseLog>> {
        return doseLogsFlow.map { list -> list.filter { it.id.contains(dateKey) } }
    }

    override suspend fun markDose(doseLog: DoseLog): Result<Unit> {
        val existing = doseLogsFlow.value.find { it.id == doseLog.id }
        val updated = if (existing != null) {
            doseLogsFlow.value.map { if (it.id == doseLog.id) doseLog else it }
        } else {
            doseLogsFlow.value + doseLog
        }
        saveDoseLogs(updated)
        return Result.Success(Unit)
    }
}
