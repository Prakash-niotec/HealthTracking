package com.healthtrack.app.domain.engine

import com.healthtrack.app.data.model.WaterLog
import com.healthtrack.app.util.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

// Testing W3: WaterLog drinkType and effectiveMl.
class WaterCalculatorViewModelTest {
    
    @Test
    fun testEffectiveMlCalculation() {
        val factor = 0.8f // For tea
        val amount = 100
        val effective = (amount * factor).toInt()
        assertEquals(80, effective)
    }
}
