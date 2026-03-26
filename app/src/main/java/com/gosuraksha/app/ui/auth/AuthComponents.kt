package com.gosuraksha.app.ui.auth

// =============================================================================
// AuthComponents.kt — PhonePe-style shared UI components
//
// Changes:
//   • AuthCardSurface: added fillMaxHeight() so card stretches to screen bottom
//     instead of cutting off mid-screen
// =============================================================================

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// AuthTextField
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AuthTextField(
    value:           String,
    onValueChange:   (String) -> Unit,
    label:           String,
    placeholder:     String        = "",
    leadingIcon:     ImageVector?  = null,
    trailingIcon:    (@Composable () -> Unit)? = null,
    keyboardType:    KeyboardType  = KeyboardType.Text,
    singleLine:      Boolean       = true,
    enabled:         Boolean       = true,
    isDark:          Boolean,
    modifier:        Modifier      = Modifier
) {
    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text          = label.uppercase(),
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            color         = AuthColors.textSec(isDark)
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            enabled       = enabled,
            placeholder   = {
                Text(
                    placeholder,
                    fontSize = 14.sp,
                    color    = AuthColors.textTert(isDark)
                )
            },
            leadingIcon   = leadingIcon?.let { icon ->
                {
                    Icon(
                        icon, null,
                        tint     = if (value.isNotEmpty()) AuthColors.Accent
                        else AuthColors.textTert(isDark),
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            trailingIcon  = trailingIcon,
            modifier      = Modifier
                .fillMaxWidth()
                .height(54.dp),
            textStyle     = LocalTextStyle.current.copy(
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = AuthColors.textPri(isDark)
            ),
            shape  = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = AuthColors.Accent,
                unfocusedBorderColor    = AuthColors.border(isDark),
                focusedContainerColor   = AuthColors.field(isDark),
                unfocusedContainerColor = AuthColors.field(isDark),
                disabledContainerColor  = AuthColors.field(isDark),
                cursorColor             = AuthColors.Accent,
                focusedTextColor        = AuthColors.textPri(isDark),
                unfocusedTextColor      = AuthColors.textPri(isDark)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine      = singleLine
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthPasswordField
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AuthPasswordField(
    value:              String,
    onValueChange:      (String) -> Unit,
    label:              String,
    placeholder:        String   = "",
    passwordVisible:    Boolean,
    onVisibilityToggle: () -> Unit,
    trailingExtra:      (@Composable () -> Unit)? = null,
    isDark:             Boolean,
    modifier:           Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text          = label.uppercase(),
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            color         = AuthColors.textSec(isDark)
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(placeholder, fontSize = 14.sp, color = AuthColors.textTert(isDark))
            },
            leadingIcon   = {
                Icon(
                    Icons.Outlined.Lock, null,
                    tint     = if (value.isNotEmpty()) AuthColors.Accent
                    else AuthColors.textTert(isDark),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon  = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(end = 4.dp)
                ) {
                    trailingExtra?.invoke()
                    IconButton(
                        onClick  = onVisibilityToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (passwordVisible) Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint               = AuthColors.textTert(isDark),
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            modifier  = Modifier.fillMaxWidth().height(54.dp),
            textStyle = LocalTextStyle.current.copy(
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = AuthColors.textPri(isDark)
            ),
            shape  = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = AuthColors.Accent,
                unfocusedBorderColor    = AuthColors.border(isDark),
                focusedContainerColor   = AuthColors.field(isDark),
                unfocusedContainerColor = AuthColors.field(isDark),
                cursorColor             = AuthColors.Accent,
                focusedTextColor        = AuthColors.textPri(isDark),
                unfocusedTextColor      = AuthColors.textPri(isDark)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine      = true
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthPhoneField
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AuthPhoneField(
    value:         String,
    onValueChange: (String) -> Unit,
    isDark:        Boolean,
    modifier:      Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text          = "MOBILE NUMBER",
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            color         = AuthColors.textSec(isDark)
        )
        OutlinedTextField(
            value         = value,
            onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) onValueChange(it) },
            placeholder   = {
                Text("Enter 10-digit number", fontSize = 14.sp, color = AuthColors.textTert(isDark))
            },
            prefix        = {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier              = Modifier.padding(end = 4.dp)
                ) {
                    Text("🇮🇳", fontSize = 15.sp)
                    Text(
                        "+91",
                        fontSize      = 15.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = AuthColors.textPri(isDark)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(AuthColors.border(isDark))
                    )
                    Spacer(Modifier.width(2.dp))
                }
            },
            modifier  = Modifier.fillMaxWidth().height(56.dp),
            textStyle = LocalTextStyle.current.copy(
                fontSize      = 16.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = AuthColors.textPri(isDark),
                letterSpacing = 0.8.sp
            ),
            shape  = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = AuthColors.Accent,
                unfocusedBorderColor    = AuthColors.border(isDark),
                focusedContainerColor   = AuthColors.field(isDark),
                unfocusedContainerColor = AuthColors.field(isDark),
                cursorColor             = AuthColors.Accent,
                focusedTextColor        = AuthColors.textPri(isDark),
                unfocusedTextColor      = AuthColors.textPri(isDark)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine      = true
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthPrimaryButton
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AuthPrimaryButton(
    text:      String,
    onClick:   () -> Unit,
    enabled:   Boolean  = true,
    isLoading: Boolean  = false,
    isDark:    Boolean,
    modifier:  Modifier = Modifier
) {
    val isActive = enabled && !isLoading

    val infiniteTransition = rememberInfiniteTransition(label = "btnPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.85f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val gradientBrush = Brush.horizontalGradient(
        colors = if (isActive)
            listOf(AuthColors.AccentGradStart, AuthColors.AccentGradEnd)
        else
            listOf(AuthColors.accentDisabled(isDark), AuthColors.accentDisabled(isDark))
    )

    Box(
        modifier         = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick   = onClick,
            enabled   = isActive,
            modifier  = Modifier.fillMaxSize(),
            shape     = RoundedCornerShape(14.dp),
            colors    = ButtonDefaults.buttonColors(
                containerColor         = Color.Transparent,
                contentColor           = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor   = Color.White.copy(alpha = 0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(18.dp).scale(pulseAlpha),
                    strokeWidth = 2.dp,
                    color       = Color.White
                )
                Spacer(Modifier.width(10.dp))
                Text("Please wait…", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Text(
                    text          = text,
                    fontSize      = 15.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthOutlinedButton
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AuthOutlinedButton(
    text:     String,
    onClick:  () -> Unit,
    enabled:  Boolean  = true,
    isDark:   Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        border   = androidx.compose.foundation.BorderStroke(1.5.dp, AuthColors.border(isDark)),
        colors   = ButtonDefaults.outlinedButtonColors(
            contentColor         = AuthColors.textSec(isDark),
            disabledContentColor = AuthColors.textTert(isDark)
        )
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthGoogleButton
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AuthGoogleButton(
    onClick:   () -> Unit,
    enabled:   Boolean  = true,
    isLoading: Boolean  = false,
    isDark:    Boolean,
    modifier:  Modifier = Modifier
) {
    OutlinedButton(
        onClick  = onClick,
        enabled  = enabled && !isLoading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        border   = androidx.compose.foundation.BorderStroke(1.5.dp, AuthColors.border(isDark)),
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor       = if (isDark) AuthColors.GoogleBgDark else AuthColors.GoogleBgLight,
            contentColor         = AuthColors.textPri(isDark),
            disabledContentColor = AuthColors.textTert(isDark)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(17.dp),
                strokeWidth = 2.dp,
                color       = AuthColors.Accent
            )
            Spacer(Modifier.width(10.dp))
            Text("Signing in…", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("G", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
            }
            Spacer(Modifier.width(10.dp))
            Text("Continue with Google", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthOrDivider
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AuthOrDivider(isDark: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = AuthColors.border(isDark), thickness = 1.dp)
        Text("or", fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp, color = AuthColors.textTert(isDark))
        HorizontalDivider(modifier = Modifier.weight(1f), color = AuthColors.border(isDark), thickness = 1.dp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthErrorRow
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AuthErrorRow(message: String, isDark: Boolean) {
    AnimatedVisibility(
        visible = message.isNotBlank(),
        enter   = fadeIn(tween(250)) + expandVertically(tween(250)),
        exit    = fadeOut(tween(150)) + shrinkVertically(tween(150))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) AuthColors.ErrorBgDark else AuthColors.ErrorBgLight)
                .border(1.dp, AuthColors.ErrorRed.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Outlined.Warning, null, tint = AuthColors.ErrorRed, modifier = Modifier.size(15.dp))
            Text(text = message, fontSize = 12.sp, color = AuthColors.ErrorRed, lineHeight = 17.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthSuccessRow
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AuthSuccessRow(message: String, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) AuthColors.SuccessBgDark else AuthColors.SuccessBgLight)
            .border(1.dp, AuthColors.Accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.CheckCircle, null, tint = AuthColors.Accent, modifier = Modifier.size(15.dp))
        Text(text = message, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AuthColors.Accent)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OtpInputRow
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun OtpInputRow(
    otp:         String,
    onOtpChange: (String) -> Unit,
    isDark:      Boolean,
    modifier:    Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(6) { index ->
                val char     = otp.getOrNull(index)
                val isFilled = char != null
                val isCursor = index == otp.length && otp.length < 6

                val boxScale by animateFloatAsState(
                    targetValue   = if (isFilled) 1.05f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                    label         = "otpScale$index"
                )
                val borderColor by animateColorAsState(
                    targetValue   = when {
                        isFilled -> AuthColors.Accent
                        isCursor -> AuthColors.Accent.copy(alpha = 0.6f)
                        else     -> AuthColors.border(isDark)
                    },
                    animationSpec = tween(200),
                    label         = "otpBorder$index"
                )
                val bgColor by animateColorAsState(
                    targetValue   = if (isFilled)
                        AuthColors.Accent.copy(alpha = if (isDark) 0.12f else 0.07f)
                    else
                        AuthColors.field(isDark),
                    animationSpec = tween(200),
                    label         = "otpBg$index"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .scale(boxScale)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor)
                        .border(
                            width = if (isFilled || isCursor) 2.dp else 1.5.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (char != null) {
                        Text(
                            text       = char.toString(),
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color      = if (isDark) AuthColors.Accent else AuthColors.AccentDim
                        )
                    } else if (isCursor) {
                        val cursorAlpha by rememberInfiniteTransition(label = "cursor")
                            .animateFloat(
                                initialValue  = 0f,
                                targetValue   = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation  = tween(500),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "cursorBlink"
                            )
                        Box(
                            modifier = Modifier
                                .size(3.dp, 22.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(AuthColors.Accent.copy(alpha = cursorAlpha))
                        )
                    }
                }
            }
        }

        androidx.compose.foundation.text.BasicTextField(
            value           = otp,
            onValueChange   = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onOtpChange(it) },
            modifier        = Modifier.fillMaxWidth().height(58.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            decorationBox   = {}
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PasswordStrengthBar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun PasswordStrengthBar(password: String, isDark: Boolean) {
    val strength = when {
        password.length >= 12
                && password.any { it.isUpperCase() }
                && password.any { it.isDigit() }
                && password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 10
                && password.any { it.isUpperCase() }
                && password.any { it.isDigit() }           -> 3
        password.length >= 8                               -> 2
        else                                               -> 1
    }

    val (label, barColor) = when (strength) {
        1    -> "Weak"        to AuthColors.ErrorRed
        2    -> "Fair"        to Color(0xFFF59E0B)
        3    -> "Strong"      to AuthColors.Accent
        else -> "Very Strong" to AuthColors.Accent
    }

    AnimatedVisibility(
        visible = password.isNotEmpty(),
        enter   = fadeIn(tween(200)) + expandVertically(tween(200)),
        exit    = fadeOut(tween(150)) + shrinkVertically(tween(150))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(4) { i ->
                    val segColor by animateColorAsState(
                        targetValue   = if (i < strength) barColor else AuthColors.border(isDark),
                        animationSpec = tween(300),
                        label         = "seg$i"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(segColor)
                    )
                }
            }
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = barColor)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthCardSurface — FIXED: fillMaxHeight() added so card stretches to bottom
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun AuthCardSurface(
    isDark:   Boolean,
    modifier: Modifier = Modifier,
    content:  @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()                                          // ← FIX
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(AuthColors.card(isDark))
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}