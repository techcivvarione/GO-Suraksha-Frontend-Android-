package com.gosuraksha.app.ui.home

// =============================================================================
// HomeScreen.kt — PhonePe-style redesign
//
// Structure (exact PhonePe mapping):
//   1. HeroBanner        — full-width dark card (replaces PhonePe's travel banner)
//   2. ScoreStrip        — inline security score row (replaces balance strip)
//   3. ScamDetection     — circular icon grid + promo strip (= Money Transfers)
//   4. DangerUpdates     — circular icon grid + promo strip (= Recharge & Bills)
//   5. YourSafety        — circular icon grid (= Loans)
//
// Design rules:
//   • Section bg: #0F0F1A dark / #FFFFFF light
//   • 8dp gap between sections (SectionGap)
//   • Circular icon tiles: 52dp, colored bg per category
//   • No heavy green — accent only in score ring
//   • ViewModel / nav wiring UNCHANGED
// =============================================================================

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.domain.model.home.HomeOverview
import com.gosuraksha.app.domain.usecase.HomeUseCaseProvider
import com.gosuraksha.app.presentation.home.HomeViewModel
import com.gosuraksha.app.presentation.home.HomeViewModelFactory
import com.gosuraksha.app.presentation.state.UiState
import kotlinx.coroutines.delay

// ── PhonePe palette (used only in this file) ──────────────────────────────────
private val PpDarkBg      = Color(0xFF0F0F1A)
private val PpDarkSurface = Color(0xFF1A1A2E)
private val PpDarkGap     = Color(0xFF090910)
private val PpLightBg     = Color(0xFFF5F5F7)
private val PpLightSurface= Color(0xFFFFFFFF)
private val PpLightGap    = Color(0xFFEEEEEE)

// Icon bg colours — dark
private val DkRed     = Color(0xFF3D1515)
private val DkAmber   = Color(0xFF3D2A00)
private val DkBlue    = Color(0xFF1E3A5F)
private val DkPurple  = Color(0xFF2D1F6E)
private val DkGreen   = Color(0xFF1A2E1A)

// Icon bg colours — light
private val LtRed     = Color(0xFFFFF0F0)
private val LtAmber   = Color(0xFFFFFBEB)
private val LtBlue    = Color(0xFFEFF6FF)
private val LtPurple  = Color(0xFFF5F3FF)
private val LtGreen   = Color(0xFFF0FDF4)

// Icon tint colours
private val TRed    = Color(0xFFF87171)
private val TAmber  = Color(0xFFFBBF24)
private val TBlue   = Color(0xFF60A5FA)
private val TPurple = Color(0xFFA78BFA)
private val TGreen  = Color(0xFF4ADE80)

private fun ppSurface(isDark: Boolean) = if (isDark) PpDarkSurface else PpLightSurface
private fun ppBg(isDark: Boolean)      = if (isDark) PpDarkBg      else PpLightBg
private fun ppGap(isDark: Boolean)     = if (isDark) PpDarkGap     else PpLightGap
private fun ppText(isDark: Boolean)    = if (isDark) Color.White    else Color(0xFF111111)
private fun ppMuted(isDark: Boolean)   = if (isDark) Color(0xFF666680) else Color(0xFFAAAAAA)
private fun ppBorder(isDark: Boolean)  = if (isDark) Color(0xFF2A2A3E) else Color(0xFFEEEEEE)

