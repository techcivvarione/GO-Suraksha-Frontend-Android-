package com.gosuraksha.app.design.tokens

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * GO SURAKSHA - SHAPE TOKEN SYSTEM
 *
 * Centralized border radius and shape definitions.
 * All components should use these tokens, not hardcoded shapes.
 *
 * DESIGN PRINCIPLES:
 * - Consistent corner radius across components
 * - Predictable visual language
 * - Optimized for PhonePe/UMANG-style clean rectangles
 * - Minimal decorative shapes
 */

object ShapeTokens {

    // ═══════════════════════════════════════════════════════════════
    // BASE SHAPE SCALE
    // ═══════════════════════════════════════════════════════════════

    val none: CornerBasedShape = RoundedCornerShape(0.dp)          // No rounding (sharp corners)
    val xxs: CornerBasedShape = RoundedCornerShape(2.dp)           // Minimal rounding
    val xs: CornerBasedShape = RoundedCornerShape(4.dp)            // Very small rounding
    val sm: CornerBasedShape = RoundedCornerShape(8.dp)            // Small rounding
    val md: CornerBasedShape = RoundedCornerShape(12.dp)           // Medium rounding (DEFAULT)
    val mdPlus: CornerBasedShape = RoundedCornerShape(14.dp)       // Medium+ rounding
    val lg: CornerBasedShape = RoundedCornerShape(16.dp)           // Large rounding
    val xl: CornerBasedShape = RoundedCornerShape(20.dp)           // Extra large rounding
    val xxl: CornerBasedShape = RoundedCornerShape(24.dp)          // Very large rounding
    val full: CornerBasedShape = RoundedCornerShape(50)            // Pill shape (percentage)

    // ═══════════════════════════════════════════════════════════════
    // SEMANTIC SHAPES (USE THESE IN CODE)
    // ═══════════════════════════════════════════════════════════════

    // Cards
    val card: CornerBasedShape = lg                                // Standard cards (16dp)
    val cardCompact: CornerBasedShape = lg                         // Compact cards (16dp)
    val cardLarge: CornerBasedShape = xl                           // Large feature cards
    val cardSpecial: CornerBasedShape = xl                         // Special cards (CyberCard)

    // Buttons
    val button: CornerBasedShape = sm                              // Standard buttons
    val buttonLarge: CornerBasedShape = md                         // Large primary buttons
    val buttonPill: CornerBasedShape = full                        // Pill-shaped buttons

    // Inputs
    val input: CornerBasedShape = sm                               // Text inputs
    val inputSearch: CornerBasedShape = full                       // Search bars

    // Badges and chips
    val badge: CornerBasedShape = xs                               // Small badges
    val chip: CornerBasedShape = full                              // Chips and tags

    // Bottom sheets and dialogs
    val bottomSheet: CornerBasedShape = RoundedCornerShape(        // Bottom sheets (rounded top only)
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    val dialog: CornerBasedShape = lg                              // Dialogs

    // Navigation
    val bottomNav: CornerBasedShape = none                         // Bottom navigation (no rounding)
    val topBar: CornerBasedShape = none                            // Top bar (no rounding)

    // Images and avatars
    val avatar: CornerBasedShape = full                            // Profile pictures
    val image: CornerBasedShape = md                               // Standard images
    val imageSmall: CornerBasedShape = sm                          // Small thumbnails

    // Special shapes
    val cyberCard: CornerBasedShape = xxl                          // CyberCard (24dp)
    val statusBadge: CornerBasedShape = xs                         // Status indicators
    val divider: CornerBasedShape = none                           // Dividers (straight)

    // ═══════════════════════════════════════════════════════════════
    // MATERIAL 3 SHAPES (For MaterialTheme)
    // ═══════════════════════════════════════════════════════════════

    val AppShapes = Shapes(
        extraSmall = xs,        // 4dp  - Small components
        small = sm,             // 8dp  - Buttons, chips
        medium = lg,            // 16dp - Cards (DEFAULT)
        large = xl,             // 20dp - Large cards
        extraLarge = xl         // 20dp - Special components
    )

    // ═══════════════════════════════════════════════════════════════
    // BORDER WIDTHS
    // ═══════════════════════════════════════════════════════════════

    object Border {
        val none = 0.dp
        val thin = 1.dp         // Standard borders
        val medium = 2.dp       // Emphasized borders
        val thick = 3.dp        // Strong borders (rare)
    }
}

/**
 * USAGE EXAMPLES:
 *
 * // Card
 * Card(shape = ShapeTokens.card) { ... }
 *
 * // Button
 * Button(shape = ShapeTokens.button) { ... }
 *
 * // Custom border
 * Modifier.border(
 *     width = ShapeTokens.Border.thin,
 *     color = ColorTokens.border(),
 *     shape = ShapeTokens.card
 * )
 */
