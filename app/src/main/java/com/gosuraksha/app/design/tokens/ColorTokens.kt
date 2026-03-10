package com.gosuraksha.app.design.tokens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * GO SURAKSHA - COLOR TOKEN SYSTEM
 *
 * Centralized color definitions following PhonePe/UMANG/Paytm patterns.
 * All colors are accessed through semantic tokens, not hardcoded values.
 *
 * DESIGN PRINCIPLES:
 * - Tonal layered surfaces in light and dark mode
 * - Green used ONLY for accents, success, active states
 * - High contrast for outdoor readability
 * - Government-grade accessibility (WCAG AAA where possible)
 */

object ColorTokens {
    val LocalAppDarkMode = staticCompositionLocalOf { false }

    @Composable
    private fun isDarkMode(): Boolean = LocalAppDarkMode.current

    // ═══════════════════════════════════════════════════════════════
    // LIGHT MODE COLORS
    // ═══════════════════════════════════════════════════════════════

    object Light {
        // Base surfaces
        val background = Color(0xFFF6F8FB)              // Tonal app background
        val surface = Color(0xFFFCFDFE)                 // Primary raised surface
        val surfaceVariant = Color(0xFFEEF2F7)          // Secondary layered surface

        // Text colors
        val textPrimary = Color(0xFF1A1A1A)             // Near black, high contrast
        val textSecondary = Color(0xFF666666)           // Medium gray
        val textTertiary = Color(0xFF999999)            // Light gray
        val textDisabled = Color(0xFFCCCCCC)            // Very light gray

        // Brand/Accent (GREEN - use sparingly)
        val accent = Color(0xFF2E7D32)                  // Professional green
        val accentVariant = Color(0xFF1B5E20)           // Darker green
        val accentLight = Color(0xFF4CAF50)             // Lighter green for hover
        val accentSurface = Color(0xFFE8F5E9)           // Very light green background

        // Status colors
        val success = Color(0xFF2E7D32)                 // Green
        val successLight = Color(0xFFE8F5E9)            // Light green bg
        val warning = Color(0xFFF57C00)                 // Orange
        val warningLight = Color(0xFFFFF3E0)            // Light orange bg
        val error = Color(0xFFD32F2F)                   // Red
        val errorLight = Color(0xFFFFEBEE)              // Light red bg
        val info = Color(0xFF1976D2)                    // Blue
        val infoLight = Color(0xFFE3F2FD)               // Light blue bg

        // Borders and dividers
        val border = Color(0xFFE0E0E0)                  // Light gray
        val borderStrong = Color(0xFFBDBDBD)            // Medium gray
        val divider = Color(0xFFF0F0F0)                 // Very light gray

        // Interactive states
        val ripple = Color(0x1F000000)                  // 12% black
        val pressed = Color(0x0F000000)                 // 6% black
        val focused = Color(0xFF2E7D32)                 // Green
        val selected = Color(0xFFE8F5E9)                // Light green

        // Overlays
        val scrim = Color(0x99000000)                   // 60% black
        val overlay = Color(0x0D000000)                 // 5% black

        // Auth gradients (premium)
        val authGradientStart = Color(0xFFF6FBF8)
        val authGradientMid = Color(0xFFEEF7F1)
        val authGradientEnd = Color(0xFFE6F2EC)
    }

    // ═══════════════════════════════════════════════════════════════
    // DARK MODE COLORS
    // ═══════════════════════════════════════════════════════════════

    object Dark {
        // Base surfaces
        val background = Color(0xFF0B1016)              // Tonal dark background
        val surface = Color(0xFF121923)                 // Primary raised surface
        val surfaceVariant = Color(0xFF1A2330)          // Secondary layered surface

        // Text colors
        val textPrimary = Color(0xFFFFFFFF)             // Pure white
        val textSecondary = Color(0xFFB3B3B3)           // Light gray
        val textTertiary = Color(0xFF808080)            // Medium gray
        val textDisabled = Color(0xFF4D4D4D)            // Dark gray

        // Brand/Accent (GREEN - use sparingly)
        val accent = Color(0xFF66BB6A)                  // Lighter green for dark bg
        val accentVariant = Color(0xFF4CAF50)           // Medium green
        val accentLight = Color(0xFF81C784)             // Very light green
        val accentSurface = Color(0xFF1B5E20)           // Dark green background