@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToRisk: () -> Unit,
    onNavigateToRealityScan: () -> Unit,
    onNavigateToCyberCard: () -> Unit = {},
    onNavigateToCyberSos: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToFamily: () -> Unit = {},
    onNavigateToSecuritySettings: () -> Unit = {},
    onNavigateToNews: () -> Unit = {},
    onNavigateToEmailCheck: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateToScamNetwork: () -> Unit = {},
    onNavigateToScamLookup: () -> Unit = {},
    onNavigateToReportScam: () -> Unit = {},
    onNavigateToScamAlertsFeed: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTrustedContacts: () -> Unit = {}
) {
    val isDark     = ColorTokens.LocalAppDarkMode.current
    val appContext = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val provider   = appContext as HomeUseCaseProvider
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(provider.homeUseCases()))

    val overviewState by viewModel.overviewState.collectAsState()
    val user          by SessionManager.user.collectAsState()
    val overview      = (overviewState as? UiState.Success<HomeOverview>)?.data

    var popupExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(600); popupExpanded = true
        delay(3000); popupExpanded = false
    }

    val scans    = overview?.securitySnapshot?.scansDone ?: 0
    val hasScans = (overview?.securitySnapshot?.scansDone ?: 0) > 0
    val threats  = overview?.securitySnapshot?.threatsDetected ?: 0
    val risk     = overview?.securitySnapshot?.overallRisk ?: "Low"

    // ── Section data ──────────────────────────────────────────────────────────
    val scamDetectionTools = listOf(
        PpTool("Threat\nScan",     Icons.Rounded.Shield,         DkRed,    LtRed,    TRed,    onNavigateToScan),
        PpTool("Number\nCheck",    Icons.Rounded.Search,         DkBlue,   LtBlue,   TBlue,   onNavigateToScamLookup),
        PpTool("Scam\nHub",        Icons.Rounded.Warning,        DkAmber,  LtAmber,  TAmber,  onNavigateToScamNetwork),
        PpTool("QR\nSecurity",     Icons.Rounded.QrCode,         DkPurple, LtPurple, TPurple, onNavigateToScan)
    )
    val dangerUpdateTools = listOf(
        PpTool("Report\nFraud",    Icons.Rounded.Report,         DkRed,    LtRed,    TRed,    onNavigateToReportScam),
        PpTool("Danger\nAlerts",   Icons.Rounded.Notifications,  DkRed,    LtRed,    TRed,    onNavigateToScamAlertsFeed),
        PpTool("Safety\nScore",    Icons.Rounded.Star,           DkAmber,  LtAmber,  TAmber,  onNavigateToRisk),
        PpTool("Latest\nFrauds",   Icons.Rounded.Feed,           DkBlue,   LtBlue,   TBlue,   onNavigateToAlerts)
    )
    val safetyTools = listOf(
        PpTool("Safety\nCard",     Icons.Rounded.CreditCard,     DkGreen,  LtGreen,  TGreen,  onNavigateToCyberCard),
        PpTool("Profile",          Icons.Rounded.Person,         DkBlue,   LtBlue,   TBlue,   onNavigateToProfile),
        PpTool("Trusted\nCircle",  Icons.Rounded.FamilyRestroom, DkGreen,  LtGreen,  TGreen,  onNavigateToTrustedContacts),
        PpTool("Emergency\nSOS",   Icons.Rounded.Shield,         DkRed,    LtRed,    TRed,    onNavigateToCyberSos)
    )
    val mediaTools = listOf(
        PpTool("Photo\nCheck",     Icons.Rounded.CameraAlt,      DkGreen,  LtGreen,  TGreen,  onNavigateToRealityScan),
        PpTool("Audio\nScan",      Icons.AutoMirrored.Rounded.Message, DkBlue, LtBlue, TBlue, onNavigateToRealityScan),
        PpTool("Safety\nNews",     Icons.AutoMirrored.Rounded.Article, DkGreen, LtGreen, TGreen, onNavigateToNews),
        PpTool("Email\nBreach",    Icons.Rounded.Email,          DkPurple, LtPurple, TPurple, onNavigateToEmailCheck)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ppBg(isDark))
    ) {
        LazyColumn(
            state   = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        ) {
            // ── 1. Hero banner — dynamic, threat-aware ─────────────────────
            item {
                PpHeroBanner(
                    isDark      = isDark,
                    name        = user?.name?.split(" ")?.firstOrNull() ?: "Guardian",
                    threats     = threats,
                    risk        = risk,
                    hasScans    = hasScans,
                    onScanNow   = onNavigateToScan,
                    onDashboard = onNavigateToScamNetwork
                )
            }

            // ── 2. Score strip ─────────────────────────────────────────────
            item {
                when (val state = overviewState) {
                    is UiState.Loading -> PpScoreLoading(isDark)
                    is UiState.Success ->
                        if (!hasScans) PpScoreEmpty(isDark, onNavigateToScan)
                        else PpScoreStrip(isDark, scans, threats, risk)
                    is UiState.Error   -> {
                        val msg = state.message
                        if (msg.contains("no scans", true) || msg.contains("not found", true))
                            PpScoreEmpty(isDark, onNavigateToScan)
                        else
                            PpScoreError(isDark, onRetry = { viewModel.loadOverview() }, onHistory = onNavigateToHistory)
                    }
                    UiState.Idle -> PpScoreLoading(isDark)
                }
            }

            // ── 2b. Cyber Safety Score shortcut ───────────────────────────
            item {
                CyberScoreShortcutCard(
                    isDark  = isDark,
                    onClick = onNavigateToCyberCard
                )
            }

            // ── 3. Quick Actions (2×3 grid — key features surfaced prominently) ──
            item { PpSectionGap(isDark) }
            item {
                QuickActionsSection(
                    isDark            = isDark,
                    onScanMessage     = onNavigateToScan,
                    onCheckImage      = onNavigateToRealityScan,
                    onEmailBreach     = onNavigateToEmailCheck,
                    onPasswordCheck   = onNavigateToScan,
                    onScanQr          = onNavigateToScan,
                    onTrustedContacts = onNavigateToTrustedContacts
                )
            }

            // ── 4. Scam Detection ──────────────────────────────────────────
            item { PpSectionGap(isDark) }
            item {
                PpSection(
                    isDark      = isDark,
                    title       = "Scam Detection",
                    badgeText   = if (threats > 0) "$threats alerts" else null,
                    badgeRed    = true,
                    tools       = scamDetectionTools,
                    promoText   = "Report fraud to protect others",
                    promoAction = "Report →",
                    onPromoClick = onNavigateToReportScam,
                    onMoreClick  = onNavigateToScan
                )
            }

            // ── 5. Danger Updates ──────────────────────────────────────────
            item { PpSectionGap(isDark) }
            item {
                PpSection(
                    isDark      = isDark,
                    title       = "Live Threat Feed",
                    badgeText   = null,
                    tools       = dangerUpdateTools,
                    promoText   = "Stay ahead of active scams in your area",
                    promoAction = "See all →",
                    onPromoClick = onNavigateToScamNetwork,
                    onMoreClick  = onNavigateToScamAlertsFeed
                )
            }

            // ── 6. Your Safety ─────────────────────────────────────────────
            item { PpSectionGap(isDark) }
            item {
                PpSection(
                    isDark      = isDark,
                    title       = "Your Safety",
                    badgeText   = null,
                    tools       = safetyTools,
                    promoText   = null,
                    promoAction = null,
                    onPromoClick = {},
                    onMoreClick  = onNavigateToProfile
                )
            }

            // ── 7. Photo & Media ───────────────────────────────────────────
            item { PpSectionGap(isDark) }
            item {
                PpSection(
                    isDark      = isDark,
                    title       = "Photo & Media Check",
                    badgeText   = null,
                    tools       = mediaTools,
                    promoText   = null,
                    promoAction = null,
                    onPromoClick = {},
                    onMoreClick  = onNavigateToRealityScan
                )
            }

            item { Spacer(Modifier.height(96.dp)) }
        }

        // ── CyberCard peek / panel (unchanged) ────────────────────────────
        AnimatedVisibility(
            visible  = true,
            enter    = fadeIn(tween(300, delayMillis = 80)),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 168.dp)
            ) {
                AnimatedVisibility(
                    visible = popupExpanded,
                    enter   = fadeIn(tween(280)) + slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { it },
                    exit    = fadeOut(tween(220)) + slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { it }
                ) {
                    CyberCardEntryPanel(
                        isDark   = isDark,
                        onClick  = onNavigateToCyberCard,
                        onClose  = { popupExpanded = false },
                        modifier = Modifier
                            .padding(end = SpacingTokens.screenPaddingHorizontal)
                            .width(230.dp)
                    )
                }
                AnimatedVisibility(
                    visible = !popupExpanded,
                    enter   = fadeIn(tween(220, delayMillis = 200)) +
                            slideInHorizontally(tween(260, delayMillis = 200, easing = FastOutSlowInEasing)) { it },
                    exit    = fadeOut(tween(160)) +
                            slideOutHorizontally(tween(200, easing = FastOutSlowInEasing)) { it }
                ) {
                    CyberCardPeekTab(isDark = isDark, onClick = { popupExpanded = true })
                }
            }
        }
    }
}

