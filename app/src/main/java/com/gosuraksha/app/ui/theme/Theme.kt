package com.gosuraksha.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Color schemes — keep your existing values here unchanged
// ─────────────────────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF00C9A7),
    onPrimary        = Color(0xFF07090F),
    primaryContainer = Color(0xFF003D32),
    secondary        = Color(0xFF0077FF),
    onSecondary      = Color(0xFFFFFFFF),
    background       = Color(0xFF0D0F1A),
    onBackground     = Color(0xFFE8EAF6),
    surface          = Color(0xFF131627),
    onSurface        = Color(0xFFE8EAF6),
    surfaceVariant   = Color(0xFF1A1E35),
    outline          = Color(0xFF252A45),
    error            = Color(0xFFEF4444),
    onError          = Color(0xFFFFFFFF),
)

private val LightColorScheme = lightColorScheme(
    primary          = Color(0xFF00897B),
    onPrimary        = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB2DFDB),
    secondary        = Color(0xFF1565C0),
    onSecondary      = Color(0xFFFFFFFF),
    background       = Color(0xFFF2F5FB),
    onBackground     = Color(0xFF1A2040),
    surface          = Color(0xFFFFFFFF),
    onSurface        = Color(0xFF1A2040),
    surfaceVariant   = Color(0xFFEEF2FA),
    outline          = Color(0xFFDDE6F5),
    error            = Color(0xFFDC2626),
    onError          = Color(0xFFFFFFFF),
)

// ─────────────────────────────────────────────────────────────────────────────
// GoSurakshaTheme
// The ONE change vs your original: typography = GoSurakshaTypography
// This replaces M3's default Roboto with Manrope across every screen.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GOSurakshaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = GoSurakshaTypography,  // ← Manrope replaces Roboto here
        content     = content
    )
}