package com.gosuraksha.app.scan.components

// =============================================================================
// ScanInputs.kt — Modern fintech-style input field + CTA button
//
// ScanInputField  — Focus-aware text field with animated border accent
// ScanPrimaryAction — Spring-press CTA button with soft glow shadow
// =============================================================================

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.scan.design.ScanTheme

private val InputShape  = RoundedCornerShape(18.dp)
private val ButtonShape = RoundedCornerShape(18.dp)

// ─── ScanInputField ───────────────────────────────────────────────────────────
@Composable
fun ScanInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
) {
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography

    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue   = if (isFocused) colors.primaryBlue else colors.border,
        animationSpec = tween(durationMillis = 200),
        label         = "input_border_color",
    )
    val labelColor by animateColorAsState(
        targetValue   = if (isFocused) colors.primaryBlue else colors.textTertiary,
        animationSpec = tween(durationMillis = 200),
        label         = "input_label_color",
    )

    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(
            text  = label,
            style = typography.chipLabel,
            color = labelColor,
        )

        BasicTextField(
            value           = value,
            onValueChange   = onValueChange,
            modifier        = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .clip(InputShape)
                .background(colors.surface)
                .border(
                    width = if (isFocused) 2.dp else 1.5.dp,
                    color = borderColor,
                    shape = InputShape,
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            textStyle       = typography.bodyText.copy(color = colors.textPrimary),
            cursorBrush     = SolidColor(colors.primaryBlue),
            minLines        = minLines,
            maxLines        = maxLines,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction    = imeAction,
            ),
            decorationBox   = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text  = placeholder,
                        style = typography.bodyText,
                        color = colors.textTertiary,
                    )
                }
                innerTextField()
            },
        )

        if (supportingText != null) {
            Text(
                text  = supportingText,
                style = typography.bodySmall,
                color = colors.textTertiary,
            )
        }
    }
}

// ─── ScanPrimaryAction ────────────────────────────────────────────────────────
// accentColor: overrides primaryBlue when supplied (e.g. purple for email,
// amber for password). Falls back to colors.primaryBlue.
@Composable
fun ScanPrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color? = null,
) {
    val colors  = ScanTheme.colors
    val bgColor = when {
        !enabled            -> colors.border
        accentColor != null -> accentColor
        else                -> colors.primaryBlue
    }

    val interSrc  = remember { MutableInteractionSource() }
    val isPressed by interSrc.collectIsPressedAsState()
    val scale     by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.70f),
        label         = "cta_btn_scale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation    = if (enabled && !isPressed) 6.dp else 0.dp,
                shape        = ButtonShape,
                ambientColor = bgColor.copy(alpha = 0.20f),
                spotColor    = bgColor.copy(alpha = 0.30f),
            )
            .clip(ButtonShape)
            .background(bgColor)
            .clickable(
                enabled           = enabled,
                indication        = null,
                interactionSource = interSrc,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = text,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 15.sp,
            color      = if (enabled) Color.White else colors.textTertiary,
        )
    }
}
