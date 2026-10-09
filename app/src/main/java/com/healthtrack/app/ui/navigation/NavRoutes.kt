package com.healthtrack.app.ui.navigation

sealed interface Screen {
    data object Splash : Screen
    data object Onboarding : Screen
    data object SignIn : Screen
    data object SignUp : Screen
    data class Main(val initialTab: String = "home") : Screen
    data class AddMedication(val editMedicationId: String? = null) : Screen
    data class EvaluationResult(
        val ingredientName: String = "",
        val amount: Double = 0.0,
        val unit: String = "mg",
        val condition: String = "",
        val riskLevel: String = "LOW",
        val recommendation: String = ""
    ) : Screen
}

enum class MainTab(val routeKey: String, val title: String) {
    HOME("home", "Home"),
    WATER("water", "Hydration"),
    MEDS("meds", "Medications"),
    EVALUATE("evaluate", "Evaluate"),
    PROFILE("profile", "Profile")
}
