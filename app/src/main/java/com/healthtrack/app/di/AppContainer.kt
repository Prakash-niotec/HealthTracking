package com.healthtrack.app.di

import com.healthtrack.app.data.remote.*
import com.healthtrack.app.data.fake.*
import com.healthtrack.app.data.repository.*
import com.healthtrack.app.util.DefaultTimeProvider
import com.healthtrack.app.util.TimeProvider

import com.healthtrack.app.data.local.*
import android.content.Context

interface AppContainer {
    val authRepository: AuthRepository
    val waterRepository: WaterRepository
    val medicationRepository: MedicationRepository
    val evaluationRepository: EvaluationRepository
    val settingsRepository: SettingsRepository
    val timeProvider: TimeProvider
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    // Switch this to true in Phase 3 to use real Firebase repositories
    private val useFirebase = true

    private val localCache by lazy { LocalCache(context) }

    override val authRepository: AuthRepository by lazy {
        if (useFirebase) FirestoreAuthRepository(localCache)
        else LocalAuthRepository(localCache)
    }

    override val waterRepository: WaterRepository by lazy {
        if (useFirebase) FirestoreWaterRepository(localCache)
        else LocalWaterRepository(localCache)
    }

    override val medicationRepository: MedicationRepository by lazy {
        if (useFirebase) FirestoreMedicationRepository(localCache)
        else LocalMedicationRepository(localCache)
    }

    override val evaluationRepository: EvaluationRepository by lazy {
        if (useFirebase) FirestoreEvaluationRepository(localCache)
        else LocalEvaluationRepository(localCache)
    }

    override val settingsRepository: SettingsRepository by lazy {
        if (useFirebase) FirestoreSettingsRepository(localCache)
        else LocalSettingsRepository(localCache)
    }

    override val timeProvider: TimeProvider by lazy {
        DefaultTimeProvider()
    }
}
