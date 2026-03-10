package com.gosuraksha.app.design.components

import com.gosuraksha.app.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.ShapeTokens
import com.gosuraksha.app.design.tokens.ElevationTokens

/**
 * GO SURAKSHA - CARD COMPONENT STYLES
 *
 * Centralized card configurations following PhonePe/UMANG patterns.
 * All cards should use these predefined styles.
 *
 * DESIGN PRINCIPLES:
 * - White cards in light mode, dark surface in dark mode
 * - Borders for separation (not shadows)
 * - Clean, rectangular cards
 * - No gradients or glassmorphism
 * - Clear visual hierarchy
 */

object CardStyles {

    // ═══════════════════════════════════════════════════════════════
    // STANDARD CARD (Most common card type)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun standardCardColors() = CardDefaults.cardColors(
        containerColor = ColorTokens.surface(),
        contentColor = ColorTokens.textPrimary(),
        disabledContainerColor = ColorTokens.surfaceVariant().copy(alpha = 0.7f),
        disabledContentColor = ColorTokens.Light.textDisabled
    )

    @Composable
    fun standardCardBorder() = BorderStroke(
        width = ShapeTokens.Border.thin,
        color = ColorTokens.border().copy(alpha = 0.7f)
    )

    @Composable
    fun standardCardElevation() = CardDefaults.cardElevation(
        defaultElevation = ElevationTokens.cardRest,
        pressedElevation = ElevationTokens.cardPressed,
        focusedElevation = ElevationTokens.cardFocused,
        hoveredElevation = ElevationTokens.cardHover,
        draggedElevation = ElevationTokens.cardRest,
        disabledElevation = ElevationTokens.none
    )

    val standardCardShape = ShapeTokens.card

    // ═══════════════════════════════════════════════════════════════
    // OUTLINED CARD (Emphasized borders)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun outlinedCardColors() = CardDefaults.outlinedCardColors(
        containerColor = ColorTokens.surface(),
        contentColor = ColorTokens.textPrimary(),
        disabledContainerColor = ColorTokens.surface().copy(alpha = 0.6f),
        disabledContentColor = ColorTokens.Light.textDisabled
    )

    fun outlinedCardBorder() = BorderStroke(
        width = ShapeTokens.Border.medium,
        color = ColorTokens.Light.borderStrong
    )

    val outlinedCardShape = ShapeTokens.card

    // ═══════════════════════════════════════════════════════════════
    // ELEVATED CARD (Rare use - for floating elements)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun elevatedCardColors() = CardDefaults.elevatedCardColors(
        containerColor = ColorTokens.surface(),
        contentColor = ColorTokens.textPrimary(),
        disabledContainerColor = ColorTokens.surface().copy(alpha = 0.6f),
        disabledContentColor = ColorTokens.Light.textDisabled
    )

    @Composable
    fun elevatedCardElevation() = CardDefaults.elevatedCardElevation(
        defaultElevation = ElevationTokens.md,
        pressedElevation = ElevationTokens.sm,
        focusedElevation = ElevationTokens.lg,
        hoveredElevation = ElevationTokens.lg,
        draggedElevation = ElevationTokens.lg,
        disabledElevation = ElevationTokens.none
    )

    // ═══════════════════════════════════════════════════════════════
    // STATUS CARDS (Success, Warning, Error, Info)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun successCardColors() = CardDefaults.cardColors(
        containerColor = ColorTokens.successLight(),
        contentColor = ColorTokens.textPrimary()
    )

    @Composable
    fun successCardBorder() = BorderStroke(
        width = ShapeTokens.Border.thin,
        color = ColorTokens.success()
    )

    @Composable
    fun warningCardColors() = CardDefaults.cardColors(
        containerColor = ColorTokens.warningLight(),
        contentColor = ColorTokens.textPrimary()
    )

    @Composable
    fun warningCardBorder() = BorderStroke(
        width = ShapeTokens.Border.thin,
        color = ColorTokens.warning()
    )

    @Composable
    fun errorCardColors() = CardDefaults.cardColors(
        containerColor = ColorTokens.errorLight(),
        contentColor = ColorTokens.textPrimary()
    )

    @Composable
    fun errorCardBorder() = BorderStroke(
        width = ShapeTokens.Border.thin,
        color = ColorTokens.error()
    )

    @Composable
    fun infoCardColors() = CardDefaults.cardColors(
        containerColor = ColorTokens.Light.infoLight,
        contentColor = ColorTokens.textPrimary()
    )

    fun infoCardBorder() = BorderStroke(
        width = ShapeTokens.Border.thin,
        color = ColorTokens.Light.info
    )

    // ═══════════════════════════════════════════════════════════════
    // ACCENT CARD (Highlighted card with green accent)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun accentCardColors() = CardDefaults.cardColors(
        containerColor = ColorTokens.surface(),
        contentColor = ColorTokens.textPrimary()
    )

    @Composable
    fun accentCardBorder() = BorderStroke(
        width = ShapeTokens.Border.medium,
        color = ColorTokens.accent()
    )

    // ═══════════════════════════════════════════════════════════════
    // LARGE FEATURE CARD (For important sections)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun largeCardColors() = standardCardColors()

    @Composable
    fun largeCardBorder() = standardCardBorder()

    val largeCardShape = ShapeTokens.cardLarge

    @Composable
    fun largeCardElevation() = CardDefaults.cardElevation(
        defaultElevation = ElevationTokens.sm,
        pressedElevation = ElevationTokens.none,
        focusedElevation = ElevationTokens.md,
        hoveredElevation = ElevationTokens.md
    )

    // ═══════════════════════════════════════════════════════════════
    // CYBER CARD (Special branded card)
    // ═══════════════════════════════════════════════════════════════

    // Note: CyberCard uses gradient background, defined separately
    // This is an exception to the no-gradient rule for branding

    val cyberCardShape = ShapeTokens.cyberCard

    @Composable
    fun cyberCardElevation() = CardDefaults.cardElevation(
        defaultElevation = ElevationTokens.cyberCard
    )

    // ═══════════════════════════════════════════════════════════════
    // CLICKABLE CARD STATES
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun clickableCardElevation() = CardDefaults.cardElevation(
        defaultElevation = ElevationTokens.cardRest,
        pressedElevation = ElevationTokens.cardPressed,
        focusedElevation = ElevationTokens.cardFocused,
        hoveredElevation = ElevationTokens.cardHover
    )
}

