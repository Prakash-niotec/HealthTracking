package com.healthtrack.app.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthtrack.app.data.model.User
import com.healthtrack.app.data.repository.AuthRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class LocalAuthRepository(
    private val localCache: LocalCache,
    private val gson: Gson = Gson()
) : AuthRepository {

    private val KEY_CURRENT_USER = "current_user"
    private val KEY_ALL_USERS = "all_users"

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    init {
        val userJson = localCache.getString(KEY_CURRENT_USER)
        if (userJson != null) {
            val user = gson.fromJson(userJson, User::class.java)
            _currentUser.value = user
        }
    }

    private fun getAllUsers(): MutableMap<String, User> {
        val json = localCache.getString(KEY_ALL_USERS) ?: return mutableMapOf()
        val type = object : TypeToken<Map<String, User>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }

    private fun saveAllUsers(users: Map<String, User>) {
        localCache.putString(KEY_ALL_USERS, gson.toJson(users))
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        val users = getAllUsers()
        val user = users.values.find { it.email == email }
        return if (user != null) {
            // Fake password check 
            _currentUser.value = user
            localCache.putString(KEY_CURRENT_USER, gson.toJson(user))
            Result.Success(Unit)
        } else {
            Result.Error("Invalid email or password")
        }
    }

    override suspend fun register(name: String, email: String, password: String, weightKg: Float): Result<Unit> {
        val users = getAllUsers()
        if (users.values.any { it.email == email }) {
            return Result.Error("Email already in use")
        }
        val waterGoalMl = (weightKg * 35).toInt().let { it - (it % 10) }
        val newUser = User(
            uid = UUID.randomUUID().toString(),
            name = name,
            email = email,
            weightKg = weightKg,
            waterGoalMl = waterGoalMl,
            goalIsManual = false,
            createdAt = System.currentTimeMillis()
        )
        users[newUser.uid] = newUser
        saveAllUsers(users)
        
        _currentUser.value = newUser
        localCache.putString(KEY_CURRENT_USER, gson.toJson(newUser))
        return Result.Success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        _currentUser.value = null
        localCache.remove(KEY_CURRENT_USER)
        return Result.Success(Unit)
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        val users = getAllUsers()
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
        
        val users = getAllUsers()
        users[current.uid] = updated
        saveAllUsers(users)
        
        _currentUser.value = updated
        localCache.putString(KEY_CURRENT_USER, gson.toJson(updated))
        return Result.Success(Unit)
    }
}
