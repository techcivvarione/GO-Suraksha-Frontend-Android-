package com.gosuraksha.app.ui.main

import com.gosuraksha.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.domain.model.scan.ScanAnalysisResult
import com.gosuraksha.app.domain.usecase.ScanUseCaseProvider
import com.gosuraksha.app.presentation.scan.ScanViewModel
import com.gosuraksha.app.presentation.scan.ScanViewModelFactory
import com.gosuraksha.app.presentation.state.UiState
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.scan.SharedScanIntentStore
import com.gosuraksha.app.ui.components.localizedUiMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

// ── Unchanged business enum ──────────────────────────────────────────────────
enum class ScanType(val label: String, val apiType: String) {
    MESSAGES("Threats", "THREAT"),
    EMAIL("Email", "EMAIL"),
    PASSWORD("Password", "PASSWORD"),
    QR("QR", "QR"),
    REALITY("Reality", "AI_IMAGE")
}

// =============================================================================
// Design tokens — scan screen specific
// Isolated here so they don't bleed into other screens.
//
// KEY FIX: All text alphas raised from 0.28–0.50 → 0.72–0.88.
// Section labels: monospace feel via letterSpacing + SemiBold.
// Reason text:    0.75 alpha (was 0.28 — nearly invisible).
// =============================================================================
private object ScanColors {
    // Surface hierarchy
    val cardBg       = Color(0xFF181D30)
    val inputBg      = Color(0xFF131627)
    val reasonBg     = Color(0xFF0D0F1A)

    // Risk semantic — vivid, never washed out
    val danger  = Color(0xFFEF4444)
    val warning = Color(0xFFF59E0B)
    val safe    = Color(0xFF10B981)

    // Text — FIXED: was 0.28–0.50, now 0.72–0.88
    fun onCard(isDark: Boolean)   = if (isDark) Color(0xFFE8EAF6) else Color(0xFF1A1F36)
    fun bodyText(isDark: Boolean) = if (isDark) Color(0xFFCDD5F0) else Color(0xFF2A3050)
    fun mutedText(isDark: Boolean)= if (isDark) Color(0xFF6B7699) else Color(0xFF8892B0)
    fun labelText(isDark: Boolean)= if (isDark) Color(0xFF4A5272) else Color(0xFF9AA5C0)
}

