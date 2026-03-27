package com.gosuraksha.app.scan.core

// =============================================================================
// ScanScreen.kt — Modern Fintech Scan Hub
//
// Hub: 2×2 grid + QR full-width card
// Sub-scan: persistent top tab row showing ALL 5 scan types (scrollable pills)
// Dark/light: follows app-wide toggle via GoSurakshaScanTheme default
// =============================================================================

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.gosuraksha.app.scan.design.ScanColors
import com.gosuraksha.app.scan.design.ScanTheme
import com.gosuraksha.app.ui.qr.QrAnalyzerScreen
import com.gosuraksha.app.scan.reality.RealityScanHubScreen
import com.gosuraksha.app.scan.reality.RealityScanViewModel
import com.gosuraksha.app.scan.reality.RealityScanViewModelFactory
import com.gosuraksha.app.scan.text.EmailScanScreen
import com.gosuraksha.app.scan.text.PasswordScanScreen
import com.gosuraksha.app.scan.text.ThreatScanScreen
import kotlinx.coroutines.delay

// ─── Destinations ─────────────────────────────────────────────────────────────
private enum class ScanDestination {
    CENTER, THREAT, EMAIL, REALITY, PASSWORD, QR
}

// ─── All 5 tabs — shown in the scrollable top tab row ─────────────────────────
private data class TabEntry(
    val destination: ScanDestination,
    val label: String,
    val icon: ImageVector,
)

private val allTabs = listOf(
    TabEntry(ScanDestination.THREAT,   "Message",  Icons.Outlined.Security),
    TabEntry(ScanDestination.EMAIL,    "Email",    Icons.Outlined.Email),
    TabEntry(ScanDestination.PASSWORD, "Password", Icons.Outlined.Password),
    TabEntry(ScanDestination.REALITY,  "Image",    Icons.Outlined.Image),
    TabEntry(ScanDestination.QR,       "QR Code",  Icons.Outlined.QrCodeScanner),
)

// ─── Root composable ──────────────────────────────────────────────────────────
@Composable
fun ScanScreen(onUpgradePlan: () -> Unit = {}) {
    val appContext   = LocalContext.current.applicationContext
    val provider     = appContext as ScanUseCaseProvider
    val user         by SessionManager.user.collectAsStateWithLifecycle()

    val textVm: TextScanViewModel = viewModel(
        factory = TextScanViewModelFactory(provider.scanUseCases())
    )
    val realityVm: RealityScanViewModel = viewModel(
        factory = RealityScanViewModelFactory(provider.scanUseCases())
    )
    val sharedPayload by SharedScanIntentStore.pending.collectAsStateWithLifecycle()
    var destination   by remember { mutableStateOf(ScanDestination.CENTER) }

    LaunchedEffect(sharedPayload) {
        if (sharedPayload != null) destination = ScanDestination.REALITY
    }

    // ── Theme auto-detects dark/light via GoSurakshaScanTheme default ─────────
    GoSurakshaScanTheme {
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

                // All 5 sub-scans render inside the persistent tab row
                else -> Column(Modifier.fillMaxSize()) {
                    ScanTabsRow(
                        current  = current,
                        onSelect = { destination = it },
                        colors   = colors,
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        when (current) {
                            ScanDestination.THREAT   -> ThreatScanScreen(textVm, onUpgradePlan = onUpgradePlan)
                            ScanDestination.EMAIL    -> EmailScanScreen(textVm, onUpgradePlan = onUpgradePlan)
                            ScanDestination.REALITY  -> RealityScanHubScreen(
                                viewModel               = realityVm,
                                sharedPayload           = sharedPayload,
                                onSharedPayloadConsumed = { SharedScanIntentStore.consume() },
                                onUpgradePlan           = onUpgradePlan,
                            )
                            ScanDestination.PASSWORD -> PasswordScanScreen(textVm, onUpgradePlan = onUpgradePlan)
                            ScanDestination.QR       -> QrAnalyzerScreen(onUpgradePlan = onUpgradePlan)
                            else                     -> Unit
                        }
                    }
                }
            }
        }
    }
}

