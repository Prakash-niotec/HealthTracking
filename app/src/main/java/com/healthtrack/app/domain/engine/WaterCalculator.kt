package com.healthtrack.app.domain.engine

object WaterCalculator {
    /**
     * Calculates the daily water goal in ml based on the user's weight in kg.
     * Rule: weightKg * 35 ml, rounded to the nearest 10 ml.
     */
    fun calculateAutoGoal(weightKg: Float): Int {
        if (weightKg <= 0f) return 2000 // Fallback

        val rawGoal = weightKg * 35f
        // Round to nearest 10
        return Math.round(rawGoal / 10f) * 10
    }

    /**
     * Recomputes the water goal if the weight changes, provided the user hasn't
     * manually overridden the goal.
     */
    fun recomputeGoalIfNeeded(
        newWeightKg: Float,
        currentGoalMl: Int,
        isGoalManual: Boolean
    ): Int {
        if (isGoalManual) {
            return currentGoalMl.coerceIn(MIN_MANUAL_GOAL_ML, MAX_MANUAL_GOAL_ML)
        }
        return calculateAutoGoal(newWeightKg)
    }

    const val MIN_MANUAL_GOAL_ML = 500
    const val MAX_MANUAL_GOAL_ML = 10000
}
