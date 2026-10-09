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

        val match = IngredientCatalogue.match(ingredientName, value, unit)

        val targetNutrientName: String
        val customPrefix: String?
        var effectiveValue = value
        var effectiveUnit = unit

        when (match) {
            is MatchResult.FoodDetected -> {
                return EvaluationOutcome.ValidationError("This looks like a food (${match.foodName}). Please enter a specific nutrient (e.g. sodium, sugar, potassium) per serving.")
            }
            is MatchResult.Suggestions -> {
                val sugStr = match.suggestions.joinToString(", ")
                return EvaluationOutcome.ValidationError("Ingredient '$ingredientName' not recognized. Did you mean: $sugStr?")
            }
            is MatchResult.Exact -> {
                targetNutrientName = match.convertedNutrient
                customPrefix = match.customExplanation
                if (match.convertedAmountMg != null) {
                    effectiveValue = match.convertedAmountMg
                    effectiveUnit = "mg"
                }
            }
            is MatchResult.NotFound -> {
                targetNutrientName = ingredientName.trim().lowercase(Locale.ROOT)
                customPrefix = null
            }
        }

        val normalizedCondition = normalizeCondition(condition)
        val normalizedUnit = effectiveUnit.trim().lowercase(Locale.ROOT)

        // Find rules matching condition
        val conditionRules = rules.filter { normalizeCondition(it.condition) == normalizedCondition }

        // Find rule matching target nutrient or aliases
        val rule = conditionRules.find { r ->
            r.nutrient.lowercase(Locale.ROOT) == targetNutrientName ||
            r.aliases.any { it.lowercase(Locale.ROOT) == targetNutrientName }
        }

        if (rule == null) {
            return EvaluationOutcome.NoRuleFound("No rule defined for '$ingredientName' under $condition.")
        }

        val normalizedValueResult = normalizeUnit(effectiveValue, normalizedUnit, rule.baseUnit.lowercase(Locale.ROOT))
            ?: return EvaluationOutcome.ValidationError("Incompatible unit: cannot convert $unit to ${rule.baseUnit}")

        val isSafe = normalizedValueResult <= rule.threshold
        val result = if (isSafe) EvaluationResult.SAFE else EvaluationResult.UNSAFE

        val baseExplanation = rule.explanationTemplate
            .replace("{ingredient}", ingredientName)
            .replace("{value}", "$normalizedValueResult ${rule.baseUnit}")
            .replace("{threshold}", "${rule.threshold} ${rule.baseUnit}")

        val finalExplanation = if (customPrefix != null) {
            "$customPrefix\n\n$baseExplanation"
        } else {
            baseExplanation
        }

        return EvaluationOutcome.Evaluated(
            result = result,
            explanation = finalExplanation,
            matchedRule = rule,
            normalizedValue = normalizedValueResult
        )
    }

    private fun normalizeCondition(cond: String): String {
        val lower = cond.trim().lowercase(Locale.ROOT)
        return when {
            lower.contains("diabetes") -> "diabetes"
            lower.contains("hypertension") || lower.contains("high blood pressure") -> "hypertension"
            lower.contains("cholesterol") -> "high cholesterol"
            lower.contains("kidney") -> "kidney disease"
            lower.contains("heart") -> "heart disease"
            lower.contains("obesity") -> "obesity"
            else -> lower
        }
    }

    private fun normalizeUnit(value: Double, fromUnit: String, toUnit: String): Double? {
        if (fromUnit == toUnit) return value

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
            else -> null
        }
    }
}