// =============================================================================
// ROOT SCREEN — logic 100% unchanged
// =============================================================================
@Composable
fun ScanScreen(
    viewModel: ScanViewModel = run {
        val ctx = androidx.compose.ui.platform.LocalContext.current.applicationContext
        val provider = ctx as ScanUseCaseProvider
        viewModel(factory = ScanViewModelFactory(provider.scanUseCases()))
    }
) {
    val isDark      = ColorTokens.LocalAppDarkMode.current
    val bg          = ColorTokens.background()
    val surface     = ColorTokens.surface()
    val borderColor = ColorTokens.border()
    val accent      = ColorTokens.accent()
    val accentDim   = accent.copy(alpha = 0.7f)
    val danger      = ScanColors.danger
    val warning     = ScanColors.warning
    val success     = ScanColors.safe
    val textPrimary = ScanColors.onCard(isDark)
    val textBody    = ScanColors.bodyText(isDark)
    val textMuted   = ScanColors.mutedText(isDark)
    val textLabel   = ScanColors.labelText(isDark)

    val state            by viewModel.state.collectAsState()
    val sharedPayload    by SharedScanIntentStore.pending.collectAsState()
    val loading           = state is UiState.Loading
    val data              = (state as? UiState.Success)?.data
    val result: ScanAnalysisResult? = data?.analysis
    val upgradeRequired   = data?.upgradeRequired ?: false
    val aiExplanation     = data?.aiExplanation
    val aiExplainLoading  = data?.aiExplainLoading ?: false
    val error             = (state as? UiState.Error)?.message

    var selectedType by remember { mutableStateOf(ScanType.MESSAGES) }
    var input        by remember { mutableStateOf("") }

    LaunchedEffect(sharedPayload) {
        if (sharedPayload != null) {
            selectedType = ScanType.REALITY
        }
    }

    // QR passthrough — unchanged
    if (selectedType == ScanType.QR) {
        Column(Modifier.fillMaxSize().background(bg)) {
            Spacer(Modifier.height(12.dp))
            Box(Modifier.padding(horizontal = 20.dp)) {
                ScanTabs(
                    selected = selectedType,
                    onSelected = { selectedType = it; input = "" },
                    accent = accent,
                    textMuted = textLabel,
                    surface = surface
                )
            }
            QrAnalyzerScreen()
        }
        return
    }

    // Reality passthrough — unchanged
    if (selectedType == ScanType.REALITY) {
        Column(Modifier.fillMaxSize().background(bg)) {
            Spacer(Modifier.height(12.dp))
            Box(Modifier.padding(horizontal = 20.dp)) {
                ScanTabs(
                    selected = selectedType,
                    onSelected = { selectedType = it; input = "" },
                    accent = accent,
                    textMuted = textLabel,
                    surface = surface
                )
            }
            RealityScanScreen(
                sharedPayload = sharedPayload,
                onSharedPayloadConsumed = { SharedScanIntentStore.consume() },
                onScan = { bytes, mimeType, onSuccess, onError ->
                    viewModel.scanAiImage(bytes, mimeType, onSuccess, onError)
                }
            )
        }
        return
    }

    Box(Modifier.fillMaxSize().background(bg)) {
        AmbientBackground(accent = accent, accentDim = accentDim)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 100.dp)
        ) {
            ScanHeader(
                accent = accent,
                textPrimary = textPrimary,
                textMuted = textLabel,
                riskState = when {
                    loading -> "scanning"
                    result != null && result.risk.lowercase() == "high" -> "danger"
                    result != null && result.risk.lowercase() == "medium" -> "warning"
                    result != null -> "safe"
                    else -> "idle"
                }
            )

            Spacer(Modifier.height(22.dp))

            ScanTabs(
                selected = selectedType,
                onSelected = { selectedType = it; input = "" },
                accent = accent,
                textMuted = textLabel,
                surface = surface
            )

            Spacer(Modifier.height(20.dp))

            ScanInputField(
                value = input,
                onValueChange = { input = it },
                selectedType = selectedType,
                accent = accent,
                surface = surface,
                border = borderColor,
                textPrimary = textPrimary,
                textMuted = textMuted
            )

            Spacer(Modifier.height(14.dp))

            ScanActionButton(
                onClick   = { viewModel.analyze(selectedType.apiType, input) },
                enabled   = input.isNotBlank() && !loading,
                loading   = loading,
                accent    = accent,
                accentDim = accentDim,
                bg        = bg,
                border    = borderColor,
                textMuted = textMuted,
                label     = stringResource(R.string.ui_scanscreen_4)
            )

            Spacer(Modifier.height(24.dp))

            // Loading spinner
            AnimatedVisibility(
                visible = loading,
                enter = fadeIn(tween(350)) + scaleIn(tween(350, easing = EaseOutBack)),
                exit  = fadeOut(tween(250)) + scaleOut(tween(250))
            ) {
                Column {
                    ScanLoader(accent = accent, accentDim = accentDim)
                    Spacer(Modifier.height(24.dp))
                }
            }

            // Result
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn(tween(450)) + slideInVertically(tween(550, easing = EaseOutBack)) { it / 3 },
                exit  = fadeOut(tween(300))
            ) {
                result?.let { scan ->
                    val riskColor = when (scan.risk.lowercase()) {
                        "high" -> danger; "medium" -> warning; else -> success
                    }
                    ScanResultCard(
                        scan            = scan,
                        riskColor       = riskColor,
                        upgradeRequired = upgradeRequired,
                        surface         = surface,
                        borderColor     = borderColor,
                        danger          = danger,
                        warning         = warning,
                        success         = success,
                        textPrimary     = textPrimary,
                        textBody        = textBody,
                        textMuted       = textMuted,
                        textLabel       = textLabel,
                        bg              = bg,
                        accent          = accent,
                        onAiExplanation = { viewModel.loadAiExplanation(buildAiExplainText(scan)) }
                    )
                }
            }

            AnimatedVisibility(
                visible = aiExplainLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Generating AI explanation...", color = textMuted, fontSize = 12.sp)
                }
            }

            // AI card
            AnimatedVisibility(
                visible = aiExplanation != null,
                enter = fadeIn(tween(500)) + expandVertically(tween(500, easing = EaseOutQuart)),
                exit  = fadeOut(tween(300))
            ) {
                aiExplanation?.let {
                    Spacer(Modifier.height(12.dp))
                    ScanAiCard(
                        explanation = it,
                        accent      = accent,
                        accentDim   = accentDim,
                        surface     = surface,
                        textPrimary = textBody      // body alpha, not muted
                    )
                }
            }

            // Error
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit  = fadeOut()
            ) {
                error?.let {
                    Spacer(Modifier.height(10.dp))
                    ScanErrorBanner(message = localizedUiMessage(it), danger = danger)
                }
            }

            // Empty state — only shown before any scan
            AnimatedVisibility(
                visible = !loading && result == null && error == null,
                enter = fadeIn(tween(300)),
                exit  = fadeOut(tween(200))
            ) {
                ScanEmptyHint(
                    accent      = accent,
                    textPrimary = textBody,
                    textLabel   = textLabel,
                    surface     = surface,
                    borderColor = borderColor
                )
            }
        }
    }
}

