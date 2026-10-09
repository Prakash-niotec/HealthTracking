package com.healthtrack.app.data.repository

import com.healthtrack.app.data.model.User
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String, weightKg: Float): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun updateProfile(name: String, weightKg: Float, waterGoalMl: Int, goalIsManual: Boolean): Result<Unit>
}
