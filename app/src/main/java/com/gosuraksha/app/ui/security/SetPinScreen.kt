package com.gosuraksha.app.ui.security

import com.gosuraksha.app.R
import androidx.compose.ui.res.stringResource
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Palette (shared across both files) ────────────────────────────────────────
internal val PinBg       = Color(0xFF0D1117)
internal val PinCard     = Color(0xFF161B22)
internal val PinBorder   = Color(0xFF30363D)
internal val PinTeal     = Color(0xFF00E5C3)
internal val PinGreen    = Color(0xFF00D68F)
internal val PinRed      = Color(0xFFFF3B5C)
internal val PinAmber    = Color(0xFFFFB020)
internal val PinText     = Color(0xFFE6EDF3)
internal val PinMuted    = Color(0xFF8B949E)
internal val KeyBg       = Color(0xFF1C2333)
internal val KeyBorder   = Color(0xFF2D3748)

private sealed interface PinKey {
    data class Digit(val value: Char) : PinKey
    data object Bio : PinKey
    data object Delete : PinKey
}

// =============================================================================
// SetPinScreen
// =============================================================================
@Composable
fun SetPinScreen(
    pinManager:   PinManager,
    onPinCreated: () -> Unit
) {
    val scope   = rememberCoroutineScope()
    var step    by remember { mutableStateOf(1) }
    var pin     by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error   by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    val currentInput = if (step == 1) pin else confirm

    // Rotating dashed orbit
    val infinite = rememberInfiniteTransition(label = stringResource(R.string.ui_setpinscreen_2))
    val orbit by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(22000, easing = LinearEasing)), label = stringResource(R.string.ui_setpinscreen_3))

    // Auto-advance on 6 digits
    LaunchedEffect(currentInput) {
        if (currentInput.length == 6) {
            delay(140)
            if (step == 1) {
                step = 2; error = null
            } else {
                if (confirm != pin) {
                    error = "PINs don't match. Try again."
                    delay(300); confirm = ""
                } else {
                    success = true
                    delay(700)
                    pinManager.savePin(pin)
                    onPinCreated()
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(PinBg),
        contentAlignment = Alignment.TopCenter
    ) {
        // Glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
                .background(Brush.radialGradient(listOf(PinTeal.copy(alpha = 0.07f), Color.Transparent)))
        )
        // Orbit ring
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopCenter)
                .offset(y = 40.dp)
        ) {
            Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = orbit }) {
                drawCircle(
                    color = PinTeal.copy(alpha = 0.07f),
                    style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 11f)))
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // Shield icon
            val iconScale by animateFloatAsState(if (success) 1.15f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = stringResource(R.string.ui_setpinscreen_4))
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .scale(iconScale)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (success) PinGreen.copy(alpha = 0.12f) else PinTeal.copy(alpha = 0.1f))
                    .border(1.dp, if (success) PinGreen.copy(alpha = 0.4f) else PinTeal.copy(alpha = 0.25f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (success) Icons.Filled.VerifiedUser else Icons.Filled.Shield,
                    null,
                    tint = if (success) PinGreen else PinTeal,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            // Step pills
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(2) { i ->
                    val isActive = i + 1 == step
                    val isDone   = i + 1 < step || success
                    val w by animateDpAsState(if (isActive) 28.dp else 12.dp, tween(250), label = stringResource(R.string.ui_setpinscreen_5, i))
                    val c by animateColorAsState(if (isDone || isActive) PinTeal else PinBorder, tween(250), label = stringResource(R.string.ui_setpinscreen_6, i))
                    Box(modifier = Modifier.height(4.dp).width(w).clip(RoundedCornerShape(2.dp)).background(c))
                }
            }

            Spacer(Modifier.height(22.dp))

            // Heading — slides on step change
            AnimatedContent(
                targetState = if (success) 3 else step,
                transitionSpec = {
                    (slideInHorizontally { it / 3 } + fadeIn(tween(220))) togetherWith
                            (slideOutHorizontally { -it / 3 } + fadeOut(tween(180)))
                },
                label = stringResource(R.string.ui_setpinscreen_7)
            ) { s ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when (s) {
                            1 -> "Create Security PIN"
                            2 -> "Confirm Your PIN"
                            else -> "PIN Created!"
                        },
                        color = PinText, fontSize = 21.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when (s) {
                            1 -> "Choose a strong 6-digit PIN to\nprotect your account"
                            2 -> "Re-enter to confirm your PIN"
                            else -> "Your account is now protected"
                        },
                        color = PinMuted, fontSize = 13.sp,
                        textAlign = TextAlign.Center, lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // PIN dots
            PinDotRow(length = currentInput.length, hasError = error != null, success = success)

            Spacer(Modifier.height(12.dp))

            // Error banner
            AnimatedVisibility(visible = error != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
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

            Spacer(Modifier.height(32.dp))

            if (!success) {
                PinKeypad(
                    onNumber = { num ->
                        error = null
                        if (currentInput.length < 6) {
                            if (step == 1) pin += num else confirm += num
                        }
                    },
                    onDelete = {
                        if (step == 1 && pin.isNotEmpty()) pin = pin.dropLast(1)
                        else if (step == 2 && confirm.isNotEmpty()) confirm = confirm.dropLast(1)
                    }
                )

                // Back to step 1
                if (step == 2) {
                    Spacer(Modifier.height(20.dp))
                    TextButton(onClick = { step = 1; confirm = ""; error = null }) {
                        Icon(Icons.Default.ArrowBack, null, tint = PinMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.ui_setpinscreen_1), color = PinMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// =============================================================================
// Shared Composables (used by both screens)
// =============================================================================

@Composable
internal fun PinDotRow(length: Int, hasError: Boolean, success: Boolean = false) {
    val dotColor = when {
        success  -> PinGreen
        hasError -> PinRed
        else     -> PinTeal
    }
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(6) { i ->
            val filled = i < length || success
            val scale by animateFloatAsState(
                if (filled) 1f else 0.65f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = stringResource(R.string.ui_setpinscreen_8, i)
            )
            val color by animateColorAsState(
                if (filled) dotColor else PinBorder,
                tween(150), label = stringResource(R.string.ui_setpinscreen_9, i)
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
internal fun PinKeypad(
    onNumber:        (String) -> Unit,
    onDelete:        () -> Unit,
    biometricSlot:   (@Composable () -> Unit)? = null
) {
    val rows = listOf(
        listOf(PinKey.Digit('1'), PinKey.Digit('2'), PinKey.Digit('3')),
        listOf(PinKey.Digit('4'), PinKey.Digit('5'), PinKey.Digit('6')),
        listOf(PinKey.Digit('7'), PinKey.Digit('8'), PinKey.Digit('9')),
        listOf(PinKey.Bio, PinKey.Digit('0'), PinKey.Delete)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        PinKey.Bio -> {
                            if (biometricSlot != null) {
                                biometricSlot()
                            } else {
                                Spacer(Modifier.size(68.dp))
                            }
                        }
                        PinKey.Delete -> PinKeyButton(isSpecial = true, onClick = onDelete) {
                            Icon(Icons.Outlined.Backspace, null, tint = PinMuted, modifier = Modifier.size(22.dp))
                        }
                        is PinKey.Digit -> PinKeyButton(onClick = { onNumber(key.value.toString()) }) {
                            Text(key.value.toString(), color = PinText, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PinKeyButton(
    isSpecial: Boolean = false,
    onClick:   () -> Unit,
    content:   @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.86f else 1f,
        spring(stiffness = Spring.StiffnessHigh, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = stringResource(R.string.ui_setpinscreen_10)
    )
    Box(
        modifier = Modifier
            .size(68.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isSpecial) Color.Transparent else KeyBg)
            .then(if (!isSpecial) Modifier.border(1.dp, KeyBorder, CircleShape) else Modifier)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}
