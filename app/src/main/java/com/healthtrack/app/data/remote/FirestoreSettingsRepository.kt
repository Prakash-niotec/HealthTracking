package com.healthtrack.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.healthtrack.app.data.local.LocalCache
import com.healthtrack.app.data.repository.NotificationSettings
import com.healthtrack.app.data.repository.SettingsRepository
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirestoreSettingsRepository(
    private val localCache: LocalCache,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val gson: Gson = Gson()
) : SettingsRepository {

    private val KEY_SETTINGS = "firestore_settings"
    private val _settings = MutableStateFlow(NotificationSettings())
    override val notificationSettings: Flow<NotificationSettings> = _settings.asStateFlow()

    private var settingsListener: ListenerRegistration? = null

    init {
        val json = localCache.getString(KEY_SETTINGS)
        if (json != null) {
            val s = gson.fromJson(json, NotificationSettings::class.java)
            if (s != null) {
                _settings.value = s
            }
        }

        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            settingsListener?.remove()

            if (user != null) {
                settingsListener = firestore.collection("users").document(user.uid)
                    .collection("settings").document("notifications")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null && snapshot.exists()) {
                            val medEnabled = snapshot.getBoolean("medicationEnabled") ?: true
                            val waterEnabled = snapshot.getBoolean("waterEnabled") ?: true
                            val waterInterval = snapshot.getLong("waterIntervalMinutes")?.toInt() ?: 60
                            val windowStart = snapshot.getLong("waterWindowStartHour")?.toInt() ?: 8
                            val windowEnd = snapshot.getLong("waterWindowEndHour")?.toInt() ?: 22

                            val newSettings = NotificationSettings(
                                medicationEnabled = medEnabled,
                                waterEnabled = waterEnabled,
                                waterIntervalMinutes = waterInterval,
                                waterWindowStartHour = windowStart,
                                waterWindowEndHour = windowEnd
                            )
                            saveToCache(newSettings)
                        }
                    }
            } else {
                saveToCache(NotificationSettings())
            }
        }
    }

    private fun saveToCache(settings: NotificationSettings) {
        _settings.value = settings
        localCache.putString(KEY_SETTINGS, gson.toJson(settings))
    }

    override suspend fun updateSettings(settings: NotificationSettings): Result<Unit> {
        val user = auth.currentUser ?: return Result.Error("Not authenticated")
        saveToCache(settings)
        
        return try {
            val map = mapOf(
                "medicationEnabled" to settings.medicationEnabled,
                "waterEnabled" to settings.waterEnabled,
                "waterIntervalMinutes" to settings.waterIntervalMinutes,
                "waterWindowStartHour" to settings.waterWindowStartHour,
                "waterWindowEndHour" to settings.waterWindowEndHour
            )
            firestore.collection("users").document(user.uid)
                .collection("settings").document("notifications")
                .set(map).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update settings", e)
        }
    }
}
