package com.healthtrack.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = HealthTealPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF74F8E5),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = HealthTealSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA2F2E8),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFF4A6363),
    onTertiary = Color.White,
    background = HealthBackgroundLight,
    onBackground = Color(0xFF191C1C),
    surface = HealthSurfaceLight,
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFEEF4F2),
    onSurfaceVariant = Color(0xFF3F4947),
    outline = Color(0xFF6F7977),
    outlineVariant = Color(0xFFD0DCDA)
)

private val DarkColorScheme = darkColorScheme(
    primary = HealthTealPrimaryDark,
    onPrimary = Color(0xFF003732),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF74F8E5),
    secondary = HealthTealSecondaryDark,
    onSecondary = Color(0xFF003732),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFFA2F2E8),
    tertiary = Color(0xFFB0CCCB),
    onTertiary = Color(0xFF1B3535),
    background = HealthBackgroundDark,
    onBackground = Color(0xFFE0E3E2),
    surface = HealthSurfaceDark,
    onSurface = Color(0xFFE0E3E2),
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBEC9C7),
    outline = Color(0xFF899391),
    outlineVariant = Color(0xFF3F4947)
)

@Composable
fun HealthNexaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default to custom HealthNexa colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    HealthNexaTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
