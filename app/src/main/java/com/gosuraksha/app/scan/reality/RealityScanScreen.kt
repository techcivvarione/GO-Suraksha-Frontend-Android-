package com.gosuraksha.app.scan.reality

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gosuraksha.app.R
import com.gosuraksha.app.scan.SharedScanPayload
import com.gosuraksha.app.scan.components.ScanErrorBanner
import com.gosuraksha.app.scan.components.ScanLoader
import com.gosuraksha.app.scan.components.ScanPrimaryAction
import com.gosuraksha.app.scan.components.ScanResultCard
import com.gosuraksha.app.scan.components.ScanRiskTone
import com.gosuraksha.app.scan.components.containerColor
import com.gosuraksha.app.scan.components.contentColor
import com.gosuraksha.app.scan.components.toScanRiskTone
import com.gosuraksha.app.scan.design.ScanTheme
import com.gosuraksha.app.ui.components.UpgradeInterceptDialog
import com.gosuraksha.app.ui.components.UpgradeTrigger
import com.gosuraksha.app.ui.components.localizedUiMessage
import java.util.Locale

// ─── Domain models ─────────────────────────────────────────────────────────────

data class RealityScanResult(
    val riskLevel: String,
    val riskScore: Int,
    val confidence: Float?,
    val confidenceLabel: String?,
    val summary: String,
    val highlights: List<String>,
    val technicalSignals: List<String>,
    val recommendation: String,
)

enum class RsState { IDLE, SCANNING, DONE, ERROR }

