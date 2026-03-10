package com.gosuraksha.app.ui.home

// =============================================================================
// HomeScreen.kt — "Obsidian Shield" Design  (Production Grade, A++++)
//
// Theme strategy (zero ambiguity):
//   isDark = ColorTokens.LocalAppDarkMode.current
//   Dark  → no borders, elevated dark surfaces, glow accents
//   Light → white/near-white surfaces, 1dp #E2EAF8 borders, reduced shadow
//
// Layout (vertical scroll, staggered entrance animations):
//   1. Status bar spacer
//   2. GreetingRow          — name + search pill
//   3. SecurityScoreCard    — animated ring, score, badge, 3 stat chips
//   4. BannerCarousel       — existing BannerData / BannerCarousel composable
//   5. SectionGrid("Core")  — 2 × 4 icon tiles (Cyber SOS → Risk Intel)
//   6. SectionGrid("Scan")  — 2 × 4 icon tiles (Link → Reports)
//   7. SectionRow("Family & Alerts") — 1 × 4
//   8. SectionRow("Settings & More") — 1 × 4 + divider + 1 × 4
//   9. NewsRow              — horizontal scroll of news chips
//
// All ViewModels, lambdas, SessionManager: UNTOUCHED.
// New optional lambdas (default = {}): onNavigateToEmailCheck, onNavigateToPremium
// =============================================================================

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.R
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens
import com.gosuraksha.app.domain.model.home.HomeOverview
import com.gosuraksha.app.domain.usecase.HomeUseCaseProvider
import com.gosuraksha.app.presentation.home.HomeViewModel
import com.gosuraksha.app.presentation.home.HomeViewModelFactory
import com.gosuraksha.app.presentation.state.UiState
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// Obsidian color palette — accent/brand only.
// All surface/background values resolved inline via isDark.
// ─────────────────────────────────────────────────────────────────────────────
internal object ObsidianColors {
    // Brand
    val Brand       = Color(0xFF4F8AFF)
    val BrandDeep   = Color(0xFF1E3A8A)

    // Accent gradients (used as Brush endpoints)
    val RingStart   = Color(0xFF4F8AFF)
    val RingEnd     = Color(0xFFA855F7)

    // Semantic
    val Safe        = Color(0xFF10C878)
    val SafeDim     = Color(0xFF047857)
    val Critical    = Color(0xFFEF4444)
    val Warning     = Color(0xFFF59E0B)
    val Rose        = Color(0xFFBE185D)
    val Sky         = Color(0xFF0EA5E9)
    val Teal        = Color(0xFF0F766E)
    val Cyan        = Color(0xFF0891B2)
    val Emerald     = Color(0xFF059669)
    val Forest      = Color(0xFF065F46)
    val Amber       = Color(0xFFB45309)
    val Gold        = Color(0xFFD97706)
    val Orange      = Color(0xFFEA580C)
    val Sand        = Color(0xFFB0891E)
    val Indigo      = Color(0xFF4338CA)
    val Violet      = Color(0xFFA855F7)
    val Neutral     = Color(0xFF6B7280)
    val Slate       = Color(0xFF334155)
    val WarmGray    = Color(0xFF78716C)

    // Dark theme surfaces
    val DarkBg      = Color(0xFF0D0F1A)
    val DarkSurface = Color(0xFF13162A)  // cards
    val DarkSurface2= Color(0xFF181B30)  // deeper cards
    val DarkBorder  = Color(0x12FFFFFF)  // 7% white

    // Light theme surfaces
    val LightBg     = Color(0xFFF0F4FF)
    val LightSurface= Color(0xFFFFFFFF)
    val LightBorder = Color(0xFFE2EAF8)
}

// Backward-compat alias so sibling files keep compiling
internal typealias HomePalette = ObsidianColors

