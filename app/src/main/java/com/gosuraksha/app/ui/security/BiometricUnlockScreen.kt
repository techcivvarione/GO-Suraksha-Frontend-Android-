package com.gosuraksha.app.ui.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private val AccentGreen = Color(0xFF00E676)
private val DarkBg = Color(0xFF0A0F1C)
private val CardBg = Color(0xFF0F1A2E)

@Composable
fun BiometricUnlockScreen(
    onUnlocked: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isPromptShowing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun showBiometricPrompt() {
        if (isPromptShowing) return

        val activity = context as? FragmentActivity
        if (activity == null) {
            errorMessage = "Authentication is unavailable right now."
            return
        }

        val canAuthenticate = BiometricManager.from(context)
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            errorMessage = "Please set up fingerprint or device lock in settings."
            return
        }

        isPromptShowing = true
        errorMessage = null

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    isPromptShowing = false
                    onUnlocked()
                }

                override fun onAuthenticationFailed() {
                    errorMessage = "Authentication failed. Please try again."
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    isPromptShowing = false
                    errorMessage = when (errorCode) {
                        BiometricPrompt.ERROR_NO_BIOMETRICS,
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> "Please set up fingerprint or device lock in settings."
                        else -> errString.toString()
                    }
                }
            }
        )

        val promptInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock GO Suraksha")
                .setSubtitle("Use your fingerprint, face, or device lock to continue")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()
        } else {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock GO Suraksha")
                .setSubtitle("Use your device lock to continue")
                .setDeviceCredentialAllowed(true)
                .build()
        }

        prompt.authenticate(promptInfo)
    }

    LaunchedEffect(Unit) {
        showBiometricPrompt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg, contentColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (errorMessage == null) Icons.Outlined.Fingerprint else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(42.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Verify your identity",
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "Use your fingerprint, face, PIN, pattern, or device password.",
                    color = Color(0xFFD2D8E2),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    enabled = !isPromptShowing,
                    onClick = { showBiometricPrompt() },
                ) {
                    Text(if (isPromptShowing) "Waiting for system prompt" else "Unlock")
                }
            }
        }
    }
}
