package com.gosuraksha.app.ui.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.navigation.Screen
import com.gosuraksha.app.scam.ScamAlertNavigationStore
import com.gosuraksha.app.scam.ui.ReportScamScreen
import com.gosuraksha.app.scam.ui.ScamAlertDetailScreen
import com.gosuraksha.app.scam.ui.ScamAlertHubScreen
import com.gosuraksha.app.scam.ui.ScamAlertsFeedScreen
import com.gosuraksha.app.scam.ui.ScamHeatmapScreen
import com.gosuraksha.app.scam.ui.ScamNumberCheckerScreen
import com.gosuraksha.app.ui.components.EnterpriseTopBar
import com.gosuraksha.app.ui.home.HomeScreen
import com.gosuraksha.app.ui.history.HistoryScreen
import com.gosuraksha.app.scan.core.ScanScreen
import com.gosuraksha.app.scan.SharedScanIntentStore
import com.gosuraksha.app.ui.trusted.TrustedContactsScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ─────────────────────────────────────────────────────────────────────────────
// Data + Palette
// ─────────────────────────────────────────────────────────────────────────────

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private object NavPalette {
    val gradientStart = Color(0xFF00C9A7)
    val gradientEnd   = Color(0xFF0077FF)
    val onActive      = Color(0xFF07090F)

    @Composable
    fun surface(): Color = MaterialTheme.colorScheme.surface

    @Composable
    fun border(): Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

    @Composable
    fun inactive(isDark: Boolean): Color =
        if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
}

