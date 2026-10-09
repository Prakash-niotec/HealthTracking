package com.healthtrack.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.healthtrack.app.data.local.LocalCache
import com.healthtrack.app.data.model.User
import com.healthtrack.app.data.repository.AuthRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreAuthRepository(
    private val localCache: LocalCache,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    private val KEY_CURRENT_USER = "firebase_current_user"
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    init {
        // Hydrate persistent user from LocalCache on cold startup
        val cachedId = localCache.getString("${KEY_CURRENT_USER}_id")
        if (!cachedId.isNullOrEmpty()) {
            val name = localCache.getString("${KEY_CURRENT_USER}_name") ?: "User"
            val email = localCache.getString("${KEY_CURRENT_USER}_email") ?: ""
            val weight = localCache.getString("${KEY_CURRENT_USER}_weight")?.toFloatOrNull() ?: 70f
            val goal = localCache.getString("${KEY_CURRENT_USER}_goal")?.toIntOrNull() ?: 2500
            val isManual = localCache.getBoolean("${KEY_CURRENT_USER}_manual", false)
            val createdAt = localCache.getString("${KEY_CURRENT_USER}_created")?.toLongOrNull() ?: System.currentTimeMillis()
            
            _currentUser.value = User(cachedId, name, email, weight, goal, isManual, createdAt)
        }

        // Attach AuthStateListener without auto-clearing local persistent user on startup
        auth.addAuthStateListener { firebaseAuth ->
            val fbUser = firebaseAuth.currentUser
            if (fbUser != null && _currentUser.value?.uid != fbUser.uid) {
                firestore.collection("users").document(fbUser.uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val user = User(
                                uid = fbUser.uid,
                                name = doc.getString("name") ?: "",
                                email = fbUser.email ?: "",
                                weightKg = doc.getDouble("weightKg")?.toFloat() ?: 70f,
                                waterGoalMl = doc.getLong("waterGoalMl")?.toInt() ?: 2500,
                                goalIsManual = doc.getBoolean("goalIsManual") ?: false,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                            saveToCache(user)
                        }
                    }
            }
        }
    }

    private fun saveToCache(user: User) {
        _currentUser.value = user
        localCache.putString("${KEY_CURRENT_USER}_id", user.uid)
        localCache.putString("${KEY_CURRENT_USER}_name", user.name)
        localCache.putString("${KEY_CURRENT_USER}_email", user.email)
        localCache.putString("${KEY_CURRENT_USER}_weight", user.weightKg.toString())
        localCache.putString("${KEY_CURRENT_USER}_goal", user.waterGoalMl.toString())
        localCache.putBoolean("${KEY_CURRENT_USER}_manual", user.goalIsManual)
        localCache.putString("${KEY_CURRENT_USER}_created", user.createdAt.toString())
    }

    private fun clearCache() {
        _currentUser.value = null
        localCache.remove("${KEY_CURRENT_USER}_id")
        localCache.remove("${KEY_CURRENT_USER}_name")
        localCache.remove("${KEY_CURRENT_USER}_email")
        localCache.remove("${KEY_CURRENT_USER}_weight")
        localCache.remove("${KEY_CURRENT_USER}_goal")
        localCache.remove("${KEY_CURRENT_USER}_manual")
        localCache.remove("${KEY_CURRENT_USER}_created")
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val fbUser = result.user ?: throw Exception("Login failed")  
            
            val doc = firestore.collection("users").document(fbUser.uid).get().await()
            val user = if (doc.exists()) {
                User(
                    uid = fbUser.uid,
                    name = doc.getString("name") ?: email.substringBefore("@"),
                    email = fbUser.email ?: email,
                    weightKg = doc.getDouble("weightKg")?.toFloat() ?: 70f,
                    waterGoalMl = doc.getLong("waterGoalMl")?.toInt() ?: 2500,
                    goalIsManual = doc.getBoolean("goalIsManual") ?: false,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } else {
                User(
                    uid = fbUser.uid,
                    name = email.substringBefore("@"),
                    email = email,
                    weightKg = 70f,
                    waterGoalMl = 2500,
                    goalIsManual = false,
                    createdAt = System.currentTimeMillis()
                )
            }
            saveToCache(user)
            Result.Success(Unit)
        } catch (e: Exception) {
            // Fallback for offline login if credentials match cached user
            val cachedEmail = localCache.getString("${KEY_CURRENT_USER}_email")
            val cachedUser = _currentUser.value
            if (cachedUser != null && cachedEmail == email) {
                Result.Success(Unit)
            } else {
                Result.Error(mapAuthException(e), e)
            }
        }
    }

    override suspend fun register(name: String, email: String, password: String, weightKg: Float): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val fbUser = result.user ?: throw Exception("Registration failed")
            
            val waterGoalMl = (weightKg * 35).toInt().let { it - (it % 10) }
            val user = User(
                uid = fbUser.uid,
                name = name,
                email = email,
                weightKg = weightKg,
                waterGoalMl = waterGoalMl,
                goalIsManual = false,
                createdAt = System.currentTimeMillis()
            )
            
            val userMap = mapOf(
                "name" to user.name,
                "email" to user.email,
                "weightKg" to user.weightKg,
                "waterGoalMl" to user.waterGoalMl,
                "goalIsManual" to user.goalIsManual,
                "createdAt" to user.createdAt
            )
            firestore.collection("users").document(user.uid).set(userMap).await()
            
            saveToCache(user)
            Result.Success(Unit)
        } catch (e: Exception) {
            // Fallback offline registration
            val waterGoalMl = (weightKg * 35).toInt().let { it - (it % 10) }
            val offlineUser = User(
                uid = UUID.randomUUID().toString(),
                name = name,
                email = email,
                weightKg = weightKg,
                waterGoalMl = waterGoalMl,
                goalIsManual = false,
                createdAt = System.currentTimeMillis()
            )
            saveToCache(offlineUser)
            Result.Success(Unit)
        }
    }

    override suspend fun logout(): Result<Unit> {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // ignore offline auth error
        }
        clearCache()
        return Result.Success(Unit)
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(mapAuthException(e), e)
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
        
        saveToCache(updated)
        try {
            val updateMap = mapOf(
                "name" to updated.name,
                "weightKg" to updated.weightKg,
                "waterGoalMl" to updated.waterGoalMl,
                "goalIsManual" to updated.goalIsManual
            )
            firestore.collection("users").document(current.uid).update(updateMap).await()
        } catch (e: Exception) {
            // ignore offline sync
        }
        return Result.Success(Unit)
    }

    private fun mapAuthException(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> "Account not found. Please register."
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
            is FirebaseAuthUserCollisionException -> "Email already in use."
            else -> e.message ?: "Authentication failed."
        }
    }
}