// ─── Tab Row — horizontally scrollable pills showing all 5 scans ──────────────
@Composable
private fun ScanTabsRow(
    current:  ScanDestination,
    onSelect: (ScanDestination) -> Unit,
    colors:   ScanColors,
) {
    // Subtle divider line under tabs
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
    ) {
        LazyRow(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding      = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(allTabs) { tab ->
                val isSelected   = current == tab.destination
                val interSrc     = remember { MutableInteractionSource() }
                val isPressed    by interSrc.collectIsPressedAsState()
                val scale        by animateFloatAsState(
                    targetValue   = if (isPressed) 0.94f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label         = "tab_scale"
                )

                Row(
                    modifier = Modifier
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isSelected) colors.primaryBlue
                            else colors.surface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else colors.border,
                            shape = RoundedCornerShape(18.dp),
                        )
                        .clickable(
                            interactionSource = interSrc,
                            indication        = null,
                        ) { onSelect(tab.destination) }
                        .padding(horizontal = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector        = tab.icon,
                        contentDescription = null,
                        tint               = if (isSelected) Color.White else colors.textSecondary,
                        modifier           = Modifier.size(13.dp),
                    )
                    Text(
                        text       = tab.label,
                        fontSize   = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (isSelected) Color.White else colors.textSecondary,
                    )
                }
            }
        }
        // Thin divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.border)
        )
    }
}