// =============================================================================
// PpHeroBanner — Dynamic greeting + threat-aware status copy
// Reflects the user's real protection state so they feel the app is "alive"
// =============================================================================
@Composable
private fun PpHeroBanner(
    isDark: Boolean,
    name: String,
    threats: Int = 0,
    risk: String = "Low",
    hasScans: Boolean = false,
    onScanNow: () -> Unit,
    onDashboard: () -> Unit
) {
    val riskNorm = risk.lowercase()
    val isHighRisk = riskNorm == "high" || threats > 0
    val isMediumRisk = riskNorm == "medium"

    // Accent colour shifts with risk level — calm teal when safe, amber/red when risky
    val accentColor = when {
        isHighRisk   -> Color(0xFFEF4444)
        isMediumRisk -> Color(0xFFFBBF24)
        else         -> Color(0xFFA78BFA)
    }
    val bgColor = when {
        isHighRisk   -> Color(0xFF1F0A0A)
        isMediumRisk -> Color(0xFF1F1708)
        else         -> Color(0xFF1E1B4B)
    }

    // Headline copy — contextual to real state
    val headline = when {
        isHighRisk && threats > 0 -> "⚠️ ${threats} threat${if (threats > 1) "s" else ""} detected\nAct now to stay safe"
        isHighRisk                -> "⚠️ High risk detected\nTake action immediately"
        isMediumRisk && hasScans  -> "Stay alert\nSome risks detected recently"
        hasScans                  -> "You're safe today ✅\nNo threats detected"
        else                      -> "Check if you're safe\nRun your first scan now"
    }

    val ctaLabel = when {
        isHighRisk   -> "Check threats now"
        !hasScans    -> "Scan something →"
        else         -> "Run scan now"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(bgColor)
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-30).dp)
                .background(accentColor.copy(alpha = 0.07f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = 20.dp)
                .background(accentColor.copy(alpha = 0.04f), CircleShape)
        )

        // Shield icon — right side
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .size(60.dp)
                .background(accentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Rounded.Shield,
                contentDescription = null,
                tint               = accentColor,
                modifier           = Modifier.size(30.dp)
            )
        }

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp, end = 100.dp)
        ) {
            Text(
                text       = "Hi $name 👋",
                fontSize   = 11.sp,
                color      = accentColor.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text          = headline,
                fontSize      = 19.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = Color.White,
                lineHeight    = 25.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onScanNow
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text       = ctaLabel,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Icon(
                    imageVector        = Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(12.dp)
                )
            }
        }
    }
}

