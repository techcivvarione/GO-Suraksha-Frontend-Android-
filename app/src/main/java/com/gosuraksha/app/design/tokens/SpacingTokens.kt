package com.gosuraksha.app.design.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * GO SURAKSHA - SPACING TOKEN SYSTEM
 *
 * Centralized spacing scale following 4dp base grid.
 * All spacing values should use these tokens, not hardcoded dp values.
 *
 * DESIGN PRINCIPLES:
 * - 4dp base grid (standard Android)
 * - Consistent vertical rhythm
 * - Predictable spacing hierarchy
 * - Optimized for PhonePe/UMANG-style layouts
 */

object SpacingTokens {

    // ═══════════════════════════════════════════════════════════════
    // BASE SPACING SCALE (4dp increments)
    // ═══════════════════════════════════════════════════════════════

    val none: Dp = 0.dp

    val xxxs: Dp = 2.dp         // Micro gaps (rare use)
    val xxs: Dp = 4.dp          // Tiny gaps between related elements
    val xs: Dp = 8.dp           // Small gaps, tight grouping
    val sm: Dp = 12.dp          // Default small spacing
    val md: Dp = 16.dp          // Default medium spacing (MOST COMMON)
    val lg: Dp = 20.dp          // Large spacing between sections
    val xl: Dp = 24.dp          // Extra large spacing
    val xxl: Dp = 32.dp         // Very large spacing
    val xxxl: Dp = 40.dp        // Huge spacing (rare use)
    val xxxxl: Dp = 48.dp       // Maximum spacing

    // ═══════════════════════════════════════════════════════════════
    // SEMANTIC SPACING (USE THESE IN CODE)
    // ═══════════════════════════════════════════════════════════════

    // Screen-level spacing
    val screenPaddingHorizontal: Dp = 16.dp     // Left/right screen padding
    val screenPaddingVertical: Dp = 16.dp       // Top/bottom screen padding

    // Card spacing
    val cardPadding: Dp = 16.dp                 // Inner card padding
    val cardSpacing: Dp = 12.dp                 // Gap between cards
    val cardPaddingLarge: Dp = 20.dp            // Large card padding (special cards)

    // Section spacing
    val sectionSpacing: Dp = 24.dp              // Between major sections
    val sectionSpacingSmall: Dp = 16.dp         // Between minor sections
    val sectionHeader: Dp = 12.dp               // Header to content gap

    // List spacing
    val listItemSpacing: Dp = 12.dp             // Between list items
    val listItemPadding: Dp = 16.dp             // Inner list item padding
    val listSectionSpacing: Dp = 20.dp          // Between list sections

    // Component spacing
    val buttonPaddingHorizontal: Dp = 24.dp     // Button horizontal padding
    val buttonPaddingVertical: Dp = 12.dp       // Button vertical padding
    val buttonSpacing: Dp = 12.dp               // Gap between buttons

    val iconTextGap: Dp = 8.dp                  // Icon to text gap
    val iconSize: Dp = 24.dp                    // Standard icon size
    val iconSizeSmall: Dp = 20.dp               // Small icon size
    val iconSizeLarge: Dp = 32.dp               // Large icon size

    // Input spacing
    val inputPadding: Dp = 16.dp                // Inner input padding
    val inputSpacing: Dp = 12.dp                // Gap between inputs
    val inputLabelGap: Dp = 4.dp                // Label to input gap

    // Bottom navigation
    val bottomNavHeight: Dp = 64.dp             // Bottom nav bar height
    val bottomNavIconSize: Dp = 24.dp           // Bottom nav icon size
    val bottomNavPadding: Dp = 8.dp             // Bottom nav padding

    // Top bar
    val topBarHeight: Dp = 64.dp                // Top app bar height
    val topBarPadding: Dp = 16.dp               // Top bar horizontal padding

    // Minimum touch targets
    val minTouchTarget: Dp = 48.dp              // Minimum touchable area (ACCESSIBILITY)
    val minTouchTargetSmall: Dp = 44.dp         // Small touchable area

    // ═══════════════════════════════════════════════════════════════
    // SPECIFIC COMPONENT SIZES
    // ═══════════════════════════════════════════════════════════════

    // CyberCard dimensions
    val cyberCardHeight: Dp = 200.dp            // Standard CyberCard height
    val cyberCardPadding: Dp = 20.dp            // CyberCard inner padding

    // Auth and onboarding visuals
    val authLogoXL: Dp = 120.dp
    val authLogoLarge: Dp = 96.dp
    val authLogoMedium: Dp = 88.dp
    val authLogoGlow: Dp = 140.dp
    val authOrbitLarge: Dp = 320.dp
    val authOrbitMedium: Dp = 220.dp
    val authOrbitSmall: Dp = 150.dp
    val authOrbitOffsetLarge: Dp = 64.dp
    val authOrbitOffsetMedium: Dp = 24.dp
    val authOrbitOffsetSmall: Dp = 16.dp
    val authHeroTopSpacing: Dp = 40.dp
    val authCardPaddingExtra: Dp = 16.dp
    val authButtonHeight: Dp = 48.dp

    // Status badges
    val badgePaddingHorizontal: Dp = 12.dp      // Badge horizontal padding
    val badgePaddingVertical: Dp = 4.dp         // Badge vertical padding

    // Dividers
    val dividerThickness: Dp = 1.dp             // Standard divider thickness
    val dividerThicknessBold: Dp = 2.dp         // Bold divider thickness

    // Elevation/Shadow distances
    val elevationNone: Dp = 0.dp
    val elevationSmall: Dp = 2.dp               // Subtle elevation
    val elevationMedium: Dp = 4.dp              // Standard elevation
    val elevationLarge: Dp = 8.dp               // Strong elevation
    val elevationXLarge: Dp = 16.dp             // Maximum elevation (rare)
}

/**
 * USAGE EXAMPLES:
 *
 * // Screen padding
 * Modifier.padding(horizontal = SpacingTokens.screenPaddingHorizontal)
 *
 * // Card spacing
 * verticalArrangement = Arrangement.spacedBy(SpacingTokens.cardSpacing)
 *
 * // Component padding
 * Modifier.padding(SpacingTokens.cardPadding)
 *
 * // Section gaps
 * Spacer(modifier = Modifier.height(SpacingTokens.sectionSpacing))
 */
