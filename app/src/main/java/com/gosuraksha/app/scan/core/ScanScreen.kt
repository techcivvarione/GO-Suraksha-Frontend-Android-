package com.gosuraksha.app.scan.core

// =============================================================================
// ScanScreen.kt — PhonePe-style Scan Hub
//
// CENTER hub: 3-card grid (Message/Link, Image, Email Breach)
// Once a card is tapped, top tabs let user switch between all 3 scan types
// without returning to the hub.  QR / Password remain accessible but are
// not surfaced in the primary hub grid.
// =============================================================================

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.domain.model.Plan
import com.gosuraksha.app.domain.usecase.ScanUseCaseProvider
import com.gosuraksha.app.scan.SharedScanIntentStore
import com.gosuraksha.app.scan.components.ScanGridCard
import com.gosuraksha.app.scan.components.ScanToolCard
import com.gosuraksha.app.scan.components.ScanToolCardModel
import com.gosuraksha.app.scan.components.ScanToolVariant
import com.gosuraksha.app.scan.design.GoSurakshaScanTheme
import com.gosuraksha.app.scan.design.ScanTheme
import com.gosuraksha.app.ui.qr.QrAnalyzerScreen
import com.gosuraksha.app.scan.reality.RealityScanHubScreen
import com.gosuraksha.app.scan.reality.RealityScanViewModel
import com.gosuraksha.app.scan.reality.RealityScanViewModelFactory
import com.gosuraksha.app.scan.text.EmailScanScreen
import com.gosuraksha.app.scan.text.PasswordScanScreen
import com.gosuraksha.app.scan.text.ThreatScanScreen

// Internal navigation destinations — not exposed outside this file
private enum class ScanDestination {
    CENTER, THREAT, EMAIL, REALITY,
    // Available but not shown in main hub grid:
    PASSWORD, QR,
}

// The 3 primary tabs shown in the top tab row
private val primaryTabs = listOf(
    Triple(ScanDestination.THREAT,  "Message",     Icons.Outlined.Security),
    Triple(ScanDestination.REALITY, "Image",        Icons.Outlined.Image),
    Triple(ScanDestination.EMAIL,   "Email Breach", Icons.Outlined.Email),
)

@Composable
fun ScanScreen(onUpgradePlan: () -> Unit = {}) {
    val appContext = LocalContext.current.applicationContext
    val provider   = appContext as ScanUseCaseProvider
    val user by SessionManager.user.collectAsStateWithLifecycle()

    val textVm: TextScanViewModel = viewModel(
        factory = TextScanViewModelFactory(provider.scanUseCases())
    )
    val realityVm: RealityScanViewModel = viewModel(
        factory = RealityScanViewModelFactory(provider.scanUseCases())
    )
    val sharedPayload by SharedScanIntentStore.pending.collectAsStateWithLifecycle()
    var destination   by remember { mutableStateOf(ScanDestination.CENTER) }

    // Auto-open image scan for shared media intent
    LaunchedEffect(sharedPayload) {
        if (sharedPayload != null) destination = ScanDestination.REALITY
    }

    GoSurakshaScanTheme(darkTheme = false) {
        val colors = ScanTheme.colors

        AnimatedContent(
            targetState = destination,
            modifier    = Modifier
                .fillMaxSize()
                .background(colors.background),
            label       = "scan_destination",
        ) { current ->
            when (current) {
                ScanDestination.CENTER -> ScanCenterContent(
                    onSelect      = { destination = it },
                    userPlan      = user?.plan ?: Plan.FREE,
                    onUpgradePlan = onUpgradePlan,
                )

                // Primary scans — render with persistent tab row at top
                ScanDestination.THREAT,
                ScanDestination.EMAIL,
                ScanDestination.REALITY -> Column(Modifier.fillMaxSize()) {
                    ScanTabsRow(
                        current  = current,
                        onSelect = { destination = it },
                        colors   = colors,
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        when (current) {
                            ScanDestination.THREAT  -> ThreatScanScreen(textVm, onUpgradePlan = onUpgradePlan)
                            ScanDestination.EMAIL   -> EmailScanScreen(textVm, onUpgradePlan = onUpgradePlan)
                            ScanDestination.REALITY -> RealityScanHubScreen(
                                viewModel               = realityVm,
                                sharedPayload           = sharedPayload,
                                onSharedPayloadConsumed = { SharedScanIntentStore.consume() },
                                onUpgradePlan           = onUpgradePlan,
                            )
                            else -> Unit
                        }
                    }
                }

                // Secondary scans — no tab row
                ScanDestination.PASSWORD -> PasswordScanScreen(textVm, onUpgradePlan = onUpgradePlan)
                ScanDestination.QR       -> QrAnalyzerScreen(onUpgradePlan = onUpgradePlan)
            }
        }
    }
}

