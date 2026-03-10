package com.gosuraksha.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

data class ThemeState(
    val isDark: Boolean,
    val setDark: (Boolean) -> Unit,
    val toggle: () -> Unit
)

val LocalThemeState = staticCompositionLocalOf<ThemeState> {
    error("ThemeState not provided")
}
