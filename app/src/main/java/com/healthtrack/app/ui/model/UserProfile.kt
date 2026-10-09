package com.healthtrack.app.ui.model


data class UserProfile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val weight: Double = 70.0,
    val dailyWaterTarget: Int = 2500,
    val riskTier: String = "Low Risk",
    val adherenceStreak: Int = 5,
    val healthConditions: List<String> = listOf("Hypertension", "Type 2 Diabetes"),
    val hipaaConsentAccepted: Boolean = true
)