// =============================================================================
// HEADER — dynamic state awareness
// =============================================================================
@Composable
private fun ScanHeader(
    accent: Color,
    textPrimary: Color,
    textMuted: Color,
    riskState: String   // "idle" | "scanning" | "danger" | "warning" | "safe"
) {
    val inf = rememberInfiniteTransition(label = "hdr")
    val glowScale by inf.animateFloat(
        1f, 1.16f,
        infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "gs"
    )
    val liveBlink by inf.animateFloat(
        1f, 0.12f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lb"
    )

    val iconColor = when (riskState) {
        "danger"  -> ScanColors.danger
        "warning" -> ScanColors.warning
        "safe"    -> ScanColors.safe
        else      -> accent
    }

    val statusText = when (riskState) {
        "scanning" -> "ANALYZING · PLEASE WAIT"
        "danger"   -> "THREAT DETECTED"
        "warning"  -> "RISK DETECTED"
        "safe"     -> "ALL CLEAR"
        else       -> "AI-POWERED · LIVE DETECTION"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            // Ambient glow
            Box(
                Modifier
                    .size(56.dp)
                    .scale(glowScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(iconColor.copy(alpha = 0.18f), Color.Transparent)
                        )
                    )
            )
            // Icon box
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconColor.copy(alpha = 0.09f))
                    .border(1.dp, iconColor.copy(alpha = 0.22f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                stringResource(R.string.ui_scanscreen_1),
                color = textPrimary,          // full brightness — was washed out
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(iconColor)
                        .graphicsLayer { alpha = liveBlink }
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    statusText,
                    color = textMuted,         // FIXED: was 0.28 → proper muted token
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

// =============================================================================
// TABS
// =============================================================================
@Composable
fun ScanTabs(
    selected: ScanType,
    onSelected: (ScanType) -> Unit,
    accent: Color,
    textMuted: Color,
    surface: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(surface.copy(alpha = 0.65f))
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp))
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ScanType.entries.forEach { type ->
            val isSelected = type == selected
            val bgAlpha   by animateFloatAsState(if (isSelected) 1f else 0f, tween(220), label = "tba")

            // FIXED: inactive text was 0.50 alpha — now a proper readable muted color
            val textColor by animateColorAsState(
                if (isSelected) accent else textMuted,
                tween(220),
                label = "ttc"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                accent.copy(alpha = 0.12f * bgAlpha),
                                accent.copy(alpha = 0.04f * bgAlpha)
                            )
                        )
                    )
                    .then(
                        if (isSelected) Modifier.border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(accent.copy(alpha = 0.32f), accent.copy(alpha = 0.09f))
                            ),
                            RoundedCornerShape(13.dp)
                        ) else Modifier
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onSelected(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                val label = when (type) {
                    ScanType.MESSAGES -> stringResource(R.string.scan_tab_threats)
                    ScanType.EMAIL    -> stringResource(R.string.scan_tab_email)
                    ScanType.PASSWORD -> stringResource(R.string.scan_tab_password)
                    ScanType.QR       -> stringResource(R.string.scan_tab_qr)
                    ScanType.REALITY  -> stringResource(R.string.scan_tab_reality)
                }
                Text(
                    label,
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    letterSpacing = 0.2.sp,
                    maxLines = 1
                )
            }
        }
    }
}

