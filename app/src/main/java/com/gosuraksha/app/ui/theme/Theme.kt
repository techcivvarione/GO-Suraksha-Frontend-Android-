package com.gosuraksha.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = PrimaryTeal,
    secondary = CyberGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    error = RiskCritical
)

private val LightColors = lightColorScheme(
    primary = PrimaryTeal,
    secondary = CyberGreen,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    error = RiskCritical
)

@Composable
fun GOSurakshaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
