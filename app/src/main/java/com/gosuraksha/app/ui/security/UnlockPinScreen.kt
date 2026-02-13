package com.gosuraksha.app.ui.security

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun UnlockPinScreen(
    pinManager: PinManager,
    onUnlocked: () -> Unit,
    onForceLogout: () -> Unit
) {

    val context = LocalContext.current
    val activity = context as ComponentActivity
    val scope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        scope.launch {
            shakeOffset.animateTo(-20f, spring())
            shakeOffset.animateTo(20f, spring())
            shakeOffset.animateTo(0f)
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun triggerHaptic() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                120,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    fun startBiometric() {

        val fragmentActivity = context as FragmentActivity

        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            fragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    onUnlocked()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Authenticate to access GO Suraksha")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF061C14),
                        Color(0xFF0B2F1F),
                        Color.Black
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .offset(x = shakeOffset.value.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Protect What Matters Most",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Enter Security PIN",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) { index ->
                    val filled = index < pin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                if (filled) Color(0xFF6EE48E)
                                else Color.White.copy(alpha = 0.25f),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            NumericKeypad(
                onNumberClick = { number ->
                    if (pin.length < 6) pin += number
                },
                onDelete = {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    if (pinManager.verifyPin(pin)) {
                        onUnlocked()
                    } else {
                        attempts++
                        error = "Incorrect PIN"
                        triggerShake()
                        triggerHaptic()
                        pin = ""

                        if (attempts >= 10) {
                            onForceLogout()
                        }
                    }
                }
            ) {
                Text("Unlock")
            }

            error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Use Biometric Authentication",
                color = Color(0xFF6EE48E),
                modifier = Modifier.clickable { startBiometric() }
            )
        }
    }
}
