package com.gosuraksha.app.ui.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Security
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

internal object DashboardColors {
    val BackgroundLight = Color(0xFFF4F7FC)
    val BackgroundDark = Color(0xFF071019)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF0D1823)
    val BorderLight = Color(0xFFE3EBF5)
    val BorderDark = Color(0xFF183040)
    val TextMutedLight = Color(0xFF64748B)
    val TextMutedDark = Color(0xFF8FA3B8)
    val Brand = Color(0xFF0B6BFF)
    val BrandDark = Color(0xFF0C4FD8)
    val Mint = Color(0xFF19C37D)
    val Amber = Color(0xFFF59E0B)
    val Red = Color(0xFFF04438)
    val Cyan = Color(0xFF06B6D4)
    val Violet = Color(0xFF7C3AED)
}

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
    onNavigateToHeatmap: () -> Unit = {},
    onNavigateToScamNetwork: () -> Unit = {},
    onNavigateToScamLookup: () -> Unit = {},
    onNavigateToReportScam: () -> Unit = {},
    onNavigateToScamAlertsFeed: () -> Unit = {}
) {
    val isDark = ColorTokens.LocalAppDarkMode.current
    val listState = rememberLazyListState()

    val appContext = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val provider = appContext as HomeUseCaseProvider
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(provider.homeUseCases()))
    val overviewState by viewModel.overviewState.collectAsStateWithLifecycle()
    val user by SessionManager.user.collectAsStateWithLifecycle()
    val overview = (overviewState as? UiState.Success<HomeOverview>)?.data

    val banners = remember(
        onNavigateToHeatmap,
        onNavigateToScamNetwork,
        onNavigateToReportScam,
        onNavigateToNews
    ) {
        listOf(
            BannerData(
                title = "Cyber Attack Radar",
                subtitle = "See city to global attack intensity in a live cyber map.",
                ctaLabel = "Open Radar",
                gradientStart = Color(0xFF071D49),
                gradientEnd = Color(0xFF0B6BFF),
                illustrationType = BannerIllustration.Scanner,
                onClick = onNavigateToHeatmap
            ),
            BannerData(
                title = "Scam Alert Network",
                subtitle = "Check trending scams, live warnings, and community reports.",
                ctaLabel = "Open Network",
                gradientStart = Color(0xFF0F3A2E),
                gradientEnd = Color(0xFF19C37D),
                illustrationType = BannerIllustration.Lock,
                onClick = onNavigateToScamNetwork
            ),
            BannerData(
                title = "Report a Scam",
                subtitle = "Help protect others by filing calls, links, or payment fraud reports.",
                ctaLabel = "Report Now",
                gradientStart = Color(0xFF4A1607),
                gradientEnd = Color(0xFFF04438),
                illustrationType = BannerIllustration.Diamond,
                onClick = onNavigateToReportScam
            ),
            BannerData(
                title = "Cyber Awareness",
                subtitle = "Read fresh security updates and practical scam prevention tips.",
                ctaLabel = "Read News",
                gradientStart = Color(0xFF4C1D95),
                gradientEnd = Color(0xFF7C3AED),
                illustrationType = BannerIllustration.Diamond,
                onClick = onNavigateToNews
            )
        )
    }

    val quickActions = remember(
        onNavigateToRealityScan,
        onNavigateToScamLookup,
        onNavigateToHeatmap,
        onNavigateToReportScam,
        onNavigateToScamAlertsFeed,
        onNavigateToCyberCard,
        onNavigateToScamNetwork,
        onNavigateToNews
    ) {
        listOf(
            HomeQuickAction("Scan QR", Icons.Rounded.QrCode, DashboardColors.Brand, onNavigateToRealityScan),
            HomeQuickAction("Scam Lookup", Icons.Rounded.Search, DashboardColors.Mint, onNavigateToScamLookup),
            HomeQuickAction("Cyber Radar", Icons.Rounded.Warning, DashboardColors.Red, onNavigateToHeatmap),
            HomeQuickAction("Report Scam", Icons.Rounded.Report, DashboardColors.Amber, onNavigateToReportScam),
            HomeQuickAction("Scam Alerts", Icons.Rounded.Notifications, DashboardColors.Violet, onNavigateToScamAlertsFeed),
            HomeQuickAction("Cyber Card", Icons.Rounded.CreditCard, DashboardColors.BrandDark, onNavigateToCyberCard),
            HomeQuickAction("Scam Network", Icons.Rounded.Groups, DashboardColors.Cyan, onNavigateToScamNetwork),
            HomeQuickAction("News", Icons.AutoMirrored.Rounded.Article, DashboardColors.Mint, onNavigateToNews)
        )
    }

    val sections = remember(
        onNavigateToRealityScan,
        onNavigateToScamLookup,
        onNavigateToHeatmap,
        onNavigateToRisk,
        onNavigateToAlerts,
        onNavigateToReportScam,
        onNavigateToScamNetwork,
        onNavigateToHistory,
        onNavigateToCyberCard,
        onNavigateToFamily,
        onNavigateToNews,
        onNavigateToSecuritySettings,
        onNavigateToCyberSos
    ) {
        listOf(
            HomeFeatureSection(
                title = "Security Tools",
                items = listOf(
                    HomeFeatureAction("Scam Lookup", "Check suspicious numbers before you trust them.", Icons.Rounded.Search, DashboardColors.Mint, onNavigateToScamLookup),
                    HomeFeatureAction("Cyber Radar", "Monitor attack spikes from city to global scope.", Icons.Rounded.Warning, DashboardColors.Red, onNavigateToHeatmap),
                    HomeFeatureAction("QR Scan", "Scan links, QR codes, and suspicious prompts safely.", Icons.Rounded.QrCode, DashboardColors.Brand, onNavigateToRealityScan),
                    HomeFeatureAction("Fraud Detection", "Open internal risk intelligence and detection tools.", Icons.Rounded.Security, DashboardColors.Violet, onNavigateToRisk)
                )
            ),
            HomeFeatureSection(
                title = "Reports & Alerts",
                items = listOf(
                    HomeFeatureAction("Scam Alerts", "See active scam campaigns and high-risk alerts.", Icons.Rounded.Notifications, DashboardColors.Amber, onNavigateToAlerts),
                    HomeFeatureAction("Report Scam", "Submit community reports for calls, links, and fraud.", Icons.Rounded.Report, DashboardColors.Red, onNavigateToReportScam),
                    HomeFeatureAction("Community Reports", "Open the Scam Network intelligence dashboard.", Icons.Rounded.Groups, DashboardColors.Cyan, onNavigateToScamNetwork),
                    HomeFeatureAction("History", "Review previous scans, checks, and activity summaries.", Icons.Rounded.History, DashboardColors.BrandDark, onNavigateToHistory)
                )
            ),
            HomeFeatureSection(
                title = "Services",
                items = listOf(
                    HomeFeatureAction("Cyber Card", "Open your protection score and personal security card.", Icons.Rounded.CreditCard, DashboardColors.Brand, onNavigateToCyberCard),
                    HomeFeatureAction("Trusted Circle", "Manage trusted family and emergency contacts.", Icons.Rounded.Groups, DashboardColors.Mint, onNavigateToFamily),
                    HomeFeatureAction("Awareness", "Read cyber awareness stories and prevention tips.", Icons.AutoMirrored.Rounded.Article, DashboardColors.Violet, onNavigateToNews),
                    HomeFeatureAction("Profile & Settings", "Manage profile, app settings, and preferences.", Icons.Rounded.Person, DashboardColors.Amber, onNavigateToSecuritySettings),
                    HomeFeatureAction("Cyber SOS", "Trigger emergency support for urgent cyber incidents.", Icons.Rounded.Shield, DashboardColors.Red, onNavigateToCyberSos)
                )
            )
        )
    }

    val background = if (isDark) DashboardColors.BackgroundDark else DashboardColors.BackgroundLight
    val horizontalPadding = SpacingTokens.screenPaddingHorizontal

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = 18.dp,
                bottom = 104.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeader(
                    name = user?.name?.split(" ")?.firstOrNull().orEmpty().ifBlank { "Guardian" },
                    isDark = isDark
                )
            }

            item {
                when (overviewState) {
                    is UiState.Loading -> HomeSnapshotLoadingCard(isDark = isDark)
                    is UiState.Success -> SecuritySnapshotCard(
                        scans = overview?.securitySnapshot?.scansDone ?: 0,
                        threats = overview?.securitySnapshot?.threatsDetected ?: 0,
                        risk = overview?.securitySnapshot?.overallRisk ?: "Low"
                    )
                    else -> HomeSnapshotErrorCard(
                        isDark = isDark,
                        onRetry = viewModel::loadOverview,
                        onOpenHistory = onNavigateToHistory
                    )
                }
            }

            item {
                BannerCarousel(banners = banners)
            }

            item {
                QuickActionsGridCard(
                    actions = quickActions,
                    isDark = isDark
                )
            }

            items(sections.size) { index ->
                FeatureSectionCard(
                    section = sections[index],
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    name: String,
    isDark: Boolean
) {
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val muted = if (isDark) DashboardColors.TextMutedDark else DashboardColors.TextMutedLight

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = getGreetingText(),
                style = TypographyTokens.bodySmall,
                color = muted
            )
            Text(
                text = name,
                style = TypographyTokens.screenTitle,
                color = textColor
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = DashboardColors.Brand.copy(alpha = if (isDark) 0.22f else 0.10f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = DashboardColors.Brand,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun QuickActionsGridCard(
    actions: List<HomeQuickAction>,
    isDark: Boolean
) {
    val surface = if (isDark) DashboardColors.SurfaceDark else DashboardColors.SurfaceLight
    val border = if (isDark) DashboardColors.BorderDark else DashboardColors.BorderLight
    val gridHeight = 196.dp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = surface,
        border = BorderStroke(1.dp, border),
        shadowElevation = if (isDark) 8.dp else 2.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = "Quick Actions",
                subtitle = "Fast access to the most-used protection tools."
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(actions) { action ->
                    QuickActionItem(action = action, isDark = isDark)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Text(
        text = title,
        style = TypographyTokens.titleMedium,
        color = ColorTokens.textPrimary()
    )
    Text(
        text = subtitle,
        style = TypographyTokens.bodySmall,
        color = ColorTokens.textSecondary()
    )
}

@Composable
private fun HomeSnapshotLoadingCard(isDark: Boolean) {
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val surface = if (isDark) DashboardColors.SurfaceDark else DashboardColors.SurfaceLight
    val border = if (isDark) DashboardColors.BorderDark else DashboardColors.BorderLight

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = surface,
        border = BorderStroke(1.dp, border),
        shadowElevation = if (isDark) 8.dp else 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = DashboardColors.Brand,
                strokeWidth = 2.5.dp
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Loading dashboard", style = TypographyTokens.titleSmall, color = textColor)
                Text("Refreshing your security snapshot", style = TypographyTokens.bodySmall, color = ColorTokens.textSecondary())
            }
        }
    }
}

@Composable
private fun HomeSnapshotErrorCard(
    isDark: Boolean,
    onRetry: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val surface = if (isDark) DashboardColors.SurfaceDark else DashboardColors.SurfaceLight
    val border = if (isDark) DashboardColors.BorderDark else DashboardColors.BorderLight

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = surface,
        border = BorderStroke(1.dp, border),
        shadowElevation = if (isDark) 8.dp else 2.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Dashboard temporarily unavailable", style = TypographyTokens.titleMedium, color = ColorTokens.textPrimary())
            Text(
                "We couldn't refresh your latest security summary. You can retry or open scan history.",
                style = TypographyTokens.bodyMedium,
                color = ColorTokens.textSecondary()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DashboardColors.Brand)
                ) {
                    Text("Retry")
                }
                OutlinedButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("History")
                }
            }
        }
    }
}

@Composable
private fun getGreetingText(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}


