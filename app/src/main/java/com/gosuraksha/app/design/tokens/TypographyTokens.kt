package com.gosuraksha.app.design.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * GO SURAKSHA - TYPOGRAPHY TOKEN SYSTEM
 *
 * Centralized text styles following Material 3 and Indian app standards.
 * Optimized for Hindi, Telugu, Tamil, and English readability.
 *
 * DESIGN PRINCIPLES:
 * - Clear hierarchy (5-6 levels maximum)
 * - Readable at all sizes (minimum 14sp for body)
 * - Support for Indian language text expansion (20-30% larger)
 * - High contrast ratios for outdoor use
 * - Consistent line heights for vertical rhythm
 */

object TypographyTokens {

    // ═══════════════════════════════════════════════════════════════
    // FONT FAMILIES
    // ═══════════════════════════════════════════════════════════════

    // Using system defaults for maximum compatibility
    // Can be replaced with custom fonts if needed
    val defaultFontFamily: FontFamily = FontFamily.Default

    // ═══════════════════════════════════════════════════════════════
    // DISPLAY STYLES (Large headings, rare use)
    // ═══════════════════════════════════════════════════════════════

    val displayLarge = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    )

    val displayMedium = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )

    // ═══════════════════════════════════════════════════════════════
    // HEADLINE STYLES (Screen titles, section headers)
    // ═══════════════════════════════════════════════════════════════

    val headlineLarge = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )

    val headlineMedium = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    val headlineSmall = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // ═══════════════════════════════════════════════════════════════
    // TITLE STYLES (Card titles, list headers)
    // ═══════════════════════════════════════════════════════════════

    val titleLarge = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    val titleMedium = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    )

    val titleSmall = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    // ═══════════════════════════════════════════════════════════════
    // BODY STYLES (Main content, descriptions)
    // ═══════════════════════════════════════════════════════════════

    val bodyLarge = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

    val bodyMedium = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,               // Minimum readable size
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )

    val bodySmall = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )

    // ═══════════════════════════════════════════════════════════════
    // LABEL STYLES (Buttons, badges, captions)
    // ═══════════════════════════════════════════════════════════════

    val labelLarge = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    val labelMedium = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    val labelSmall = TextStyle(
        fontFamily = defaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    // ═══════════════════════════════════════════════════════════════
    // SEMANTIC TEXT STYLES (USE THESE IN CODE)
    // ═══════════════════════════════════════════════════════════════

    // Screen titles
    val screenTitle = headlineLarge
    val screenSubtitle = titleMedium

    // Section headers
    val sectionHeader = titleLarge
    val sectionSubheader = titleMedium

    // Card content
    val cardTitle = titleMedium
    val cardSubtitle = bodyMedium
    val cardLabel = labelMedium

    // Button text
    val buttonText = labelLarge
    val buttonTextSmall = labelMedium

    // List items
    val listTitle = titleMedium
    val listSubtitle = bodyMedium
    val listCaption = labelSmall

    // Status/Badge text
    val badgeText = labelMedium.copy(fontWeight = FontWeight.SemiBold)

    // Input labels
    val inputLabel = labelLarge
    val inputText = bodyMedium
    val inputHelper = labelSmall
    val inputError = labelSmall.copy(fontWeight = FontWeight.Medium)

    // Bottom navigation
    val bottomNavLabel = labelSmall

    // ═══════════════════════════════════════════════════════════════
    // MATERIAL 3 TYPOGRAPHY (For MaterialTheme)
    // ═══════════════════════════════════════════════════════════════

    val AppTypography = Typography(
        displayLarge = displayLarge,
        displayMedium = displayMedium,
        displaySmall = headlineLarge,

        headlineLarge = headlineLarge,
        headlineMedium = headlineMedium,
        headlineSmall = headlineSmall,

        titleLarge = titleLarge,
        titleMedium = titleMedium,
        titleSmall = titleSmall,

        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = bodySmall,

        labelLarge = labelLarge,
        labelMedium = labelMedium,
        labelSmall = labelSmall
    )
}

/**
 * USAGE EXAMPLES:
 *
 * // Screen title
 * Text(
 *     text = "Home",
 *     style = TypographyTokens.screenTitle,
 *     color = ColorTokens.textPrimary()
 * )
 *
 * // Card title
 * Text(
 *     text = "Security Status",
 *     style = TypographyTokens.cardTitle,
 *     color = ColorTokens.textPrimary()
 * )
 *
 * // Button
 * Text(
 *     text = "Scan Now",
 *     style = TypographyTokens.buttonText
 * )
 */