package com.healthtrack.app.data.model

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val weightKg: Float,
    val waterGoalMl: Int,
    val goalIsManual: Boolean,
    val createdAt: Long
)
