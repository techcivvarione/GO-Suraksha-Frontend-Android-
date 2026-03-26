package com.gosuraksha.app.ui.security

// =============================================================================
// BiometricUnlockScreen.kt — App lock using Android BiometricPrompt
//
// Flow:
//   • On first composition → auto-trigger BiometricPrompt
//   • Success → onUnlocked()
//   • Cancel / Fallback → show "Unlock" button for manual retry
//   • Biometric not enrolled / not supported → skip directly to onUnlocked()
// =============================================================================

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private val AccentGreen  = Color(0xFF00E676)
private val DarkBg       = Color(0xFF0A0F1C)
private val CardBg       = Color(0xFF0F1A2E)
private val TextPri      = Color(0xFFEEEEFF)
private val TextSec      = Color(0xFF8888AA)

private enum class UnlockUiState { IDLE, FAILED, SKIPPED }

@Composable
fun BiometricUnlockScreen(
    onUnlocked: () -> Unit,
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf(UnlockUiState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ── Check availability & trigger prompt ───────────────────────────────────
    fun canAuthenticate(): Boolean {
        val bm = BiometricManager.from(context)
        return bm.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt() {
        val activity = context as? FragmentActivity ?: run {
            onUnlocked(); return
        }
        if (!canAuthenticate()) {
            onUnlocked(); return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onUnlocked()
            }
            override fun onAuthenticationFailed() {
                errorMessage = "Authentication failed. Please try again."
                uiState = UnlockUiState.FAILED
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_NO_BIOMETRICS,
                    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                        errorMessage = null
                        uiState = UnlockUiState.FAILED
                    }
                    BiometricPrompt.ERROR_HW_UNAVAILABLE,
                    BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                        // No biometric hardware — allow access
                        onUnlocked()
                    }
                    else -> {
                        errorMessage = errString.toString()
                        uiState = UnlockUiState.FAILED
                    }
                }
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        // Use BIOMETRIC_STRONG | DEVICE_CREDENTIAL for API 30+;
        // fallback to negative-button style for older devices
        val info = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock GO Suraksha")
                .setSubtitle("Confirm your identity to continue")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()
        } else {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock GO Suraksha")
                .setSubtitle("Confirm your identity to continue")
                .setNegativeButtonText("Cancel")
                .build()
        }

        prompt.authenticate(info)
    }

    // Auto-trigger on first render
    LaunchedEffect(Unit) { showBiometricPrompt() }

    // ── Animated ring ─────────────────────────────────────────────────────────
    val infinite = rememberInfiniteTransition(label = "ring")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = LinearEasing),
            RepeatMode.Restart,
        ),
        label = "rotate",
    )

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier         = Modifier.fillMaxSize().background(DarkBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // App name
            Text(
                text          = "GO SURAKSHA",
                fontSize      = 13.sp,
                fontWeight    = FontWeight.Bold,
                color         = AccentGreen,
                letterSpacing = 3.sp,
            )

            Spacer(Modifier.height(36.dp))

            // Animated lock / fingerprint icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .rotate(rotation)
                    .border(
                        2.dp,
                        Brush.sweepGradient(listOf(AccentGreen.copy(0.8f), Color.Transparent, AccentGreen.copy(0.2f))),
                        CircleShape,
                    ),
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(CardBg, CircleShape)
                    .border(1.dp, AccentGreen.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = if (uiState == UnlockUiState.FAILED) Icons.Outlined.Lock else Icons.Outlined.Fingerprint,
                    contentDescription = null,
                    tint               = if (uiState == UnlockUiState.FAILED) Color(0xFFDC2626) else AccentGreen,
                    modifier           = Modifier.size(42.dp),
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text       = when (uiState) {
                    UnlockUiState.FAILED -> "Biometric required"
                    else                 -> "Verify your identity"
                },
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPri,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = errorMessage ?: "Use your fingerprint or device PIN to unlock.",
                fontSize  = 13.sp,
                color     = if (errorMessage != null) Color(0xFFDC2626) else TextSec,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 40.dp),
            )

            Spacer(Modifier.height(36.dp))

            // Retry button
            Button(
                onClick = { showBiometricPrompt() },
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .height(50.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor   = DarkBg,
                ),
            ) {
                Icon(Icons.Outlined.Fingerprint, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text       = "Unlock",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