// ─────────────────────────────────────────────────────────────────────────────
// Internal model
// ─────────────────────────────────────────────────────────────────────────────
private data class ShortcutItem(
    val label: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

// =============================================================================
// HomeScreen
// =============================================================================
@Composable
fun HomeScreen(
    // ── Existing lambdas — UNCHANGED ─────────────────────────────────────────
    onNavigateToHistory: () -> Unit,
    onNavigateToRisk: () -> Unit,
    onNavigateToRealityScan: () -> Unit,
    onNavigateToCyberSos: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToFamily: () -> Unit = {},
    onNavigateToSecuritySettings: () -> Unit = {},
    onNavigateToNews: () -> Unit = {},
    // ── New optional lambdas (backward-compatible) ────────────────────────────
    onNavigateToEmailCheck: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {}
) {
    val isDark = ColorTokens.LocalAppDarkMode.current

    // ── ViewModel — UNTOUCHED ────────────────────────────────────────────────
    val appContext = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val provider   = appContext as HomeUseCaseProvider
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(provider.homeUseCases())
    )
    val overviewState by viewModel.overviewState.collectAsState()
    val user          by SessionManager.user.collectAsState()
    val overview = (overviewState as? UiState.Success<HomeOverview>)?.data

    // ── Entrance animation ────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    // ── Banner data — existing lambdas wired ─────────────────────────────────
    val banners = remember(onNavigateToEmailCheck, onNavigateToRealityScan, onNavigateToPremium) {
        listOf(
            BannerData(
                title = "Password Breach Awareness",
                subtitle = "Is your email compromised? Check now.",
                ctaLabel = "Check Now",
                gradientStart = Color(0xFF1A237E),
                gradientEnd   = Color(0xFF3949AB),
                illustrationType = BannerIllustration.Lock,
                onClick = onNavigateToEmailCheck
            ),
            BannerData(
                title = "Reality Scanner",
                subtitle = "Detect deepfakes & fake content instantly",
                ctaLabel = "Scan Now",
                gradientStart = Color(0xFF0D3B2E),
                gradientEnd   = Color(0xFF1B6B4A),
                illustrationType = BannerIllustration.Scanner,
                onClick = onNavigateToRealityScan
            ),
            BannerData(
                title = "GoSuraksha Premium",
                subtitle = "Full protection. Zero worry.",
                ctaLabel = "Explore Plan",
                gradientStart = Color(0xFF1A1200),
                gradientEnd   = Color(0xFF7C4F00),
                illustrationType = BannerIllustration.Diamond,
                onClick = onNavigateToPremium
            )
        )
    }

    // ── Shortcut sections ─────────────────────────────────────────────────────
    // Row A — Primary tools (Cyber SOS, Reality Scan, Email Check, Password)
    val primaryRow1 = listOf(
        ShortcutItem(stringResource(R.string.ui_apptopbar_1),
            Icons.Rounded.Warning, ObsidianColors.Critical, onNavigateToCyberSos),
        ShortcutItem(stringResource(R.string.home_quick_reality_scan),
            Icons.Rounded.CameraAlt, ObsidianColors.Sky, onNavigateToRealityScan),
        ShortcutItem(stringResource(R.string.home_quick_email_scan),
            Icons.Rounded.Email, ObsidianColors.Emerald, onNavigateToEmailCheck),
        ShortcutItem(stringResource(R.string.scan_tab_password),
            Icons.Rounded.Password, ObsidianColors.Violet, onNavigateToSecuritySettings)
    )

    // Row B — Scan tools (Link, QR, Risk Intel, Alerts)
    val primaryRow2 = listOf(
        ShortcutItem(stringResource(R.string.home_quick_link_scanner),
            Icons.Rounded.Link, ObsidianColors.Cyan, onNavigateToRealityScan),
        ShortcutItem(stringResource(R.string.home_quick_qr_scanner),
            Icons.Rounded.QrCode, ObsidianColors.Teal, onNavigateToRealityScan),
        ShortcutItem(stringResource(R.string.home_quick_risk_intel),
            Icons.Rounded.Shield, ObsidianColors.Brand, onNavigateToRisk),
        ShortcutItem(stringResource(R.string.ui_mainshell_11),
            Icons.Rounded.Notifications, ObsidianColors.Warning, onNavigateToAlerts)
    )

    // Row C — Family & history (Family, History, Reports, Messages)
    val familyRow = listOf(
        ShortcutItem(stringResource(R.string.alerts_tab_family),
            Icons.Rounded.FamilyRestroom, ObsidianColors.Rose, onNavigateToFamily),
        ShortcutItem(stringResource(R.string.home_quick_history),
            Icons.Rounded.History, ObsidianColors.Amber, onNavigateToHistory),
        ShortcutItem(stringResource(R.string.home_quick_reports),
            Icons.Rounded.Report, ObsidianColors.Forest, onNavigateToHistory),
        ShortcutItem(stringResource(R.string.ui_mainshell_10),
            Icons.AutoMirrored.Rounded.Article, ObsidianColors.Gold, onNavigateToNews)
    )

    // Row D1 — Settings tools
    val settingsRow = listOf(
        ShortcutItem(stringResource(R.string.ui_mainshell_12),
            Icons.Rounded.Person, ObsidianColors.Neutral, onNavigateToSecuritySettings),
        ShortcutItem(stringResource(R.string.ui_navigationconfig_2),
            Icons.Rounded.Settings, ObsidianColors.Slate, onNavigateToSecuritySettings),
        ShortcutItem(stringResource(R.string.home_quick_language),
            Icons.Rounded.Language, ObsidianColors.WarmGray, onNavigateToSecuritySettings),
        ShortcutItem(stringResource(R.string.home_quick_theme),
            Icons.Rounded.Palette, ObsidianColors.Indigo, onNavigateToSecuritySettings)
    )

    // Row D2 — More
    val moreRow = listOf(
        ShortcutItem(stringResource(R.string.home_quick_tips),
            Icons.Rounded.Lightbulb, ObsidianColors.Gold, onNavigateToNews),
        ShortcutItem(stringResource(R.string.home_quick_premium),
            Icons.Rounded.Diamond, ObsidianColors.Sand, onNavigateToPremium),
        ShortcutItem(stringResource(R.string.home_quick_refer),
            Icons.Rounded.Star, ObsidianColors.Orange, onNavigateToNews),
        ShortcutItem(stringResource(R.string.home_quick_refer),
            Icons.AutoMirrored.Rounded.Message, ObsidianColors.Sky, onNavigateToNews)
    )

    // ── Theme tokens ──────────────────────────────────────────────────────────
    val screenBg = if (isDark) ObsidianColors.DarkBg else ObsidianColors.LightBg
    val H = SpacingTokens.screenPaddingHorizontal   // horizontal screen padding

    // =========================================================================
    // ROOT
    // =========================================================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 92.dp)
        ) {

            // ── 1. Greeting ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(340)) +
                        slideInVertically(tween(340, easing = FastOutSlowInEasing)) { -20 }
            ) {
                GreetingRow(
                    name = user?.name?.split(" ")?.firstOrNull()
                        ?: stringResource(R.string.home_user_fallback),
                    isDark = isDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, start = H, end = H)
                )
            }

            Spacer(Modifier.height(18.dp))

            // ── 2. Security Score Card ───────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(320, delayMillis = 60)) +
                        slideInVertically(tween(320, delayMillis = 60, easing = FastOutSlowInEasing)) { 28 }
            ) {
                when (overviewState) {
                    is UiState.Loading ->
                        LoadingScoreCard(
                            isDark = isDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = H)
                        )
                    is UiState.Success ->
                        SecurityScoreCard(
                            isDark = isDark,
                            scans   = overview?.securitySnapshot?.scansDone ?: 0,
                            threats = overview?.securitySnapshot?.threatsDetected ?: 0,
                            risk    = overview?.securitySnapshot?.overallRisk ?: "Low",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = H)
                        )
                    else ->
                        ErrorScoreCard(
                            isDark = isDark,
                            onRetry = { viewModel.loadOverview() },
                            onHistory = onNavigateToHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = H)
                        )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── 3. Banner Carousel ───────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300, delayMillis = 100))
            ) {
                BannerCarousel(
                    banners = banners,
                    modifier = Modifier.padding(horizontal = H)
                )
            }

            Spacer(Modifier.height(22.dp))

            // ── 4. Core Tools (2 × 4) ────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300, delayMillis = 140))
            ) {
                ShortcutSection(
                    title = "Core Tools",
                    isDark = isDark,
                    rows = listOf(primaryRow1, primaryRow2),
                    modifier = Modifier.padding(horizontal = H)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── 5. Family & Activity (1 × 4) ────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300, delayMillis = 180))
            ) {
                ShortcutSection(
                    title = "Family & Activity",
                    isDark = isDark,
                    rows = listOf(familyRow),
                    modifier = Modifier.padding(horizontal = H)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── 6. Settings & More (2 × 4 with divider) ─────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300, delayMillis = 220))
            ) {
                ShortcutSection(
                    title = "Settings & More",
                    isDark = isDark,
                    rows = listOf(settingsRow, moreRow),
                    modifier = Modifier.padding(horizontal = H)
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// =============================================================================
// GreetingRow
// =============================================================================
@Composable
private fun GreetingRow(
    name: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val onBg    = if (isDark) Color(0xFFE6E9F4) else Color(0xFF0D0F1A)
    val subAlpha = if (isDark) 0.45f else 0.48f

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = getGreetingText(),
                fontSize = 12.sp,
                color = onBg.copy(alpha = subAlpha),
                letterSpacing = 0.4.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = onBg,
                    letterSpacing = (-0.5).sp
                )
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = ObsidianColors.Brand.copy(alpha = 0.80f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        // Search pill
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = ObsidianColors.Brand.copy(alpha = if (isDark) 0.12f else 0.09f),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = ObsidianColors.Brand,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// =============================================================================
// SecurityScoreCard
// Glass card with animated ring, score number, safe badge, 3 stat chips.
// =============================================================================
@Composable
private fun SecurityScoreCard(
    isDark: Boolean,
    scans: Int,
    threats: Int,
    risk: String,
    modifier: Modifier = Modifier
) {
    val surface = if (isDark) ObsidianColors.DarkSurface else ObsidianColors.LightSurface
    val border  = if (isDark) null else BorderStroke(1.dp, ObsidianColors.LightBorder)
    val onSurf  = if (isDark) Color(0xFFE6E9F4) else Color(0xFF0D0F1A)

    // Gradient overlay for the card background
    val cardBrush = if (isDark) {
        Brush.linearGradient(
            listOf(
                Color(0xFF1A2040).copy(alpha = 0.95f),
                Color(0xFF0D1630).copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFFEEF3FF),
                Color(0xFFF8F0FF)
            )
        )
    }

    // Animated ring sweep
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue  = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val safeColor = if (threats == 0) ObsidianColors.Safe else ObsidianColors.Warning

    Surface(
        modifier  = modifier,
        shape     = RoundedCornerShape(22.dp),
        color     = Color.Transparent,
        border    = border,
        shadowElevation = if (isDark) 6.dp else 1.dp,
        tonalElevation  = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
        ) {
            // Glow blob top-right
            if (isDark) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    ObsidianColors.Brand.copy(alpha = glowAlpha * 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top row: ring + info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Score ring
                    ScoreRing(
                        score  = 82,
                        isDark = isDark,
                        modifier = Modifier.size(76.dp)
                    )

                    // Info block
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Strong Protection",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurf,
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            text = "2 recommendations to review",
                            fontSize = 12.sp,
                            color = onSurf.copy(alpha = 0.50f),
                            lineHeight = 16.sp
                        )
                        // Protected badge
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(safeColor.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(safeColor, CircleShape)
                            )
                            Text(
                                text = if (threats == 0) "Protected" else "$threats Threat${if (threats > 1) "s" else ""}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = safeColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            if (isDark) Color.White.copy(0.07f)
                            else Color(0xFF0D0F1A).copy(0.07f)
                        )
                )

                Spacer(Modifier.height(14.dp))

                // 3 stat chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatChip(
                        label = "Scans",
                        value = scans.toString(),
                        accent = ObsidianColors.Brand,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        label = "Threats",
                        value = threats.toString(),
                        accent = if (threats == 0) ObsidianColors.Safe else ObsidianColors.Critical,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        label = "Risk",
                        value = risk,
                        accent = when (risk.lowercase()) {
                            "low"    -> ObsidianColors.Safe
                            "medium" -> ObsidianColors.Warning
                            else     -> ObsidianColors.Critical
                        },
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// =============================================================================
// ScoreRing — Canvas drawn arc with gradient simulation
// =============================================================================
@Composable
private fun ScoreRing(
    score: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val onSurf = if (isDark) Color(0xFFE6E9F4) else Color(0xFF0D0F1A)
    val trackColor = if (isDark) Color.White.copy(0.07f) else Color(0xFF0D0F1A).copy(0.07f)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Track
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color      = trackColor,
                startAngle = -230f,
                sweepAngle = 280f,
                useCenter  = false,
                style      = stroke
            )
        }
        // Fill arc (blue)
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            val sweep  = (score / 100f) * 280f
            drawArc(
                color      = ObsidianColors.Brand,
                startAngle = -230f,
                sweepAngle = sweep,
                useCenter  = false,
                style      = stroke
            )
        }
        // Score text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "$score",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = onSurf,
                lineHeight = 18.sp
            )
            Text(
                text     = "/ 100",
                fontSize = 9.sp,
                color    = onSurf.copy(alpha = 0.40f),
                letterSpacing = 0.2.sp
            )
        }
    }
}

