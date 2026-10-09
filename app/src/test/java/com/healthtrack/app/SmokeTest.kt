package com.healthtrack.app

import com.healthtrack.app.util.Validators
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class SmokeTest {
    @Test
    fun testWeightValidation() {
        assertTrue(Validators.isValidWeight(70f))
        assertFalse(Validators.isValidWeight(10f)) // Below min
        assertFalse(Validators.isValidWeight(350f)) // Above max
    }
}
