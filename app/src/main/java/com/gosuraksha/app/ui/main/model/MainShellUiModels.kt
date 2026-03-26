package com.gosuraksha.app.ui.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(val route: String, val label: String, val icon: ImageVector)

data class NavSection(val title: String, val items: List<NavItem>)

object NavPalette {
    val gradientStart = Color(0xFF00C9A7)
    val gradientEnd = Color(0xFF0077FF)
    val onActive = Color(0xFF07090F)

    @Composable
    fun surface(): Color = MaterialTheme.colorScheme.surface

    @Composable
    fun border(): Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

    @Composable
    fun inactive(isDark: Boolean): Color =
        if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
}
