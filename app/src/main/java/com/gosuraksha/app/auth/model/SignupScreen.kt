package com.gosuraksha.app.ui.auth

import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.gosuraksha.app.R
import com.gosuraksha.app.design.components.AppButton
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.design.components.AppIconButton
import com.gosuraksha.app.design.components.AppTextField
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.ShapeTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens
import com.gosuraksha.app.presentation.auth.AuthViewModel
import com.gosuraksha.app.ui.components.localizedUiMessage

@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }

    // OTP WIRING START
    val isSendingOtp by viewModel.isSendingOtp.collectAsState()
    val isVerifyingOtp by viewModel.isVerifyingOtp.collectAsState()
    val otpSecondsLeft by viewModel.otpSecondsLeft.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val emailVerified by viewModel.emailVerified.collectAsState()
    val otpError by viewModel.otpError.collectAsState()

    val emailError = remember(email) {
        if (email.isBlank()) "Email is required"
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Invalid email"
        else null
    }
    // OTP WIRING END

    val bg = ColorTokens.background()
    val surface = ColorTokens.surface()
    val border = ColorTokens.border()
    val accent = ColorTokens.accent()
    val success = ColorTokens.success()
    val warning = ColorTokens.warning()
    val errorColor = ColorTokens.error()
    val textPrimary = ColorTokens.textPrimary()
    val textSecondary = ColorTokens.textSecondary()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SpacingTokens.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(SpacingTokens.authHeroTopSpacing))

            Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(SpacingTokens.authLogoLarge)
            )

            Spacer(Modifier.height(SpacingTokens.xs))

            Text(stringResource(R.string.ui_signupscreen_1), color = textPrimary, style = TypographyTokens.screenTitle)
            Spacer(Modifier.height(SpacingTokens.xxs))
            Text(
                stringResource(R.string.ui_signupscreen_2),
                color = textSecondary.copy(alpha = 0.8f),
                style = TypographyTokens.labelSmall
            )

            Spacer(Modifier.height(SpacingTokens.md))

            AppCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(SpacingTokens.authCardPaddingExtra),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
                ) {
                    AppTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        label = stringResource(R.string.ui_signupscreen_8),
                        leadingIcon = Icons.Outlined.Person,
                        textStyle = TypographyTokens.bodySmall
                    )
                    AppTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            error = null
                            otp = ""
                            viewModel.resetOtpState()
                        },
                        label = stringResource(R.string.ui_signupscreen_9),
                        leadingIcon = Icons.Outlined.Email,
                        textStyle = TypographyTokens.bodySmall,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        )
                    )

                    // OTP WIRING START
                    AppButton(
                        onClick = { viewModel.sendOtp(email = email, emailError = emailError) },
                        enabled = emailError == null && otpSecondsLeft == 0 && !isSendingOtp,
                        modifier = Modifier.fillMaxWidth().height(SpacingTokens.authButtonHeight)
                    ) {
                        if (isSendingOtp) {
                            CircularProgressIndicator(
                                color = ColorTokens.background(),
                                modifier = Modifier.size(SpacingTokens.iconSizeSmall),
                                strokeWidth = SpacingTokens.xxs
                            )
                        } else {
                            val text = if (otpSecondsLeft > 0) "Resend OTP (${otpSecondsLeft}s)" else "Send OTP"
                            Text(text, style = TypographyTokens.buttonText)
                        }
                    }

                    AnimatedVisibility(visible = isOtpSent || emailVerified) {
                        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
                            AppTextField(
                                value = otp,
                                onValueChange = { if (it.length <= 6) otp = it },
                                label = "Email OTP",
                                leadingIcon = Icons.Filled.Info,
                                textStyle = TypographyTokens.bodySmall,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
                            )

                            AppButton(
                                onClick = { viewModel.verifyOtp(email = email, otp = otp) },
                                enabled = otp.length == 6 && !isVerifyingOtp && !emailVerified,
                                modifier = Modifier.fillMaxWidth().height(SpacingTokens.authButtonHeight)
                            ) {
                                if (isVerifyingOtp) {
                                    CircularProgressIndicator(
                                        color = ColorTokens.background(),
                                        modifier = Modifier.size(SpacingTokens.iconSizeSmall),
                                        strokeWidth = SpacingTokens.xxs
                                    )
                                } else {
                                    Text(
                                        if (emailVerified) "Email Verified" else "Verify OTP",
                                        style = TypographyTokens.buttonText
                                    )
                                }
                            }

                            if (otpError != null) {
                                Text(
                                    text = otpError ?: "",
                                    color = errorColor,
                                    style = TypographyTokens.labelSmall
                                )
                            }
                        }
                    }
                    // OTP WIRING END

                    AppTextField(
                        value = phone,
                        onValueChange = { phone = it; error = null },
                        label = stringResource(R.string.ui_signupscreen_10),
                        leadingIcon = Icons.Outlined.Phone,
                        textStyle = TypographyTokens.bodySmall,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        prefix = { Text(stringResource(R.string.ui_signupscreen_3), color = textSecondary, style = TypographyTokens.inputText) }
                    )

                    AppTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = stringResource(R.string.ui_signupscreen_11),
                        leadingIcon = Icons.Outlined.Lock,
                        textStyle = TypographyTokens.bodySmall,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        trailingIcon = {
                            AppIconButton(
                                onClick = { showPass = !showPass },
                                icon = if (showPass) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility
                            )
                        }
                    )

                    AnimatedVisibility(visible = password.isNotEmpty()) {
                        PasswordStrengthBar(
                            password = password,
                            errorColor = errorColor,
                            warningColor = warning,
                            successColor = success,
                            accentColor = accent
                        )
                    }

                    AppTextField(
                        value = confirm,
                        onValueChange = { confirm = it; error = null },
                        label = stringResource(R.string.ui_signupscreen_12),
                        leadingIcon = Icons.Outlined.LockOpen,
                        textStyle = TypographyTokens.bodySmall,
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val statusIcon = when {
                                    confirm.isEmpty() -> null
                                    confirm == password -> Icons.Filled.CheckCircle
                                    else -> Icons.Filled.Cancel
                                }
                                statusIcon?.let {
                                    val tint = if (confirm == password) success else errorColor
                                    Icon(it, null, tint = tint)
                                    Spacer(Modifier.width(SpacingTokens.xxs))
                                }
                                AppIconButton(
                                    onClick = { showConfirm = !showConfirm },
                                    icon = if (showConfirm) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility
                                )
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = error != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ShapeTokens.card)
                            .background(errorColor.copy(alpha = 0.08f))
                            .border(ShapeTokens.Border.thin, errorColor.copy(alpha = 0.2f), ShapeTokens.card)
                            .padding(SpacingTokens.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                            Icon(Icons.Filled.Cancel, null, tint = errorColor, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
                            Spacer(Modifier.width(SpacingTokens.xs))
                            Text(error?.let { localizedUiMessage(it) }.orEmpty(), color = errorColor, style = TypographyTokens.bodySmall)
                        }
                    }

                    val enabled = name.isNotBlank() &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        confirm.isNotBlank() &&
                        emailVerified &&
                        !loading

                    AppButton(
                        onClick = {
                            error = null
                            loading = true
                            viewModel.signup(
                                name = name,
                                email = email,
                                phone = phone,
                                password = password,
                                confirmPassword = confirm,
                                onSuccess = { loading = false; onSignupSuccess() },
                                onError = { loading = false; error = it }
                            )
                        },
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth().height(SpacingTokens.authButtonHeight)
                    ) {
                        if (loading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = ColorTokens.background(),
                                    modifier = Modifier.size(SpacingTokens.iconSizeSmall),
                                    strokeWidth = SpacingTokens.xxs
                                )
                                Spacer(Modifier.width(SpacingTokens.sm))
                                Text(stringResource(R.string.ui_signupscreen_4), style = TypographyTokens.buttonText)
                            }
                        } else {
                            Text(stringResource(R.string.ui_signupscreen_5), style = TypographyTokens.buttonText)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs)) {
                        TrustPoint(stringResource(R.string.signup_trust_1), accent)
                        TrustPoint(stringResource(R.string.signup_trust_2), accent)
                        TrustPoint(stringResource(R.string.signup_trust_3), accent)
                    }
                }
            }

            Spacer(Modifier.height(SpacingTokens.sm))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.ui_signupscreen_6), color = textSecondary, style = TypographyTokens.bodySmall)
                Text(
                    stringResource(R.string.ui_signupscreen_7),
                    color = accent,
                    style = TypographyTokens.bodySmall,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigateToLogin() }
                )
            }

            Spacer(Modifier.height(SpacingTokens.md))
        }
    }
}