// ─── Hub (CENTER) ─────────────────────────────────────────────────────────────
@Composable
private fun ScanCenterContent(
    onSelect:      (ScanDestination) -> Unit,
    userPlan:      Plan = Plan.FREE,
    onUpgradePlan: () -> Unit = {},
) {
    val colors     = ScanTheme.colors
    val spacing    = ScanTheme.spacing
    val typography = ScanTheme.typography

    data class ToolEntry(val destination: ScanDestination, val model: ScanToolCardModel)

    val tools = remember {
        listOf(
            ToolEntry(
                ScanDestination.THREAT,
                ScanToolCardModel(
                    title       = "Message / Link Scan",
                    description = "Check if a message, link or forwarded text is safe.",
                    buttonLabel = "Check",
                    icon        = Icons.Outlined.Security,
                    tags        = listOf("Messages", "Links"),
                    variant     = ScanToolVariant.THREAT,
                ),
            ),
            ToolEntry(
                ScanDestination.REALITY,
                ScanToolCardModel(
                    title       = "Image Scan",
                    description = "Detect AI-generated or manipulated photos instantly.",
                    buttonLabel = "Scan",
                    icon        = Icons.Outlined.Image,
                    tags        = listOf("AI Images", "Deepfakes"),
                    variant     = ScanToolVariant.DEEPFAKE,
                ),
            ),
            ToolEntry(
                ScanDestination.EMAIL,
                ScanToolCardModel(
                    title       = "Email Breach Check",
                    description = "See if your email appeared in known data breaches.",
                    buttonLabel = "Check",
                    icon        = Icons.Outlined.Email,
                    tags        = listOf("Breaches", "Leaks"),
                    variant     = ScanToolVariant.EMAIL,
                ),
            ),
            ToolEntry(
                ScanDestination.PASSWORD,
                ScanToolCardModel(
                    title       = "Password Strength",
                    description = "Check if your password has been leaked online.",
                    buttonLabel = "Check",
                    icon        = Icons.Outlined.Password,
                    tags        = listOf("Strength", "Leaks"),
                    variant     = ScanToolVariant.PASSWORD,
                ),
            ),
            ToolEntry(
                ScanDestination.QR,
                ScanToolCardModel(
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

    // ── Staggered entrance animation ──────────────────────────────────────────
    val visible = remember { mutableStateListOf(false, false, false) }  // 3 items: header, banner, cards
    LaunchedEffect(Unit) {
        visible.indices.forEach { i ->
            delay(80L + 80L * i)
            visible[i] = true
        }
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = spacing.lg, vertical = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {

        // ── Hero header ────────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = visible.getOrElse(0) { false },
                enter   = fadeIn(tween(360)) + slideInVertically(tween(360)) { it / 3 }
            ) {
                HubHeader(colors = colors)
            }
        }

        // ── Status / plan banner ───────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = visible.getOrElse(1) { false },
                enter   = fadeIn(tween(360)) + slideInVertically(tween(360)) { it / 3 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    StatusBanner(userPlan = userPlan)
                    if (userPlan == Plan.FREE) {
                        UpgradeBanner(onClick = onUpgradePlan)
                    }
                }
            }
        }

        // ── Tools grid ────────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = visible.getOrElse(2) { false },
                enter   = fadeIn(tween(380)) + slideInVertically(tween(380)) { it / 4 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {

                    // Section label
                    Text(
                        text       = "SECURITY TOOLS",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color      = colors.textTertiary,
                        modifier   = Modifier.padding(bottom = 4.dp, start = 2.dp),
                    )

                    // Row 1: Message | Image
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

                    // Row 2: Email | Password
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

                    // Row 3: QR — full-width
                    ScanToolCard(
                        model   = tools[4].model,
                        onClick = { onSelect(tools[4].destination) },
                    )
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ─── Hub Header ───────────────────────────────────────────────────────────────
@Composable
private fun HubHeader(colors: ScanColors) {
    val typography = ScanTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Animated pulse dot
            val inf = rememberInfiniteTransition(label = "dot_pulse")
            val dotAlpha by inf.animateFloat(
                initialValue  = 1f,
                targetValue   = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(900, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot_alpha",
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(colors.safeGreen.copy(alpha = dotAlpha), CircleShape)
            )
            Text(
                text       = "GO SURAKSHA",
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color      = colors.primaryBlue,
            )
        }
        Text(
            text       = "Scan & Protect",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            color      = colors.textPrimary,
            letterSpacing = (-0.3).sp,
        )
        Text(
            text  = "Detect threats across messages, emails and images",
            style = typography.bodySmall,
            color = colors.textSecondary,
        )
    }
}

// ─── Status Banner — premium plan card ────────────────────────────────────────
@Composable
private fun StatusBanner(userPlan: Plan = Plan.FREE) {
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography

    val planLabel = when (userPlan) {
        Plan.GO_ULTRA -> "ULTRA"
        Plan.GO_PRO   -> "PRO"
        else          -> "Free"
    }
    val planSub = when (userPlan) {
        Plan.GO_ULTRA -> "Unlimited scans & AI analysis"
        Plan.GO_PRO   -> "Extended scan access"
        else          -> "Limited scans — upgrade for more"
    }
    val planColor = when (userPlan) {
        Plan.GO_ULTRA -> Color(0xFF00C896)   // green
        Plan.GO_PRO   -> Color(0xFF3B9EFF)   // blue
        else          -> Color(0xFF94A3B8)   // gray
    }

    // Pulsing green protection dot
    val inf   = rememberInfiniteTransition(label = "banner_pulse")
    val pulse by inf.animateFloat(
        initialValue  = 0.9f,
        targetValue   = 1.3f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.statusBannerBg, colors.statusBannerBg2)
                )
            )
            .border(1.dp, colors.statusBannerBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        // Shield icon with pulsing ring
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer { scaleX = pulse; scaleY = pulse }
                    .background(colors.safeGreen.copy(alpha = 0.08f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(colors.safeGreen.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint               = colors.safeGreen,
                    modifier           = Modifier.size(22.dp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text       = "Protection Active",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
            )
            Text(
                text  = planSub,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.55f),
            )
        }

        // Plan badge pill
        Box(
            modifier = Modifier
                .background(planColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .border(1.dp, planColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp),
        ) {
            Text(
                text       = planLabel,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = planColor,
            )
        }
    }
}

// ─── Upgrade Banner (FREE only) ───────────────────────────────────────────────
@Composable
private fun UpgradeBanner(onClick: () -> Unit) {
    val interSrc  = remember { MutableInteractionSource() }
    val isPressed by interSrc.collectIsPressedAsState()
    val scale     by animateFloatAsState(
        targetValue   = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "upgrade_scale",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF1A3FAA), Color(0xFF6D28D9)))
            )
            .clickable(interactionSource = interSrc, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                color    = Color.White.copy(alpha = 0.72f),
            )
        }
        Row(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                .padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text       = "Upgrade",
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
            )
            Icon(
                Icons.Rounded.ArrowForwardIos, null,
                tint     = Color.White,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}
