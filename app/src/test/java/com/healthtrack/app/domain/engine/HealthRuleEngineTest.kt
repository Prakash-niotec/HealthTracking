package com.healthtrack.app.domain.engine

import com.healthtrack.app.data.model.EvaluationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthRuleEngineTest {
    private val engine = HealthRuleEngine()

    @Test
    fun testRequiredExamples() {
        // Sugar 15 g + Diabetes -> UNSAFE (threshold 10 g)
        val result1 = engine.evaluate("Sugar", 15.0, "g", "Diabetes")
        assertTrue(result1 is EvaluationOutcome.Evaluated)
        assertEquals(EvaluationResult.UNSAFE, (result1 as EvaluationOutcome.Evaluated).result)

        // Sodium 200 mg + Hypertension -> SAFE (threshold 400 mg)
        val result2 = engine.evaluate("Sodium", 200.0, "mg", "Hypertension")
        assertTrue(result2 is EvaluationOutcome.Evaluated)
        assertEquals(EvaluationResult.SAFE, (result2 as EvaluationOutcome.Evaluated).result)
    }

    @Test
    fun testSafeUnsafeAndExactlyAtThreshold() {
        // Threshold for Diabetes carbs is 45g
        val safe = engine.evaluate("carbohydrate", 20.0, "g", "Diabetes")
        assertEquals(EvaluationResult.SAFE, (safe as EvaluationOutcome.Evaluated).result)

        val exact = engine.evaluate("carbohydrate", 45.0, "g", "Diabetes")
        assertEquals(EvaluationResult.SAFE, (exact as EvaluationOutcome.Evaluated).result)

        val unsafe = engine.evaluate("carbohydrate", 46.0, "g", "Diabetes")
        assertEquals(EvaluationResult.UNSAFE, (unsafe as EvaluationOutcome.Evaluated).result)
    }

    @Test
    fun testUnitConversion() {
        // Kidney disease potassium threshold: 200 mg. Let's input 0.2g (which is 200mg, SAFE)
        val gToMgSafe = engine.evaluate("potassium", 0.2, "g", "Kidney disease")
        assertEquals(EvaluationResult.SAFE, (gToMgSafe as EvaluationOutcome.Evaluated).result)

        // 0.21g = 210mg -> UNSAFE
        val gToMgUnsafe = engine.evaluate("potassium", 0.21, "g", "Kidney disease")
        assertEquals(EvaluationResult.UNSAFE, (gToMgUnsafe as EvaluationOutcome.Evaluated).result)

        // mcg to mg conversion (200000 mcg = 200 mg)
        val mcgToMgSafe = engine.evaluate("potassium", 200000.0, "mcg", "Kidney disease")
        assertEquals(EvaluationResult.SAFE, (mcgToMgSafe as EvaluationOutcome.Evaluated).result)
    }

    @Test
    fun testAliasAndCaseInsensitivity() {
        // Alias for sodium is "salt" and "na". Heart disease sodium threshold is 400mg
        val result1 = engine.evaluate(" SAlt ", 350.0, "mg", "HEART DISEASE")
        assertTrue(result1 is EvaluationOutcome.Evaluated)
        assertEquals(EvaluationResult.SAFE, (result1 as EvaluationOutcome.Evaluated).result)

        val result2 = engine.evaluate("nA", 450.0, "mg", "heart disease")
        assertEquals(EvaluationResult.UNSAFE, (result2 as EvaluationOutcome.Evaluated).result)
    }

    @Test
    fun testUnknownNutrient() {
        val result = engine.evaluate("unknown_nutrient", 10.0, "g", "Diabetes")
        assertTrue(result is EvaluationOutcome.NoRuleFound)
    }

    @Test
    fun testIncompatibleUnit() {
        val result = engine.evaluate("sugar", 15.0, "kcal", "Diabetes")
        assertTrue(result is EvaluationOutcome.ValidationError)
        assertTrue((result as EvaluationOutcome.ValidationError).message.contains("Incompatible unit"))
    }
}