// =============================================================================
// INPUT FIELD
// =============================================================================
@Composable
private fun ScanInputField(
    value: String,
    onValueChange: (String) -> Unit,
    selectedType: ScanType,
    accent: Color,
    surface: Color,
    border: Color,
    textPrimary: Color,
    textMuted: Color
) {
    val inf = rememberInfiniteTransition(label = "inp_inf")
    val activeBorderA by inf.animateFloat(
        0.22f, 0.48f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "iba"
    )
    val borderCol = if (value.isNotBlank()) accent.copy(alpha = activeBorderA) else border

    Column {
        // FIXED: label was nearly invisible at 0.50 alpha
        Text(
            text = stringResource(R.string.ui_scanscreen_3, selectedType.label.lowercase()),
            color = textMuted,          // proper muted — readable
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (selectedType == ScanType.MESSAGES) Modifier.height(108.dp) else Modifier),
            singleLine = selectedType != ScanType.MESSAGES,
            placeholder = {
                Text(
                    text = if (selectedType == ScanType.MESSAGES)
                        "Paste suspicious text, URLs or messages…"
                    else
                        "Enter ${selectedType.label.lowercase()} to analyze…",
                    // FIXED: placeholder was 0.28 — completely invisible
                    color = textMuted.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = accent.copy(alpha = 0.44f),
                unfocusedBorderColor    = borderCol,
                focusedTextColor        = textPrimary,    // full brightness when typing
                unfocusedTextColor      = textPrimary,
                cursorColor             = accent,
                focusedContainerColor   = surface.copy(alpha = 0.55f),
                unfocusedContainerColor = surface.copy(alpha = 0.45f)
            ),
            shape = RoundedCornerShape(16.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        )
    }
}

// =============================================================================
// ACTION BUTTON — with explosion particles (unchanged logic, refreshed visuals)
// =============================================================================
private data class ExplosionParticle(
    val angle: Float,
    val distance: Float,
    val color: Color,
    val size: Float
)

@Composable
private fun ScanActionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean,
    accent: Color,
    accentDim: Color,
    bg: Color,
    border: Color,
    textMuted: Color,
    label: String
) {
    val scope = rememberCoroutineScope()
    val inf   = rememberInfiniteTransition(label = "btn_inf")
    val shimmer by inf.animateFloat(
        -1f, 2f,
        infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "sh"
    )

    val explosionProgress = remember { Animatable(0f) }
    val shockwaveProgress = remember { Animatable(0f) }
    var particles  by remember { mutableStateOf<List<ExplosionParticle>>(emptyList()) }
    var exploding  by remember { mutableStateOf(false) }

    val pColors = listOf(accent, accentDim, Color.White, Color(0xFF00D68F), Color(0xFF7B5FFF))

    fun fire() {
        if (!enabled || loading) return
        exploding = true
        particles = (0 until 26).map { i ->
            ExplosionParticle(
                angle    = (i.toFloat() / 26f) * 360f + (i * 6f % 28f),
                distance = 55f + (i * 8f % 65f),
                color    = pColors[i % pColors.size],
                size     = 3f + (i * 1.6f % 5f)
            )
        }
        scope.launch {
            explosionProgress.snapTo(0f)
            shockwaveProgress.snapTo(0f)
            launch { shockwaveProgress.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
            explosionProgress.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
            exploding = false
        }
        onClick()
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        if (exploding) {
            Canvas(Modifier.fillMaxSize()) {
                val sw = shockwaveProgress.value
                if (sw > 0f) {
                    val rw = size.width * 0.95f * sw
                    val rh = 60.dp.toPx() * 2f * sw
                    val a  = (1f - sw).coerceIn(0f, 1f)
                    drawOval(
                        accent.copy(alpha = a * 0.7f),
                        topLeft = Offset(center.x - rw / 2, center.y - rh / 2),
                        size = Size(rw, rh),
                        style = Stroke(2.5f)
                    )
                }
                val ep = explosionProgress.value
                particles.forEach { p ->
                    val rad  = Math.toRadians(p.angle.toDouble())
                    val ease = FastOutSlowInEasing.transform(ep)
                    val tx   = center.x + (cos(rad) * p.distance * ease * 2.2f).toFloat()
                    val ty   = center.y + (sin(rad) * p.distance * ease * 0.62f).toFloat()
                    val a    = (1f - ep * 1.2f).coerceIn(0f, 1f)
                    val r    = (p.size * (1f - ep * 0.6f)).coerceAtLeast(0.5f)
                    drawCircle(p.color.copy(alpha = a), r, Offset(tx, ty))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(
                    when {
                        loading -> Brush.horizontalGradient(
                            listOf(accent.copy(alpha = 0.09f), accentDim.copy(alpha = 0.06f))
                        )
                        enabled -> Brush.linearGradient(listOf(accent, Color(0xFF0077FF)))
                        else    -> Brush.horizontalGradient(
                            listOf(border.copy(alpha = 0.4f), border.copy(alpha = 0.25f))
                        )
                    }
                )
                .then(
                    if (enabled && !loading)
                        Modifier.border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.20f), Color.Transparent)
                            ),
                            RoundedCornerShape(17.dp)
                        )
                    else Modifier
                )
                .then(
                    if (loading)
                        Modifier.border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(17.dp))
                    else Modifier
                )
                .clickable(
                    enabled = enabled && !loading,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { fire() },
            contentAlignment = Alignment.Center
        ) {
            // Shimmer sweep
            if (enabled && !loading) {
                Canvas(Modifier.fillMaxSize()) {
                    val sw   = size.width * 0.28f
                    val left = shimmer * (size.width + sw) - sw
                    drawRect(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.22f),
                                Color.Transparent
                            ),
                            startX = left,
                            endX   = left + sw
                        ),
                        size = size
                    )
                }
            }
            Text(
                text = if (loading) "Analyzing…" else label,
                // FIXED: button label color — full contrast on bg
                color = when {
                    loading -> accent
                    enabled -> bg
                    else    -> textMuted
                },
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

// =============================================================================
// LOADER
// =============================================================================
@Composable
fun ScanLoader(accent: Color, accentDim: Color) {
    val inf = rememberInfiniteTransition(label = "ld")
    val r1 by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(1400, easing = LinearEasing)), label = "r1")
    val r2 by inf.animateFloat(360f, 0f, infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "r2")
    val r3 by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(2800, easing = LinearEasing)), label = "r3")
    val scanY by inf.animateFloat(-1f, 1f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sy")
    val iconS by inf.animateFloat(0.9f, 1.06f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "is")

    Box(
        Modifier
            .fillMaxWidth()
            .height(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(160.dp)) {
            val cx = size.width / 2f; val cy = size.height / 2f; val sw = 2.5f
            rotate(r3, Offset(cx, cy)) {
                drawCircle(accent.copy(alpha = 0.10f), cx - 2f, style = Stroke(sw))
                drawArc(accent.copy(alpha = 0.80f), 0f, 75f, false, style = Stroke(sw, cap = StrokeCap.Round))
            }
            val rm = (cx - 2f) * 0.7f
            rotate(r2, Offset(cx, cy)) {
                drawCircle(accentDim.copy(alpha = 0.08f), rm, center = Offset(cx, cy), style = Stroke(sw * 1.4f))
                drawArc(accentDim.copy(alpha = 0.88f), 0f, 90f, false, topLeft = Offset(cx - rm, cy - rm), size = Size(rm * 2, rm * 2), style = Stroke(sw * 1.4f, cap = StrokeCap.Round))
            }
            val ri = (cx - 2f) * 0.42f
            rotate(r1, Offset(cx, cy)) {
                drawCircle(Color.White.copy(alpha = 0.06f), ri, center = Offset(cx, cy), style = Stroke(sw * 1.8f))
                drawArc(Color.White.copy(alpha = 0.70f), 45f, 55f, false, topLeft = Offset(cx - ri, cy - ri), size = Size(ri * 2, ri * 2), style = Stroke(sw * 1.8f, cap = StrokeCap.Round))
            }
            val lineY = cy + scanY * (rm * 0.7f)
            drawLine(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, accent.copy(alpha = 0.70f), Color.Transparent)
                ),
                Offset(cx - rm * 0.85f, lineY),
                Offset(cx + rm * 0.85f, lineY),
                1.5f
            )
        }
        Icon(
            Icons.Default.Shield,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(46.dp).scale(iconS)
        )
    }
}