// ─────────────────────────────────────────────────────────────────────────────
// MainShell
// ─────────────────────────────────────────────────────────────────────────────
@SuppressLint("ComposableDestinationInComposeScope")
@Composable
fun MainShell(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val isDark        = ColorTokens.LocalAppDarkMode.current
    val screenBg      = ColorTokens.background()
    val sharedPayload by SharedScanIntentStore.pending.collectAsStateWithLifecycle()
    val pendingScamRoute by ScamAlertNavigationStore.pending.collectAsStateWithLifecycle()

    LaunchedEffect(sharedPayload) {
        if (sharedPayload != null && navController.currentDestination?.route != Screen.Scan.route) {
            navController.navigate(Screen.Scan.route) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(pendingScamRoute) {
        val route = pendingScamRoute ?: return@LaunchedEffect
        val destination = if (route.alertId.isNullOrBlank()) {
            route.route
        } else {
            Screen.ScamAlertDetail.createRoute(route.alertId)
        }
        navController.navigate(destination) { launchSingleTop = true }
        ScamAlertNavigationStore.consume()
    }

    Scaffold(
        modifier            = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        containerColor      = screenBg,
        topBar = {
            EnterpriseTopBar(
                onCyberSosClick = {
                    navController.navigate(Screen.CyberSos.route) {
                        launchSingleTop = true
                    }
                }
            )
        },
        bottomBar = {
            LiquidExpanderNav(
                navController = navController,
                isDark        = isDark,
            )
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToHistory          = { navController.navigate(Screen.History.route) },
                    onNavigateToRisk             = { navController.navigate(Screen.RiskInternal.route) },
                    onNavigateToRealityScan      = { navController.navigate(Screen.Scan.route) },
                    onNavigateToCyberCard        = { navController.navigate(Screen.CyberCard.route) },
                    onNavigateToCyberSos         = { navController.navigate(Screen.CyberSos.route) },
                    onNavigateToAlerts           = { navController.navigate(Screen.Alerts.route) },
                    onNavigateToHeatmap          = { navController.navigate(Screen.ScamHeatmap.route) },
                    onNavigateToScamNetwork      = { navController.navigate(Screen.ScamAlertHub.route) },
                    onNavigateToFamily           = { navController.navigate(Screen.TrustedContacts.route) },
                    onNavigateToSecuritySettings = { navController.navigate(Screen.Profile.route) },
                    onNavigateToNews             = { navController.navigate(Screen.News.route) },
                    onNavigateToScamLookup       = { navController.navigate(Screen.CheckNumber.route) },
                    onNavigateToReportScam       = { navController.navigate(Screen.ReportScam.route) },
                    onNavigateToScamAlertsFeed   = { navController.navigate(Screen.ScamAlertsFeed.route) }
                )
            }
            composable(Screen.News.route)    { NewsScreen() }
            composable(Screen.Scan.route)    {
                ScanScreen(onUpgradePlan = { navController.navigate(Screen.Profile.route) })
            }
            composable(Screen.Alerts.route)  {
                AlertsScreen(
                    onOpenScamNetwork = { navController.navigate(Screen.ScamAlertHub.route) },
                    onOpenScamDetail = { navController.navigate(Screen.ScamAlertDetail.createRoute(it)) }
                )
            }
            composable(Screen.Profile.route) { ProfileScreen(onLogout = onLogout) }
            composable(Screen.ScamAlertHub.route) {
                ScamAlertHubScreen(
                    onReportScamClick = { navController.navigate(Screen.ReportScam.route) },
                    onCheckNumberClick = { navController.navigate(Screen.CheckNumber.route) },
                    onHeatmapClick = { navController.navigate(Screen.ScamHeatmap.route) },
                    onTrendingClick = { navController.navigate(Screen.ScamAlertsFeed.route) },
                    onAlertClick = { navController.navigate(Screen.ScamAlertDetail.createRoute(it)) }
                )
            }
            composable(Screen.ReportScam.route) {
                ReportScamScreen(onBackToHub = { navController.popBackStack() })
            }
            composable(Screen.CheckNumber.route) { ScamNumberCheckerScreen() }
            composable(Screen.ScamHeatmap.route) {
                ScamHeatmapScreen(onAlertClick = { navController.navigate(Screen.ScamAlertDetail.createRoute(it)) })
            }
            composable(Screen.ScamAlertsFeed.route) {
                ScamAlertsFeedScreen(onAlertClick = { navController.navigate(Screen.ScamAlertDetail.createRoute(it)) })
            }
            composable(Screen.ScamAlertDetail.route) { backStackEntry ->
                ScamAlertDetailScreen(alertId = backStackEntry.arguments?.getString("alertId"))
            }
            composable(Screen.CyberCard.route) {
                CyberCardScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                    onUpgradePlan = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.TrustedContacts.route) {
                TrustedContactsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.RiskInternal.route) { RiskScreen() }
            composable(Screen.CyberSos.route) {
                com.gosuraksha.app.ui.screens.CyberSosScreen()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LiquidExpanderNav
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LiquidExpanderNav(
    navController: NavHostController,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val navItems = listOf(
        NavItem(Screen.Home.route,    stringResource(R.string.ui_mainshell_9),  Icons.Rounded.Home),
        NavItem(Screen.News.route,    stringResource(R.string.ui_mainshell_10), Icons.Rounded.Article),
        NavItem(Screen.Scan.route,    stringResource(R.string.ui_mainshell_1),  Icons.Rounded.Search),
        NavItem(Screen.Alerts.route,  stringResource(R.string.ui_mainshell_11), Icons.Rounded.Notifications),
        NavItem(Screen.Profile.route, stringResource(R.string.ui_mainshell_12), Icons.Rounded.Person)
    )

    val pillSurface = NavPalette.surface()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(screenBgColor(isDark))        // solid bg behind the pill row
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation    = if (isDark) 28.dp else 12.dp,
                shape        = RoundedCornerShape(26.dp),
                ambientColor = if (isDark) Color.Black.copy(0.9f) else Color.Black.copy(0.08f),
                spotColor    = if (isDark) Color.Black.copy(0.9f) else Color.Black.copy(0.08f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(pillSurface)
            .then(
                if (!isDark) Modifier
                    .padding(1.dp)
                    .background(NavPalette.border(), RoundedCornerShape(25.dp))
                    .padding(1.dp)
                    .background(pillSurface, RoundedCornerShape(24.dp))
                else Modifier
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        navItems.forEach { item ->
            val isSelected  = currentRoute == item.route
            val tabModifier = if (isSelected)
                Modifier.weight(1f).height(52.dp)
            else
                Modifier.size(52.dp)

            LiquidTab(
                item       = item,
                isSelected = isSelected,
                isDark     = isDark,
                modifier   = tabModifier,
                onClick    = {
                    Log.d("NAV", "→ ${item.route}")
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

// helper — matches the scaffold containerColor so the bar bg blends flush
@Composable
private fun screenBgColor(isDark: Boolean): Color =
    ColorTokens.background()

// ─────────────────────────────────────────────────────────────────────────────
// LiquidTab
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LiquidTab(
    item: NavItem,
    isSelected: Boolean,
    isDark: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconScale by animateFloatAsState(
        targetValue   = if (isSelected) 1.10f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "iconScale"
    )

    val labelAlpha by animateFloatAsState(
        targetValue   = if (isSelected) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isSelected) 200 else 100,
            delayMillis    = if (isSelected) 80 else 0,
            easing         = FastOutSlowInEasing
        ),
        label = "labelAlpha"
    )

    val iconAlpha by animateFloatAsState(
        targetValue   = if (isSelected) 1f else 0.38f,
        animationSpec = tween(160, easing = FastOutSlowInEasing),
        label         = "iconAlpha"
    )

    val inactiveIconColor = NavPalette.inactive(isDark)

    if (isSelected) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(NavPalette.gradientStart, NavPalette.gradientEnd)
                    )
                )
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.18f), Color.Transparent)
                    )
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication        = null,
                    onClick           = onClick
                )
                .padding(horizontal = 20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = item.icon,
                contentDescription = item.label,
                tint               = NavPalette.onActive,
                modifier           = Modifier
                    .size(21.dp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
            )
            if (labelAlpha > 0.01f) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = item.label,
                    color      = NavPalette.onActive,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    modifier   = Modifier.graphicsLayer { alpha = labelAlpha }
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication        = null,
                    onClick           = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = item.icon,
                contentDescription = item.label,
                tint               = inactiveIconColor,
                modifier           = Modifier
                    .size(21.dp)
                    .graphicsLayer { alpha = iconAlpha }
            )
        }
    }
}