// ─── Tab row ──────────────────────────────────────────────────────────────────
@Composable
private fun ScanTabsRow(
    current:  ScanDestination,
    onSelect: (ScanDestination) -> Unit,
    colors:   com.gosuraksha.app.scan.design.ScanColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        primaryTabs.forEach { (dest, label, _) ->
            val isSelected = current == dest
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) colors.primaryBlue else colors.surface)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) colors.primaryBlue else colors.border,
                        shape = RoundedCornerShape(10.dp),
                    )
                    .clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onSelect(dest) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = label,
                    fontSize   = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color      = if (isSelected) Color.White else colors.textSecondary,
                )
            }
        }
    }
}

// ─── Scan Hub (center grid) ────────────────────────────────────────────────────
@Composable
private fun ScanCenterContent(
    onSelect: (ScanDestination) -> Unit,
    userPlan: Plan = Plan.FREE,
    onUpgradePlan: () -> Unit = {},
) {
    val colors     = ScanTheme.colors
    val spacing    = ScanTheme.spacing
    val typography = ScanTheme.typography

    data class ToolEntry(val destination: ScanDestination, val model: ScanToolCardModel)

    // 5 core security tools — full hub restored
    val tools = remember {
        listOf(
            ToolEntry(
                destination = ScanDestination.THREAT,
                model = ScanToolCardModel(
                    title       = "Message / Link Scan",
                    description = "Check if a message, link or forwarded text is safe.",
                    buttonLabel = "Check",
                    icon        = Icons.Outlined.Security,
                    tags        = listOf("Messages", "Links"),
                    variant     = ScanToolVariant.THREAT,
                ),
            ),
            ToolEntry(
                destination = ScanDestination.REALITY,
                model = ScanToolCardModel(
                    title       = "Image Scan",
                    description = "Detect AI-generated or manipulated photos instantly.",
                    buttonLabel = "Scan",
                    icon        = Icons.Outlined.Image,
                    tags        = listOf("AI Images", "Deepfakes"),
                    variant     = ScanToolVariant.DEEPFAKE,
                ),
            ),
            ToolEntry(
                destination = ScanDestination.EMAIL,
                model = ScanToolCardModel(
                    title       = "Email Breach Check",
                    description = "See if your email appeared in known data breaches.",
                    buttonLabel = "Check",
                    icon        = Icons.Outlined.Email,
                    tags        = listOf("Breaches", "Leaks"),
                    variant     = ScanToolVariant.EMAIL,
                ),
            ),
            ToolEntry(
                destination = ScanDestination.PASSWORD,
                model = ScanToolCardModel(
                    title       = "Password Strength",
                    description = "Check how strong your password is and if it's been leaked.",
                    buttonLabel = "Check",
                    icon        = Icons.Outlined.Password,
                    tags        = listOf("Strength", "Leaks"),
                    variant     = ScanToolVariant.PASSWORD,
                ),
            ),
            ToolEntry(
                destination = ScanDestination.QR,
                model = ScanToolCardModel(
                    title       = "QR Code Scanner",
                    description = "Scan any QR code to verify it is safe before opening.",
                    buttonLabel = "Scan",
                    icon        = Icons.Outlined.QrCodeScanner,
                    tags        = listOf("QR Codes", "Payments"),
                    variant     = ScanToolVariant.QR,
                ),
            ),
        )
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = spacing.lg, vertical = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        // ── Page header ────────────────────────────────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text  = "GO SURAKSHA",
                    style = typography.chipLabel,
                    color = colors.primaryBlue,
                )
                Text(
                    text  = "Scan & Protect",
                    style = typography.sectionHeading,
                    color = colors.textPrimary,
                )
                Text(
                    text  = "Check messages, images, and emails for threats in seconds.",
                    style = typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            Spacer(Modifier.height(spacing.lg))
        }

        // ── Status banner ──────────────────────────────────────────────────
        item {
            StatusBanner(userPlan = userPlan)
            Spacer(Modifier.height(spacing.lg))
        }

        // ── Upgrade banner — shown only for FREE users ──────────────────────
        if (userPlan == Plan.FREE) {
            item {
                UpgradeBanner(onClick = onUpgradePlan)
                Spacer(Modifier.height(spacing.lg))
            }
        }

        // ── Section label ──────────────────────────────────────────────────
        item {
            Text(
                text  = "SCAN TOOLS",
                style = typography.chipLabel,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(spacing.sm))
        }

        // ── Tool cards — 2-column grid ─────────────────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {

                // Row 1: Message / Link  |  Image Scan
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    ScanGridCard(
                        model    = tools[0].model,
                        onClick  = { onSelect(tools[0].destination) },
                        modifier = Modifier.weight(1f),
                    )
                    ScanGridCard(
                        model    = tools[1].model,
                        onClick  = { onSelect(tools[1].destination) },
                        modifier = Modifier.weight(1f),
                    )
                }

                // Row 2: Email Breach  |  Password Strength
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    ScanGridCard(
                        model    = tools[2].model,
                        onClick  = { onSelect(tools[2].destination) },
                        modifier = Modifier.weight(1f),
                    )
                    ScanGridCard(
                        model    = tools[3].model,
                        onClick  = { onSelect(tools[3].destination) },
                        modifier = Modifier.weight(1f),
                    )
                }

                // Row 3: QR Code Scanner — full-width premium card
                ScanToolCard(
                    model   = tools[4].model,
                    onClick = { onSelect(tools[4].destination) },
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

// ─── Status Banner ─────────────────────────────────────────────────────────────
@Composable
private fun StatusBanner(userPlan: Plan = Plan.FREE) {
    val colors     = ScanTheme.colors
    val spacing    = ScanTheme.spacing
    val typography = ScanTheme.typography

    val planBadgeText = when (userPlan) {
        Plan.GO_ULTRA -> "ULTRA"
        Plan.GO_PRO   -> "PRO"
        else          -> "Free"
    }
    val planSubtext = when (userPlan) {
        Plan.GO_ULTRA -> "Unlimited scans & AI"
        Plan.GO_PRO   -> "3 scans/day per tool"
        else          -> "Limited scans — upgrade for more"
    }

    val bgModifier = if (colors.statusBannerBg == colors.statusBannerBg2) {
        Modifier
            .fillMaxWidth()
            .background(colors.statusBannerBg, RoundedCornerShape(22.dp))
            .border(1.dp, colors.statusBannerBorder, RoundedCornerShape(22.dp))
    } else {
        Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(listOf(colors.statusBannerBg, colors.statusBannerBg2)),
                shape = RoundedCornerShape(22.dp),
            )
    }

    Row(
        modifier              = bgModifier.padding(horizontal = spacing.lg, vertical = spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(colors.safeGreenSoft, CircleShape)
                .border(1.5.dp, colors.safeGreen.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Outlined.Shield,
                contentDescription = null,
                tint               = colors.safeGreen,
                modifier           = Modifier.size(24.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = "Protection Active",
                style = typography.cardTitle,
                color = Color.White,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = planSubtext,
                style = typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
            )
        }

        Box(
            modifier = Modifier
                .background(colors.safeGreenSoft, RoundedCornerShape(20.dp))
                .border(1.dp, colors.safeGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Text(
                text  = planBadgeText,
                style = typography.chipLabel,
                color = colors.safeGreen,
            )
        }
    }
}

// ─── Upgrade Banner (FREE users only) ─────────────────────────────────────────
@Composable
private fun UpgradeBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF1E3A8A), Color(0xFF7C3AED))
                )
            )
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text       = "Upgrade to GO PRO",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
            )
            Text(
                text     = "Unlimited scans · AI deepfake detection · Priority alerts",
                fontSize = 11.sp,
                color    = Color.White.copy(alpha = 0.75f),
            )
        }
        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text       = "Upgrade →",
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
            )
        }
    }
}
