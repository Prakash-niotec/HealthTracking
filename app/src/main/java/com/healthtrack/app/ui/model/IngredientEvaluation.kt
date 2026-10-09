package com.healthtrack.app.ui.model


enum class RiskLevel(val displayName: String, val levelCode: String) {
    LOW("Safe", "LOW"),
    MODERATE("Caution", "MODERATE"),
    HIGH("High Risk", "HIGH"),
    CRITICAL("Severe Warning", "CRITICAL")
}

data class IngredientEvaluation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val unit: String = "mg",
    val healthCondition: String,
    val limit: Double,
    val ratio: Double,
    val riskLevel: RiskLevel,
    val recommendation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
