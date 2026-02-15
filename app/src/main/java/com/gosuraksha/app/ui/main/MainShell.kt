package com.gosuraksha.app.ui.main

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.gosuraksha.app.navigation.MainRoutes
import com.gosuraksha.app.navigation.Routes
import com.gosuraksha.app.ui.components.AppTopBar
import com.gosuraksha.app.ui.history.HistoryScreen
import com.gosuraksha.app.ui.home.HomeScreen
import com.gosuraksha.app.ui.screens.CyberSosScreen

// ── Palette (matches ScanScreen) ─────────────────────────────────────────────
private val NavBg        = Color(0xFF0D1117)
private val NavSurface   = Color(0xFF161B22)
private val NavBorder    = Color(0xFF30363D)
private val CyberTeal    = Color(0xFF00E5C3)
private val TextPrimary  = Color(0xFFE6EDF3)
private val TextMuted    = Color(0xFF8B949E)

// ── Nav item model ────────────────────────────────────────────────────────────
private data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navItems = listOf(
    NavItem(MainRoutes.Home.route,    "Home",    Icons.Filled.Home,          Icons.Outlined.Home),
    NavItem(MainRoutes.News.route,    "News",    Icons.Filled.Article,       Icons.Outlined.Article),
    // CENTER SLOT — reserved for FAB, not a real nav item
    NavItem(MainRoutes.Alerts.route,  "Alerts",  Icons.Filled.Notifications, Icons.Outlined.Notifications),
    NavItem(MainRoutes.Profile.route, "Profile", Icons.Filled.Person,        Icons.Outlined.Person)
)

