package com.healthtrack.app.domain.engine

import com.healthtrack.app.data.model.EvaluationResult
import java.util.Locale

data class HealthRule(
    val id: String,
    val nutrient: String,
    val aliases: List<String>,
    val condition: String,
    val threshold: Double,
    val baseUnit: String,
    val perServingBasis: String,
    val explanationTemplate: String,
    val criteriaSource: String
)

sealed class EvaluationOutcome {
    data class Evaluated(
        val result: EvaluationResult,
        val explanation: String,
        val matchedRule: HealthRule,
        val normalizedValue: Double
    ) : EvaluationOutcome()

    data class ValidationError(val message: String) : EvaluationOutcome()
    data class NoRuleFound(val message: String) : EvaluationOutcome()
}

class HealthRuleEngine(private val rules: List<HealthRule> = StarterRules.defaultRules) {

    fun evaluate(
        ingredientName: String,
        value: Double,
        unit: String,
        condition: String
    ): EvaluationOutcome {
        if (value <= 0) {
            return EvaluationOutcome.ValidationError("Value must be greater than 0")
        }

        val normalizedInputName = ingredientName.trim().lowercase(Locale.ROOT)
        val normalizedCondition = condition.trim().lowercase(Locale.ROOT)
        val normalizedUnit = unit.trim().lowercase(Locale.ROOT)

        // Find rules matching the condition
        val conditionRules = rules.filter { it.condition.lowercase(Locale.ROOT) == normalizedCondition }
        
        // Find specific rule matching the nutrient or aliases
        val rule = conditionRules.find { r ->
            r.nutrient.lowercase(Locale.ROOT) == normalizedInputName ||
            r.aliases.any { it.lowercase(Locale.ROOT) == normalizedInputName }
        }

        if (rule == null) {
            return EvaluationOutcome.NoRuleFound("No rule defined for this nutrient and condition")
        }

        // Normalize unit
        val normalizedValueResult = normalizeUnit(value, normalizedUnit, rule.baseUnit.lowercase(Locale.ROOT))
        if (normalizedValueResult == null) {
            return EvaluationOutcome.ValidationError("Incompatible unit: cannot convert $unit to ${rule.baseUnit}")
        }

        val isSafe = normalizedValueResult <= rule.threshold
        val result = if (isSafe) EvaluationResult.SAFE else EvaluationResult.UNSAFE

        val explanation = rule.explanationTemplate
            .replace("{ingredient}", ingredientName)
            .replace("{value}", "$normalizedValueResult ${rule.baseUnit}")
            .replace("{threshold}", "${rule.threshold} ${rule.baseUnit}")

        return EvaluationOutcome.Evaluated(
            result = result,
            explanation = explanation,
            matchedRule = rule,
            normalizedValue = normalizedValueResult
        )
    }

    private fun normalizeUnit(value: Double, fromUnit: String, toUnit: String): Double? {
        if (fromUnit == toUnit) return value

        // g, mg, mcg conversions
        return when (fromUnit) {
            "g" -> when (toUnit) {
                "mg" -> value * 1000.0
                "mcg", "ug" -> value * 1000000.0
                else -> null
            }
            "mg" -> when (toUnit) {
                "g" -> value / 1000.0
                "mcg", "ug" -> value * 1000.0
                else -> null
            }
            "mcg", "ug" -> when (toUnit) {
                "g" -> value / 1000000.0
                "mg" -> value / 1000.0
                else -> null
            }
            // kcal is not compatible with weight units
            else -> null
        }
    }
}
