package com.gosuraksha.app.ui.signup.components

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.ui.signup.model.SignupDarkBadgeBg
import com.gosuraksha.app.ui.signup.model.SignupDarkBadgeBorder
import com.gosuraksha.app.ui.signup.model.SignupDarkBg
import com.gosuraksha.app.ui.signup.model.SignupDarkBorder
import com.gosuraksha.app.ui.signup.model.SignupDarkIconTint
import com.gosuraksha.app.ui.signup.model.SignupDarkSurface
import com.gosuraksha.app.ui.signup.model.SignupDarkTextPri
import com.gosuraksha.app.ui.signup.model.SignupDarkTextSec
import com.gosuraksha.app.ui.signup.model.SignupDarkTextTert
import com.gosuraksha.app.ui.signup.model.SignupFormState
import com.gosuraksha.app.ui.signup.model.SignupGreen400
import com.gosuraksha.app.ui.signup.model.SignupLightBg
import com.gosuraksha.app.ui.signup.model.SignupLightBorder
import com.gosuraksha.app.ui.signup.model.SignupLightSurface
import com.gosuraksha.app.ui.signup.model.SignupLightTextPri
import com.gosuraksha.app.ui.signup.model.SignupLightTextSec
import com.gosuraksha.app.ui.signup.model.SignupLightTextTert

