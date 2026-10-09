package com.healthtrack.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class WaterCalculatorTest {

    @Test
    fun testNormalWeights() {
        // 70 kg * 35 = 2450
        assertEquals(2450, WaterCalculator.calculateAutoGoal(70f))
        
        // 50.5 kg * 35 = 1767.5 -> rounded to 1770
        assertEquals(1770, WaterCalculator.calculateAutoGoal(50.5f))
    }

    @Test
    fun testRounding() {
        // 70.1 kg * 35 = 2453.5 -> rounded to nearest 10 is 2450
        assertEquals(2450, WaterCalculator.calculateAutoGoal(70.1f))

        // 70.2 kg * 35 = 2457 -> rounded to nearest 10 is 2460
        assertEquals(2460, WaterCalculator.calculateAutoGoal(70.2f))
    }

    @Test
    fun testWeightChangeRecompute() {
        // Auto goal changes
        val newAutoGoal = WaterCalculator.recomputeGoalIfNeeded(
            newWeightKg = 80f,
            currentGoalMl = 2450,
            isGoalManual = false
        )
        assertEquals(2800, newAutoGoal) // 80 * 35

        // Manual goal preserved
        val preservedGoal = WaterCalculator.recomputeGoalIfNeeded(
            newWeightKg = 80f,
            currentGoalMl = 3000,
            isGoalManual = true
        )
        assertEquals(3000, preservedGoal)
    }
}
