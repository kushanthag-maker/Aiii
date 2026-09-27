package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NovaBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = NovaCyan,
    onSecondary = Color.Black,
    tertiary = NovaEmerald,
    background = NovaDarkBackground,
    onBackground = NovaTextPrimary,
    surface = NovaDarkSurface,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaDarkSurfaceElevated,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaDarkSurfaceBorder,
    outlineVariant = Color(0xFF333333)
)

private val LightColorScheme = darkColorScheme(
    // Default to sleek dark look requested by user and screenshots
    primary = NovaBlueLight,
    onPrimary = Color.White,
    background = NovaDarkBackground,
    onBackground = NovaTextPrimary,
    surface = NovaDarkSurface,
    onSurface = NovaTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
