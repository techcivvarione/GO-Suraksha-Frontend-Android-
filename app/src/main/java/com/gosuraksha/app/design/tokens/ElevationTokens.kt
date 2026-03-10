package com.gosuraksha.app.design.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * GO SURAKSHA - ELEVATION TOKEN SYSTEM
 *
 * Centralized elevation (shadow) definitions.
 * Following PhonePe/UMANG pattern: minimal shadows, clean separations.
 *
 * DESIGN PRINCIPLES:
 * - Minimal shadow usage (prefer borders for separation)
 * - Subtle elevations for hierarchy
 * - No heavy drop shadows
 * - Clean, flat design with strategic depth
 */

object ElevationTokens {

    // ═══════════════════════════════════════════════════════════════
    // ELEVATION SCALE (in dp)
    // ═══════════════════════════════════════════════════════════════

    val none: Dp = 0.dp         // No elevation (flat)
    val xs: Dp = 1.dp           // Hairline lift
    val sm: Dp = 2.dp           // Subtle premium card lift
    val md: Dp = 4.dp           // Medium elevation (raised cards)
    val lg: Dp = 8.dp           // Large elevation (floating elements)
    val xl: Dp = 12.dp          // Extra large (dialogs, modals)
    val xxl: Dp = 16.dp         // Maximum elevation (rare use)

    // ═══════════════════════════════════════════════════════════════
    // SEMANTIC ELEVATIONS (USE THESE IN CODE)
    // ═══════════════════════════════════════════════════════════════

    // Surface elevations
    val surface: Dp = xs                // Base tonal surface lift
    val surfaceRaised: Dp = sm          // Raised surface

    // Card elevations
    val cardRest: Dp = sm               // Consistent card baseline
    val cardHover: Dp = md              // Cards on hover
    val cardPressed: Dp = xs            // Cards when pressed
    val cardFocused: Dp = md            // Cards when focused

    // Button elevations
    val buttonRest: Dp = xs             // Buttons at rest
    val buttonHover: Dp = sm            // Buttons on hover
    val buttonPressed: Dp = none        // Buttons when pressed

    // Floating elements
    val fab: Dp = md                    // Floating action button
    val bottomNav: Dp = sm              // Bottom navigation bar
    val topBar: Dp = sm                 // Top app bar

    // Overlays
    val dialog: Dp = xl                 // Dialogs and modals
    val bottomSheet: Dp = lg            // Bottom sheets
    val menu: Dp = md                   // Dropdown menus
    val snackbar: Dp = md               // Snackbars and toasts

    // Special components
    val cyberCard: Dp = sm              // CyberCard (subtle depth)
    val statusCard: Dp = none           // Status cards (use border instead)

    // ═══════════════════════════════════════════════════════════════
    // DESIGN GUIDELINES
    // ═══════════════════════════════════════════════════════════════

    /**
     * WHEN TO USE ELEVATION:
     * - Floating action buttons
     * - Dialogs and modals
     * - Bottom sheets
     * - Top/bottom navigation bars
     * - Dropdown menus
     *
     * WHEN TO USE BORDERS INSTEAD:
     * - Standard cards (use 1dp border)
     * - List items (use dividers)
     * - Input fields (use border)
     * - Most static content containers
     *
     * WHY MINIMAL SHADOWS?
     * - PhonePe/UMANG use borders for separation
     * - Cleaner, more accessible design
     * - Better performance (fewer render layers)
     * - More professional appearance
     */
}

/**
 * USAGE EXAMPLES:
 *
 * // Card with border (PREFERRED)
 * Card(
 *     elevation = CardDefaults.cardElevation(
 *         defaultElevation = ElevationTokens.none
 *     ),
 *     border = BorderStroke(
 *         width = ShapeTokens.Border.thin,
 *         color = ColorTokens.border()
 *     )
 * ) { ... }
 *
 * // Top bar with subtle shadow
 * TopAppBar(
 *     modifier = Modifier.shadow(ElevationTokens.topBar)
 * )
 *
 * // Dialog with strong elevation
 * Dialog(...) {
 *     Card(elevation = ElevationTokens.dialog) { ... }
 * }
 */