@Composable
private fun PasswordStrengthBar(
    password: String,
    errorColor: androidx.compose.ui.graphics.Color,
    warningColor: androidx.compose.ui.graphics.Color,
    successColor: androidx.compose.ui.graphics.Color,
    accentColor: androidx.compose.ui.graphics.Color
) {
    val strength = when {
        password.length >= 12 && password.any { it.isUpperCase() } && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 10 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 3
        password.length >= 8 -> 2
        else -> 1
    }
    val label = listOf(
        "",
        stringResource(R.string.profile_password_weak),
        stringResource(R.string.profile_password_fair),
        stringResource(R.string.profile_password_strong),
        stringResource(R.string.profile_password_very_strong)
    )[strength]
    val color = listOf(
        ColorTokens.transparent(),
        errorColor,
        warningColor,
        successColor,
        accentColor
    )[strength]

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xxs)) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(SpacingTokens.xxs)
                        .clip(ShapeTokens.xs)
                        .background(if (i < strength) color else ColorTokens.border())
                )
            }
        }
        Spacer(Modifier.height(SpacingTokens.xxs))
        Text(label, color = color, style = TypographyTokens.labelSmall)
    }
}

@Composable
private fun TrustPoint(text: String, accent: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(SpacingTokens.iconSizeSmall)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, null, tint = accent, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
        }
        Spacer(Modifier.width(SpacingTokens.sm))
        Text(text, color = ColorTokens.textSecondary(), style = TypographyTokens.bodySmall)
    }
}
