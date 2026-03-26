package com.gosuraksha.app.ui.auth

// =============================================================================
// SignupScreen.kt — Hero Split redesign
//
// Layout:
//   • Green hero — "Create Your Secure Account"
//   • Scrollable white/dark card:
//       - Full Name field
//       - Email + Send OTP + OTP input + Verify (animated)
//       - Phone field (+91)
//       - Password + strength bar
//       - Confirm password
//       - Terms checkbox
//       - Create Account CTA
//       - Trust points
//       - "Already have account? Login" footer
//
// All ViewModel wiring preserved:
//   - isSendingOtp, isVerifyingOtp, otpSecondsLeft, isOtpSent, emailVerified, otpError
//   - sendOtp(email, emailError), verifyOtp(email, otp)
//   - signup(name, email, phone, password, confirmPassword, onSuccess, onError)
// =============================================================================

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.presentation.auth.AuthViewModel
import com.gosuraksha.app.ui.components.localizedUiMessage

@Composable
fun SignupScreen(
    viewModel:         AuthViewModel,
    onSignupSuccess:   () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isDark     = ColorTokens.LocalAppDarkMode.current
    val uriHandler = LocalUriHandler.current

    // ── ViewModel state ───────────────────────────────────────────────────
    val isSendingOtp   by viewModel.isSendingOtp.collectAsStateWithLifecycle()
    val isVerifyingOtp by viewModel.isVerifyingOtp.collectAsStateWithLifecycle()
    val otpSecondsLeft by viewModel.otpSecondsLeft.collectAsStateWithLifecycle()
    val isOtpSent      by viewModel.isOtpSent.collectAsStateWithLifecycle()
    val emailVerified  by viewModel.emailVerified.collectAsStateWithLifecycle()
    val otpError       by viewModel.otpError.collectAsStateWithLifecycle()

    // ── Local form state ──────────────────────────────────────────────────
    var name          by remember { mutableStateOf("") }
    var email         by remember { mutableStateOf("") }
    var phone         by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    var confirm       by remember { mutableStateOf("") }
    var otp           by remember { mutableStateOf("") }
    var showPass      by remember { mutableStateOf(false) }
    var showConfirm   by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var formError     by remember { mutableStateOf<String?>(null) }
    var loading       by remember { mutableStateOf(false) }

    val emailValid = remember(email) {
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val emailFieldError = remember(email) {
        when {
            email.isBlank()  -> "Email is required"
            !emailValid      -> "Invalid email"
            else             -> null
        }
    }

    val ctaEnabled = name.isNotBlank()
            && email.isNotBlank()
            && password.isNotBlank()
            && confirm.isNotBlank()
            && emailVerified
            && acceptedTerms
            && !loading

    // ── Screen ────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthColors.bg(isDark))
    ) {
        // ── Hero ──────────────────────────────────────────────────────────
        AuthHero(
            isDark     = isDark,
            title      = "Create Your\nSecure Account",
            subtitle   = "Join 50,000+ Indians staying safe online",
            height     = 175.dp,
            showShield = true
        )

        // ── Scrollable card ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AuthCardSurface(isDark = isDark) {

                // ── IDENTITY SECTION ──────────────────────────────────────
                AuthSectionLabel("Identity", isDark)

                // Full name
                AuthTextField(
                    value         = name,
                    onValueChange = { name = it; formError = null },
                    label         = "Full Name",
                    placeholder   = "Your full name",
                    leadingIcon   = Icons.Outlined.Person,
                    isDark        = isDark
                )

                // Email
                AuthTextField(
                    value         = email,
                    onValueChange = {
                        email = it
                        formError = null
                        otp   = ""
                        viewModel.resetOtpState()
                    },
                    label        = "Email Address",
                    placeholder  = stringResource(R.string.ui_signupscreen_9),
                    leadingIcon  = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email,
                    trailingIcon = if (emailVerified) {
                        { Icon(Icons.Filled.CheckCircle, null, tint = AuthColors.Accent, modifier = Modifier.size(18.dp)) }
                    } else null,
                    isDark = isDark
                )

                // Send / Resend OTP button
                if (!emailVerified) {
                    OutlinedButton(
                        onClick  = { viewModel.sendOtp(email = email, emailError = emailFieldError) },
                        enabled  = emailValid && otpSecondsLeft == 0 && !isSendingOtp,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape    = RoundedCornerShape(12.dp),
                        border   = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (emailValid && otpSecondsLeft == 0) AuthColors.Accent.copy(alpha = 0.5f)
                            else AuthColors.border(isDark)
                        ),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            containerColor       = AuthColors.field(isDark),
                            contentColor         = AuthColors.Accent,
                            disabledContentColor = AuthColors.textTert(isDark)
                        )
                    ) {
                        if (isSendingOtp) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color       = AuthColors.Accent
                            )
                            Spacer(Modifier.width(7.dp))
                            Text("Sending OTP...", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(
                                text       = if (otpSecondsLeft > 0) "Resend OTP (${otpSecondsLeft}s)"
                                else "Send Verification OTP",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // OTP input + verify (animated)
                AnimatedVisibility(
                    visible = (isOtpSent || emailVerified),
                    enter   = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit    = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!emailVerified) {
                            OtpInputRow(
                                otp         = otp,
                                onOtpChange = { otp = it },
                                isDark      = isDark
                            )
                            Button(
                                onClick  = { viewModel.verifyOtp(email = email, otp = otp) },
                                enabled  = otp.length == 6 && !isVerifyingOtp,
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor         = AuthColors.Accent,
                                    disabledContainerColor = AuthColors.accentDisabled(isDark)
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                if (isVerifyingOtp) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color       = Color.White
                                    )
                                    Spacer(Modifier.width(7.dp))
                                    Text("Verifying...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                } else {
                                    Text("Verify OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        } else {
                            AuthSuccessRow("Email verified successfully ✓", isDark)
                        }

                        otpError?.let { AuthErrorRow(it, isDark) }
                    }
                }

                // Phone
                AuthPhoneField(
                    value         = phone,
                    onValueChange = { phone = it; formError = null },
                    isDark        = isDark
                )

                // ── SECURITY SECTION ──────────────────────────────────────
                AuthSectionLabel("Security", isDark)

                // Password
                AuthPasswordField(
                    value              = password,
                    onValueChange      = { password = it; formError = null },
                    label              = stringResource(R.string.ui_signupscreen_11),
                    placeholder        = "Create a strong password",
                    passwordVisible    = showPass,
                    onVisibilityToggle = { showPass = !showPass },
                    isDark             = isDark
                )

                // Strength bar
                PasswordStrengthBar(password = password, isDark = isDark)

                // Confirm password
                AuthPasswordField(
                    value              = confirm,
                    onValueChange      = { confirm = it; formError = null },
                    label              = stringResource(R.string.ui_signupscreen_12),
                    placeholder        = "Re-enter your password",
                    passwordVisible    = showConfirm,
                    onVisibilityToggle = { showConfirm = !showConfirm },
                    trailingExtra      = when {
                        confirm.isEmpty()   -> null
                        confirm == password -> { { Icon(Icons.Filled.CheckCircle, null, tint = AuthColors.Accent, modifier = Modifier.size(16.dp)) } }
                        else                -> { { Icon(Icons.Filled.Cancel, null, tint = AuthColors.ErrorRed, modifier = Modifier.size(16.dp)) } }
                    },
                    isDark = isDark
                )

                // ── TERMS ──────────────────────────────────────────────────
                TermsRow(
                    accepted    = acceptedTerms,
                    onAccept    = { acceptedTerms = it; formError = null },
                    isDark      = isDark,
                    uriHandler  = uriHandler
                )

                // ── Form error ────────────────────────────────────────────
                formError?.let { AuthErrorRow(localizedUiMessage(it), isDark) }

                // ── CTA ───────────────────────────────────────────────────
                AuthPrimaryButton(
                    text      = stringResource(R.string.ui_signupscreen_5),
                    onClick   = {
                        formError = null
                        if (!acceptedTerms) {
                            formError = "Please accept Privacy Policy and Terms to continue."
                            return@AuthPrimaryButton
                        }
                        loading = true
                        viewModel.signup(
                            name            = name,
                            email           = email,
                            phone           = phone.ifBlank { "" },
                            password        = password,
                            confirmPassword = confirm,
                            onSuccess       = { loading = false; onSignupSuccess() },
                            onError         = { loading = false; formError = it }
                        )
                    },
                    enabled   = ctaEnabled,
                    isLoading = loading,
                    isDark    = isDark
                )

                // ── Trust points ──────────────────────────────────────────
                TrustPointsCard(isDark = isDark)

                // ── Login link ────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.ui_signupscreen_6),
                        fontSize = 12.sp,
                        color    = AuthColors.textSec(isDark)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text      = stringResource(R.string.ui_signupscreen_7),
                        fontSize  = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color     = AuthColors.Accent,
                        modifier  = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onNavigateToLogin
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section label — "── IDENTITY ──"
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AuthSectionLabel(label: String, isDark: Boolean) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(Modifier.weight(1f), color = AuthColors.border(isDark))
        Text(
            text          = label.uppercase(),
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color         = AuthColors.textTert(isDark)
        )
        HorizontalDivider(Modifier.weight(1f), color = AuthColors.border(isDark))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Terms row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TermsRow(
    accepted:   Boolean,
    onAccept:   (Boolean) -> Unit,
    isDark:     Boolean,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked         = accepted,
            onCheckedChange = onAccept,
            colors          = CheckboxDefaults.colors(
                checkedColor   = AuthColors.Accent,
                uncheckedColor = AuthColors.border(isDark),
                checkmarkColor = Color.White
            ),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))

        val normalColor = AuthColors.textSec(isDark)
        val annotated = buildAnnotatedString {
            withStyle(SpanStyle(fontSize = 11.sp, color = normalColor)) { append("I agree to the ") }
            pushStringAnnotation("privacy", "https://gosuraksha.in/privacy")
            withStyle(SpanStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AuthColors.Accent, textDecoration = TextDecoration.Underline)) {
                append("Privacy Policy")
            }
            pop()
            withStyle(SpanStyle(fontSize = 11.sp, color = normalColor)) { append(" and ") }
            pushStringAnnotation("terms", "https://gosuraksha.in/terms")
            withStyle(SpanStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AuthColors.Accent, textDecoration = TextDecoration.Underline)) {
                append("Terms & Conditions")
            }
            pop()
        }

        ClickableText(
            text     = annotated,
            modifier = Modifier.weight(1f),
            onClick  = { offset ->
                annotated.getStringAnnotations("privacy", offset, offset).firstOrNull()?.let { uriHandler.openUri(it.item) }
                annotated.getStringAnnotations("terms", offset, offset).firstOrNull()?.let { uriHandler.openUri(it.item) }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Trust points card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TrustPointsCard(isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDark) AuthColors.SuccessBgDark
                else AuthColors.Accent.copy(alpha = 0.05f)
            )
            .border(
                1.dp,
                AuthColors.Accent.copy(alpha = if (isDark) 0.15f else 0.18f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            stringResource(R.string.signup_trust_1),
            stringResource(R.string.signup_trust_2),
            stringResource(R.string.signup_trust_3)
        ).forEach { text ->
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(AuthColors.Accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check, null,
                        tint     = AuthColors.Accent,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Text(
                    text       = text,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color      = AuthColors.textSec(isDark)
                )
            }
        }
    }
}