// =============================================================================
// RESULT CARD
// FIXED: ALL text alphas raised, labels use SemiBold, reason text clearly visible
// =============================================================================
@Composable
private fun ScanResultCard(
    scan: ScanAnalysisResult,
    riskColor: Color,
    upgradeRequired: Boolean,
    surface: Color,
    borderColor: Color,
    danger: Color,
    warning: Color,
    success: Color,
    textPrimary: Color,
    textBody: Color,
    textMuted: Color,
    textLabel: Color,
    bg: Color,
    accent: Color,
    onAiExplanation: () -> Unit
) {
    var showBreachedSites by remember { mutableStateOf(false) }
    val inf = rememberInfiniteTransition(label = "rc")
    val badgeDot by inf.animateFloat(
        1f, 0.15f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bd"
    )
    val scoreAnim by animateFloatAsState(
        scan.score.toFloat() / 100f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "sa"
    )

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ScanColors.cardBg)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(22.dp))
    ) {
        // Corner glow
        Canvas(Modifier.fillMaxWidth().height(90.dp)) {
            drawCircle(riskColor.copy(alpha = 0.06f), size.width * 0.55f, Offset(size.width, 0f))
        }
        // Top accent bar
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(riskColor, riskColor.copy(alpha = 0.14f), Color.Transparent)
                    )
                )
        )

        Column(
            Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Risk badge + score circle
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    // FIXED: section label — was 0.50 alpha, now labelText token
                    Text(
                        stringResource(R.string.ui_scanscreen_5).uppercase(),
                        color = textLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(riskColor.copy(alpha = 0.11f))
                            .border(1.dp, riskColor.copy(alpha = 0.24f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 13.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(riskColor)
                                .graphicsLayer { alpha = badgeDot }
                        )
                        // FIXED: risk label — was tinted, now full riskColor at full opacity
                        Text(
                            scan.risk.uppercase(),
                            color = riskColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.8.sp
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                // Score ring
                Box(Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val sw = 5.5f; val r = size.minDimension / 2f - sw / 2f
                        rotate(-90f) {
                            drawArc(riskColor.copy(alpha = 0.12f), 0f, 360f, false,
                                topLeft = Offset(center.x - r, center.y - r),
                                size = Size(r * 2, r * 2), style = Stroke(sw))
                            drawArc(
                                Brush.sweepGradient(listOf(riskColor.copy(alpha = 0.2f), riskColor)),
                                0f, scoreAnim * 360f, false,
                                topLeft = Offset(center.x - r, center.y - r),
                                size = Size(r * 2, r * 2),
                                style = Stroke(sw, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.ui_scanscreen_6, scan.score),
                            color = riskColor,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 19.sp
                        )
                        Text(
                            stringResource(R.string.ui_scanscreen_7),
                            // FIXED: was riskColor.copy(0.4f) — raised to 0.65f
                            color = riskColor.copy(alpha = 0.65f),
                            fontSize = 7.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Divider
            Box(
                Modifier.fillMaxWidth().height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.09f),
                                Color.White.copy(alpha = 0.09f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Reasons
            if (scan.reasons.isNotEmpty()) {
                Text(
                    stringResource(R.string.ui_scanscreen_21).uppercase(),
                    // FIXED: was 0.50 alpha — now labelText
                    color = textLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    scan.reasons.forEachIndexed { i, reason ->
                        ScanReasonBullet(
                            text      = reason,
                            dotColor  = riskColor,
                            // FIXED: textBody instead of textPrimary.copy(0.72f)
                            textColor = textBody,
                            delayMs   = i * 75
                        )
                    }
                }
            }

            scan.confidence?.let { confidence ->
                QrSectionValue(
                    title = "Confidence",
                    value = "${(confidence * 100).toInt()}%",
                    textLabel = textLabel,
                    textBody = textBody,
                    borderColor = Color.White.copy(alpha = 0.08f)
                )
            }

            scan.recommendation?.takeIf { it.isNotBlank() }?.let { recommendation ->
                QrSectionValue(
                    title = "Recommendation",
                    value = recommendation,
                    textLabel = textLabel,
                    textBody = textBody,
                    borderColor = Color.White.copy(alpha = 0.08f)
                )
            }

            // Breach count
            scan.breachCount?.let { count ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(danger.copy(alpha = 0.06f))
                        .border(1.dp, danger.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // FIXED: was textMuted — now textBody
                    Text(
                        if (!scan.breaches.isNullOrEmpty()) "Breached sites" else stringResource(R.string.ui_scanscreen_8),
                        color = textBody,
                        fontSize = 13.sp
                    )
                    Text(
                        if (!scan.breaches.isNullOrEmpty()) "Breached in $count known breaches" else stringResource(R.string.ui_scanscreen_9, count),
                        color = danger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            if (!scan.breaches.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showBreachedSites = !showBreachedSites }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show breached sites", color = textBody, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(if (showBreachedSites) "−" else "+", color = textLabel, fontSize = 16.sp)
                }
                AnimatedVisibility(visible = showBreachedSites) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        scan.breaches.forEach { breach ->
                            val year = breach.breachDate?.take(4)?.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()
                            Text("• ${breach.name}$year", color = textBody, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }

            // Sites list
            if (!upgradeRequired && !scan.sites.isNullOrEmpty()) {
                Text(
                    stringResource(R.string.ui_scanscreen_22).uppercase(),
                    color = textLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    scan.sites.forEach { ScanSiteRow(it, riskColor, textBody) }
                }
            }

            // Breach analysis
            if (!upgradeRequired && scan.breachAnalysis != null) {
                scan.breachAnalysis.categories?.forEach { (cat, detail) ->
                    if (detail.sites.isNotEmpty()) {
                        val sc = when (detail.severity.lowercase()) {
                            "high" -> danger; "medium" -> warning; else -> success
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                cat.uppercase(),
                                color = textLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.5.sp
                            )
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(sc.copy(alpha = 0.11f))
                                    .border(1.dp, sc.copy(alpha = 0.22f), RoundedCornerShape(7.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(detail.severity.uppercase(), color = sc, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            detail.sites.forEach { ScanSiteRow(it, sc, textBody) }
                        }
                    }
                }
            }

            // Domains
            if (!upgradeRequired && !scan.domains.isNullOrEmpty()) {
                Text(
                    stringResource(R.string.ui_scanscreen_23).uppercase(),
                    color = textLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    scan.domains.forEach { ScanSiteRow(it, riskColor, textBody) }
                }
            }

            // Upgrade gate
            if (upgradeRequired) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(warning.copy(alpha = 0.08f), warning.copy(alpha = 0.02f))
                            )
                        )
                        .border(1.dp, warning.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(stringResource(R.string.ui_scanscreen_10), fontSize = 28.sp)
                        Text(
                            stringResource(R.string.ui_scanscreen_11),
                            color = warning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            scan.upgrade?.message ?: stringResource(R.string.scan_upgrade_message),
                            // FIXED: was warning.copy(0.65f) — raised to 0.85f
                            color = warning.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // AI explain CTA
            if (!upgradeRequired) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp))
                        .background(accent.copy(alpha = 0.06f))
                        .border(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.08f))
                            ),
                            RoundedCornerShape(13.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onAiExplanation
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.ui_scanscreen_12),
                        color = accent,              // full accent — clearly tappable
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text("→", color = accent.copy(alpha = 0.65f), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun QrSectionValue(
    title: String,
    value: String,
    textLabel: Color,
    textBody: Color,
    borderColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            title.uppercase(),
            color = textLabel,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(value, color = textBody, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

// =============================================================================
// AI CARD
// =============================================================================
@Composable
fun ScanAiCard(
    explanation: String,
    accent: Color,
    accentDim: Color,
    surface: Color,
    textPrimary: Color
) {
    val inf = rememberInfiniteTransition(label = "ai")
    val dot by inf.animateFloat(
        1f, 0.2f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot"
    )

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ScanColors.cardBg)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.22f), Color.Transparent, accent.copy(alpha = 0.07f))
                ),
                RoundedCornerShape(22.dp)
            )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(accent, accentDim.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )
        Column(
            Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(accent)
                        .graphicsLayer { alpha = dot }
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    stringResource(R.string.ui_scanscreen_13).uppercase(),
                    color = accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.5.sp
                )
            }
            // FIXED: was 0.80 alpha — now full textPrimary (passed as textBody)
            Text(
                explanation,
                color = textPrimary,
                fontSize = 14.sp,
                lineHeight = 23.sp
            )
        }
    }
}

// =============================================================================
// ERROR BANNER
// =============================================================================
@Composable
private fun ScanErrorBanner(message: String, danger: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(danger.copy(alpha = 0.07f))
            .border(1.dp, danger.copy(alpha = 0.20f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(danger))
        Spacer(Modifier.width(10.dp))
        // FIXED: was danger.copy(0.90) — now full danger color for error messages
        Text(message, color = danger, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

// =============================================================================
// EMPTY HINT — shown before first scan
// =============================================================================
@Composable
private fun ScanEmptyHint(
    accent: Color,
    textPrimary: Color,
    textLabel: Color,
    surface: Color,
    borderColor: Color
) {
    val items = listOf(
        Triple("🎣", Color(0xFFEF4444), "Phishing links & scam messages"),
        Triple("🔑", Color(0xFF8B5CF6), "Compromised passwords & leaks"),
        Triple("📧", Color(0xFF3B82F6), "Email breach detection"),
        Triple("🤖", Color(0xFF00C9A7), "AI-generated deepfake images")
    )

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(surface.copy(alpha = 0.55f))
            .border(1.dp, borderColor.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "WHAT I CAN DETECT",
                color = textLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            items.forEach { (emoji, color, desc) ->
                Row(
                    Modifier.padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.11f))
                            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    // FIXED: full textPrimary — was nearly invisible
                    Text(desc, color = textPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}

// =============================================================================
// AMBIENT BACKGROUND — unchanged
// =============================================================================
@Composable
fun AmbientBackground(accent: Color, accentDim: Color) {
    val inf = rememberInfiniteTransition(label = "amb")
    val t1 by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(18000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "t1"
    )
    val t2 by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(22000, easing = FastOutSlowInEasing), RepeatMode.Reverse, StartOffset(5000)),
        label = "t2"
    )
    Canvas(Modifier.fillMaxSize()) {
        fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
        drawCircle(
            accent.copy(alpha = 0.07f),
            radius = size.width * 0.9f,
            center = Offset(lerp(-size.width * 0.2f, -size.width * 0.05f, t1), lerp(-size.height * 0.15f, -size.height * 0.25f, t1))
        )
        drawCircle(
            accentDim.copy(alpha = 0.055f),
            radius = size.width * 0.75f,
            center = Offset(lerp(size.width * 1.2f, size.width * 1.05f, t2), lerp(size.height * 1.1f, size.height * 0.95f, t2))
        )
        drawCircle(
            Color(0xFF7B2FFF).copy(alpha = 0.03f),
            radius = size.width * 0.5f,
            center = Offset(size.width * 0.5f + lerp(-20f, 20f, t1), size.height * 0.45f + lerp(-20f, 20f, t2))
        )
    }
}

// =============================================================================
// SMALL HELPERS
// =============================================================================
@Composable
private fun ScanReasonBullet(
    text: String,
    dotColor: Color,
    textColor: Color,
    delayMs: Int
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delayMs.toLong()); visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInHorizontally(tween(320)) { -16 }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(ScanColors.reasonBg.copy(alpha = 0.8f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(11.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(11.dp))
            // FIXED: was 0.72 alpha — now full textColor (textBody token)
            Text(text, color = textColor, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun ScanSiteRow(site: String, accentColor: Color, textColor: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.18f))
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.ui_scanscreen_14),
            fontSize = 12.sp,
            // FIXED: was accentColor.copy(0.65f) — raised to 0.85f
            color = accentColor.copy(alpha = 0.85f)
        )
        Spacer(Modifier.width(9.dp))
        // FIXED: was 0.72 alpha — now textColor (textBody)
        Text(site, color = textColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun buildAiExplainText(scan: ScanAnalysisResult): String {
    val reasonsText = scan.reasons.joinToString(separator = ". ")
    val recommendationText = scan.recommendation?.takeIf { it.isNotBlank() }.orEmpty()
    return listOf(reasonsText, recommendationText)
        .filter { it.isNotBlank() }
        .joinToString(separator = ". ")
}

// Backward-compat aliases — keep existing call sites working
@Composable
fun GlassHeader(accent: Color, textPrimary: Color, textMuted: Color) =
    ScanHeader(accent, textPrimary, textMuted, "idle")

@Composable
fun GlassTabs(selected: ScanType, onSelected: (ScanType) -> Unit, accent: Color, textMuted: Color, surface: Color) =
    ScanTabs(selected, onSelected, accent, textMuted, surface)

@Composable
fun GlassLoader(accent: Color, accentDim: Color) = ScanLoader(accent, accentDim)

@Composable
fun GlassAiCard(explanation: String, accent: Color, accentDim: Color, surface: Color, textPrimary: Color) =
    ScanAiCard(explanation, accent, accentDim, surface, textPrimary)

@Composable
fun GlassError(message: String, danger: Color) = ScanErrorBanner(message, danger)

@Composable
fun GlassExplosionButton(onClick: () -> Unit, enabled: Boolean, loading: Boolean, accent: Color, accentDim: Color, bg: Color, border: Color, textMuted: Color, label: String) =
    ScanActionButton(onClick, enabled, loading, accent, accentDim, bg, border, textMuted, label)

@Composable
fun GlassResultCard(scan: ScanAnalysisResult, riskColor: Color, upgradeRequired: Boolean, surface: Color, borderColor: Color, danger: Color, warning: Color, success: Color, textPrimary: Color, textMuted: Color, bg: Color, accent: Color, onAiExplanation: () -> Unit) {
    val isDark = ColorTokens.LocalAppDarkMode.current
    ScanResultCard(scan, riskColor, upgradeRequired, surface, borderColor, danger, warning, success, textPrimary, ScanColors.bodyText(isDark), textMuted, ScanColors.labelText(isDark), bg, accent, onAiExplanation)
}

@Composable
fun SegmentedControl(selected: ScanType, onSelected: (ScanType) -> Unit, cyberTeal: Color, textSecondary: Color, cardDark: Color) =
    ScanTabs(selected, onSelected, cyberTeal, textSecondary, cardDark)

@Composable
fun ShieldBuildingLoader(arcColor: Color) = ScanLoader(arcColor, arcColor.copy(alpha = 0.6f))

@Composable
fun AnimatedCyberBackground(dotColor: Color) = AmbientBackground(dotColor, dotColor.copy(alpha = 0.6f))
