package com.gosuraksha.app.design.components

import com.gosuraksha.app.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.ShapeTokens
import com.gosuraksha.app.design.tokens.ElevationTokens

/**
 * GO SURAKSHA - BUTTON COMPONENT STYLES
 *
 * Centralized button configurations following PhonePe/UMANG patterns.
 * All buttons should use these predefined styles.
 *
 * DESIGN PRINCIPLES:
 * - Large, tappable buttons (minimum 48dp height)
 * - Clear visual hierarchy
 * - High contrast for accessibility
 * - No gradients or decorative effects
 */

object ButtonStyles {

    // ═══════════════════════════════════════════════════════════════
    // PRIMARY BUTTON (Main actions - green background)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun primaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = ColorTokens.accent(),
        contentColor = Color.White,
        disabledContainerColor = ColorTokens.Light.textDisabled,
        disabledContentColor = Color.White.copy(alpha = 0.6f)
    )

    val primaryButtonPadding = PaddingValues(
        horizontal = SpacingTokens.buttonPaddingHorizontal,
        vertical = SpacingTokens.buttonPaddingVertical
    )

    val primaryButtonShape = ShapeTokens.button

    @Composable
    fun primaryButtonElevation() = ButtonDefaults.buttonElevation(
        defaultElevation = ElevationTokens.none,
        pressedElevation = ElevationTokens.none,
        disabledElevation = ElevationTokens.none,
        hoveredElevation = ElevationTokens.buttonHover,
        focusedElevation = ElevationTokens.buttonHover
    )

    // ═══════════════════════════════════════════════════════════════
    // SECONDARY BUTTON (Alternative actions - outlined)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun secondaryButtonColors() = ButtonDefaults.outlinedButtonColors(
        contentColor = ColorTokens.accent(),
        disabledContentColor = ColorTokens.Light.textDisabled
    )

    @Composable
    fun secondaryButtonBorder() = BorderStroke(
        width = ShapeTokens.Border.medium,
        color = ColorTokens.accent()
    )

    fun secondaryButtonBorderDisabled() = BorderStroke(
        width = ShapeTokens.Border.medium,
        color = ColorTokens.Light.textDisabled
    )

    val secondaryButtonPadding = primaryButtonPadding
    val secondaryButtonShape = ShapeTokens.button

    // ═══════════════════════════════════════════════════════════════
    // TERTIARY BUTTON (Subtle actions - text only)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun tertiaryButtonColors() = ButtonDefaults.textButtonColors(
        contentColor = ColorTokens.accent(),
        disabledContentColor = ColorTokens.Light.textDisabled
    )

    val tertiaryButtonPadding = PaddingValues(
        horizontal = SpacingTokens.md,
        vertical = SpacingTokens.xs
    )

    // ═══════════════════════════════════════════════════════════════
    // DANGER BUTTON (Destructive actions - red)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun dangerButtonColors() = ButtonDefaults.buttonColors(
        containerColor = ColorTokens.error(),
        contentColor = Color.White,
        disabledContainerColor = ColorTokens.Light.textDisabled,
        disabledContentColor = Color.White.copy(alpha = 0.6f)
    )

    val dangerButtonPadding = primaryButtonPadding
    val dangerButtonShape = ShapeTokens.button

    @Composable
    fun dangerButtonElevation() = primaryButtonElevation()

    // ═══════════════════════════════════════════════════════════════
    // SUCCESS BUTTON (Success actions - green variant)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun successButtonColors() = ButtonDefaults.buttonColors(
        containerColor = ColorTokens.success(),
        contentColor = Color.White,
        disabledContainerColor = ColorTokens.Light.textDisabled,
        disabledContentColor = Color.White.copy(alpha = 0.6f)
    )

    // ═══════════════════════════════════════════════════════════════
    // ICON BUTTON (Icon-only buttons)
    // ═══════════════════════════════════════════════════════════════

    @Composable
    fun iconButtonColors() = IconButtonDefaults.iconButtonColors(
        containerColor = Color.Transparent,
        contentColor = ColorTokens.textPrimary(),
        disabledContainerColor = Color.Transparent,
        disabledContentColor = ColorTokens.Light.textDisabled
    )

    // ═══════════════════════════════════════════════════════════════
    // FLOATING ACTION BUTTON
    // ═══════════════════════════════════════════════════════════════


    @Composable
    fun fabElevation() = FloatingActionButtonDefaults.elevation(
        defaultElevation = ElevationTokens.fab,
        pressedElevation = ElevationTokens.md,
        hoveredElevation = ElevationTokens.lg,
        focusedElevation = ElevationTokens.lg
    )

    // ═══════════════════════════════════════════════════════════════
    // BUTTON SIZE VARIANTS
    // ═══════════════════════════════════════════════════════════════

    object Size {
        // Large button (for primary CTAs)
        val largePadding = PaddingValues(
            horizontal = 32.dp,
            vertical = 16.dp
        )

        // Small button (for compact spaces)
        val smallPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        )
    }
}

/**
 * USAGE EXAMPLES:
 *
 * // Primary button
 * Button(
 *     onClick = { },
 *     colors = ButtonStyles.primaryButtonColors(),
 *     shape = ButtonStyles.primaryButtonShape,
 *     contentPadding = ButtonStyles.primaryButtonPadding,
 *     elevation = ButtonStyles.primaryButtonElevation
 * ) {
 *     Text(stringResource(R.string.ui_buttonstyles_1), style = TypographyTokens.buttonText)
 * }
 *
 * // Secondary (outlined) button
 * OutlinedButton(
 *     onClick = { },
 *     colors = ButtonStyles.secondaryButtonColors(),
 *     border = ButtonStyles.secondaryButtonBorder(),
 *     shape = ButtonStyles.secondaryButtonShape,
 *     contentPadding = ButtonStyles.secondaryButtonPadding
 * ) {
 *     Text(stringResource(R.string.ui_buttonstyles_2), style = TypographyTokens.buttonText)
 * }
 *
 * // Danger button
 * Button(
 *     onClick = { },
 *     colors = ButtonStyles.dangerButtonColors(),
 *     shape = ButtonStyles.dangerButtonShape,
 *     contentPadding = ButtonStyles.dangerButtonPadding
 * ) {
 *     Text(stringResource(R.string.ui_buttonstyles_3), style = TypographyTokens.buttonText)
 * }
 */