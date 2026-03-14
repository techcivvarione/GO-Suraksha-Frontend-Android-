package com.gosuraksha.app.ui.auth

import android.util.Patterns
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ─────────────────────────────────────────────────────────────────────────────
// Design tokens — identical to LoginScreen.kt
// ─────────────────────────────────────────────────────────────────────────────

private val Green400         = Color(0xFF2EC472)
private val Green600         = Color(0xFF17753D)
private val HeroBg           = Color(0xFF0D1F14)

// Light mode
private val LightBg          = Color(0xFFF0F6F2)
private val LightSurface     = Color(0xFFFFFFFF)
private val LightBorder      = Color(0xFFCCE3D4)
private val LightTextPri     = Color(0xFF0D1F14)
private val LightTextSec     = Color(0xFF000000)
private val LightTextTert    = Color(0xFF000000)

// Dark mode
private val DarkBg           = Color(0xFF000000)
private val DarkSurface      = Color(0xFF040705)
private val DarkSurfaceAlt   = Color(0xFF1A3322)
private val DarkBorder       = Color(0xFF254D30)
private val DarkTextPri      = Color(0xFFD4EDD9)
private val DarkTextSec      = Color(0xFFFFFFFF)
private val DarkTextTert     = Color(0xFFF2FAF3)
private val DarkIconTint     = Color(0xFF3A7A50)
private val DarkBadgeBg      = Color(0xFF132318)
private val DarkBadgeBorder  = Color(0xFF1E3D28)

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isDark = ColorTokens.LocalAppDarkMode.current

    // Form state
    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var phone           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirm         by remember { mutableStateOf("") }
    var showPass        by remember { mutableStateOf(false) }
    var showConfirm     by remember { mutableStateOf(false) }
    var acceptedTerms   by remember { mutableStateOf(false) }
    var otp             by remember { mutableStateOf("") }
    var error           by remember { mutableStateOf<String?>(null) }
    var loading         by remember { mutableStateOf(false) }

    // OTP flow from ViewModel
    val isSendingOtp    by viewModel.isSendingOtp.collectAsStateWithLifecycle()
    val isVerifyingOtp  by viewModel.isVerifyingOtp.collectAsStateWithLifecycle()
    val otpSecondsLeft  by viewModel.otpSecondsLeft.collectAsStateWithLifecycle()
    val isOtpSent       by viewModel.isOtpSent.collectAsStateWithLifecycle()
    val emailVerified   by viewModel.emailVerified.collectAsStateWithLifecycle()
    val otpError        by viewModel.otpError.collectAsStateWithLifecycle()

    val emailValid = remember(email) {
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val emailError = remember(email) {
        when {
            email.isBlank()  -> "Email is required"
            !emailValid      -> "Invalid email"
            else             -> null
        }
    }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
    ) {
        // Fixed hero at top
        SignupHero(isDark = isDark)

        // Scrollable form body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(if (isDark) DarkBg else LightBg)
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Heading ───────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text          = stringResource(R.string.ui_signupscreen_1),
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = (-0.4).sp,
                    color         = if (isDark) DarkTextPri else LightTextPri
                )
                Text(
                    text       = stringResource(R.string.ui_signupscreen_2),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color      = if (isDark) DarkTextSec else LightTextSec
                )
            }

            SectionDivider(label = "Identity", isDark = isDark)

            // ── Full name ─────────────────────────────────────────────────────
            SignupTextField(
                value         = name,
                onValueChange = { name = it; error = null },
                label         = "Full Name",
                placeholder   = "Your full name",
                leadingIcon   = Icons.Outlined.Person,
                isDark        = isDark
            )

            // ── Email + OTP ───────────────────────────────────────────────────
            SignupTextField(
                value         = email,
                onValueChange = {
                    email = it
                    error = null
                    otp   = ""
                    viewModel.resetOtpState()
                },
                label        = "Email Address",
                placeholder  = stringResource(R.string.ui_signupscreen_9),
                leadingIcon  = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                trailingContent = if (emailVerified) {
                    {
                        Icon(
                            Icons.Filled.CheckCircle, null,
                            tint     = Green400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null,
                isDark = isDark
            )

            // Send OTP / Resend button
            if (!emailVerified) {
                OutlinedButton(
                    onClick  = { viewModel.sendOtp(email = email, emailError = emailError) },
                    enabled  = emailValid && otpSecondsLeft == 0 && !isSendingOtp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape  = RoundedCornerShape(11.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (emailValid && otpSecondsLeft == 0) Green400.copy(alpha = 0.5f)
                        else if (isDark) DarkBorder else LightBorder
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor       = if (isDark) DarkSurface else LightSurface,
                        contentColor         = Green400,
                        disabledContentColor = if (isDark) DarkTextTert else LightTextTert
                    )
                ) {
                    if (isSendingOtp) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color       = Green400
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

            // OTP input — shown after OTP sent
            AnimatedVisibility(
                visible = (isOtpSent || emailVerified),
                enter   = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit    = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!emailVerified) {
                        SignupTextField(
                            value         = otp,
                            onValueChange = { if (it.length <= 6) otp = it },
                            label         = "Email OTP",
                            placeholder   = "6-digit code",
                            leadingIcon   = Icons.Outlined.Lock,
                            keyboardType  = KeyboardType.Number,
                            isDark        = isDark
                        )

                        Button(
                            onClick  = { viewModel.verifyOtp(email = email, otp = otp) },
                            enabled  = otp.length == 6 && !isVerifyingOtp && !emailVerified,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape  = RoundedCornerShape(11.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor         = Green400,
                                disabledContainerColor = if (isDark) Color(0xFF1E5C35) else Color(0xFFB0D8C0)
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
                                Text("Verifying...", fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold, color = Color.White)
                            } else {
                                Text("Verify OTP", fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        // Verified badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Green400.copy(alpha = if (isDark) 0.10f else 0.08f))
                                .border(1.dp, Green400.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, null,
                                tint = Green400, modifier = Modifier.size(15.dp))
                            Text("Email verified successfully",
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = Green400)
                        }
                    }

                    // OTP error
                    if (otpError != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.10f))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.Error, null,
                                tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                            Text(otpError ?: "", fontSize = 11.sp, color = Color(0xFFEF4444))
                        }
                    }
                }
            }

            // ── Phone ──────────────────────────────────────────────────────────
            SignupTextField(
                value         = phone,
                onValueChange = { phone = it; error = null },
                label         = stringResource(R.string.ui_signupscreen_10),
                placeholder   = "Phone number",
                leadingIcon   = Icons.Outlined.Phone,
                keyboardType  = KeyboardType.Phone,
                prefix        = "+91",
                isDark        = isDark
            )

            SectionDivider(label = "Security", isDark = isDark)

            // ── Password ──────────────────────────────────────────────────────
            SignupPasswordField(
                value              = password,
                onValueChange      = { password = it; error = null },
                label              = stringResource(R.string.ui_signupscreen_11),
                placeholder        = "Create a password",
                passwordVisible    = showPass,
                onVisibilityToggle = { showPass = !showPass },
                isDark             = isDark
            )

            // Strength bar — shown when password non-empty
            AnimatedVisibility(
                visible = password.isNotEmpty(),
                enter   = fadeIn(tween(250)) + expandVertically(tween(250)),
                exit    = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                PasswordStrengthIndicator(password = password, isDark = isDark)
            }

            // ── Confirm password ──────────────────────────────────────────────
            SignupPasswordField(
                value              = confirm,
                onValueChange      = { confirm = it; error = null },
                label              = stringResource(R.string.ui_signupscreen_12),
                placeholder        = "Confirm your password",
                passwordVisible    = showConfirm,
                onVisibilityToggle = { showConfirm = !showConfirm },
                matchIcon = when {
                    confirm.isEmpty()    -> null
                    confirm == password  -> Icons.Filled.CheckCircle to Green400
                    else                 -> Icons.Filled.Cancel to Color(0xFFEF4444)
                },
                isDark = isDark
            )


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it; error = null },
                    colors = CheckboxDefaults.colors(
                        checkedColor   = Green400,
                        uncheckedColor = if (isDark) DarkBorder else LightBorder,
                        checkmarkColor = if (isDark) Color(0xFF051209) else Color.White
                    ),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                androidx.compose.ui.text.buildAnnotatedString { }.let {
                    // Use inline clickable text via ClickableText for proper wrapping
                    val normalColor = if (isDark) DarkTextSec else LightTextSec
                    val linkColor   = Green400
                    val annotated   = androidx.compose.ui.text.buildAnnotatedString {
                        withStyle(androidx.compose.ui.text.SpanStyle(
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color      = normalColor
                        )) { append("I agree to the ") }
                        pushStringAnnotation("privacy", "https://gosuraksha.in/privacy")
                        withStyle(androidx.compose.ui.text.SpanStyle(
                            fontSize       = 11.sp,
                            fontWeight     = FontWeight.SemiBold,
                            color          = linkColor,
                            textDecoration = TextDecoration.Underline
                        )) { append("Privacy Policy") }
                        pop()
                        withStyle(androidx.compose.ui.text.SpanStyle(
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color      = normalColor
                        )) { append(" and ") }
                        pushStringAnnotation("terms", "https://gosuraksha.in/terms")
                        withStyle(androidx.compose.ui.text.SpanStyle(
                            fontSize       = 11.sp,
                            fontWeight     = FontWeight.SemiBold,
                            color          = linkColor,
                            textDecoration = TextDecoration.Underline
                        )) { append("Terms & Conditions") }
                        pop()
                    }
                    androidx.compose.foundation.text.ClickableText(
                        text  = annotated,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize   = 11.sp,
                            color      = normalColor,
                            fontWeight = FontWeight.Normal
                        ),
                        onClick = { offset ->
                            annotated.getStringAnnotations("privacy", offset, offset)
                                .firstOrNull()?.let { uriHandler.openUri(it.item) }
                            annotated.getStringAnnotations("terms", offset, offset)
                                .firstOrNull()?.let { uriHandler.openUri(it.item) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Form error ────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = error != null,
                enter   = fadeIn(tween(250)) + expandVertically(tween(250)),
                exit    = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.10f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Cancel, null,
                        tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    Text(
                        text     = error?.let { localizedUiMessage(it) }.orEmpty(),
                        fontSize = 11.sp,
                        color    = Color(0xFFEF4444)
                    )
                }
            }

            // ── Create account CTA ────────────────────────────────────────────
            val ctaEnabled = name.isNotBlank()
                    && email.isNotBlank()
                    && password.isNotBlank()
                    && confirm.isNotBlank()
                    && emailVerified
                    && acceptedTerms
                    && !loading

            Button(
                onClick = {
                    error   = null
                    if (!acceptedTerms) {
                        error = "Please accept Privacy Policy and Terms to continue."
                        return@Button
                    }
                    loading = true
                    viewModel.signup(
                        name            = name,
                        email           = email,
                        phone           = phone,
                        password        = password,
                        confirmPassword = confirm,
                        onSuccess       = { loading = false; onSignupSuccess() },
                        onError         = { loading = false; error = it }
                    )
                },
                enabled   = ctaEnabled,
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape     = RoundedCornerShape(13.dp),
                colors    = ButtonDefaults.buttonColors(
                    containerColor         = Green400,
                    disabledContainerColor = if (isDark) Color(0xFF1E5C35) else Color(0xFFB8D8C4)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.ui_signupscreen_4),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Text(
                        text       = stringResource(R.string.ui_signupscreen_5),
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isDark) Color(0xFF051209) else Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier         = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color(0xFF0A2010) else Color.White.copy(alpha = 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("→", fontSize = 11.sp,
                            color = if (isDark) Green400 else Color.White)
                    }
                }
            }

            // ── Trust points ──────────────────────────────────────────────────
            TrustPoints(isDark = isDark)

            // ── Login link ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.ui_signupscreen_6),
                    fontSize = 11.sp,
                    color    = if (isDark) DarkTextSec else LightTextSec)
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = stringResource(R.string.ui_signupscreen_7),
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Green400,
                    modifier   = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onNavigateToLogin
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero — mirrors LoginScreen hero exactly
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SignupHero(isDark: Boolean) {
    val fadeTo = if (isDark) DarkBg else LightBg
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(HeroBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSignupPattern()
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp)
        ) {
            Text(
                text          = "Go Suraksha · Security",
                fontSize      = 9.sp,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.4.sp,
                color         = Green400.copy(alpha = 0.55f)
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text          = "Create Your\nSecure Account",
                fontSize      = 20.sp,
                fontWeight    = FontWeight.ExtraBold,
                lineHeight    = 25.sp,
                letterSpacing = (-0.4).sp,
                color         = Color.White
            )
        }
    }
}

