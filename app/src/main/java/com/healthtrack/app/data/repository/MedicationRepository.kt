package com.healthtrack.app.data.repository

import com.healthtrack.app.data.model.DoseLog
import com.healthtrack.app.data.model.DoseStatus
import com.healthtrack.app.data.model.Medication
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getMedications(): Flow<List<Medication>>
    suspend fun addMedication(medication: Medication): Result<Unit>
    suspend fun updateMedication(medication: Medication): Result<Unit>
    suspend fun deleteMedication(medId: String): Result<Unit>
    
    fun getDoseLogsForDate(dateKey: String): Flow<List<DoseLog>>
    suspend fun markDose(doseLog: DoseLog): Result<Unit>
}
