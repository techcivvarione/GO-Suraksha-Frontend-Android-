package com.gosuraksha.app.ui.auth

// =============================================================================
// OtpScreen.kt — Final redesign
//
// Changes from previous version:
//   • Hero: #060E08 dark, dot grid via drawBehind, logo top-center
//   • No radar rings, no glow, no "GoSuraksha" text label under logo
//   • No scrolling ticker
//   • Timer ring + Resend: proper space-between alignment fixed
//   • OTP boxes: uniform width, perfectly centered
//   • "Change number" centered below Verify button
//   • ViewModel wiring unchanged
// =============================================================================

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.presentation.auth.AuthViewModel
import com.gosuraksha.app.ui.components.FullScreenLoader

private const val OTP_TOTAL_SECONDS = 30f

@Composable
fun OtpScreen(
    viewModel:  AuthViewModel,
    onVerified: (Boolean) -> Unit,
    onBack:     () -> Unit
) {
    val isDark = ColorTokens.LocalAppDarkMode.current

    // ── ViewModel state ───────────────────────────────────────────────────
    val phone by viewModel.pendingPhone.collectAsStateWithLifecycle()
    val isVerifying by viewModel.isVerifyingOtp.collectAsStateWithLifecycle()
    val isSending by viewModel.isSendingOtp.collectAsStateWithLifecycle()
    val secondsLeft by viewModel.otpSecondsLeft.collectAsStateWithLifecycle()
    val vmError by viewModel.otpError.collectAsStateWithLifecycle()

    // ── Local state ───────────────────────────────────────────────────────
    var otp by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val error = vmError ?: localError
    BackHandler(onBack = onBack)

    val displayPhone = phone?.let {
        val digits = it.removePrefix("+91").removePrefix("91").trim()
        if (digits.length == 10) "+91 ${digits.take(5)} ${digits.drop(5)}"
        else it
    } ?: ""

    // ── Card slide-up ──────────────────────────────────────────────────────
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cardVisible = true }

    // ── OTP auto-verify ───────────────────────────────────────────────────
    LaunchedEffect(otp) {
        if (otp.length == 6 && !isVerifying) {
            localError = null
            if (BuildConfig.DEBUG) Log.d("AUTH_FLOW", "Auto-verify OTP")
            viewModel.verifyOtp(
                phone = phone.orEmpty(),
                otp = otp,
                onResult = onVerified,
                onError = { localError = it }
            )
        }
    }

    // ── Screen ────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060E08))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Compact dark hero (shorter than login) ─────────────────────
            AuthHeroDark(
                title = "Verify\nyour number",
                subtitle = if (displayPhone.isNotBlank()) "OTP sent to $displayPhone"
                else "Enter the OTP we sent",
                height = 230.dp
            )

            // ── Bottom card ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = cardVisible,
                modifier = Modifier.weight(1f),
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    initialOffsetY = { it }
                ) + fadeIn(tween(350))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // This box fills the bottom half with card color so no dark gap shows
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.5f)
                            .align(Alignment.BottomCenter)
                            .background(AuthColors.card(isDark))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AuthCardSurface(isDark = isDark) {

                            // Header
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "Enter OTP",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp,
                                    color = AuthColors.textPri(isDark)
                                )
                                Text(
                                    text = "6-digit code · valid for 10 minutes",
                                    fontSize = 13.sp,
                                    color = AuthColors.textSec(isDark)
                                )
                            }

                            // OTP boxes — centered, uniform width
                            OtpInputRow(
                                otp = otp,
                                onOtpChange = { otp = it; localError = null },
                                isDark = isDark
                            )

                            // Timer ring + Resend — fixed alignment
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (secondsLeft > 0) {
                                    CircularCountdownTimer(
                                        secondsLeft = secondsLeft,
                                        totalSeconds = OTP_TOTAL_SECONDS,
                                        isDark = isDark
                                    )
                                } else {
                                    Text(
                                        text = "Didn't receive it?",
                                        fontSize = 12.sp,
                                        color = AuthColors.textSec(isDark)
                                    )
                                }

                                Text(
                                    text = if (isSending) "Sending…" else "Resend OTP",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (secondsLeft > 0 || isSending)
                                        AuthColors.textTert(isDark)
                                    else
                                        AuthColors.Accent,
                                    modifier = Modifier.clickable(
                                        enabled = secondsLeft == 0 && !isSending && phone != null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        localError = null
                                        otp = ""
                                        viewModel.sendOtp(
                                            phone = phone.orEmpty(),
                                            onSuccess = {},
                                            onError = { localError = it }
                                        )
                                    }
                                )
                            }

                            // Verify button
                            AuthPrimaryButton(
                                text = "Verify & Continue",
                                onClick = {
                                    localError = null
                                    if (BuildConfig.DEBUG) Log.d(
                                        "AUTH_FLOW",
                                        "Manual verify tapped"
                                    )
                                    viewModel.verifyOtp(
                                        phone = phone.orEmpty(),
                                        otp = otp,
                                        onResult = onVerified,
                                        onError = { localError = it }
                                    )
                                },
                                enabled = otp.length == 6,
                                isLoading = isVerifying,
                                isDark = isDark
                            )

                            // Error
                            error?.let { AuthErrorRow(it, isDark) }

                            // Change number — centered
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "← Change number",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AuthColors.Accent,
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onBack
                                    )
                                )
                            }

                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }

            FullScreenLoader(visible = isVerifying)
        }
    }
}

// =============================================================================
// CircularCountdownTimer — unchanged logic, kept here for colocation
// =============================================================================
@Composable
private fun CircularCountdownTimer(
    secondsLeft:  Int,
    totalSeconds: Float,
    isDark:       Boolean
) {
    val progress = (secondsLeft / totalSeconds).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(800, easing = LinearOutSlowInEasing),
        label         = "timerProgress"
    )

    val ringColor = when {
        secondsLeft > 15 -> AuthColors.Accent
        secondsLeft > 7  -> Color(0xFFF59E0B)
        else             -> AuthColors.ErrorRed
    }
    val ringColorAnimated by animateColorAsState(
        targetValue   = ringColor,
        animationSpec = tween(600),
        label         = "timerColor"
    )

    Box(
        modifier         = Modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        // Track ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawArc(
                        color      = if (isDark) AuthColors.BorderDark else AuthColors.BorderLight,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter  = false,
                        style      = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
                        size       = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                        topLeft    = Offset(2.dp.toPx(), 2.dp.toPx())
                    )
                }
        )
        // Progress arc
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawArc(
                        color      = ringColorAnimated,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter  = false,
                        style      = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
                        size       = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                        topLeft    = Offset(2.dp.toPx(), 2.dp.toPx())
                    )
                }
        )
        // Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = "$secondsLeft",
                fontSize   = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = ringColorAnimated
            )
            Text(
                text     = "sec",
                fontSize = 8.sp,
                color    = AuthColors.textTert(isDark)
            )
        }
    }
} // AuthCardSurface
