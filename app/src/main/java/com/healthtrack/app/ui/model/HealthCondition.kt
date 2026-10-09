package com.healthtrack.app.ui.model


data class HealthCondition(
    val id: String,
    val name: String,
    val category: String,
    val description: String = "",
    val restrictedIngredients: List<String> = emptyList()
)
