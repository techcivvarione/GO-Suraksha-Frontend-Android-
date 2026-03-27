package com.gosuraksha.app.scan.text

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gosuraksha.app.R
import com.gosuraksha.app.presentation.state.UiState
import com.gosuraksha.app.scan.components.ScanErrorBanner
import com.gosuraksha.app.scan.components.ScanInputField
import com.gosuraksha.app.scan.components.ScanLoader
import com.gosuraksha.app.scan.components.ScanPrimaryAction
import com.gosuraksha.app.scan.components.ScanResultCard
import com.gosuraksha.app.scan.components.ScanRiskTone
import com.gosuraksha.app.scan.components.containerColor
import com.gosuraksha.app.scan.components.contentColor
import com.gosuraksha.app.scan.components.toScanRiskTone
import com.gosuraksha.app.scan.core.ScanCategory
import com.gosuraksha.app.scan.core.TextScanViewModel
import com.gosuraksha.app.scan.design.GoSurakshaScanTheme
import com.gosuraksha.app.scan.design.ScanTheme
import com.gosuraksha.app.ui.components.UpgradeInterceptDialog
import com.gosuraksha.app.ui.components.UpgradeTrigger
import com.gosuraksha.app.ui.components.localizedUiMessage
import kotlinx.coroutines.delay

// ─── Category accent helper ───────────────────────────────────────────────────
// Threat = primaryBlue, Email = accentEmail (purple), Password = accentPassword (amber)
@Composable
private fun categoryAccent(category: ScanCategory): Color {
    val c = ScanTheme.colors
    return when (category) {
        ScanCategory.EMAIL    -> c.accentEmail
        ScanCategory.PASSWORD -> c.accentPassword
        else                  -> c.primaryBlue
    }
}

