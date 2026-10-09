package com.healthtrack.app.domain.engine

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

data class CompoundConversion(
    val compoundName: String,
    val factorToSodiumMg: Double
)

data class IngredientItem(
    val canonicalNutrient: String,
    val displayName: String,
    val aliases: List<String>,
    val unitFamily: String, // "weight" or "energy"
    val supportedUnits: List<String>,
    val conversionFactorToMg: Double = 1.0,
    val compoundConversions: List<CompoundConversion> = emptyList()
)

sealed class MatchResult {
    data class Exact(val item: IngredientItem, val convertedNutrient: String = item.canonicalNutrient, val customExplanation: String? = null, val convertedAmountMg: Double? = null) : MatchResult()
    data class FoodDetected(val foodName: String) : MatchResult()
    data class Suggestions(val query: String, val suggestions: List<String>) : MatchResult()
    object NotFound : MatchResult()
}

object IngredientCatalogue {

    private val commonFoods = setOf(
        "banana", "bananas", "apple", "apples", "orange", "oranges", "rice", "bread",
        "pizza", "burger", "pasta", "potato", "potatoes", "chicken", "beef", "milk",
        "egg", "eggs", "cheese", "soup", "cookie", "cookies", "cake", "chocolate", "salad"
    )

    private var catalog: List<IngredientItem> = emptyList()