// =============================================================================
// StatChip
// =============================================================================
@Composable
private fun StatChip(
    label: String,
    value: String,
    accent: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val chipBg  = if (isDark) Color.White.copy(0.04f) else Color(0xFF0D0F1A).copy(0.04f)
    val onSurf  = if (isDark) Color(0xFFE6E9F4) else Color(0xFF0D0F1A)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chipBg)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text       = value,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Bold,
            color      = accent,
            lineHeight = 15.sp
        )
        Text(
            text     = label,
            fontSize = 10.sp,
            color    = onSurf.copy(alpha = 0.45f),
            letterSpacing = 0.3.sp
        )
    }
}

// =============================================================================
// ShortcutSection
// Generic bento card that accepts N rows of 4 shortcuts each.
// Rows are separated by a 1dp tonal divider.
// =============================================================================
@Composable
private fun ShortcutSection(
    title: String,
    isDark: Boolean,
    rows: List<List<ShortcutItem>>,
    modifier: Modifier = Modifier
) {
    val surface  = if (isDark) ObsidianColors.DarkSurface  else ObsidianColors.LightSurface
    val border   = if (isDark) null                         else BorderStroke(1.dp, ObsidianColors.LightBorder)
    val divider  = if (isDark) Color.White.copy(0.06f)      else Color(0xFF0D0F1A).copy(0.06f)
    val titleClr = if (isDark) Color(0xFFE6E9F4).copy(0.38f) else Color(0xFF0D0F1A).copy(0.38f)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Section label
        Text(
            text = title.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.4.sp,
            color = titleClr
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            color    = surface,
            border   = border,
            shadowElevation = if (isDark) 4.dp else 1.dp,
            tonalElevation  = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                rows.forEachIndexed { index, row ->
                    if (index > 0) {
                        // Tonal divider between rows
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(divider)
                        )
                        Spacer(Modifier.height(10.dp))
                    } else {
                        Spacer(Modifier.height(4.dp))
                    }
                    ShortcutRow(
                        items  = row,
                        isDark = isDark
                    )
                    if (index == rows.lastIndex) Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

// =============================================================================
// ShortcutRow — exactly 4 tiles in a Row, equal weight
// =============================================================================
@Composable
private fun ShortcutRow(
    items: List<ShortcutItem>,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            ShortcutTile(
                item   = item,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// =============================================================================
// ShortcutTile — icon chip + label
// =============================================================================
@Composable
private fun ShortcutTile(
    item: ShortcutItem,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val tileBg   = if (isDark) Color.White.copy(0.04f) else item.accent.copy(0.06f)
    val labelClr = if (isDark) Color(0xFFE6E9F4).copy(0.60f) else Color(0xFF0D0F1A).copy(0.58f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tileBg)
            .clickable { item.onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Icon chip
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    color = item.accent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = item.accent,
                modifier = Modifier.size(17.dp)
            )
        }
        // Label
        Text(
            text      = item.label,
            fontSize  = 9.5.sp,
            color     = labelClr,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            maxLines  = 2,
            overflow  = TextOverflow.Ellipsis
        )
    }
}

// =============================================================================
// LoadingScoreCard
// =============================================================================
@Composable
private fun LoadingScoreCard(isDark: Boolean, modifier: Modifier = Modifier) {
    val surface = if (isDark) ObsidianColors.DarkSurface else ObsidianColors.LightSurface
    val border  = if (isDark) null else BorderStroke(1.dp, ObsidianColors.LightBorder)
    val onSurf  = if (isDark) Color(0xFFE6E9F4) else Color(0xFF0D0F1A)

    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(22.dp),
        color    = surface,
        border   = border,
        shadowElevation = if (isDark) 4.dp else 1.dp,
        tonalElevation  = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier    = Modifier.size(20.dp),
                color       = ObsidianColors.Brand,
                strokeWidth = 2.5.dp
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text  = stringResource(R.string.common_loading),
                    style = TypographyTokens.titleSmall,
                    color = onSurf
                )
                Text(
                    text  = stringResource(R.string.home_snapshot_title),
                    style = TypographyTokens.bodySmall,
                    color = onSurf.copy(alpha = 0.45f)
                )
            }
        }
    }
}

