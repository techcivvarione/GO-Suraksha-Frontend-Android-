package com.gosuraksha.app.scan.reality

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gosuraksha.app.R
import com.gosuraksha.app.scan.SharedScanPayload
import com.gosuraksha.app.scan.components.HeroRiskCard
import com.gosuraksha.app.scan.components.ScanErrorBanner
import com.gosuraksha.app.scan.components.ScanLoader
import com.gosuraksha.app.scan.components.ScanPrimaryAction
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
        val dropZoneShape = RoundedCornerShape(20.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (uri != null && rsState != RsState.SCANNING) 180.dp else 140.dp)
                .shadow(
                    elevation    = if (uri != null) 2.dp else 0.dp,
                    shape        = dropZoneShape,
                    ambientColor = accent.copy(alpha = 0.12f),
                )
                .clip(dropZoneShape)
                .background(colors.surface)
                .border(
                    width = if (uri != null) 1.5.dp else 1.dp,
                    color = if (uri != null) accent.copy(alpha = 0.35f) else colors.border,
                    shape = dropZoneShape,
                )
                .clickable(
                    enabled           = rsState != RsState.SCANNING,
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { if (uri == null) picker.launch("image/*") },
            contentAlignment = Alignment.Center,
        ) {
            // Image thumbnail
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
                        .clip(RoundedCornerShape(19.dp)),
                )
                // "Change" pill overlay — bottom center
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .background(colors.background.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(
                        text  = "Tap to change image",
                        style = typography.chipLabel,
                        color = colors.textSecondary,
                    )
                }
            }

            // Empty state or scanning overlay
            if (uri == null || rsState == RsState.SCANNING) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier            = Modifier.padding(horizontal = 24.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (uri != null) accent.copy(alpha = 0.12f)
                                else colors.surface2,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Image,
                            contentDescription = null,
                            tint               = if (uri != null) accent else colors.textTertiary,
                            modifier           = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = when {
                            rsState == RsState.SCANNING -> "Detection in progress…"
                            else                        -> "Tap to select an image"
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp,
                        color      = colors.textPrimary,
                        textAlign  = TextAlign.Center,
                    )
                    Text(
                        text      = "jpg · png · webp supported",
                        style     = typography.bodySmall,
                        color     = colors.textTertiary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Scanning spinner overlay
            if (rsState == RsState.SCANNING) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            colors.background.copy(alpha = 0.75f),
                            dropZoneShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    ScanLoader(label = "Analysing image…")
                }
            }
        }

        // ── Choose / Replace file ──────────────────────────────────────────
        val pickSrc  = remember { MutableInteractionSource() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(colors.surface, RoundedCornerShape(14.dp))
                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                .clickable(
                    enabled           = rsState != RsState.SCANNING,
                    indication        = null,
                    interactionSource = pickSrc,
                ) { picker.launch("image/*") }
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

        // ── Run Detection — spring press primary button ─────────────────────
        val runSrc     = remember { MutableInteractionSource() }
        val runPressed by runSrc.collectIsPressedAsState()
        val runScale   by animateFloatAsState(
            targetValue   = if (runPressed) 0.97f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.70f),
            label         = "run_btn_scale",
        )
        val runBtnColor = if (runEnabled) accent else colors.border
        val runBtnShape = RoundedCornerShape(18.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .graphicsLayer { scaleX = runScale; scaleY = runScale }
                .shadow(
                    elevation    = if (runEnabled && !runPressed) 6.dp else 0.dp,
                    shape        = runBtnShape,
                    ambientColor = runBtnColor.copy(alpha = 0.20f),
                    spotColor    = runBtnColor.copy(alpha = 0.28f),
                )
                .clip(runBtnShape)
                .background(runBtnColor)
                .clickable(
                    enabled           = runEnabled,
                    indication        = null,
                    interactionSource = runSrc,
                ) {
                    val currentUri = uri ?: return@clickable
                    val mime = resolveMimeType(context, currentUri, selectedMimeType)
                    rsState = RsState.SCANNING
                    onScan(
                        currentUri,
                        mime,
                        { scanResult ->
                            rsState  = RsState.DONE
                            result   = scanResult
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
                text       = if (rsState == RsState.SCANNING) "Scanning…" else "Run Detection",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = if (runEnabled) Color.White else colors.textTertiary,
            )
        }

        // ── Result card — verdict-first design ─────────────────────────────
        result?.let { scanResult ->
            val tone      = scanResult.riskLevel.toScanRiskTone()
            val toneColor = tone.contentColor(colors)
            var showWhy   by remember { mutableStateOf(false) }
            var showTech  by remember { mutableStateOf(false) }

            // Trust score = 100 - riskScore (how authentic/safe the image is)
            val trustScore = (100 - scanResult.riskScore).coerceIn(0, 100)
            val verdictTitle = when (tone) {
                ScanRiskTone.DANGER  -> "🚨 This image is very likely fake"
                ScanRiskTone.WARNING -> "⚠️ This image may have been edited"
                ScanRiskTone.SAFE    -> "✅ This image looks like a real photo"
            }
            val verdictSummary = scanResult.summary.takeIf { it.isNotBlank() } ?: when (tone) {
                ScanRiskTone.DANGER  -> "We found signs it could be edited or AI-generated."
                ScanRiskTone.WARNING -> "Some indicators suggest this image may have been modified."
                ScanRiskTone.SAFE    -> "No significant signs of manipulation were detected."
            }

            AnimatedVisibility(
                visible = true,
                enter   = fadeIn() + slideInVertically { it / 3 },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    // ── 1. Premium hero risk card ──────────────────────────
                    HeroRiskCard(
                        title   = verdictTitle,
                        summary = verdictSummary,
                        score   = trustScore,
                        tone    = tone,
                        label   = "Trust Score",
                    )

                    // ── 2. Why this result (expandable) ───────────────────
                    if (scanResult.highlights.isNotEmpty()) {
                        val expandShape = RoundedCornerShape(18.dp)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface, expandShape)
                                .border(1.dp, colors.border, expandShape),
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
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(toneColor.copy(alpha = 0.70f), CircleShape),
                                    )
                                    Text(
                                        text  = "Why this result",
                                        style = typography.cardTitle,
                                        color = colors.textPrimary,
                                    )
                                }
                                Text(
                                    text  = if (showWhy) "▲ Hide" else "▼ Show",
                                    style = typography.chipLabel,
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
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(colors.border),
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    scanResult.highlights.forEachIndexed { index, bullet ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment     = Alignment.Top,
                                        ) {
                                            // Numbered circle
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 1.dp)
                                                    .size(20.dp)
                                                    .background(toneColor.copy(alpha = 0.12f), CircleShape)
                                                    .border(1.dp, toneColor.copy(alpha = 0.20f), CircleShape),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text       = "${index + 1}",
                                                    fontSize   = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color      = toneColor,
                                                )
                                            }
                                            Text(
                                                text     = bullet,
                                                style    = typography.bodySmall,
                                                color    = colors.textPrimary,
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                        if (index < scanResult.highlights.lastIndex) {
                                            Spacer(Modifier.height(10.dp))
                                            Box(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(colors.border),
                                            )
                                            Spacer(Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── 3. What to do — left accent border ─────────────────
                    if (!scanResult.recommendation.isNullOrBlank()) {
                        val recShape = RoundedCornerShape(16.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .background(toneColor.copy(alpha = 0.06f), recShape)
                                .border(1.dp, toneColor.copy(alpha = 0.14f), recShape),
                        ) {
                            // Left accent bar fills Row height via IntrinsicSize.Min
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                                    .background(toneColor.copy(alpha = 0.60f)),
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
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

                    // ── 4. AI explanation ──────────────────────────────────
                    if (aiExplainLoading) {
                        ScanLoader(label = "Generating simple explanation…")
                    }

                    if (!aiExplanation.isNullOrBlank()) {
                        AnimatedVisibility(
                            visible = true,
                            enter   = fadeIn() + expandVertically(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(accent.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
                                    .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
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
                                        text  = "EXPLAIN SIMPLY",
                                        style = typography.chipLabel,
                                        color = accent,
                                    )
                                }
                                Text(
                                    text  = aiExplanation,
                                    style = typography.bodySmall,
                                    color = colors.textPrimary,
                                )
                            }
                        }
                    } else if (!aiExplainLoading) {
                        // "Explain Simply" tap button
                        val explainSrc     = remember { MutableInteractionSource() }
                        val explainPressed by explainSrc.collectIsPressedAsState()
                        val explainScale   by animateFloatAsState(
                            targetValue   = if (explainPressed) 0.97f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label         = "explain_scale",
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .graphicsLayer { scaleX = explainScale; scaleY = explainScale }
                                .background(colors.surface, RoundedCornerShape(14.dp))
                                .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
                                .clickable(
                                    indication        = null,
                                    interactionSource = explainSrc,
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
                                    text  = "Explain Simply",
                                    style = typography.chipLabel,
                                    color = accent,
                                )
                            }
                        }
                    }

                    // ── 5. Technical details (expandable) ─────────────────
                    if (scanResult.technicalSignals.isNotEmpty() || scanResult.confidenceLabel != null) {
                        val techShape = RoundedCornerShape(16.dp)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface, techShape)
                                .border(1.dp, colors.border, techShape),
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
                                    text  = "Technical details",
                                    style = typography.chipLabel,
                                    color = colors.textSecondary,
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
                                        .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(colors.border),
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    // Trust level label
                                    scanResult.confidenceLabel?.let { label ->
                                        Row(
                                            modifier              = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text  = "Trust level",
                                                style = typography.bodySmall,
                                                color = colors.textTertiary,
                                            )
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

                    // ── 6. Actions ─────────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Scan Another — secondary
                        val scanAnotherSrc  = remember { MutableInteractionSource() }
                        val scanAnotherPrs  by scanAnotherSrc.collectIsPressedAsState()
                        val scanAnotherScl  by animateFloatAsState(
                            targetValue   = if (scanAnotherPrs) 0.96f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label         = "scan_another_scale",
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .graphicsLayer { scaleX = scanAnotherScl; scaleY = scanAnotherScl }
                                .background(colors.surface, RoundedCornerShape(14.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                .clickable(
                                    indication        = null,
                                    interactionSource = scanAnotherSrc,
                                ) {
                                    uri = null; selectedMimeType = null
                                    rsState = RsState.IDLE; result = null
                                    errorMsg = null; showSignals = false
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text  = "Scan Another",
                                style = typography.chipLabel,
                                color = colors.textSecondary,
                            )
                        }

                        // Report — danger only
                        if (tone == ScanRiskTone.DANGER) {
                            val reportSrc  = remember { MutableInteractionSource() }
                            val reportPrs  by reportSrc.collectIsPressedAsState()
                            val reportScl  by animateFloatAsState(
                                targetValue   = if (reportPrs) 0.96f else 1f,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label         = "report_scale",
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .graphicsLayer { scaleX = reportScl; scaleY = reportScl }
                                    .shadow(
                                        elevation    = if (!reportPrs) 4.dp else 0.dp,
                                        shape        = RoundedCornerShape(14.dp),
                                        ambientColor = colors.dangerRed.copy(alpha = 0.20f),
                                        spotColor    = colors.dangerRed.copy(alpha = 0.25f),
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.dangerRed)
                                    .clickable(
                                        indication        = null,
                                        interactionSource = reportSrc,
                                    ) { /* report */ },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text  = "Report Content",
                                    style = typography.chipLabel,
                                    color = Color.White,
                                )
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
