package com.gosuraksha.app.ui.qr.components

// =============================================================================
// QrAnalysisResult.kt — Payment-first QR result screen
//
// UPI QR codes show: merchant name, amount, UPI ID, risk verdict, CTAs.
// Non-UPI QR codes show: domain/type info, risk verdict, recommended action.
// NO raw payload is ever displayed.
//
// Design: Premium fintech cards, spring-press buttons, left-accent recommendation,
//         numbered evidence, colored header icon box, light/dark adaptive.
// =============================================================================

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.scan.components.ScanErrorBanner
import com.gosuraksha.app.scan.components.ScanPrimaryAction
import com.gosuraksha.app.scan.components.ScanRiskTone
import com.gosuraksha.app.scan.components.containerColor
import com.gosuraksha.app.scan.components.contentColor
import com.gosuraksha.app.scan.components.toScanRiskTone
import com.gosuraksha.app.scan.design.ScanTheme
import com.gosuraksha.app.scan.qr.QrAnalysisUiState
import com.gosuraksha.app.scan.qr.QrUiState
import com.gosuraksha.app.ui.components.UpgradeInterceptDialog
import com.gosuraksha.app.ui.components.UpgradeTrigger
import com.gosuraksha.app.ui.components.localizedUiMessage
import java.text.NumberFormat
import java.util.Locale

// ─── Number formatting ────────────────────────────────────────────────────────
private fun formatRupees(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
    nf.minimumFractionDigits = 0
    nf.maximumFractionDigits = 2
    return "₹${nf.format(amount)}"
}