@Composable
fun SignupForm(
    isDark: Boolean,
    state: SignupFormState,
    isSendingOtp: Boolean,
    isVerifyingOtp: Boolean,
    otpSecondsLeft: Int,
    isOtpSent: Boolean,
    emailVerified: Boolean,
    otpError: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onPasswordToggle: () -> Unit,
    onConfirmToggle: () -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onSendOtp: (String?) -> Unit,
    onVerifyOtp: () -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val emailValid = remember(state.email) { state.email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(state.email).matches() }
    val emailError = remember(state.email) {
        when {
            state.email.isBlank() -> "Email is required"
            !emailValid -> "Invalid email"
            else -> null
        }
    }
    val uriHandler = LocalUriHandler.current
    val ctaEnabled = state.name.isNotBlank() && state.email.isNotBlank() && state.password.isNotBlank() && state.confirm.isNotBlank() && emailVerified && state.acceptedTerms && !state.isSubmitting

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(if (isDark) SignupDarkBg else SignupLightBg).padding(horizontal = 20.dp).padding(top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Create Account", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp, color = if (isDark) SignupDarkTextPri else SignupLightTextPri)
            Text("Start secure with verified identity and stronger account protection.", fontSize = 13.sp, color = if (isDark) SignupDarkTextSec else SignupLightTextSec)
        }
        SectionDivider("Identity", isDark)
        SignupTextField(state.name, onNameChange, "Full Name", "Your full name", Icons.Outlined.Person, isDark = isDark)
        SignupTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = "Email Address",
            placeholder = "Enter your email",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            trailingContent = if (emailVerified) ({ Icon(Icons.Filled.CheckCircle, null, tint = SignupGreen400, modifier = Modifier.size(18.dp)) }) else null,
            isDark = isDark
        )
        if (!emailVerified) {
            OutlinedButton(
                onClick = { onSendOtp(emailError) },
                enabled = emailValid && otpSecondsLeft == 0 && !isSendingOtp,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(1.5.dp, if (emailValid && otpSecondsLeft == 0) SignupGreen400.copy(alpha = 0.5f) else if (isDark) SignupDarkBorder else SignupLightBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isDark) SignupDarkSurface else SignupLightSurface, contentColor = SignupGreen400, disabledContentColor = if (isDark) SignupDarkTextTert else SignupLightTextTert)
            ) {
                if (isSendingOtp) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = SignupGreen400)
                    Spacer(Modifier.size(7.dp))
                    Text("Sending OTP...", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Text(if (otpSecondsLeft > 0) "Resend OTP (${otpSecondsLeft}s)" else "Send Verification OTP", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        OtpVerificationSection(isOtpSent || emailVerified, emailVerified, state.otp, isVerifyingOtp, otpError, isDark, onOtpChange, onVerifyOtp)
        SignupTextField(state.phone, onPhoneChange, "Phone Number", "Phone number", Icons.Outlined.Phone, KeyboardType.Phone, prefix = "+91", isDark = isDark)
        SectionDivider("Security", isDark)
        SignupPasswordField(state.password, onPasswordChange, "Password", "Create a password", state.showPassword, onPasswordToggle, isDark = isDark)
        AnimatedVisibility(visible = state.password.isNotEmpty(), enter = fadeIn(tween(250)) + expandVertically(tween(250)), exit = fadeOut(tween(150)) + shrinkVertically(tween(150))) {
            PasswordStrengthIndicator(state.password, isDark)
        }
        SignupPasswordField(
            value = state.confirm,
            onValueChange = onConfirmChange,
            label = "Confirm Password",
            placeholder = "Confirm your password",
            passwordVisible = state.showConfirm,
            onVisibilityToggle = onConfirmToggle,
            matchIcon = when {
                state.confirm.isEmpty() -> null
                state.confirm == state.password -> Icons.Filled.CheckCircle to SignupGreen400
                else -> Icons.Filled.Cancel to Color(0xFFEF4444)
            },
            isDark = isDark
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = state.acceptedTerms,
                onCheckedChange = onTermsChange,
                colors = CheckboxDefaults.colors(checkedColor = SignupGreen400, uncheckedColor = if (isDark) SignupDarkBorder else SignupLightBorder, checkmarkColor = if (isDark) Color(0xFF051209) else Color.White),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(8.dp))
            val normalColor = if (isDark) SignupDarkTextSec else SignupLightTextSec
            val annotated = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, color = normalColor)) { append("I agree to the ") }
                pushStringAnnotation("privacy", "https://gosuraksha.in/privacy")
                withStyle(SpanStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SignupGreen400, textDecoration = TextDecoration.Underline)) { append("Privacy Policy") }
                pop()
                withStyle(SpanStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, color = normalColor)) { append(" and ") }
                pushStringAnnotation("terms", "https://gosuraksha.in/terms")
                withStyle(SpanStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SignupGreen400, textDecoration = TextDecoration.Underline)) { append("Terms & Conditions") }
                pop()
            }
            ClickableText(
                text = annotated,
                style = TextStyle(fontSize = 11.sp, color = normalColor, fontWeight = FontWeight.Normal),
                onClick = { offset ->
                    annotated.getStringAnnotations("privacy", offset, offset).firstOrNull()?.let { uriHandler.openUri(it.item) }
                    annotated.getStringAnnotations("terms", offset, offset).firstOrNull()?.let { uriHandler.openUri(it.item) }
                },
                modifier = Modifier.weight(1f)
            )
        }
        SignupStates(state.errorMessage)
        Button(
            onClick = onSubmit,
            enabled = ctaEnabled,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SignupGreen400, disabledContainerColor = if (isDark) Color(0xFF1E5C35) else Color(0xFFB8D8C4)),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(Modifier.size(8.dp))
                Text("Creating Account...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Create Account", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF051209) else Color.White)
                Spacer(Modifier.size(8.dp))
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(if (isDark) Color(0xFF0A2010) else Color.White.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                    Text("→", fontSize = 11.sp, color = if (isDark) SignupGreen400 else Color.White)
                }
            }
        }
        TrustPoints(isDark)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?", fontSize = 11.sp, color = if (isDark) SignupDarkTextSec else SignupLightTextSec)
            Spacer(Modifier.size(4.dp))
            Text("Log In", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SignupGreen400, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onNavigateToLogin))
        }
    }
}

@Composable
internal fun SectionDivider(label: String, isDark: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = if (isDark) SignupDarkBorder else SignupLightBorder)
        Text(label.uppercase(), fontSize = 8.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, color = if (isDark) SignupDarkTextSec else SignupLightTextSec)
        HorizontalDivider(modifier = Modifier.weight(1f), color = if (isDark) SignupDarkBorder else SignupLightBorder)
    }
}