// =============================================================================
// ErrorScoreCard
// =============================================================================
@Composable
private fun ErrorScoreCard(
    isDark: Boolean,
    onRetry: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surface = if (isDark) ObsidianColors.DarkSurface else ObsidianColors.LightSurface
    val border  = if (isDark) null else BorderStroke(1.dp, ObsidianColors.LightBorder)
    val onSurf  = if (isDark) Color(0xFFE6E9F4) else Color(0xFF0D0F1A)

    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(22.dp),
        color    = surface,
        border   = border,
        shadowElevation = if (isDark) 4.dp else 1.dp,
        tonalElevation  = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text  = stringResource(R.string.home_snapshot_title),
                style = TypographyTokens.titleSmall,
                color = onSurf
            )
            Text(
                text  = stringResource(R.string.error_generic),
                style = TypographyTokens.bodyMedium,
                color = onSurf.copy(alpha = 0.48f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick  = onRetry,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = ObsidianColors.Brand,
                        contentColor   = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp)
                ) {
                    Text(
                        text  = stringResource(R.string.ui_screenlayouts_1),
                        style = TypographyTokens.labelLarge
                    )
                }
                OutlinedButton(
                    onClick  = onHistory,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = BorderStroke(
                        1.dp,
                        if (isDark) ObsidianColors.DarkBorder else ObsidianColors.LightBorder
                    ),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = ObsidianColors.Slate
                    )
                ) {
                    Text(
                        text  = stringResource(R.string.home_quick_history),
                        style = TypographyTokens.labelLarge
                    )
                }
            }
        }
    }
}

// =============================================================================
// Preserved from original — no logic changes
// =============================================================================
@Composable
private fun getGreetingText(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> stringResource(R.string.home_greeting_morning)
        hour < 17 -> stringResource(R.string.home_greeting_afternoon)
        else      -> stringResource(R.string.home_greeting_evening)
    }
}