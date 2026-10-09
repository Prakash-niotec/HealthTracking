package com.healthtrack.app.data.model

enum class EvaluationResult {
    SAFE,
    UNSAFE
}

data class Evaluation(
    val id: String,
    val ingredient: String,
    val value: Float,
    val unit: String,
    val condition: String,
    val ruleId: String?,
    val threshold: Float?,
    val thresholdUnit: String?,
    val result: EvaluationResult?,
    val reason: String,
    val timestamp: Long
)