    fun load(context: Context) {
        try {
            val json = context.assets.open("ingredients.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<IngredientItem>>() {}.type
            catalog = Gson().fromJson(json, type) ?: getDefaultCatalog()
        } catch (e: Exception) {
            catalog = getDefaultCatalog()
        }
    }

    fun init(items: List<IngredientItem>) {
        catalog = items
    }

    fun getAllItems(): List<IngredientItem> = catalog.ifEmpty { getDefaultCatalog() }

    fun search(query: String): List<IngredientItem> {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return getAllItems()

        return getAllItems().filter { item ->
            item.displayName.lowercase(Locale.ROOT).contains(q) ||
            item.canonicalNutrient.lowercase(Locale.ROOT).contains(q) ||
            item.aliases.any { it.lowercase(Locale.ROOT).contains(q) }
        }
    }

    fun match(inputName: String, value: Double, unit: String): MatchResult {
        val q = inputName.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return MatchResult.NotFound

        // 1. Check if it's a food, not a nutrient
        if (commonFoods.contains(q)) {
            return MatchResult.FoodDetected(inputName)
        }

        val all = getAllItems()

        // 2. Check compound conversion aliases
        if (q.contains("salt") || q == "nacl" || q == "table salt" || q == "uppu") {
            val sodiumItem = all.find { it.canonicalNutrient == "sodium" }
            if (sodiumItem != null) {
                // Conversion: 1g salt = 393.4 mg sodium
                val valueInG = when (unit.lowercase(Locale.ROOT)) {
                    "g" -> value
                    "mg" -> value / 1000.0
                    else -> value
                }
                val sodiumMg = valueInG * 393.4
                return MatchResult.Exact(
                    item = sodiumItem,
                    convertedNutrient = "sodium",
                    customExplanation = "$value $unit salt (sodium chloride) contains approximately ${String.format(Locale.ROOT, "%.1f", sodiumMg)} mg sodium.",
                    convertedAmountMg = sodiumMg
                )
            }
        }

        if (q.contains("potassium chloride") || q == "kcl") {
            val potassiumItem = all.find { it.canonicalNutrient == "potassium" }
            if (potassiumItem != null) {
                val valueInG = when (unit.lowercase(Locale.ROOT)) {
                    "g" -> value
                    "mg" -> value / 1000.0
                    else -> value
                }
                val potassiumMg = valueInG * 524.5
                return MatchResult.Exact(
                    item = potassiumItem,
                    convertedNutrient = "potassium",
                    customExplanation = "$value $unit potassium chloride contains approximately ${String.format(Locale.ROOT, "%.1f", potassiumMg)} mg potassium.",
                    convertedAmountMg = potassiumMg
                )
            }
        }

        // 3. Exact alias match
        val exactMatch = all.find { item ->
            item.canonicalNutrient.equals(q, ignoreCase = true) ||
            item.displayName.equals(q, ignoreCase = true) ||
            item.aliases.any { it.equals(q, ignoreCase = true) }
        }
        if (exactMatch != null) {
            return MatchResult.Exact(exactMatch)
        }

        // 4. Prefix match
        val prefixMatch = all.find { item ->
            item.canonicalNutrient.lowercase(Locale.ROOT).startsWith(q) ||
            item.aliases.any { it.lowercase(Locale.ROOT).startsWith(q) }
        }
        if (prefixMatch != null) {
            return MatchResult.Exact(prefixMatch)
        }

        // 5. Contains match
        val containsMatch = all.find { item ->
            item.canonicalNutrient.lowercase(Locale.ROOT).contains(q) ||
            item.aliases.any { it.lowercase(Locale.ROOT).contains(q) }
        }
        if (containsMatch != null) {
            return MatchResult.Exact(containsMatch)
        }

        // 6. Fuzzy match (edit distance <= 2 for strings 5+ chars)
        if (q.length >= 4) {
            val suggestions = mutableListOf<String>()
            for (item in all) {
                for (alias in item.aliases + item.displayName + item.canonicalNutrient) {
                    if (levenshteinDistance(q, alias.lowercase(Locale.ROOT)) <= 2) {
                        suggestions.add(item.displayName)
                        break
                    }
                }
            }
            if (suggestions.isNotEmpty()) {
                return MatchResult.Suggestions(q, suggestions.distinct())
            }
        }

        return MatchResult.NotFound
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    fun getDefaultCatalog(): List<IngredientItem> {
        return listOf(
            IngredientItem("sodium", "Sodium", listOf("sodium", "salt", "table salt", "sodium chloride", "nacl", "na", "uppu"), "weight", listOf("mg", "g")),
            IngredientItem("sugar", "Sugar", listOf("sugar", "sucrose", "added sugar", "total sugars", "glucose", "fructose", "hfcs"), "weight", listOf("g", "mg")),
            IngredientItem("saturated fat", "Saturated Fat", listOf("saturated fat", "sat fat", "saturates", "saturated fatty acids"), "weight", listOf("g", "mg")),
            IngredientItem("trans fat", "Trans Fat", listOf("trans fat", "trans fatty acids", "partially hydrogenated oil"), "weight", listOf("g", "mg")),
            IngredientItem("carbohydrate", "Carbohydrates", listOf("carbohydrate", "carbs", "total carbohydrate", "carbohydrates"), "weight", listOf("g", "mg")),
            IngredientItem("calories", "Calories", listOf("calories", "energy", "kcal", "cal"), "energy", listOf("kcal")),
            IngredientItem("cholesterol", "Cholesterol", listOf("cholesterol", "dietary cholesterol"), "weight", listOf("mg", "g")),
            IngredientItem("potassium", "Potassium", listOf("potassium", "k", "potassium chloride"), "weight", listOf("mg", "g")),
            IngredientItem("phosphorus", "Phosphorus", listOf("phosphorus", "phosphate", "phosphoric acid", "p"), "weight", listOf("mg", "g")),
            IngredientItem("total fat", "Total Fat", listOf("total fat", "fat", "fats", "lipids"), "weight", listOf("g", "mg")),
            IngredientItem("caffeine", "Caffeine", listOf("caffeine"), "weight", listOf("mg")),
            IngredientItem("protein", "Protein", listOf("protein", "proteins"), "weight", listOf("g", "mg")),
            IngredientItem("fibre", "Fiber / Fibre", listOf("fibre", "fiber", "dietary fiber", "dietary fibre"), "weight", listOf("g", "mg"))
        )
    }
}
