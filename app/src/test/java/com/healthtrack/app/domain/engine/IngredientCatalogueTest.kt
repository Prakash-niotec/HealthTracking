package com.healthtrack.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientCatalogueTest {

    @Test
    fun testExactAndSynonymMatching() {
        val res1 = IngredientCatalogue.match("nacl", 5.0, "g")
        assertTrue(res1 is MatchResult.Exact)
        val exact1 = res1 as MatchResult.Exact
        assertEquals("sodium", exact1.item.canonicalNutrient)
        assertTrue(exact1.customExplanation?.contains("1967.0 mg sodium") == true)

        val res2 = IngredientCatalogue.match("uppu", 1.0, "g")
        assertTrue(res2 is MatchResult.Exact)
        assertEquals("sodium", (res2 as MatchResult.Exact).item.canonicalNutrient)
    }

    @Test
    fun testFoodDetection() {
        val res = IngredientCatalogue.match("banana", 100.0, "g")
        assertTrue(res is MatchResult.FoodDetected)
    }

    @Test
    fun testFuzzySuggestions() {
        val res = IngredientCatalogue.match("sodim", 200.0, "mg")
        assertTrue(res is MatchResult.Suggestions)
        val sug = res as MatchResult.Suggestions
        assertTrue(sug.suggestions.contains("Sodium"))
    }
}
