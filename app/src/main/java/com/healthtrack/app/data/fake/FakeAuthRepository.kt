package com.healthtrack.app.data.fake

import com.healthtrack.app.data.model.User
import com.healthtrack.app.data.repository.AuthRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    private val users = mutableMapOf<String, User>()

    override suspend fun login(email: String, password: String): Result<Unit> {
        val user = users.values.find { it.email == email }
        return if (user != null) {
            // Fake password check always succeeds if user exists
            _currentUser.value = user
            Result.Success(Unit)
        } else {
            Result.Error("Invalid email or password")
        }
    }

    override suspend fun register(name: String, email: String, password: String, weightKg: Float): Result<Unit> {
        if (users.values.any { it.email == email }) {
            return Result.Error("Email already in use")
        }
        val waterGoalMl = (weightKg * 35).toInt().let { it - (it % 10) }
        val newUser = User(
            uid = "fake_uid_${System.currentTimeMillis()}",
            name = name,
            email = email,
            weightKg = weightKg,
            waterGoalMl = waterGoalMl,
            goalIsManual = false,
            createdAt = System.currentTimeMillis()
        )
        users[newUser.uid] = newUser
        _currentUser.value = newUser
        return Result.Success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        _currentUser.value = null
        return Result.Success(Unit)
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return if (users.values.any { it.email == email }) {
            Result.Success(Unit)
        } else {
            Result.Error("Email not found")
        }
    }

    override suspend fun updateProfile(name: String, weightKg: Float, waterGoalMl: Int, goalIsManual: Boolean): Result<Unit> {
        val current = _currentUser.value ?: return Result.Error("Not logged in")
        val updated = current.copy(
            name = name,
            weightKg = weightKg,
            waterGoalMl = waterGoalMl,
            goalIsManual = goalIsManual
        )
        users[current.uid] = updated
        _currentUser.value = updated
        return Result.Success(Unit)
    }
}