// =============================================================================
// PpScoreStrip — compact inline score row (PhonePe balance-row style)
// =============================================================================
@Composable
private fun PpScoreStrip(isDark: Boolean, scans: Int, threats: Int, risk: String) {
    val riskNorm   = risk.lowercase()
    val score      = when (riskNorm) { "high" -> 28; "medium" -> 55; else -> 82 }
    val riskColor  = when (riskNorm) { "high" -> TRed; "medium" -> TAmber; else -> TGreen }
    val riskLabel  = when (riskNorm) { "high" -> "High Risk ⚠️"; "medium" -> "Medium Risk"; else -> "Low Risk ✓" }
    val subtitle   = when {
        riskNorm == "high" && threats > 0 -> "$threats threat${if (threats > 1) "s" else ""} need your attention"
        riskNorm == "high"                -> "Action needed — risks detected"
        riskNorm == "medium"              -> "Stay alert — check your recent scans"
        else                              -> "You're protected today"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ppSurface(isDark))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Arc score ring
        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = Stroke(5.dp.toPx(), cap = StrokeCap.Round)
                drawArc(ppBorder(isDark), -220f, 260f, false, style = stroke)
                drawArc(riskColor, -220f, (score / 100f) * 260f, false, style = stroke)
            }
            Text(
                text       = "$score",
                fontSize   = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = ppText(isDark)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(subtitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ppText(isDark))
            Text(riskLabel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = riskColor)
        }

        // Stats
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PpStat("$scans",   "Scans",   TGreen,                               isDark)
            Box(Modifier.width(0.5.dp).height(20.dp).background(ppBorder(isDark)))
            PpStat("$threats", "Threats", if (threats > 0) TRed else TGreen,    isDark)
            Box(Modifier.width(0.5.dp).height(20.dp).background(ppBorder(isDark)))
            PpStat(riskLabel.split(" ").first(), "Risk", riskColor,              isDark)
        }
    }
}

