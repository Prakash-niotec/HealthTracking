package com.healthtrack.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ColorContrastTest {

    @Test
    fun testButtonAndTextContrastRatios() {
        // Primary button: HealthTealPrimaryLight background, White content
        val primaryBtnBg = HealthTealPrimaryLight
        val primaryBtnFg = Color.White
        val ratio1 = ColorUtils.calculateContrast(primaryBtnFg.toArgb(), primaryBtnBg.toArgb())
        assertTrue("Primary button contrast ratio should be >= 4.5:1 (was $ratio1)", ratio1 >= 4.5)

        // Light Surface text: HealthSurfaceLight bg, Dark text (0xFF191C1C)
        val surfaceBg = HealthSurfaceLight
        val textDark = Color(0xFF191C1C)
        val ratio2 = ColorUtils.calculateContrast(textDark.toArgb(), surfaceBg.toArgb())
        assertTrue("Surface text contrast ratio should be >= 4.5:1 (was $ratio2)", ratio2 >= 4.5)

        // On-primary text in dark mode: HealthTealPrimaryDark bg, Dark text (0xFF003732)
        val darkPrimaryBg = HealthTealPrimaryDark
        val darkPrimaryFg = Color(0xFF003732)
        val ratio3 = ColorUtils.calculateContrast(darkPrimaryFg.toArgb(), darkPrimaryBg.toArgb())
        assertTrue("Dark primary text contrast ratio should be >= 4.5:1 (was $ratio3)", ratio3 >= 4.5)
    }
}
