package com.gosuraksha.app.scan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.scan.design.ScanTheme

private val InputShape  = RoundedCornerShape(16.dp)
private val ButtonShape = RoundedCornerShape(16.dp)

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

    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text  = label,
            style = typography.chipLabel,
            color = colors.textTertiary,
        )

        BasicTextField(
            value           = value,
            onValueChange   = onValueChange,
            modifier        = Modifier
                .fillMaxWidth()
                .clip(InputShape)
                .background(colors.surface)
                .border(1.5.dp, colors.border, InputShape)
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
// amber for password, pink for deepfake). Falls back to colors.primaryBlue.
@Composable
fun ScanPrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color? = null,
) {
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography
    val bgColor    = when {
        !enabled            -> colors.border
        accentColor != null -> accentColor
        else                -> colors.primaryBlue
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(ButtonShape)
            .background(color = bgColor)
            .clickable(
                enabled           = enabled,
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = text,
            style = typography.buttonText,
            color = if (enabled) Color.White else colors.textTertiary,
        )
    }
}