@Composable
private fun PpStat(value: String, label: String, color: Color, isDark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold,   color = color)
        Text(label, fontSize = 8.sp,  color = ppMuted(isDark))
    }
}

@Composable
private fun PpScoreLoading(isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ppSurface(isDark))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier    = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color       = TGreen
        )
        Text("Loading security status…", fontSize = 12.sp, color = ppMuted(isDark))
    }
}

@Composable
private fun PpScoreEmpty(isDark: Boolean, onScan: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ppSurface(isDark))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onScan
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Rounded.Shield, null, tint = TAmber, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Your safety is unknown", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ppText(isDark))
            Text("Scan a message or link to check if you're at risk", fontSize = 10.sp, color = ppMuted(isDark))
        }
        Text("Scan →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TGreen)
    }
}

@Composable
private fun PpScoreError(isDark: Boolean, onRetry: () -> Unit, onHistory: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ppSurface(isDark))
            .padding(16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Rounded.Warning, null, tint = TAmber, modifier = Modifier.size(20.dp))
        Text("Dashboard unavailable", fontSize = 12.sp, color = ppText(isDark), modifier = Modifier.weight(1f))
        Text(
            text     = "Retry",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color    = TGreen,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onRetry
            )
        )
    }
}

// =============================================================================
// PpSection — exact PhonePe section card
// =============================================================================
@Composable
private fun PpSection(
    isDark:      Boolean,
    title:       String,
    badgeText:   String?,
    badgeRed:    Boolean   = false,
    tools:       List<PpTool>,
    promoText:   String?,
    promoAction: String?,
    onPromoClick: () -> Unit,
    onMoreClick:  () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ppSurface(isDark))
            .padding(top = 16.dp, bottom = 18.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text       = title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = ppText(isDark)
            )
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (badgeRed) Color(0xFF7C1D1D) else Color(0xFF3D2A00)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text       = badgeText,
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (badgeRed) TRed else TAmber
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Tool grid — 4 per row max
        tools.chunked(4).forEach { row ->
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { tool -> PpToolTile(tool, isDark) }
                repeat(4 - row.size) { Box(Modifier.width(56.dp)) }
            }
            Spacer(Modifier.height(14.dp))
        }

        // Promo strips (PhonePe's "Save VISA card / Start Gold savings" row)
        if (promoText != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xFF1A1A2E) else Color(0xFFF9F9F9))
                        .background(Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onPromoClick
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Shield, null,
                        tint     = TRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text     = promoText,
                        fontSize = 10.sp,
                        color    = ppMuted(isDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (promoAction != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF2D1F6E) else Color(0xFFEDE9FE))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = onMoreClick
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = promoAction,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TPurple
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// PpToolTile — 52dp circle icon + label (exact PhonePe)
// =============================================================================
@Composable
private fun PpToolTile(tool: PpTool, isDark: Boolean) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = tool.onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    if (isDark) tool.iconBgDark else tool.iconBgLight,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = tool.icon,
                contentDescription = tool.label,
                tint               = tool.iconTint,
                modifier           = Modifier.size(22.dp)
            )
        }
        Text(
            text      = tool.label,
            fontSize  = 9.sp,
            fontWeight = FontWeight.Medium,
            color     = ppMuted(isDark),
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            maxLines  = 2,
            overflow  = TextOverflow.Ellipsis
        )
    }
}

