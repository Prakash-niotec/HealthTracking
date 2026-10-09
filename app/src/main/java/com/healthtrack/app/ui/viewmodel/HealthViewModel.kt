package com.healthtrack.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthtrack.app.data.model.DailyWaterGoal
import com.healthtrack.app.data.model.DoseLog
import com.healthtrack.app.data.model.DoseStatus
import com.healthtrack.app.data.model.FrequencyType
import com.healthtrack.app.data.model.Medication
import com.healthtrack.app.data.model.User
import com.healthtrack.app.data.model.WaterLog
import com.healthtrack.app.data.repository.*
import com.healthtrack.app.domain.engine.HealthRuleEngine
import com.healthtrack.app.domain.engine.EvaluationOutcome
import com.healthtrack.app.domain.engine.StarterRules
import com.healthtrack.app.ui.model.*
import com.healthtrack.app.ui.navigation.MainTab
import com.healthtrack.app.ui.navigation.Screen
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class HealthViewModel(
    private val authRepository: AuthRepository,
    private val waterRepository: WaterRepository,
    private val medicationRepository: MedicationRepository,
    private val evaluationRepository: EvaluationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val navStack = mutableListOf<Screen>(Screen.Splash)
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentMainTab = MutableStateFlow(MainTab.HOME)
    val currentMainTab: StateFlow<MainTab> = _currentMainTab.asStateFlow()

    private val _lastEvaluation = MutableStateFlow<Screen.EvaluationResult?>(null)
    val lastEvaluation: StateFlow<Screen.EvaluationResult?> = _lastEvaluation.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = authRepository.currentUser
        .map { it?.toUserProfile() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val authState: StateFlow<AuthState> = authRepository.currentUser
        .map { if (it != null) AuthState.Authenticated(it.toUserProfile()) else AuthState.Unauthenticated }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Unauthenticated)

    // Current date key for queries
    private val currentDateKey = LocalDate.now().toString()

    val hydrationLogs: StateFlow<List<HydrationLog>> = waterRepository.getLogsForDate(currentDateKey)
        .map { logs -> logs.map { HydrationLog(id = it.id, amountMl = it.amountMl, label = it.drinkType, timestamp = it.timestamp) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Convert core Medications into UI MedicationItems
    val medications: StateFlow<List<MedicationItem>> = medicationRepository.getMedications()
        .map { list -> list.map { MedicationItem(id = it.id, name = it.name, dosage = it.dosage) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun navigateTo(screen: Screen, clearStack: Boolean = false) {
        if (clearStack) navStack.clear()
        navStack.add(screen)
        _currentScreen.value = screen
    }

    fun popBackStack(): Boolean {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.size - 1)
            _currentScreen.value = navStack.last()
            return true
        }
        return false
    }

    fun selectMainTab(tab: MainTab) {
        _currentMainTab.value = tab
        if (_currentScreen.value !is Screen.Main) {
            navigateTo(Screen.Main(tab.routeKey))
        }
    }

    fun handleSplashFinished() {
        if (authState.value is AuthState.Authenticated) {
            navigateTo(Screen.Main(), clearStack = true)
        } else {
            navigateTo(Screen.Onboarding, clearStack = true)
        }
    }

    fun signIn(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = authRepository.login(email, pass)
            if (res is Result.Success) {
                navigateTo(Screen.Main(), clearStack = true)
                onSuccess()
            } else if (res is Result.Error) {
                onError(res.message)
            }
        }
    }

    fun signUp(name: String, email: String, pass: String, weight: Double, conditions: List<String>, hipaaConsent: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (!hipaaConsent) {
                onError("HIPAA consent required")
                return@launch
            }
            val res = authRepository.register(name, email, pass, weight.toFloat())
            if (res is Result.Success) {
                navigateTo(Screen.Main(), clearStack = true)
                onSuccess()
            } else if (res is Result.Error) {
                onError(res.message)
            }
        }
    }

    fun updateProfile(name: String, weightKg: Float, waterGoalMl: Int, goalIsManual: Boolean) {
        viewModelScope.launch {
            authRepository.updateProfile(name, weightKg, waterGoalMl, goalIsManual)
            waterRepository.setGoalForDate(DailyWaterGoal(currentDateKey, waterGoalMl))
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.logout()
            navigateTo(Screen.Onboarding, clearStack = true)
        }
    }

    fun addHydrationLog(amountMl: Int, label: String, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val settings = settingsRepository.notificationSettings.first()
            val factor = settings.hydrationFactors[label] ?: 1.0f
            val effectiveMl = (amountMl * factor).toInt()
            val log = WaterLog(
                id = UUID.randomUUID().toString(),
                amountMl = amountMl,
                timestamp = timestamp,
                dateKey = currentDateKey,
                drinkType = label,
                effectiveMl = effectiveMl
            )
            waterRepository.addLog(log)
        }
    }

    fun deleteHydrationLog(id: String) {
        viewModelScope.launch {
            waterRepository.deleteLog(id)
        }
    }
    
    fun editHydrationLog(id: String, newAmountMl: Int, newType: String, newTimestamp: Long) {
        viewModelScope.launch {
            val settings = settingsRepository.notificationSettings.first()
            val factor = settings.hydrationFactors[newType] ?: 1.0f
            val effectiveMl = (newAmountMl * factor).toInt()
            
            val log = WaterLog(
                id = id,
                amountMl = newAmountMl,
                timestamp = newTimestamp,
                dateKey = currentDateKey,
                drinkType = newType,
                effectiveMl = effectiveMl
            )
            waterRepository.updateLog(log)
        }
    }

    fun addMedication(item: MedicationItem) {
        viewModelScope.launch {
            val med = Medication(
                id = item.id,
                name = item.name,
                dosage = item.dosage,
                frequencyType = FrequencyType.DAILY,
                times = listOf("08:00"),
                days = emptySet(),
                startDate = System.currentTimeMillis(),
                endDate = null,
                isActive = true
            )
            medicationRepository.addMedication(med)
        }
    }

    fun deleteMedication(id: String) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(id)
        }
    }

    fun toggleMedicationTaken(id: String) {
        viewModelScope.launch {
            val doseLogs = medicationRepository.getDoseLogsForDate(currentDateKey).first()
            val log = doseLogs.find { it.medId == id }
            if (log != null) {
                val newStatus = if (log.status == DoseStatus.TAKEN) {
                    DoseStatus.PENDING
                } else {
                    DoseStatus.TAKEN
                }
                medicationRepository.markDose(log.copy(status = newStatus, actedAt = System.currentTimeMillis()))
            } else {
                val med = medicationRepository.getMedications().first().find { it.id == id } ?: return@launch
                val newLog = DoseLog(
                    id = "${id}_${System.currentTimeMillis()}",
                    medId = id,
                    medName = med.name,
                    medDosage = med.dosage,
                    scheduledAt = System.currentTimeMillis(),
                    status = DoseStatus.TAKEN,
                    actedAt = System.currentTimeMillis()
                )
                medicationRepository.markDose(newLog)
            }
        }
    }

    fun evaluateIngredient(name: String, amount: Double, unit: String, condition: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val engine = HealthRuleEngine(StarterRules.defaultRules)
            val outcome = engine.evaluate(name, amount, unit, condition)

            when (outcome) {
                is EvaluationOutcome.Evaluated -> {
                    _lastEvaluation.value = Screen.EvaluationResult(
                        ingredientName = name,
                        amount = amount,
                        unit = unit,
                        condition = condition,
                        riskLevel = outcome.result.name,
                        recommendation = outcome.explanation
                    )
                    onSuccess()
                }
                is EvaluationOutcome.NoRuleFound -> {
                    _lastEvaluation.value = Screen.EvaluationResult(
                        ingredientName = name,
                        amount = amount,
                        unit = unit,
                        condition = condition,
                        riskLevel = "MODERATE",
                        recommendation = outcome.message
                    )
                    onSuccess()
                }
                is EvaluationOutcome.ValidationError -> {
                    _lastEvaluation.value = Screen.EvaluationResult(
                        ingredientName = name,
                        amount = amount,
                        unit = unit,
                        condition = condition,
                        riskLevel = "HIGH",
                        recommendation = outcome.message
                    )
                    onSuccess()
                }
            }
        }
    }

    private fun User.toUserProfile() = UserProfile(
        id = this.uid,
        name = this.name,
        email = this.email,
        weight = this.weightKg.toDouble(),
        dailyWaterTarget = this.waterGoalMl,
        riskTier = "Low Risk",
        adherenceStreak = 0,
        healthConditions = emptyList(),
        hipaaConsentAccepted = true
    )
}
