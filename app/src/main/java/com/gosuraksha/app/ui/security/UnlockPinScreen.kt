package com.gosuraksha.app.ui.security

import com.gosuraksha.app.R
import androidx.compose.ui.res.stringResource
import android.Manifest
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresPermission
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =============================================================================
// UnlockPinScreen
// =============================================================================
@Composable
fun UnlockPinScreen(
    pinManager:    PinManager,
    onUnlocked:    () -> Unit,
    onForceLogout: () -> Unit
) {
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()

    var pin      by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(0) }
    var error    by remember { mutableStateOf<String?>(null) }
    var unlocked by remember { mutableStateOf(false) }
    var locked   by remember { mutableStateOf(false) }

    val negativeButtonText = stringResource(R.string.ui_unlockpinscreen_1)
    val shakeX = remember { Animatable(0f) }

    // Rotating orbit
    val infinite = rememberInfiniteTransition(label = stringResource(R.string.ui_unlockpinscreen_7))
    val orbit by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(25000, easing = LinearEasing)), label = stringResource(R.string.ui_unlockpinscreen_8)
    )
    // Pulse for lock icon
    val pulse by infinite.animateFloat(
        0.97f, 1.03f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = stringResource(R.string.ui_unlockpinscreen_9)
    )

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun triggerHaptic() {
        val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vib.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun triggerShake() {
        scope.launch {
            repeat(4) {
                shakeX.animateTo(if (it % 2 == 0) -16f else 16f, spring(stiffness = Spring.StiffnessHigh))
            }
            shakeX.animateTo(0f, spring(stiffness = Spring.StiffnessHigh))
        }
    }

    fun startBiometric() {
        val activity = context as FragmentActivity
        val prompt   = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    scope.launch { unlocked = true; delay(600); onUnlocked() }
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify Identity")
                .setSubtitle("Use biometrics to access GO Suraksha")
                .setNegativeButtonText(negativeButtonText)
                .build()
        )
    }

    // Auto-verify on 6 digits
    LaunchedEffect(pin) {
        if (pin.length == 6) {
            delay(100)
            if (pinManager.verifyPin(pin)) {
                unlocked = true
                delay(600)
                onUnlocked()
            } else {
                attempts++
                triggerShake()
                try { triggerHaptic() } catch (_: Exception) {}
                if (attempts >= 10) {
                    locked = true
                    delay(1500)
                    onForceLogout()
                } else {
                    error = if (attempts >= 7) "⚠️ ${10 - attempts} attempts left before lockout" else "Incorrect PIN"
                    pin = ""
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(PinBg),
        contentAlignment = Alignment.Center
    ) {
        // Glow
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .background(Brush.radialGradient(
                    listOf(
                        if (unlocked) PinGreen.copy(alpha = 0.08f)
                        else if (locked) PinRed.copy(alpha = 0.08f)
                        else PinTeal.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                ))
        )

        // Orbit ring
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopCenter)
                .offset(y = 50.dp)
        ) {
            Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = orbit }) {
                drawCircle(
                    color = PinTeal.copy(alpha = 0.06f),
                    style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 12f)))
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = shakeX.value.dp)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Lock icon
            val iconScale by animateFloatAsState(
                if (unlocked) 1.18f else if (locked) 0.9f else pulse,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = stringResource(R.string.ui_unlockpinscreen_10)
            )
            val iconBg    = when { unlocked -> PinGreen;  locked -> PinRed;  else -> PinTeal }
            val iconVec   = when { unlocked -> Icons.Filled.LockOpen; locked -> Icons.Filled.Lock; else -> Icons.Filled.Lock }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(iconScale)
                    .clip(RoundedCornerShape(26.dp))
                    .background(iconBg.copy(alpha = 0.12f))
                    .border(1.5.dp, iconBg.copy(alpha = 0.35f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconVec, null, tint = iconBg, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text(stringResource(R.string.ui_unlockpinscreen_2), color = PinText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    unlocked -> "Access Granted"
                    locked   -> "Account Locked"
                    else     -> "Enter PIN to continue"
                },
                color = when { unlocked -> PinGreen; locked -> PinRed; else -> PinMuted },
                fontSize = 13.sp
            )

            Spacer(Modifier.height(28.dp))

            // Attempts bar — only show after first wrong attempt
            AnimatedVisibility(visible = attempts > 0 && !unlocked && !locked) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val left       = 10 - attempts
                    val progress   = left / 10f
                    val barColor   = when {
                        left <= 3 -> PinRed
                        left <= 6 -> PinAmber
                        else      -> PinTeal
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.ui_unlockpinscreen_3), color = PinMuted, fontSize = 10.sp)
                        Text(stringResource(R.string.ui_unlockpinscreen_4, left), color = barColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(3.dp)
                            .clip(RoundedCornerShape(2.dp)).background(PinBorder)
                    ) {
                        val animProg by animateFloatAsState(progress, tween(400), label = stringResource(R.string.ui_unlockpinscreen_11))
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animProg)
                                .clip(RoundedCornerShape(2.dp))
                                .background(barColor)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // PIN dots
            PinDotRow(length = pin.length, hasError = error != null, success = unlocked)

            Spacer(Modifier.height(12.dp))

            // Error
            AnimatedVisibility(
                visible = error != null,
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PinRed.copy(alpha = 0.08f))
                        .border(1.dp, PinRed.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Error, null, tint = PinRed, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(error ?: "", color = PinRed, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            // Keypad — hide when unlocked or locked
            if (!unlocked && !locked) {
                PinKeypad(
                    onNumber = { num ->
                        error = null
                        if (pin.length < 6) pin += num
                    },
                    onDelete = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                    biometricSlot = {
                        PinKeyButton(isSpecial = true, onClick = { startBiometric() }) {
                            Icon(Icons.Outlined.Fingerprint, null, tint = PinTeal, modifier = Modifier.size(28.dp))
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))

                // Biometric text link
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(PinTeal.copy(alpha = 0.06f))
                        .border(1.dp, PinTeal.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { startBiometric() }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Fingerprint, null, tint = PinTeal, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(stringResource(R.string.ui_unlockpinscreen_5), color = PinTeal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Locked state CTA
            if (locked) {
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.ui_unlockpinscreen_6),
                    color = PinRed.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}