// =============================================================================
// PpSectionGap
// =============================================================================
@Composable
private fun PpSectionGap(isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(ppGap(isDark))
    )
}

// =============================================================================
// QuickActionsSection — 2×3 grid of key features with icon + title + description
// Each tile is a wide horizontal card (not a small circle) so the user can
// immediately understand what each feature does.
// =============================================================================
@Composable
private fun QuickActionsSection(
    isDark: Boolean,
    onScanMessage: () -> Unit,
    onCheckImage: () -> Unit,
    onEmailBreach: () -> Unit,
    onPasswordCheck: () -> Unit,
    onScanQr: () -> Unit,
    onTrustedContacts: () -> Unit,
) {
    val actions = listOf(
        QaAction("Scan Message or Link",  "Check if SMS or link is fake",      Icons.AutoMirrored.Rounded.Message,    DkBlue,   LtBlue,   TBlue,   onScanMessage),
        QaAction("Check Image (AI/Fake)", "Detect fake or edited photos",       Icons.Rounded.CameraAlt,               DkGreen,  LtGreen,  TGreen,  onCheckImage),
        QaAction("Email Breach Check",    "See if your email was leaked",        Icons.Rounded.Email,                   DkPurple, LtPurple, TPurple, onEmailBreach),
        QaAction("Password Safety",       "Check if your password is weak",      Icons.Rounded.Lock,                    DkAmber,  LtAmber,  TAmber,  onPasswordCheck),
        QaAction("Scan QR Code",          "Verify a QR before you scan it",     Icons.Rounded.QrCode,                  DkRed,    LtRed,    TRed,    onScanQr),
        QaAction("Trusted Contacts",      "Add family members for protection",   Icons.Rounded.FamilyRestroom,          DkGreen,  LtGreen,  TGreen,  onTrustedContacts),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ppSurface(isDark))
            .padding(top = 16.dp, bottom = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Quick Actions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ppText(isDark))
            Text("All features", fontSize = 10.sp, color = ppMuted(isDark))
        }
        Spacer(Modifier.height(12.dp))
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowActions.forEach { action ->
                    QaActionTile(action, isDark, Modifier.weight(1f))
                }
                if (rowActions.size < 2) Box(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun QaActionTile(action: QaAction, isDark: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color(0xFF1A1A2E) else Color(0xFFF4F4F8))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = action.onClick
            )
            .padding(horizontal = 10.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(if (isDark) action.iconBgDark else action.iconBgLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(action.icon, null, tint = action.iconTint, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(action.title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ppText(isDark), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(action.description, fontSize = 8.5.sp, color = ppMuted(isDark), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// =============================================================================
// Data models
// =============================================================================
private data class QaAction(
    val title:       String,
    val description: String,
    val icon:        ImageVector,
    val iconBgDark:  Color,
    val iconBgLight: Color,
    val iconTint:    Color,
    val onClick:     () -> Unit
)

private data class PpTool(
    val label:      String,
    val icon:       ImageVector,
    val iconBgDark: Color,
    val iconBgLight:Color,
    val iconTint:   Color,
    val onClick:    () -> Unit
)

// =============================================================================
// CyberScoreShortcutCard — Prominent tap-to-open card shown on Home
// Sits between the Score Strip and Quick Actions for maximum visibility.
// =============================================================================
@Composable
private fun CyberScoreShortcutCard(isDark: Boolean, onClick: () -> Unit) {
    val surface       = if (isDark) Color(0xFF111827) else Color(0xFFFFFFFF)
    val textPrimary   = if (isDark) Color(0xFFF9FAFB) else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF)
    val green         = Color(0xFF22C55E)
    val dividerColor  = if (isDark) Color(0xFF1F2937) else Color(0xFFE5E7EB)

    Surface(
        onClick        = onClick,
        color          = surface,
        shape          = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        modifier       = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Shield icon badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(green.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Security, null,
                    tint     = green,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Title + subtitle
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "Cyber Safety Score",
                    color      = textPrimary,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "See how safe you are right now",
                    color    = textSecondary,
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Rounded.ChevronRight, null,
                tint     = textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}