@Composable
fun QrAnalysisResult(
    state:            QrUiState,
    onReport:         () -> Unit,
    onProceedPayment: (String) -> Unit,
    onScanAnother:    () -> Unit,
    onUpgradePlan:    () -> Unit = {},
) {
    val analysis   = (state.analysisState as? QrAnalysisUiState.Success)?.data
    val rawPayload = state.parsedQr?.rawPayload
    val tone       = analysis?.riskLevel?.toScanRiskTone() ?: ScanRiskTone.WARNING
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography
    val accent     = colors.accentQR

    val errorMessage = (state.analysisState as? QrAnalysisUiState.Error)?.message

    var showUpgradeDialog by remember { mutableStateOf(false) }
    val upgradeTrigger    = remember(errorMessage) { errorMessage.toUpgradeTrigger() }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isUpgradeError()) showUpgradeDialog = true
    }

    UpgradeInterceptDialog(
        visible       = showUpgradeDialog,
        trigger       = upgradeTrigger,
        onDismiss     = { showUpgradeDialog = false },
        onSelectPro   = { showUpgradeDialog = false; onUpgradePlan() },
        onSelectUltra = { showUpgradeDialog = false; onUpgradePlan() },
    )

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(colors.background),
        contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {

        // ── Page header ────────────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                // QR icon box
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation    = 4.dp,
                            shape        = RoundedCornerShape(15.dp),
                            ambientColor = accent.copy(alpha = 0.15f),
                            spotColor    = accent.copy(alpha = 0.20f),
                        )
                        .size(52.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(accent.copy(alpha = 0.12f))
                        .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                        tint               = accent,
                        modifier           = Modifier.size(24.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .background(accent.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text          = "QR SECURITY SCAN",
                            style         = typography.chipLabel,
                            color         = accent,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "Scan Result",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 22.sp,
                        color      = colors.textPrimary,
                    )
                }
            }
        }

        if (analysis != null) {
            val toneColor  = tone.contentColor(colors)
            val isPayment  = analysis.isPayment
            val canProceed = isPayment &&
                    tone != ScanRiskTone.DANGER &&
                    !rawPayload.isNullOrBlank()

            // ── UPI Payment summary card ───────────────────────────────────
            if (isPayment) {
                item {
                    val cardShape = RoundedCornerShape(22.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation    = 2.dp,
                                shape        = cardShape,
                                ambientColor = toneColor.copy(alpha = 0.10f),
                            )
                            .clip(cardShape)
                            .background(colors.surface)
                            .border(1.dp, toneColor.copy(alpha = 0.18f), cardShape)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // Verdict indicator dot row
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(toneColor, CircleShape),
                            )
                            Text(
                                text  = when (tone) {
                                    ScanRiskTone.SAFE    -> "Verified Safe"
                                    ScanRiskTone.WARNING -> "Proceed with Caution"
                                    ScanRiskTone.DANGER  -> "High Risk — Do Not Pay"
                                },
                                style = typography.chipLabel,
                                color = toneColor,
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Requesting entity
                        Text(
                            text       = analysis.merchantName ?: analysis.upiId ?: "Unknown Merchant",
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = colors.textPrimary,
                            textAlign  = TextAlign.Center,
                        )

                        // Amount
                        if (analysis.amount != null) {
                            Text(
                                text       = "is requesting ${formatRupees(analysis.amount)}",
                                fontSize   = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color      = toneColor,
                                textAlign  = TextAlign.Center,
                            )
                        }

                        // Summary
                        Text(
                            text      = analysis.summary
                                ?: if (tone == ScanRiskTone.SAFE) "This QR code is verified. No hidden or suspicious amount detected."
                                else "Proceed with caution — verify with the sender.",
                            fontSize  = 12.sp,
                            color     = colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(4.dp))

                        // Payment details sub-card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface2, RoundedCornerShape(14.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(colors.textTertiary, CircleShape),
                                )
                                Text(
                                    text          = "PAYMENT DETAILS",
                                    style         = typography.chipLabel,
                                    color         = colors.textTertiary,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(colors.border),
                            )
                            if (!analysis.merchantName.isNullOrBlank()) {
                                PaymentDetailRow("Requested by", analysis.merchantName, colors.textPrimary, colors)
                            }
                            if (!analysis.upiId.isNullOrBlank()) {
                                PaymentDetailRow("UPI ID", analysis.upiId, colors.textPrimary, colors)
                            }
                            if (analysis.amount != null) {
                                PaymentDetailRow("Amount", formatRupees(analysis.amount), toneColor, colors)
                            }
                            PaymentDetailRow(
                                label = "Security Check",
                                value = when (tone) {
                                    ScanRiskTone.SAFE    -> "✅ Verified"
                                    ScanRiskTone.WARNING -> "⚠️ Check carefully"
                                    ScanRiskTone.DANGER  -> "🚨 High risk"
                                },
                                valueColor = toneColor,
                                colors = colors,
                            )
                        }
                    }
                }
            } else {
                // ── Non-UPI: premium verdict card ─────────────────────────
                item {
                    val verdictShape = RoundedCornerShape(20.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation    = 2.dp,
                                shape        = verdictShape,
                                ambientColor = toneColor.copy(alpha = 0.10f),
                            )
                            .clip(verdictShape)
                            .background(tone.containerColor(colors))
                            .border(1.dp, toneColor.copy(alpha = 0.22f), verdictShape)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Icon circle + verdict badge row
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(toneColor.copy(alpha = 0.18f), CircleShape)
                                    .border(1.dp, toneColor.copy(alpha = 0.30f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = when (tone) {
                                        ScanRiskTone.DANGER  -> "🚨"
                                        ScanRiskTone.WARNING -> "⚠️"
                                        ScanRiskTone.SAFE    -> "✅"
                                    },
                                    fontSize = 20.sp,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(toneColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .border(1.dp, toneColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text  = when (tone) {
                                        ScanRiskTone.DANGER  -> "HIGH RISK"
                                        ScanRiskTone.WARNING -> "MODERATE RISK"
                                        ScanRiskTone.SAFE    -> "ALL CLEAR"
                                    },
                                    style = typography.chipLabel,
                                    color = toneColor,
                                )
                            }
                        }

                        Text(
                            text       = when (tone) {
                                ScanRiskTone.DANGER  -> "This QR code is dangerous"
                                ScanRiskTone.WARNING -> "This QR code may be unsafe"
                                ScanRiskTone.SAFE    -> "This QR code is safe"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp,
                            color      = colors.textPrimary,
                        )
                        Text(
                            text  = analysis.summary ?: when (tone) {
                                ScanRiskTone.DANGER  -> "Do NOT open. Risk of fraud or malware."
                                ScanRiskTone.WARNING -> "Verify the source before proceeding."
                                ScanRiskTone.SAFE    -> "No known threats detected in this QR."
                            },
                            style = typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }

                // QR type + domain details card (non-UPI)
                val domain = rawPayload?.let { extractQrDomain(it) }
                if (!domain.isNullOrBlank() || !analysis.detectedType.isNullOrBlank()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface, RoundedCornerShape(16.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(colors.textTertiary, CircleShape),
                                )
                                Text(
                                    text  = "QR DETAILS",
                                    style = typography.chipLabel,
                                    color = colors.textTertiary,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(colors.border),
                            )
                            if (!analysis.detectedType.isNullOrBlank()) {
                                PaymentDetailRow(
                                    "Type",
                                    humanQrType(analysis.detectedType.uppercase()),
                                    colors.textPrimary,
                                    colors,
                                )
                            }
                            if (!domain.isNullOrBlank()) {
                                PaymentDetailRow("Domain", domain, colors.textPrimary, colors)
                                val flag = qrDomainFlag(domain, tone)
                                if (flag != null) {
                                    PaymentDetailRow("Status", flag, toneColor, colors)
                                }
                            }
                        }
                    }
                }
            }

            // ── Expandable risk details ────────────────────────────────────
            if (analysis.reasons.isNotEmpty() && tone != ScanRiskTone.SAFE) {
                item {
                    var showReasons by remember { mutableStateOf(false) }
                    val toneColor2 = tone.contentColor(colors)
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
                                ) { showReasons = !showReasons }
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
                                        .background(toneColor2.copy(alpha = 0.7f), CircleShape),
                                )
                                Text(
                                    text       = "Why this result",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 14.sp,
                                    color      = colors.textPrimary,
                                )
                            }
                            Text(
                                text  = if (showReasons) "▲ Hide" else "▼ Show",
                                style = typography.chipLabel,
                                color = colors.textTertiary,
                            )
                        }
                        AnimatedVisibility(
                            visible = showReasons,
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
                                analysis.reasons.forEachIndexed { index, reason ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment     = Alignment.Top,
                                    ) {
                                        // Numbered circle
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 1.dp)
                                                .size(20.dp)
                                                .background(toneColor2.copy(alpha = 0.12f), CircleShape)
                                                .border(1.dp, toneColor2.copy(alpha = 0.20f), CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text       = "${index + 1}",
                                                fontSize   = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color      = toneColor2,
                                            )
                                        }
                                        Text(
                                            text     = reason,
                                            fontSize = 13.sp,
                                            color    = colors.textPrimary,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    if (index < analysis.reasons.lastIndex) {
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
            }

            // ── Recommendation — left accent border ────────────────────────
            val recommendation = analysis.recommendedAction
            if (!recommendation.isNullOrBlank()) {
                item {
                    val toneColor2 = tone.contentColor(colors)
                    val recShape  = RoundedCornerShape(16.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .background(toneColor2.copy(alpha = 0.06f), recShape)
                            .border(1.dp, toneColor2.copy(alpha = 0.14f), recShape),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                                .background(toneColor2.copy(alpha = 0.60f)),
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text  = "WHAT TO DO",
                                style = typography.chipLabel,
                                color = toneColor2,
                            )
                            Text(
                                text  = recommendation,
                                fontSize = 13.sp,
                                color    = colors.textPrimary,
                            )
                        }
                    }
                }
            }

            // ── Action buttons ─────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Proceed to Payment — spring press button
                    if (canProceed && rawPayload != null) {
                        val proceedSrc  = remember { MutableInteractionSource() }
                        val proceedPrs  by proceedSrc.collectIsPressedAsState()
                        val proceedScl  by animateFloatAsState(
                            targetValue   = if (proceedPrs) 0.97f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.70f),
                            label         = "proceed_scale",
                        )
                        val proceedShape = RoundedCornerShape(18.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .graphicsLayer { scaleX = proceedScl; scaleY = proceedScl }
                                .shadow(
                                    elevation    = if (!proceedPrs) 6.dp else 0.dp,
                                    shape        = proceedShape,
                                    ambientColor = accent.copy(alpha = 0.22f),
                                    spotColor    = accent.copy(alpha = 0.30f),
                                )
                                .clip(proceedShape)
                                .background(accent)
                                .clickable(
                                    indication        = null,
                                    interactionSource = proceedSrc,
                                ) { onProceedPayment(rawPayload) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text       = "Proceed to Payment",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp,
                                color      = Color.White,
                            )
                        }
                    }

                    // Report button — danger only
                    if (tone == ScanRiskTone.DANGER) {
                        val reportSrc  = remember { MutableInteractionSource() }
                        val reportPrs  by reportSrc.collectIsPressedAsState()
                        val reportScl  by animateFloatAsState(
                            targetValue   = if (reportPrs) 0.97f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label         = "report_scale",
                        )
                        val reportShape = RoundedCornerShape(16.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .graphicsLayer { scaleX = reportScl; scaleY = reportScl }
                                .shadow(
                                    elevation    = if (!reportPrs) 4.dp else 0.dp,
                                    shape        = reportShape,
                                    ambientColor = colors.dangerRed.copy(alpha = 0.22f),
                                )
                                .clip(reportShape)
                                .background(colors.dangerRed)
                                .clickable(
                                    indication        = null,
                                    interactionSource = reportSrc,
                                ) { onReport() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text       = "🚨  Report This QR",
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp,
                                color      = Color.White,
                            )
                        }
                    }

                    // Scan Another / Cancel — ghost button with spring press
                    val scanSrc  = remember { MutableInteractionSource() }
                    val scanPrs  by scanSrc.collectIsPressedAsState()
                    val scanScl  by animateFloatAsState(
                        targetValue   = if (scanPrs) 0.97f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label         = "scan_another_scale",
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .graphicsLayer { scaleX = scanScl; scaleY = scanScl }
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                            .clickable(
                                indication        = null,
                                interactionSource = scanSrc,
                            ) { onScanAnother() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = if (isPayment) "Cancel" else "Scan Another",
                            fontSize   = 14.sp,
                            color      = colors.textSecondary,
                        )
                    }
                }
            }

        } else {
            // ── Non-success states ─────────────────────────────────────────
            when (state.analysisState) {
                is QrAnalysisUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface, RoundedCornerShape(20.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                CircularProgressIndicator(
                                    color       = ScanTheme.colors.accentQR,
                                    strokeWidth = 2.dp,
                                    modifier    = Modifier.size(28.dp),
                                )
                                Text(
                                    text  = "Analyzing QR code…",
                                    style = ScanTheme.typography.bodySmall,
                                    color = ScanTheme.colors.textSecondary,
                                )
                            }
                        }
                    }
                }

                is QrAnalysisUiState.Error -> {
                    item {
                        ScanErrorBanner(
                            message = localizedUiMessage(errorMessage ?: "error_generic")
                        )
                    }
                    if (errorMessage.isUpgradeError()) {
                        item {
                            ScanPrimaryAction(
                                text        = "Upgrade Plan",
                                onClick     = { showUpgradeDialog = true },
                                accentColor = ScanTheme.colors.accentQR,
                            )
                        }
                    } else {
                        item {
                            val trySrc  = remember { MutableInteractionSource() }
                            val tryPrs  by trySrc.collectIsPressedAsState()
                            val tryScl  by animateFloatAsState(
                                targetValue   = if (tryPrs) 0.97f else 1f,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label         = "try_again_scale",
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .graphicsLayer { scaleX = tryScl; scaleY = tryScl }
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ScanTheme.colors.surface)
                                    .border(1.dp, ScanTheme.colors.border, RoundedCornerShape(16.dp))
                                    .clickable(
                                        indication        = null,
                                        interactionSource = trySrc,
                                    ) { onScanAnother() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text  = "Try Again",
                                    fontSize = 14.sp,
                                    color = ScanTheme.colors.textSecondary,
                                )
                            }
                        }
                    }
                }

                else -> { /* Idle — render nothing */ }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