        // Status colors
        val success = Color(0xFF66BB6A)                 // Green
        val successLight = Color(0xFF1B5E20)            // Dark green bg
        val warning = Color(0xFFFFA726)                 // Orange
        val warningLight = Color(0xFF4E2A00)            // Dark orange bg
        val error = Color(0xFFEF5350)                   // Red
        val errorLight = Color(0xFF5F0000)              // Dark red bg
        val info = Color(0xFF42A5F5)                    // Blue
        val infoLight = Color(0xFF003D5C)               // Dark blue bg

        // Borders and dividers
        val border = Color(0xFF2E2E2E)                  // Dark gray
        val borderStrong = Color(0xFF424242)            // Medium gray
        val divider = Color(0xFF1A1A1A)                 // Very dark gray

        // Interactive states
        val ripple = Color(0x1FFFFFFF)                  // 12% white
        val pressed = Color(0x0FFFFFFF)                 // 6% white
        val focused = Color(0xFF66BB6A)                 // Green
        val selected = Color(0xFF1B5E20)                // Dark green

        // Overlays
        val scrim = Color(0x99000000)                   // 60% black
        val overlay = Color(0x0DFFFFFF)                 // 5% white

        // Auth gradients (premium)
        val authGradientStart = Color(0xFF0A1412)
        val authGradientMid = Color(0xFF0E1C18)
        val authGradientEnd = Color(0xFF0B241D)
    }

    // ═══════════════════════════════════════════════════════════════
    // SEMANTIC COLOR ACCESSORS (USE THESE IN CODE)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun background(): Color =
        if (isDarkMode()) Dark.background else Light.background

    @Composable
    fun surface(): Color =
        if (isDarkMode()) Dark.surface else Light.surface

    @Composable
    fun surfaceVariant(): Color =
        if (isDarkMode()) Dark.surfaceVariant else Light.surfaceVariant

    @Composable
    fun textPrimary(): Color =
        if (isDarkMode()) Dark.textPrimary else Light.textPrimary

    @Composable
    fun textSecondary(): Color =
        if (isDarkMode()) Dark.textSecondary else Light.textSecondary

    @Composable
    fun textTertiary(): Color =
        if (isDarkMode()) Dark.textTertiary else Light.textTertiary

    @Composable
    fun accent(): Color =
        if (isDarkMode()) Dark.accent else Light.accent

    @Composable
    fun accentVariant(): Color =
        if (isDarkMode()) Dark.accentVariant else Light.accentVariant

    @Composable
    fun success(): Color =
        if (isDarkMode()) Dark.success else Light.success

    @Composable
    fun successLight(): Color =
        if (isDarkMode()) Dark.successLight else Light.successLight

    @Composable
    fun warning(): Color =
        if (isDarkMode()) Dark.warning else Light.warning

    @Composable
    fun warningLight(): Color =
        if (isDarkMode()) Dark.warningLight else Light.warningLight

    @Composable
    fun error(): Color =
        if (isDarkMode()) Dark.error else Light.error

    @Composable
    fun errorLight(): Color =
        if (isDarkMode()) Dark.errorLight else Light.errorLight

    @Composable
    fun border(): Color =
        if (isDarkMode()) Dark.border else Light.border

    @Composable
    fun divider(): Color =
        if (isDarkMode()) Dark.divider else Light.divider

    @Composable
    fun ripple(): Color =
        if (isDarkMode()) Dark.ripple else Light.ripple

    @Composable
    fun focused(): Color =
        if (isDarkMode()) Dark.focused else Light.focused

    @Composable
    fun selected(): Color =
        if (isDarkMode()) Dark.selected else Light.selected

    @Composable
    fun transparent(): Color = Color.Transparent

    @Composable
    fun authGradientStart(): Color =
        if (isDarkMode()) Dark.authGradientStart else Light.authGradientStart

    @Composable
    fun authGradientMid(): Color =
        if (isDarkMode()) Dark.authGradientMid else Light.authGradientMid

    @Composable
    fun authGradientEnd(): Color =
        if (isDarkMode()) Dark.authGradientEnd else Light.authGradientEnd
}
