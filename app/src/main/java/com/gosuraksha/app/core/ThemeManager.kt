package com.gosuraksha.app.core

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Theme Manager Composable
 * Manages theme state across the app
 */

@Composable
fun ThemeManager(
    content: @Composable (Boolean) -> Unit
) {
    val context = LocalContext.current

    // Get saved theme preference
    val savedDarkMode by ThemePrefs.isDarkMode(context)
        .collectAsStateWithLifecycle(initialValue = false)

    // Use saved preference, fallback to system
    val isDark = savedDarkMode

    content(isDark)
}