// ─── Upgrade-error helpers ────────────────────────────────────────────────────
private fun String?.isUpgradeError(): Boolean = when (this) {
    "error_scan_limit_reached_free",
    "error_scan_limit_reached_pro",
    "error_forbidden" -> true
    else              -> false
}

private fun String?.toUpgradeTrigger(): UpgradeTrigger = when (this) {
    "error_scan_limit_reached_free" -> UpgradeTrigger.ScanLimitFree
    "error_scan_limit_reached_pro"  -> UpgradeTrigger.ScanLimitPro
    "error_forbidden"               -> UpgradeTrigger.FeatureLocked
    else                            -> UpgradeTrigger.Generic
}

// ─── Shared detail row ────────────────────────────────────────────────────────
@Composable
private fun PaymentDetailRow(
    label:      String,
    value:      String,
    valueColor: Color,
    colors:     com.gosuraksha.app.scan.design.ScanColors,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = colors.textSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

// ─── Domain helpers ───────────────────────────────────────────────────────────
private fun extractQrDomain(payload: String): String? {
    if (!payload.startsWith("http", ignoreCase = true) &&
        !payload.startsWith("www.", ignoreCase = true)
    ) return null
    return try {
        val withScheme = if (payload.startsWith("www.", ignoreCase = true)) "https://$payload" else payload
        java.net.URI(withScheme.trim()).host?.removePrefix("www.")?.lowercase()
    } catch (_: Exception) { null }
}

private fun humanQrType(raw: String) = when {
    raw.contains("UPI")     -> "Payment (UPI)"
    raw.contains("URL")     -> "Website / Link"
    raw.contains("EMAIL")   -> "Email"
    raw.contains("PHONE")   -> "Phone Number"
    raw.contains("WIFI")    -> "Wi-Fi Network"
    raw.contains("TEXT")    -> "Plain Text"
    raw.contains("CONTACT") -> "Contact Card"
    else                    -> "Unknown"
}

private val qrShorteners = setOf(
    "bit.ly", "tinyurl.com", "goo.gl", "t.co", "ow.ly", "short.link", "cutt.ly", "rb.gy",
)

private fun qrDomainFlag(domain: String, tone: ScanRiskTone): String? = when {
    qrShorteners.any { domain.equals(it, ignoreCase = true) } ->
        "⚠️ Shortened — real destination hidden"
    tone == ScanRiskTone.DANGER  -> "🚨 Not official — possible fraud"
    tone == ScanRiskTone.WARNING -> "⚠️ Verify with official source"
    else                         -> null
}