@Composable
fun TextScanScreen(
    title: String,
    placeholder: String,
    category: ScanCategory,
    onAnalyze: (String) -> Unit,
    viewModel: TextScanViewModel,
    onUpgradePlan: () -> Unit = {},
) {
    val state            by viewModel.state.collectAsStateWithLifecycle()
    val aiExplanation    by viewModel.aiExplanation.collectAsStateWithLifecycle()
    val aiExplainLoading by viewModel.aiExplainLoading.collectAsStateWithLifecycle()

    val result    = (state as? UiState.Success)?.data
    val error     = (state as? UiState.Error)?.message
    val isLoading = state is UiState.Loading
    val tone      = result?.risk?.toScanRiskTone() ?: ScanRiskTone.SAFE

    var showUpgradeDialog by remember { mutableStateOf(false) }
    val upgradeTrigger    = remember(error) { error.toUpgradeTrigger() }

    // Auto-show upgrade dialog whenever a plan-limit or feature-gated error surfaces
    LaunchedEffect(error) {
        if (error.isUpgradeError()) showUpgradeDialog = true
    }

    var input by rememberSaveable(category.name) { mutableStateOf("") }
    val canAnalyze by remember(input, isLoading) {
        derivedStateOf { input.isNotBlank() && !isLoading }
    }

    LaunchedEffect(category) { input = "" }

    // ── Staggered entrance animation state ─────────────────────────────────
    var headerVisible by remember { mutableStateOf(false) }
    var chipsVisible  by remember { mutableStateOf(false) }
    var inputVisible  by remember { mutableStateOf(false) }
    var tipVisible    by remember { mutableStateOf(false) }
    var ctaVisible    by remember { mutableStateOf(false) }

    LaunchedEffect(category) {
        headerVisible = false; chipsVisible = false
        inputVisible  = false; tipVisible   = false; ctaVisible = false
        delay(30)
        headerVisible = true
        delay(70)
        chipsVisible  = true
        delay(70)
        inputVisible  = true
        delay(60)
        tipVisible    = true
        delay(50)
        ctaVisible    = true
    }

    // darkTheme defaults to ColorTokens.LocalAppDarkMode via GoSurakshaScanTheme
    GoSurakshaScanTheme {
        val colors     = ScanTheme.colors
        val typography = ScanTheme.typography
        val accent     = categoryAccent(category)

        // ── Upgrade dialog — rendered as an overlay Dialog ─────────────────
        UpgradeInterceptDialog(
            visible       = showUpgradeDialog,
            trigger       = upgradeTrigger,
            onDismiss     = { showUpgradeDialog = false },
            onSelectPro   = { showUpgradeDialog = false; onUpgradePlan() },
            onSelectUltra = { showUpgradeDialog = false; onUpgradePlan() },
        )

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── 1. Header ──────────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = headerVisible,
                    enter   = fadeIn() + slideInVertically { -it / 3 },
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        // Colored icon box matching category accent
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation    = 4.dp,
                                    shape        = RoundedCornerShape(16.dp),
                                    ambientColor = accent.copy(alpha = 0.15f),
                                    spotColor    = accent.copy(alpha = 0.20f),
                                )
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(accent.copy(alpha = 0.12f))
                                .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = when (category) {
                                    ScanCategory.EMAIL    -> Icons.Outlined.Email
                                    ScanCategory.PASSWORD -> Icons.Outlined.Lock
                                    else                  -> Icons.Outlined.Shield
                                },
                                contentDescription = null,
                                tint     = accent,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Column(
                            modifier            = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            // Category label pill
                            Box(
                                modifier = Modifier
                                    .background(accent.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 9.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = when (category) {
                                        ScanCategory.THREAT   -> "MESSAGE SCAN"
                                        ScanCategory.EMAIL    -> "EMAIL BREACH CHECK"
                                        ScanCategory.PASSWORD -> "PASSWORD CHECK"
                                        else                  -> "SCAN"
                                    },
                                    style = typography.chipLabel,
                                    color = accent,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = when (category) {
                                    ScanCategory.THREAT   -> "Check Message or Link"
                                    ScanCategory.EMAIL    -> "Email Breach Check"
                                    ScanCategory.PASSWORD -> "Password Check"
                                    else                  -> title
                                },
                                style = typography.sectionHeading,
                                color = colors.textPrimary,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = when (category) {
                                    ScanCategory.THREAT   -> "Paste any suspicious message, link, or forwarded text to check if it's safe."
                                    ScanCategory.EMAIL    -> "Check if your email has appeared in any known data breaches."
                                    ScanCategory.PASSWORD -> "See how strong your password is and if it has been leaked."
                                    else                  -> "Scan content for threats."
                                },
                                style = typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            }

            // ── 2. Context chips — Threat only ─────────────────────────────
            if (category == ScanCategory.THREAT) {
                item {
                    AnimatedVisibility(
                        visible = chipsVisible,
                        enter   = fadeIn() + slideInVertically { it / 4 },
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Forwarded message", "Phishing link", "OTP scam").forEach { chipLabel ->
                                Box(
                                    modifier = Modifier
                                        .background(accent.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                                        .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text  = chipLabel,
                                        style = typography.chipLabel,
                                        color = accent,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. Input field ─────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = inputVisible,
                    enter   = fadeIn() + slideInVertically { it / 4 },
                ) {
                    ScanInputField(
                        value         = input,
                        onValueChange = { input = it },
                        label         = when (category) {
                            ScanCategory.EMAIL    -> "Email Address"
                            ScanCategory.PASSWORD -> "Password"
                            else                  -> "Message or URL"
                        },
                        placeholder   = placeholder,
                        minLines      = if (category == ScanCategory.THREAT) 4 else 1,
                        maxLines      = if (category == ScanCategory.THREAT) 8 else 1,
                        keyboardType  = when (category) {
                            ScanCategory.EMAIL    -> KeyboardType.Email
                            ScanCategory.PASSWORD -> KeyboardType.Password
                            else                  -> KeyboardType.Text
                        },
                    )
                }
            }

            // ── 4. Info tip — Email & Password only ────────────────────────
            if (category == ScanCategory.EMAIL || category == ScanCategory.PASSWORD) {
                item {
                    AnimatedVisibility(
                        visible = tipVisible,
                        enter   = fadeIn() + slideInVertically { it / 4 },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(accent.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                                .border(1.dp, accent.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment     = Alignment.Top,
                        ) {
                            Icon(
                                imageVector = if (category == ScanCategory.PASSWORD)
                                    Icons.Outlined.Lock else Icons.Outlined.Shield,
                                contentDescription = null,
                                tint     = accent,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(15.dp),
                            )
                            Text(
                                text  = when (category) {
                                    ScanCategory.EMAIL    -> "We check against 15B+ leaked credentials. Your email is never stored."
                                    ScanCategory.PASSWORD -> "Your password is analyzed locally and never sent to any server."
                                    else                  -> ""
                                },
                                style    = typography.bodySmall,
                                color    = colors.textSecondary,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            // ── 5. CTA button ──────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = ctaVisible,
                    enter   = fadeIn() + slideInVertically { it / 4 },
                ) {
                    ScanPrimaryAction(
                        text    = if (isLoading) "Checking…" else when (category) {
                            ScanCategory.EMAIL    -> "Check My Email"
                            ScanCategory.PASSWORD -> "Check Password"
                            else                  -> "Check for Threats"
                        },
                        onClick     = { onAnalyze(input) },
                        enabled     = canAnalyze,
                        accentColor = accent,
                    )
                }
            }

            // ── 6. Loader ──────────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = isLoading,
                    enter   = fadeIn() + slideInVertically { it / 2 },
                    exit    = fadeOut() + slideOutVertically { it / 2 },
                ) {
                    ScanLoader(
                        label = when (category) {
                            ScanCategory.THREAT   -> "Scanning threat indicators…"
                            ScanCategory.EMAIL    -> "Checking breach databases…"
                            ScanCategory.PASSWORD -> "Calculating strength & exposure…"
                            else                  -> "Analyzing…"
                        }
                    )
                }
            }

            // ── 7. Results ─────────────────────────────────────────────────
            if (result != null) {
                item {
                    var showBreachDialog by remember { mutableStateOf(false) }

                    AnimatedVisibility(
                        visible = true,
                        enter   = fadeIn() + slideInVertically { it / 3 },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                            // ── Email: breach count hero card ──────────────
                            if (category == ScanCategory.EMAIL && (result.breachCount ?: 0) > 0) {
                                val breachCount = result.breachCount ?: result.breaches?.size ?: 0

                                // ── Email breach hero card ─────────────────
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(tone.containerColor(colors), RoundedCornerShape(20.dp))
                                        .border(1.dp, tone.contentColor(colors).copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    // Verdict row
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(tone.contentColor(colors).copy(alpha = 0.18f), CircleShape)
                                                .border(1.dp, tone.contentColor(colors).copy(alpha = 0.30f), CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text("🚨", fontSize = 20.sp)
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text       = "Data Exposed",
                                                fontWeight = FontWeight.Bold,
                                                fontSize   = 16.sp,
                                                color      = tone.contentColor(colors),
                                            )
                                            Text(
                                                text     = "Change your passwords immediately.",
                                                fontSize = 12.sp,
                                                color    = colors.textSecondary,
                                            )
                                        }
                                    }

                                    // Divider
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(tone.contentColor(colors).copy(alpha = 0.12f)),
                                    )

                                    // Hero count
                                    Row(
                                        modifier          = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            Text(
                                                text       = "$breachCount",
                                                fontSize   = 52.sp,
                                                fontWeight = FontWeight.Black,
                                                color      = tone.contentColor(colors),
                                            )
                                            Text(
                                                text       = "data breach${if (breachCount > 1) "es" else ""} found",
                                                fontSize   = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color      = colors.textSecondary,
                                            )
                                        }
                                    }

                                    Text(
                                        text  = result.summary
                                            ?: "Your email address has been exposed in known data breaches. Change your passwords immediately.",
                                        fontSize = 13.sp,
                                        color    = colors.textSecondary,
                                    )

                                    // View affected sites button
                                    if (!result.breaches.isNullOrEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .background(accent, RoundedCornerShape(14.dp))
                                                .clickable(
                                                    indication        = null,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                ) { showBreachDialog = true },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text       = "View affected sites (${result.breaches.size})",
                                                fontSize   = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color      = Color.White,
                                            )
                                        }
                                    }
                                }
                            } else {
                                // ── Threat / Password verdict card ─────────
                                val verdictEmoji = when {
                                    category == ScanCategory.PASSWORD -> when (tone) {
                                        ScanRiskTone.DANGER  -> "🔴"
                                        ScanRiskTone.WARNING -> "🟡"
                                        ScanRiskTone.SAFE    -> "🟢"
                                    }
                                    else -> when (tone) {
                                        ScanRiskTone.DANGER  -> "🚨"
                                        ScanRiskTone.WARNING -> "⚠️"
                                        ScanRiskTone.SAFE    -> "✅"
                                    }
                                }
                                val verdictText = when {
                                    category == ScanCategory.THREAT -> when (tone) {
                                        ScanRiskTone.DANGER  -> "This message is likely a SCAM"
                                        ScanRiskTone.WARNING -> "This message looks suspicious"
                                        ScanRiskTone.SAFE    -> "This message looks safe"
                                    }
                                    category == ScanCategory.PASSWORD -> when (tone) {
                                        ScanRiskTone.DANGER  -> "This password is NOT safe"
                                        ScanRiskTone.WARNING -> "This password can be improved"
                                        ScanRiskTone.SAFE    -> "This password is strong"
                                    }
                                    else -> when (tone) {
                                        ScanRiskTone.DANGER  -> "High risk detected"
                                        ScanRiskTone.WARNING -> "Some risk detected"
                                        ScanRiskTone.SAFE    -> "Looks safe"
                                    }
                                }
                                ScanResultCard(
                                    title   = "$verdictEmoji $verdictText",
                                    summary = result.summary
                                        ?: "Review the evidence below before taking action.",
                                    tone    = tone,
                                    score   = result.score,
                                    evidence = result.highlights.ifEmpty { result.reasons },
                                    recommendation       = result.recommendation,
                                    confidenceLabel      = when (category) {
                                        ScanCategory.EMAIL    -> "Exposure Score"
                                        ScanCategory.PASSWORD -> "Strength Score"
                                        else                  -> "Risk Score"
                                    },
                                    accentColor          = accent,
                                    primaryActionLabel   = "Get AI explanation",
                                    onPrimaryAction      = {
                                        viewModel.loadAiExplanation(
                                            buildString {
                                                append(result.highlights.ifEmpty { result.reasons }.joinToString())
                                                if (!result.recommendation.isNullOrBlank()) {
                                                    append(". ${result.recommendation}")
                                                }
                                            }
                                        )
                                    },
                                    secondaryActionLabel = "Check again",
                                    onSecondaryAction    = { onAnalyze(input) },
                                )
                            }

                            // ── Threat: domain intelligence card ──────────
                            if (category == ScanCategory.THREAT) {
                                val domain = remember(input) { extractFirstDomain(input) }
                                if (!domain.isNullOrBlank()) {
                                    val isShortener = isShortenerDomain(domain)
                                    val domainRisk  = if (isShortener || tone == ScanRiskTone.DANGER) tone
                                                      else if (tone == ScanRiskTone.WARNING)          ScanRiskTone.WARNING
                                                      else                                             ScanRiskTone.SAFE
                                    val domainColor = domainRisk.contentColor(colors)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(colors.surface, RoundedCornerShape(18.dp))
                                            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                                            .padding(16.dp),
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
                                                text  = "LINK CHECK",
                                                style = typography.chipLabel,
                                                color = colors.textTertiary,
                                            )
                                        }
                                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                                        Row(
                                            modifier              = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment     = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text  = "Domain",
                                                style = typography.bodySmall,
                                                color = colors.textSecondary,
                                            )
                                            Text(
                                                text  = domain,
                                                style = typography.chipLabel,
                                                color = colors.textPrimary,
                                            )
                                        }
                                        Row(
                                            modifier              = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment     = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text  = "Status",
                                                style = typography.bodySmall,
                                                color = colors.textSecondary,
                                            )
                                            Text(
                                                text  = when {
                                                    isShortener             -> "⚠️ Shortened link — origin hidden"
                                                    tone == ScanRiskTone.DANGER  -> "🚨 High-risk domain"
                                                    tone == ScanRiskTone.WARNING -> "⚠️ Check official source"
                                                    else                    -> "✅ Looks OK"
                                                },
                                                style = typography.chipLabel,
                                                color = domainColor,
                                            )
                                        }
                                    }
                                }
                            }

                            // ── Email: what to do card ─────────────────────
                            if (category == ScanCategory.EMAIL && !result.recommendation.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(accent.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
                                        .border(1.dp, accent.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment     = Alignment.Top,
                                ) {
                                    Text("💡", fontSize = 16.sp)
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            text          = "WHAT TO DO",
                                            fontSize      = 10.sp,
                                            fontWeight    = FontWeight.SemiBold,
                                            color         = accent,
                                            letterSpacing = 0.8.sp,
                                        )
                                        Text(
                                            text     = result.recommendation,
                                            fontSize = 13.sp,
                                            color    = colors.textPrimary,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Breach list dialog ─────────────────────────────────
                    if (showBreachDialog && !result.breaches.isNullOrEmpty()) {
                        AlertDialog(
                            onDismissRequest = { showBreachDialog = false },
                            title = {
                                Text(
                                    text       = "Affected Sites (${result.breaches.size})",
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            text = {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    items(result.breaches) { breach ->
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text       = breach.name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize   = 14.sp,
                                                color      = colors.textPrimary,
                                            )
                                            if (!breach.domain.isNullOrBlank()) {
                                                Text(
                                                    text     = breach.domain,
                                                    fontSize = 12.sp,
                                                    color    = colors.textSecondary,
                                                )
                                            }
                                            if (!breach.breachDate.isNullOrBlank()) {
                                                Text(
                                                    text     = "Breach date: ${breach.breachDate}",
                                                    fontSize = 11.sp,
                                                    color    = colors.textTertiary,
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showBreachDialog = false }) {
                                    Text("Close")
                                }
                            },
                        )
                    }
                }
            }

            // ── 8. AI explanation loader ───────────────────────────────────
            item {
                AnimatedVisibility(visible = aiExplainLoading, enter = fadeIn(), exit = fadeOut()) {
                    ScanLoader(label = "Writing simple explanation…")
                }
            }

            // ── 9. AI explanation — friendly paragraph card ────────────────
            if (!aiExplanation.isNullOrBlank()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter   = fadeIn() + expandVertically(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(accent.copy(alpha = 0.07f), RoundedCornerShape(20.dp))
                                .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            // Header row
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
                            // Explanation as a natural, friendly paragraph — no bullets, no jargon
                            Text(
                                text       = aiExplanation.orEmpty(),
                                fontSize   = 14.sp,
                                color      = colors.textPrimary,
                                lineHeight = 21.sp,
                            )
                        }
                    }
                }
            }

            // ── 10. Error ──────────────────────────────────────────────────
            if (error != null) {
                item { ScanErrorBanner(message = localizedUiMessage(error)) }
            }

            if (error.isUpgradeError()) {
                item {
                    ScanPrimaryAction(
                        text        = stringResource(R.string.scan_upgrade_plan),
                        onClick     = { showUpgradeDialog = true },
                        accentColor = accent,
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

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

// ─── Domain intelligence helpers ─────────────────────────────────────────────
private val urlRegex = Regex("""https?://[^\s<>"']+|www\.[^\s<>"']+""")

private fun extractFirstDomain(text: String): String? {
    val match = urlRegex.find(text) ?: return null
    return try {
        val raw = match.value.trim()
        val withScheme = if (raw.startsWith("www.", ignoreCase = true)) "https://$raw" else raw
        java.net.URI(withScheme).host?.removePrefix("www.")?.lowercase()
    } catch (_: Exception) { null }
}

private val shortenerDomains = setOf(
    "bit.ly", "tinyurl.com", "goo.gl", "t.co", "ow.ly",
    "short.link", "cutt.ly", "rb.gy", "is.gd", "buff.ly",
)

private fun isShortenerDomain(domain: String): Boolean =
    shortenerDomains.any { domain.equals(it, ignoreCase = true) }
