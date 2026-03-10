package com.gosuraksha.app.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.ShapeTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens
import com.gosuraksha.app.ui.motion.MotionSpec

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonStyles.primaryButtonColors(),
    shape: androidx.compose.foundation.shape.CornerBasedShape = ButtonStyles.primaryButtonShape,
    elevation: ButtonElevation? = ButtonStyles.primaryButtonElevation(),
    contentPadding: PaddingValues = ButtonStyles.primaryButtonPadding,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = SpacingTokens.minTouchTarget),
        enabled = enabled,
        colors = colors,
        shape = shape,
        elevation = elevation,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonStyles.secondaryButtonColors(),
    border: BorderStroke = ButtonStyles.secondaryButtonBorder(),
    shape: androidx.compose.foundation.shape.CornerBasedShape = ButtonStyles.secondaryButtonShape,
    contentPadding: PaddingValues = ButtonStyles.secondaryButtonPadding,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = SpacingTokens.minTouchTarget),
        enabled = enabled,
        colors = colors,
        border = if (enabled) border else ButtonStyles.secondaryButtonBorderDisabled(),
        shape = shape,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefix: (@Composable () -> Unit)? = null,
    textStyle: TextStyle = TypographyTokens.inputText,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label, style = TypographyTokens.inputLabel) },
        singleLine = singleLine,
        leadingIcon = leadingIcon?.let { { Icon(it, null) } },
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        prefix = prefix,
        textStyle = textStyle,
        enabled = enabled,
        shape = ShapeTokens.input,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ColorTokens.accent(),
            unfocusedBorderColor = ColorTokens.border(),
            focusedTextColor = ColorTokens.textPrimary(),
            unfocusedTextColor = ColorTokens.textPrimary(),
            cursorColor = ColorTokens.accent(),
            focusedLabelColor = ColorTokens.accent(),
            unfocusedLabelColor = ColorTokens.textSecondary(),
            focusedContainerColor = ColorTokens.surface(),
            unfocusedContainerColor = ColorTokens.surface()
        )
    )
}

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonStyles.iconButtonColors()
    ) {
        Icon(icon, contentDescription = null)
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    border: BorderStroke? = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    ),
    shape: Shape = RoundedCornerShape(18.dp),
    elevation: CardElevation = CardDefaults.cardElevation(
        defaultElevation = 2.dp,
        pressedElevation = 4.dp,
        focusedElevation = 2.dp,
        hoveredElevation = 2.dp,
        draggedElevation = 2.dp,
        disabledElevation = 0.dp
    ),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = MotionSpec.FAST_MS),
        label = "app_card_press_scale"
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
            colors = colors,
            border = border,
            shape = shape,
            elevation = elevation,
            interactionSource = interactionSource,
            content = {
                Column(
                    modifier = Modifier.padding(contentPadding),
                    content = content
                )
            }
        )
    } else {
        Card(
            modifier = modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
            colors = colors,
            border = border,
            shape = shape,
            elevation = elevation,
            content = {
                Column(
                    modifier = Modifier.padding(contentPadding),
                    content = content
                )
            }
        )
    }
}