/**
 * USAGE EXAMPLES:
 *
 * // Standard card (MOST COMMON)
 * Card(
 *     colors = CardStyles.standardCardColors(),
 *     border = CardStyles.standardCardBorder(),
 *     shape = CardStyles.standardCardShape,
 *     elevation = CardStyles.standardCardElevation
 * ) {
 *     Column(modifier = Modifier.padding(SpacingTokens.cardPadding)) {
 *         Text(stringResource(R.string.ui_cardstyles_1), style = TypographyTokens.cardTitle)
 *         Text(stringResource(R.string.ui_cardstyles_2), style = TypographyTokens.cardSubtitle)
 *     }
 * }
 *
 * // Success card (for positive status)
 * Card(
 *     colors = CardStyles.successCardColors(),
 *     border = CardStyles.successCardBorder(),
 *     shape = CardStyles.standardCardShape
 * ) {
 *     Column(modifier = Modifier.padding(SpacingTokens.cardPadding)) {
 *         Text(stringResource(R.string.ui_cardstyles_3), style = TypographyTokens.cardTitle)
 *     }
 * }
 *
 * // Clickable card
 * Card(
 *     onClick = { },
 *     colors = CardStyles.standardCardColors(),
 *     border = CardStyles.standardCardBorder(),
 *     elevation = CardStyles.clickableCardElevation
 * ) {
 *     Text(stringResource(R.string.ui_cardstyles_4))
 * }
 */
