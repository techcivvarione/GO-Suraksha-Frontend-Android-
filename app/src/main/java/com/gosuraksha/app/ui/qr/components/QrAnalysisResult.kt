package com.gosuraksha.app.ui.qr.components

// =============================================================================
// QrAnalysisResult.kt — Payment-first QR result screen
//
// UPI QR codes show: merchant name, amount, UPI ID, risk verdict, CTAs.
// Non-UPI QR codes show: domain/type info, risk verdict, recommended action.
// NO raw payload is ever displayed.
// =============================================================================

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.scan.components.ScanErrorBanner
import com.gosuraksha.app.scan.components.ScanRiskTone
import com.gosuraksha.app.scan.components.containerColor
import com.gosuraksha.app.scan.components.contentColor
import com.gosuraksha.app.scan.components.toScanRiskTone
import com.gosuraksha.app.scan.components.ScanPrimaryAction
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text          = "QR SECURITY SCAN",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = colors.accentQR,
                    letterSpacing = 1.sp,
                )
                Text(
                    text       = "Scan Result",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colors.textPrimary,
                )
            }
        }

        if (analysis != null) {
            val toneColor = tone.contentColor(colors)
            val isPayment = analysis.isPayment
            val canProceed = isPayment &&
                    tone != ScanRiskTone.DANGER &&
                    !rawPayload.isNullOrBlank()

            // ── UPI Payment summary card ───────────────────────────────────
            if (isPayment) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        colors.surface,
                                        colors.surface2,
                                    )
                                )
                            )
                            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // Requesting entity
                        Text(
                            text       = analysis.merchantName ?: analysis.upiId ?: "Unknown Merchant",
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = colors.textPrimary,
                            textAlign  = TextAlign.Center,
                        )

                        // Amount — largest text on screen
                        if (analysis.amount != null) {
                            Text(
                                text       = "is requesting ${formatRupees(analysis.amount)}",
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color      = toneColor,
                                textAlign  = TextAlign.Center,
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Risk verdict line
                        Text(
                            text      = analysis.summary
                                ?: if (tone == ScanRiskTone.SAFE) "This QR code is verified. No hidden or suspicious amount detected."
                                else "Proceed with caution — verify with the sender.",
                            fontSize  = 13.sp,
                            color     = colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(4.dp))

                        // Payment details card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.background, RoundedCornerShape(14.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text          = "PAYMENT DETAILS",
                                fontSize      = 10.sp,
                                fontWeight    = FontWeight.SemiBold,
                                color         = colors.textTertiary,
                                letterSpacing = 1.sp,
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
                // ── Non-UPI: strong verdict banner ────────────────────────
                item {
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
                                text     = when (tone) {
                                    ScanRiskTone.DANGER  -> "🚨"
                                    ScanRiskTone.WARNING -> "⚠️"
                                    ScanRiskTone.SAFE    -> "✅"
                                },
                                fontSize = 26.sp,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text       = when (tone) {
                                        ScanRiskTone.DANGER  -> "This QR code is dangerous"
                                        ScanRiskTone.WARNING -> "This QR code may be unsafe"
                                        ScanRiskTone.SAFE    -> "This QR code is safe"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 16.sp,
                                    color      = toneColor,
                                )
                                Text(
                                    text     = analysis.summary ?: when (tone) {
                                        ScanRiskTone.DANGER  -> "Do NOT open. Risk of fraud or malware."
                                        ScanRiskTone.WARNING -> "Verify the source before proceeding."
                                        ScanRiskTone.SAFE    -> "No known threats detected in this QR."
                                    },
                                    fontSize = 12.sp,
                                    color    = colors.textSecondary,
                                )
                            }
                        }
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
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text          = "🔍  QR DETAILS",
                                fontSize      = 11.sp,
                                fontWeight    = FontWeight.SemiBold,
                                color         = colors.textTertiary,
                                letterSpacing = 0.8.sp,
                            )
                            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
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
                                ) { showReasons = !showReasons }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text       = "Why this result",
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp,
                                color      = colors.textPrimary,
                            )
                            Text(
                                text  = if (showReasons) "▲" else "▼",
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
                                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                                Spacer(Modifier.height(4.dp))
                                analysis.reasons.forEach { reason ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment     = Alignment.Top,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 5.dp)
                                                .size(6.dp)
                                                .background(toneColor.copy(0.7f), RoundedCornerShape(3.dp))
                                        )
                                        Text(
                                            text     = reason,
                                            fontSize = 13.sp,
                                            color    = colors.textPrimary,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Recommendation ─────────────────────────────────────────────
            val recommendation = analysis.recommendedAction
            if (!recommendation.isNullOrBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(toneColor.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .border(1.dp, toneColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.Top,
                    ) {
                        Text("💡", fontSize = 16.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text          = "WHAT TO DO",
                                fontSize      = 10.sp,
                                fontWeight    = FontWeight.SemiBold,
                                color         = toneColor,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text     = recommendation,
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
                    if (canProceed && rawPayload != null) {
                        // Proceed to Payment — prominent gradient button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0062FF), Color(0xFF00C896))
                                    )
                                )
                                .clickable(
                                    indication        = null,
                                    interactionSource = remember { MutableInteractionSource() },
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

                    if (tone == ScanRiskTone.DANGER) {
                        // Report button for dangerous QR
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.dangerRed)
                                .clickable(
                                    indication        = null,
                                    interactionSource = remember { MutableInteractionSource() },
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

                    // Cancel / Scan Another — ghost button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() },
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
            // ── Non-success states: render each explicitly ─────────────────
            // IMPORTANT: only show the error banner when state IS actually an Error.
            // Showing it for Loading or Idle caused the "Something went wrong" flicker
            // that appeared briefly before the real result arrived.
            when (state.analysisState) {
                is QrAnalysisUiState.Loading -> {
                    // API call in flight — show a spinner, never an error banner
                    item {
                        Box(
                            modifier         = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color       = ScanTheme.colors.primaryBlue,
                                strokeWidth = 3.dp,
                            )
                        }
                    }
                }

                is QrAnalysisUiState.Error -> {
                    // Real API failure — show error banner + action button
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
                                accentColor = ScanTheme.colors.primaryBlue,
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ScanTheme.colors.surface)
                                    .border(1.dp, ScanTheme.colors.border, RoundedCornerShape(16.dp))
                                    .clickable(
                                        indication        = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) { onScanAnother() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("Try Again", fontSize = 14.sp, color = ScanTheme.colors.textSecondary)
                            }
                        }
                    }
                }

                else -> {
                    // Idle — should never reach here after the ViewModel fix,
                    // but render nothing rather than a misleading error banner.
                }
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