@SuppressLint("ComposableDestinationInComposeScope")
@Composable
fun MainShell(onLogout: () -> Unit) {

    val navController  = rememberNavController()
    val currentRoute   = navController.currentBackStackEntryAsState().value?.destination?.route
    val isScanSelected = currentRoute == MainRoutes.Scan.route

    Scaffold(
        containerColor = NavBg,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AppTopBar(
                onCyberSosClick = {
                    navController.navigate("cyber_sos") {
                        launchSingleTop = true
                    }
                }
            )
        },

        bottomBar = {
            GlassNavBar(
                currentRoute    = currentRoute,
                isScanSelected  = isScanSelected,
                onNavClick      = { route ->
                    navController.navigate(route) { launchSingleTop = true }
                },
                onScanClick     = {
                    navController.navigate(MainRoutes.Scan.route) { launchSingleTop = true }
                }
            )
        }
    ) { padding ->

        NavHost(
            navController    = navController,
            startDestination = MainRoutes.Home.route,
            modifier         = Modifier.padding(padding)
        ) {
            composable(MainRoutes.Home.route) {
                HomeScreen(

                    onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                    onNavigateToRisk    = { navController.navigate("risk_internal") }
                )

            }
            composable(MainRoutes.News.route)    { NewsScreen() }
            composable(MainRoutes.Scan.route)    { ScanScreen() }
            composable(MainRoutes.Alerts.route)  { AlertsScreen() }
            composable(MainRoutes.Profile.route) { ProfileScreen(onLogout = onLogout) }
            composable(Routes.HISTORY)           { HistoryScreen(onBack = { navController.popBackStack() }) }
            composable("risk_internal")          { RiskScreen() }
            composable("cyber_sos") { CyberSosScreen() }

        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GlassNavBar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GlassNavBar(
    currentRoute: String?,
    isScanSelected: Boolean,
    onNavClick: (String) -> Unit,
    onScanClick: () -> Unit
) {
    // Outer box gives room for the FAB to overflow upward
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // ── GLASS BAR ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 0.dp)
                .shadow(
                    elevation = 32.dp,
                    shape     = RoundedCornerShape(28.dp),
                    ambientColor = CyberTeal.copy(alpha = 0.08f),
                    spotColor    = Color.Black.copy(alpha = 0.9f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1C2333),
                            Color(0xFF0D1117)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            // Four items: 2 left of FAB, 2 right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // LEFT TWO
                navItems.take(2).forEach { item ->
                    GlassNavItem(
                        item       = item,
                        isSelected = currentRoute == item.route,
                        onClick    = { onNavClick(item.route) },
                        modifier   = Modifier.weight(1f)
                    )
                }

                // CENTER SPACER — FAB placeholder
                Spacer(Modifier.weight(1.2f))

                // RIGHT TWO
                navItems.drop(2).forEach { item ->
                    GlassNavItem(
                        item       = item,
                        isSelected = currentRoute == item.route,
                        onClick    = { onNavClick(item.route) },
                        modifier   = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── SCAN FAB ────────────────────────────────────────────────
        ScanFab(
            isSelected = isScanSelected,
            onClick    = onScanClick,
            modifier   = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Individual nav item
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GlassNavItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue    = if (isSelected) CyberTeal else TextMuted,
        animationSpec  = tween(250),
        label          = "nav_icon_color"
    )
    val bgAlpha by animateFloatAsState(
        targetValue    = if (isSelected) 1f else 0f,
        animationSpec  = tween(250),
        label          = "nav_bg_alpha"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CyberTeal.copy(alpha = bgAlpha * 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector     = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint            = iconColor,
                modifier        = Modifier.size(20.dp)
            )
        }

        Text(
            text       = item.label,
            color      = iconColor,
            fontSize   = 0.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Scan FAB  — oversized, animated, floating above nav bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ScanFab(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Continuous rotation when selected
    val infiniteRotation = rememberInfiniteTransition(label = "fab_ring_rot")
    val ringAngle by infiniteRotation.animateFloat(
        initialValue   = 0f,
        targetValue    = 360f,
        animationSpec  = infiniteRepeatable(tween(3500, easing = LinearEasing)),
        label          = "ring_angle"
    )

    // Pulse scale when selected
    val infinitePulse = rememberInfiniteTransition(label = "fab_pulse")
    val pulseScale by infinitePulse.animateFloat(
        initialValue   = 1f,
        targetValue    = 1.08f,
        animationSpec  = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label          = "pulse_scale"
    )

    // Press scale
    val pressScale by animateFloatAsState(
        targetValue   = if (isSelected) pulseScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "fab_scale"
    )

    // Glow alpha
    val glowAlpha by animateFloatAsState(
        targetValue   = if (isSelected) 0.55f else 0.22f,
        animationSpec = tween(400),
        label         = "fab_glow"
    )

    Box(
        modifier        = modifier.size(72.dp),
        contentAlignment = Alignment.Center
    ) {

        // ── OUTER GLOW RING ────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            CyberTeal.copy(alpha = glowAlpha),
                            CyberTeal.copy(alpha = 0f)
                        )
                    )
                )
                .blur(8.dp)
        )

        // ── SPINNING DASHED RING ───────────────────────────────────
        if (isSelected) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(68.dp)
                    .graphicsLayer { rotationZ = ringAngle }
            ) {
                val strokeWidth = 2.dp.toPx()
                val radius      = size.minDimension / 2f - strokeWidth
                drawCircle(
                    color  = CyberTeal.copy(alpha = 0.5f),
                    radius = radius,
                    style  = androidx.compose.ui.graphics.drawscope.Stroke(
                        width      = strokeWidth,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(12f, 8f), 0f
                        )
                    )
                )
            }
        }

        // ── FAB BUTTON ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(58.dp)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .shadow(
                    elevation    = if (isSelected) 20.dp else 10.dp,
                    shape        = CircleShape,
                    ambientColor = CyberTeal.copy(alpha = 0.4f),
                    spotColor    = CyberTeal.copy(alpha = 0.6f)
                )
                .clip(CircleShape)
                .background(
                    if (isSelected)
                        Brush.radialGradient(
                            listOf(Color(0xFF00E5C3), Color(0xFF00A896))
                        )
                    else
                        Brush.radialGradient(
                            listOf(Color(0xFF1E2A35), Color(0xFF161B22))
                        )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            CyberTeal.copy(alpha = if (isSelected) 0.9f else 0.35f),
                            CyberTeal.copy(alpha = if (isSelected) 0.4f else 0.1f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Filled.Search,
                contentDescription = "Scan",
                tint               = if (isSelected) Color(0xFF0D1117) else CyberTeal,
                modifier           = Modifier.size(26.dp)
            )
        }

        // ── SCAN LABEL ─────────────────────────────────────────────
        Text(
            text       = "Scan",
            color      = if (isSelected) CyberTeal else TextMuted,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier   = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 4.dp)
        )
    }
}