package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = TechCyan,
    onPrimary = DeepCockpit,
    primaryContainer = TechCyanDark,
    onPrimaryContainer = Color.White,
    secondary = SafetyGreen,
    onSecondary = DeepCockpit,
    secondaryContainer = CardSlateLight,
    onSecondaryContainer = Color.White,
    tertiary = WarningAmber,
    onTertiary = DeepCockpit,
    error = AlertRed,
    onError = Color.White,
    background = DeepCockpit,
    onBackground = TextPrimaryDark,
    surface = CardSlate,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardSlateLight,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceBorder
)

private val LightColorScheme = lightColorScheme(
    primary = TechBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = TechBlue,
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    error = AlertRed,
    onError = Color.White,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = LightSurfaceBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to cockpit dark theme for automotive feel
    dynamicColor: Boolean = false, // Keep high-contrast custom palette for safety HUD
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