// ─── Core image scan screen ──────────────────────────────────────────────────────
@Composable
fun RealityScanScreen(
    sharedPayload: SharedScanPayload? = null,
    onSharedPayloadConsumed: () -> Unit = {},
    onScan: (Uri, String, (RealityScanResult) -> Unit, (String) -> Unit) -> Unit,
    onExplain: (RealityScanResult) -> Unit = {},
    aiExplanation: String? = null,
    aiExplainLoading: Boolean = false,
    accent: Color = ScanTheme.colors.primaryBlue,
    onUpgradePlan: () -> Unit = {},
    // Lets callers pass the ViewModel's isScanning flow so rsState stays
    // correct after screen recreation (rotation, process restart).
    isViewModelScanning: Boolean = false,
) {
    val context    = LocalContext.current
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography

    var uri              by remember { mutableStateOf<Uri?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }
    var rsState          by rememberSaveable { mutableStateOf(RsState.IDLE) }
    var result           by remember { mutableStateOf<RealityScanResult?>(null) }
    var errorMsg         by remember { mutableStateOf<String?>(null) }
    var showSignals      by remember { mutableStateOf(false) }

    var showUpgradeDialog by remember { mutableStateOf(false) }
    val upgradeTrigger    = remember(errorMsg) { errorMsg.toUpgradeTrigger() }

    // Auto-show upgrade dialog when a limit or feature-gated error occurs
    LaunchedEffect(errorMsg) {
        if (errorMsg.isUpgradeError()) showUpgradeDialog = true
    }

    // Auto-load shared payload if it is an image
    LaunchedEffect(sharedPayload) {
        val payload = sharedPayload ?: return@LaunchedEffect
        val resolved = resolveMimeType(context, payload.uri, payload.mimeType)
        if (resolved.startsWith("image/")) {
            uri = payload.uri
            selectedMimeType = resolved
            result = null; errorMsg = null; rsState = RsState.IDLE
        }
        onSharedPayloadConsumed()
    }

    // Sync local scan state to the ViewModel's isScanning flag.
    // This keeps rsState correct after screen recreation (e.g. rotation while
    // a scan is in flight). The ViewModel survives recreation; rememberSaveable does not.
    LaunchedEffect(isViewModelScanning) {
        when {
            isViewModelScanning && rsState != RsState.SCANNING -> rsState = RsState.SCANNING
            !isViewModelScanning && rsState == RsState.SCANNING -> {
                // ViewModel finished but no callback was delivered (recreation race).
                // Reset to IDLE so the user can try again.
                rsState = RsState.IDLE
                errorMsg = null
            }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { picked ->
        if (picked == null) return@rememberLauncherForActivityResult
        val mimeType = resolveMimeType(context, picked)
        val ext = resolveExtension(context, picked)
        if (mimeType.startsWith("image/") || ext in setOf("jpg", "jpeg", "png", "webp")) {
            uri = picked
            selectedMimeType = mimeType
            result = null; errorMsg = null; rsState = RsState.IDLE
            showSignals = false
        } else {
            errorMsg = "Please choose a JPG, PNG, or WebP image."; rsState = RsState.ERROR
        }
    }

    val runEnabled = uri != null && rsState != RsState.SCANNING

    // ── Upgrade intercept dialog ──────────────────────────────────────────────
    UpgradeInterceptDialog(
        visible       = showUpgradeDialog,
        trigger       = upgradeTrigger,
        onDismiss     = { showUpgradeDialog = false },
        onSelectPro   = { showUpgradeDialog = false; onUpgradePlan() },
        onSelectUltra = { showUpgradeDialog = false; onUpgradePlan() },
    )

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        // ── Drop zone / image preview ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (uri != null) 160.dp else 120.dp)
                .background(colors.surface, RoundedCornerShape(18.dp))
                .border(
                    width = 1.5.dp,
                    color = if (uri != null) accent.copy(alpha = 0.35f) else colors.border,
                    shape = RoundedCornerShape(18.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Image thumbnail (visible when a file is selected and not scanning)
            if (uri != null && rsState != RsState.SCANNING) {
                AsyncImage(
                    model              = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier           = Modifier
                        .matchParentSize()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(17.dp)),
                )
            }

            // Empty state / scanning overlay
            if (uri == null || rsState == RsState.SCANNING) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier            = Modifier.padding(horizontal = 16.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Image,
                        contentDescription = null,
                        tint               = if (uri != null) accent else colors.textTertiary,
                        modifier           = Modifier.size(26.dp),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = when {
                            rsState == RsState.SCANNING -> "Detection in progress…"
                            uri != null -> resolveDisplayName(context, uri!!)
                                ?.substringAfterLast('/')
                                ?.ifBlank { "Image ready for scan" }
                                ?: "Image ready for scan"
                            else -> "No image selected"
                        },
                        style     = typography.cardTitle,
                        color     = if (uri != null && rsState != RsState.SCANNING) accent
                                    else colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text      = "jpg · png · webp supported",
                        style     = typography.bodySmall,
                        color     = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Scanning spinner overlay
            if (rsState == RsState.SCANNING) {
                Box(
                    modifier         = Modifier
                        .matchParentSize()
                        .background(
                            colors.background.copy(alpha = 0.72f),
                            RoundedCornerShape(18.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    ScanLoader(label = "Analysing image…")
                }
            }
        }

        // ── Choose / Replace file ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(colors.surface, RoundedCornerShape(14.dp))
                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                .clickable(enabled = rsState != RsState.SCANNING) { picker.launch("image/*") }
                .padding(horizontal = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector        = Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint               = colors.textSecondary,
                modifier           = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text     = if (uri == null) "Choose Image" else "Replace Image",
                style    = typography.buttonText,
                color    = colors.textSecondary,
                maxLines = 1,
            )
        }

        // ── Run Detection — gradient button ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = if (runEnabled)
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1E40AF), Color(0xFF3B82F6))
                        )
                    else
                        Brush.horizontalGradient(
                            colors = listOf(colors.border, colors.border)
                        ),
                )
                .clickable(
                    enabled           = runEnabled,
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    val currentUri = uri ?: return@clickable
                    val mime = resolveMimeType(context, currentUri, selectedMimeType)
                    rsState = RsState.SCANNING
                    onScan(
                        currentUri,
                        mime,
                        { scanResult ->
                            rsState = RsState.DONE
                            result = scanResult
                            errorMsg = null
                            showSignals = false
                        },
                        { message ->
                            rsState  = RsState.ERROR
                            errorMsg = message.ifBlank { "error_generic" }
                        }
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = if (rsState == RsState.SCANNING) "Scanning…" else "Run Detection",
                style = typography.buttonText,
                color = if (runEnabled) Color.White else colors.textTertiary,
            )
        }

        // ── Result card — verdict-first design (Step 5) ────────────────────
        result?.let { scanResult ->
            val tone      = scanResult.riskLevel.toScanRiskTone()
            val toneColor = tone.contentColor(colors)
            val toneBg    = tone.containerColor(colors)
            var showWhy   by remember { mutableStateOf(false) }
            var showTech  by remember { mutableStateOf(false) }

            AnimatedVisibility(
                visible = true,
                enter   = fadeIn() + slideInVertically { it / 3 },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    // ── 0. Strong verdict banner ───────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(toneColor.copy(alpha = 0.13f), RoundedCornerShape(16.dp))
                            .border(1.dp, toneColor.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text  = when (tone) {
                                    ScanRiskTone.DANGER  -> "🚨"
                                    ScanRiskTone.WARNING -> "⚠️"
                                    ScanRiskTone.SAFE    -> "✅"
                                },
                                style = typography.sectionHeading,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text       = when (tone) {
                                        ScanRiskTone.DANGER  -> "Unsafe — Do NOT trust this image"
                                        ScanRiskTone.WARNING -> "Be careful — This image may not be safe"
                                        ScanRiskTone.SAFE    -> "Safe — This looks like a real photo"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 16.sp,
                                    color      = toneColor,
                                )
                                Text(
                                    text     = when (tone) {
                                        ScanRiskTone.DANGER  -> "High risk of manipulation detected."
                                        ScanRiskTone.WARNING -> "Some signs of editing found. Review carefully."
                                        ScanRiskTone.SAFE    -> "No significant signs of manipulation."
                                    },
                                    fontSize = 12.sp,
                                    color    = colors.textSecondary,
                                )
                            }
                        }
                    }

                    // ── 1. Verdict hero card ───────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(toneBg, RoundedCornerShape(20.dp))
                            .border(1.dp, toneColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Icon + verdict headline
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = when (tone) {
                                    ScanRiskTone.DANGER  -> "🚨"
                                    ScanRiskTone.WARNING -> "⚠️"
                                    ScanRiskTone.SAFE    -> "✅"
                                },
                                style = typography.sectionHeading,
                            )
                            Text(
                                text  = when (tone) {
                                    ScanRiskTone.DANGER  -> "This image is very likely fake"
                                    ScanRiskTone.WARNING -> "This image may have been edited or generated"
                                    ScanRiskTone.SAFE    -> "This image looks like a real photo"
                                },
                                style = typography.sectionHeading,
                                color = colors.textPrimary,
                            )
                        }

                        // Backend summary or sensible fallback
                        Text(
                            text  = scanResult.summary.takeIf { it.isNotBlank() } ?: when (tone) {
                                ScanRiskTone.DANGER  -> "We found signs it could be edited or AI-generated."
                                ScanRiskTone.WARNING -> "Some indicators suggest this image may have been modified."
                                ScanRiskTone.SAFE    -> "No significant signs of manipulation were detected."
                            },
                            style = typography.bodySmall,
                            color = colors.textSecondary,
                        )

                        // Trust Score row  (Trust = 100 − riskScore)
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text  = "Trust Score",
                                style = typography.chipLabel,
                                color = colors.textTertiary,
                            )
                            Text(
                                text  = "${(100 - scanResult.riskScore).coerceIn(0, 100)} / 100",
                                style = typography.sectionHeading,
                                color = toneColor,
                            )
                        }
                    }

                    // ── 2. Why this result (expandable) ───────────────────
                    if (scanResult.highlights.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface, RoundedCornerShape(16.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication        = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) { showWhy = !showWhy }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text  = "Why this result",
                                    style = typography.cardTitle,
                                    color = colors.textPrimary,
                                )
                                Text(
                                    text  = if (showWhy) "▲" else "▼",
                                    style = typography.bodySmall,
                                    color = colors.textTertiary,
                                )
                            }
                            AnimatedVisibility(
                                visible = showWhy,
                                enter   = fadeIn() + expandVertically(),
                                exit    = fadeOut() + shrinkVertically(),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                                    Spacer(Modifier.height(4.dp))
                                    scanResult.highlights.forEach { bullet ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment     = Alignment.Top,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 5.dp)
                                                    .size(6.dp)
                                                    .background(toneColor.copy(alpha = 0.7f), CircleShape)
                                            )
                                            Text(
                                                text     = bullet,
                                                style    = typography.bodySmall,
                                                color    = colors.textPrimary,
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── 3. What to do ──────────────────────────────────────
                    if (!scanResult.recommendation.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(toneColor.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
                                .border(1.dp, toneColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment     = Alignment.Top,
                        ) {
                            Text("💡", style = typography.cardTitle)
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text  = "WHAT TO DO",
                                    style = typography.chipLabel,
                                    color = toneColor,
                                )
                                Text(
                                    text  = scanResult.recommendation,
                                    style = typography.bodySmall,
                                    color = colors.textPrimary,
                                )
                            }
                        }
                    }

                    // ── 4. Explain Simply — AI explanation card ────────────
                    if (aiExplainLoading) {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .background(colors.surface, RoundedCornerShape(16.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier  = Modifier.size(16.dp),
                                color     = accent,
                                strokeWidth = 2.dp,
                            )
                            Text(
                                text  = "Generating simple explanation…",
                                style = typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                    }

                    if (!aiExplanation.isNullOrBlank()) {
                        // Friendly AI explanation card — plain paragraph, no jargon
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(accent.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
                                .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector        = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint               = accent,
                                    modifier           = Modifier.size(14.dp),
                                )
                                Text(
                                    text       = "EXPLAIN SIMPLY",
                                    style      = typography.chipLabel,
                                    color      = accent,
                                )
                            }
                            Text(
                                text  = aiExplanation,
                                style = typography.bodySmall,
                                color = colors.textPrimary,
                            )
                        }
                    } else if (!aiExplainLoading) {
                        // "Explain Simply" tap button — shown when no explanation yet
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .background(colors.surface, RoundedCornerShape(14.dp))
                                .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .clickable(
                                    indication        = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { onExplain(scanResult) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector        = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint               = accent,
                                    modifier           = Modifier.size(14.dp),
                                )
                                Text(
                                    text       = "Explain Simply",
                                    style      = typography.chipLabel,
                                    color      = accent,
                                )
                            }
                        }
                    }

                    // ── 5. Show technical details (hidden by default) ──────
                    if (scanResult.technicalSignals.isNotEmpty() || scanResult.confidenceLabel != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface, RoundedCornerShape(14.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(14.dp)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication        = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) { showTech = !showTech }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text  = "Show technical details",
                                    style = typography.chipLabel,
                                    color = colors.textTertiary,
                                )
                                Text(
                                    text  = if (showTech) "Hide ↑"
                                            else "Show (${scanResult.technicalSignals.size}) ↓",
                                    style = typography.chipLabel,
                                    color = colors.textTertiary,
                                )
                            }
                            AnimatedVisibility(
                                visible = showTech,
                                enter   = fadeIn() + expandVertically(),
                                exit    = fadeOut() + shrinkVertically(),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                                    Spacer(Modifier.height(6.dp))
                                    // Trust level label
                                    scanResult.confidenceLabel?.let { label ->
                                        Row(
                                            modifier              = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text("Trust level", style = typography.bodySmall, color = colors.textTertiary)
                                            Text(
                                                text  = label,
                                                style = typography.chipLabel,
                                                color = when (label) {
                                                    "High"     -> colors.primaryBlue
                                                    "Moderate" -> colors.warningOrange
                                                    else       -> colors.textSecondary
                                                },
                                            )
                                        }
                                    }
                                    scanResult.technicalSignals.forEach { signal ->
                                        Text(
                                            text  = signal,
                                            style = typography.bodySmall,
                                            color = colors.textTertiary,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── 5. Actions ─────────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Scan Another
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .background(colors.surface, RoundedCornerShape(14.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                .clickable(
                                    indication        = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    uri = null; selectedMimeType = null
                                    rsState = RsState.IDLE; result = null
                                    errorMsg = null; showSignals = false
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Scan Another", style = typography.chipLabel, color = colors.textSecondary)
                        }
                        // Report (danger only)
                        if (tone == ScanRiskTone.DANGER) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .background(colors.dangerRed, RoundedCornerShape(14.dp))
                                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { /* report */ },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("Report Content", style = typography.chipLabel, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ── Error banner ───────────────────────────────────────────────────
        errorMsg?.let { ScanErrorBanner(message = localizedUiMessage(it)) }
        if (errorMsg.isUpgradeError()) {
            ScanPrimaryAction(
                text        = stringResource(R.string.scan_upgrade_plan),
                onClick     = { showUpgradeDialog = true },
                accentColor = accent,
            )
        }
    }
}

// ─── Entry point (used by RealityScanHubScreen) ─────────────────────────────────
@Composable
fun ImageRealityScanScreen(
    sharedPayload: SharedScanPayload? = null,
    onSharedPayloadConsumed: () -> Unit = {},
    onScan: (Uri, String, (RealityScanResult) -> Unit, (String) -> Unit) -> Unit,
    onExplain: (RealityScanResult) -> Unit = {},
    aiExplanation: String? = null,
    aiExplainLoading: Boolean = false,
    accent: Color = ScanTheme.colors.primaryBlue,
    onUpgradePlan: () -> Unit = {},
    isViewModelScanning: Boolean = false,
) = RealityScanScreen(
    sharedPayload           = sharedPayload,
    onSharedPayloadConsumed = onSharedPayloadConsumed,
    onScan                  = onScan,
    onExplain               = onExplain,
    aiExplanation           = aiExplanation,
    aiExplainLoading        = aiExplainLoading,
    accent                  = accent,
    onUpgradePlan           = onUpgradePlan,
    isViewModelScanning     = isViewModelScanning,
)

// ─── Private helpers ────────────────────────────────────────────────────────────
private fun resolveMimeType(
    context: android.content.Context,
    uri: Uri,
    fallback: String? = null,
): String {
    return context.contentResolver.getType(uri)
        ?.lowercase(Locale.ROOT)
        ?.takeIf { it.isNotBlank() }
        ?: fallback?.lowercase(Locale.ROOT).orEmpty()
}

private fun resolveDisplayName(
    context: android.content.Context,
    uri: Uri,
): String? {
    return context.contentResolver.query(
        uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
    )?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
    } ?: uri.lastPathSegment
}

private fun resolveExtension(
    context: android.content.Context,
    uri: Uri,
): String = resolveDisplayName(context, uri)
    ?.substringAfterLast('.', "")
    ?.lowercase(Locale.ROOT)
    .orEmpty()

/** Returns true for any error that should surface the upgrade dialog. */
private fun String?.isUpgradeError(): Boolean = when (this) {
    "error_scan_limit_reached_free",
    "error_scan_limit_reached_pro",
    "error_forbidden" -> true
    else              -> false
}

/** Maps an error code to the appropriate UpgradeTrigger copy variant. */
private fun String?.toUpgradeTrigger(): UpgradeTrigger = when (this) {
    "error_scan_limit_reached_free" -> UpgradeTrigger.ScanLimitFree
    "error_scan_limit_reached_pro"  -> UpgradeTrigger.ScanLimitPro
    "error_forbidden"               -> UpgradeTrigger.FeatureLocked
    else                            -> UpgradeTrigger.Generic
}