@Composable
internal fun SignupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    isDark: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.9.sp, color = if (isDark) SignupDarkTextSec else SignupLightTextSec)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 12.sp, color = if (isDark) SignupDarkTextTert else SignupLightTextTert) },
            leadingIcon = { Icon(leadingIcon, null, tint = if (value.isNotEmpty()) SignupGreen400 else if (isDark) SignupDarkIconTint else SignupLightTextTert, modifier = Modifier.size(17.dp)) },
            trailingIcon = trailingContent,
            prefix = prefix?.let { { Text("$it  ", fontSize = 12.sp, color = if (isDark) SignupDarkTextSec else SignupLightTextSec, fontWeight = FontWeight.Medium) } },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (isDark) SignupDarkTextPri else SignupLightTextPri),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SignupGreen400, unfocusedBorderColor = if (isDark) SignupDarkBorder else SignupLightBorder, focusedContainerColor = if (isDark) SignupDarkSurface else SignupLightSurface, unfocusedContainerColor = if (isDark) SignupDarkSurface else SignupLightSurface, cursorColor = SignupGreen400, focusedTextColor = if (isDark) SignupDarkTextPri else SignupLightTextPri, unfocusedTextColor = if (isDark) SignupDarkTextPri else SignupLightTextPri),
            shape = RoundedCornerShape(11.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}

@Composable
private fun SignupPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    passwordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    matchIcon: Pair<ImageVector, Color>? = null,
    isDark: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.9.sp, color = if (isDark) SignupDarkTextSec else SignupLightTextSec)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 12.sp, color = if (isDark) SignupDarkTextTert else SignupLightTextTert) },
            leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = if (value.isNotEmpty()) SignupGreen400 else if (isDark) SignupDarkIconTint else SignupLightTextTert, modifier = Modifier.size(17.dp)) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
                    matchIcon?.let { (icon, tint) ->
                        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(2.dp))
                    }
                    IconButton(onClick = onVisibilityToggle, modifier = Modifier.size(36.dp)) {
                        Icon(if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, tint = if (isDark) SignupDarkIconTint else SignupLightTextTert, modifier = Modifier.size(17.dp))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (isDark) SignupDarkTextPri else SignupLightTextPri),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SignupGreen400, unfocusedBorderColor = if (isDark) SignupDarkBorder else SignupLightBorder, focusedContainerColor = if (isDark) SignupDarkSurface else SignupLightSurface, unfocusedContainerColor = if (isDark) SignupDarkSurface else SignupLightSurface, cursorColor = SignupGreen400, focusedTextColor = if (isDark) SignupDarkTextPri else SignupLightTextPri, unfocusedTextColor = if (isDark) SignupDarkTextPri else SignupLightTextPri),
            shape = RoundedCornerShape(11.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
    }
}

@Composable
private fun PasswordStrengthIndicator(password: String, isDark: Boolean) {
    val strength = when {
        password.length >= 12 && password.any { it.isUpperCase() } && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 10 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 3
        password.length >= 8 -> 2
        else -> 1
    }
    val (label, barColor) = when (strength) {
        1 -> "Weak" to Color(0xFFEF4444)
        2 -> "Fair" to Color(0xFFF59E0B)
        3 -> "Strong" to SignupGreen400
        else -> "Very strong" to SignupGreen400
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { i ->
                Box(modifier = Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (i < strength) barColor else if (isDark) SignupDarkBorder else SignupLightBorder))
            }
        }
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = barColor)
    }
}

@Composable
private fun TrustPoints(isDark: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (isDark) SignupDarkBadgeBg else SignupLightSurface).border(1.dp, if (isDark) SignupDarkBadgeBorder else SignupLightBorder, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf("Verified email onboarding", "Stronger account protection", "Safer access across devices").forEach { text ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(SignupGreen400.copy(alpha = if (isDark) 0.12f else 0.10f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, null, tint = SignupGreen400, modifier = Modifier.size(10.dp))
                }
                Text(text, fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = if (isDark) SignupDarkTextSec else SignupLightTextSec)
            }
        }
    }
}