// Reuses the same dot-grid + circuit pattern as LoginScreen
private fun DrawScope.drawSignupPattern() {
    val w = size.width; val h = size.height
    val sp = 22.dp.toPx()
    val dotColor  = Color(0xFF2A6640)
    val traceColor = Color(0xFF2A6640)
    val nodeColor  = Color(0xFF3A7A50)
    val iconColor  = Color(0xFF3A7A50)
    val traceW     = 0.7.dp.toPx()

    // Dot grid
    var gy = sp / 2f
    while (gy < h) {
        var gx = sp / 2f
        while (gx < w) {
            drawCircle(dotColor, radius = 1.3.dp.toPx(), center = Offset(gx, gy))
            gx += sp
        }
        gy += sp
    }

    // Circuit traces
    fun seg(x1: Float, y1: Float, x2: Float, y2: Float) =
        drawLine(traceColor, Offset(x1, y1), Offset(x2, y2), traceW)

    val g = sp
    seg(2*g, .5f*g, 2*g, 2.5f*g);   seg(2*g, 2.5f*g, 4.5f*g, 2.5f*g)
    seg(4.5f*g, 2.5f*g, 6.5f*g, 2.5f*g); seg(6.5f*g, 2.5f*g, 6.5f*g, 1.5f*g)
    seg(9.5f*g, .5f*g, 9.5f*g, 1.5f*g); seg(9.5f*g, 1.5f*g, 11.5f*g, 1.5f*g); seg(11.5f*g, 1.5f*g, 11.5f*g, .5f*g)
    seg(.5f*g, 3.5f*g, 2.5f*g, 3.5f*g); seg(2.5f*g, 3.5f*g, 2.5f*g, 4.5f*g); seg(2.5f*g, 4.5f*g, 4.5f*g, 4.5f*g); seg(4.5f*g, 4.5f*g, 4.5f*g, 3.5f*g)
    seg(9.5f*g, 3.5f*g, 11.5f*g, 3.5f*g); seg(11.5f*g, 3.5f*g, 11.5f*g, 4.5f*g)
    seg(.5f*g, 5.5f*g, 3.5f*g, 5.5f*g); seg(3.5f*g, 5.5f*g, 3.5f*g, 6.5f*g)
    seg(6.5f*g, 4.5f*g, 6.5f*g, 5.5f*g); seg(6.5f*g, 5.5f*g, 8.5f*g, 5.5f*g); seg(8.5f*g, 5.5f*g, 8.5f*g, 4.5f*g)

    // Nodes
    fun node(x: Float, y: Float, r: Float = 2.2.dp.toPx()) =
        drawCircle(nodeColor, radius = r, center = Offset(x, y))
    node(2*g, 2.5f*g); node(4.5f*g, 2.5f*g, 1.8.dp.toPx()); node(11.5f*g, 1.5f*g)
    node(2.5f*g, 4.5f*g, 1.8.dp.toPx()); node(4.5f*g, 4.5f*g); node(6.5f*g, 4.5f*g, 1.8.dp.toPx())

    // Shield icon
    val iconStroke = Stroke(1.1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val cx = w * 0.52f; val cy = h * 0.40f
    val sw = 24.dp.toPx(); val sh = 27.dp.toPx()
    drawPath(Path().apply {
        moveTo(cx, cy - sh / 2f)
        lineTo(cx + sw / 2f, cy - sh / 2f + sh * 0.15f)
        lineTo(cx + sw / 2f, cy - sh / 2f + sh * 0.65f)
        quadraticBezierTo(cx + sw / 2f, cy + sh / 2f, cx, cy + sh / 2f)
        quadraticBezierTo(cx - sw / 2f, cy + sh / 2f, cx - sw / 2f, cy - sh / 2f + sh * 0.65f)
        lineTo(cx - sw / 2f, cy - sh / 2f + sh * 0.15f)
        close()
    }, iconColor, style = iconStroke)
    val ck = 4.dp.toPx()
    drawPath(Path().apply {
        moveTo(cx - ck * 1.5f, cy + 1.dp.toPx())
        lineTo(cx - ck * 0.3f, cy + ck)
        lineTo(cx + ck * 1.5f, cy - ck * 0.8f)
    }, iconColor, style = iconStroke)

    // Padlock
    val lx = w - 44.dp.toPx(); val ly = h * 0.28f
    val lw = 16.dp.toPx();     val lh = 12.dp.toPx()
    val lTop = ly + 8.dp.toPx()
    drawArc(iconColor, 180f, 180f, false,
        Offset(lx - lw / 2f + 3.dp.toPx(), ly - 5.dp.toPx()),
        Size(lw - 6.dp.toPx(), 10.dp.toPx()),
        style = Stroke(1.dp.toPx(), cap = StrokeCap.Round))
    drawRoundRect(iconColor, Offset(lx - lw / 2f, lTop), Size(lw, lh),
        CornerRadius(2.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawCircle(iconColor, 1.6.dp.toPx(), Offset(lx, lTop + lh / 2f))
}

// ─────────────────────────────────────────────────────────────────────────────
// Section divider with label
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionDivider(label: String, isDark: Boolean) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color    = if (isDark) DarkBorder else LightBorder
        )
        Text(
            text          = label.uppercase(),
            fontSize      = 8.5.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color         = if (isDark) DarkTextSec else LightTextSec
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color    = if (isDark) DarkBorder else LightBorder
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Text field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SignupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    isDark: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text          = label.uppercase(),
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            color         = if (isDark) DarkTextSec else LightTextSec
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(placeholder, fontSize = 12.sp,
                    color = if (isDark) DarkTextTert else LightTextTert)
            },
            leadingIcon   = {
                Icon(
                    leadingIcon, null,
                    tint     = if (value.isNotEmpty()) Green400
                    else if (isDark) DarkIconTint else LightTextTert,
                    modifier = Modifier.size(17.dp)
                )
            },
            trailingIcon  = trailingContent,
            prefix = prefix?.let { {
                Text("$it  ", fontSize = 12.sp,
                    color = if (isDark) DarkTextSec else LightTextSec,
                    fontWeight = FontWeight.Medium)
            } },
            modifier      = Modifier
                .fillMaxWidth()
                .height(50.dp),
            textStyle     = LocalTextStyle.current.copy(
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = if (isDark) DarkTextPri else LightTextPri
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Green400,
                unfocusedBorderColor    = if (isDark) DarkBorder else LightBorder,
                focusedContainerColor   = if (isDark) DarkSurface else LightSurface,
                unfocusedContainerColor = if (isDark) DarkSurface else LightSurface,
                cursorColor             = Green400,
                focusedTextColor        = if (isDark) DarkTextPri else LightTextPri,
                unfocusedTextColor      = if (isDark) DarkTextPri else LightTextPri,
            ),
            shape           = RoundedCornerShape(11.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType
            ),
            singleLine = true
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Password field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SignupPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    passwordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    matchIcon: Pair<ImageVector, Color>? = null,
    isDark: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text          = label.uppercase(),
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            color         = if (isDark) DarkTextSec else LightTextSec
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(placeholder, fontSize = 12.sp,
                    color = if (isDark) DarkTextTert else LightTextTert)
            },
            leadingIcon   = {
                Icon(
                    Icons.Outlined.Lock, null,
                    tint     = if (value.isNotEmpty()) Green400
                    else if (isDark) DarkIconTint else LightTextTert,
                    modifier = Modifier.size(17.dp)
                )
            },
            trailingIcon  = {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.padding(end = 4.dp)
                ) {
                    matchIcon?.let { (icon, tint) ->
                        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(2.dp))
                    }
                    IconButton(
                        onClick  = onVisibilityToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (passwordVisible) Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff,
                            null,
                            tint     = if (isDark) DarkIconTint else LightTextTert,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            },
            modifier      = Modifier
                .fillMaxWidth()
                .height(50.dp),
            textStyle     = LocalTextStyle.current.copy(
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = if (isDark) DarkTextPri else LightTextPri
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Green400,
                unfocusedBorderColor    = if (isDark) DarkBorder else LightBorder,
                focusedContainerColor   = if (isDark) DarkSurface else LightSurface,
                unfocusedContainerColor = if (isDark) DarkSurface else LightSurface,
                cursorColor             = Green400,
                focusedTextColor        = if (isDark) DarkTextPri else LightTextPri,
                unfocusedTextColor      = if (isDark) DarkTextPri else LightTextPri,
            ),
            shape           = RoundedCornerShape(11.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Password strength indicator
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PasswordStrengthIndicator(password: String, isDark: Boolean) {
    val strength = when {
        password.length >= 12
                && password.any { it.isUpperCase() }
                && password.any { it.isDigit() }
                && password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 10
                && password.any { it.isUpperCase() }
                && password.any { it.isDigit() }           -> 3
        password.length >= 8                           -> 2
        else                                           -> 1
    }

    val (label, barColor) = when (strength) {
        1    -> stringResource(R.string.profile_password_weak)        to Color(0xFFEF4444)
        2    -> stringResource(R.string.profile_password_fair)        to Color(0xFFF59E0B)
        3    -> stringResource(R.string.profile_password_strong)      to Green400
        else -> stringResource(R.string.profile_password_very_strong) to Green400
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i < strength) barColor else if (isDark) DarkBorder else LightBorder)
                )
            }
        }
        Text(
            text      = label,
            fontSize  = 9.sp,
            fontWeight= FontWeight.SemiBold,
            color     = barColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Trust points
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TrustPoints(isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) DarkBadgeBg else LightSurface)
            .border(
                1.dp,
                if (isDark) DarkBadgeBorder else LightBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
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
                        .background(Green400.copy(alpha = if (isDark) 0.12f else 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, null,
                        tint     = Green400,
                        modifier = Modifier.size(10.dp))
                }
                Text(
                    text       = text,
                    fontSize   = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color      = if (isDark) DarkTextSec else LightTextSec
                )
            }
        }
    }
}


