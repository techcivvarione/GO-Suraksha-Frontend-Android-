package com.gosuraksha.app.ui.main

// =============================================================================
// BottomNavigationBar.kt — PhonePe-style redesign
//
// Layout (exact PhonePe):
//   Home | Search | [Scan FAB floating center] | Family | History
//   • Flat dark/light bar — no pill, no shadow card
//   • Center Scan button: 52dp circle, purple, floats above bar
//   • Active item: icon + label in white, inactive: icon only dimmed
//   • No gradient pill — pure PhonePe flat style
//   • ViewModel / nav wiring unchanged
// =============================================================================

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.R
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.navigation.Screen

// ── Colours ───────────────────────────────────────────────────────────────────
private val NavBgDark    = Color(0xFF0F0F1A)
private val NavBgLight   = Color(0xFFFFFFFF)
private val ActiveDark   = Color(0xFFFFFFFF)
private val ActiveLight  = Color(0xFF111111)
private val InactiveDark = Color(0xFF555570)
private val InactiveLight= Color(0xFFAAAAAA)
private val ScanPurple   = Color(0xFF7C3AED)
private val TopBorderDark  = Color(0xFF2A2A3E)
private val TopBorderLight = Color(0xFFF0F0F0)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Left items — Home, Search
    val leftItems = listOf(
        NavItem(Screen.Home.route,   stringResource(R.string.ui_mainshell_9), Icons.Rounded.Home),
        NavItem(Screen.Search.route, "Search",                                 Icons.Rounded.Search)
    )
    // Right items — Family (Threat Center = AlertsScreen with 3 tabs), History
    val rightItems = listOf(
        NavItem(Screen.Alerts.route, "Family",  Icons.Rounded.FamilyRestroom),
        NavItem(Screen.History.route,            "History", Icons.Rounded.History)
    )

    val navBg    = if (isDark) NavBgDark  else NavBgLight
    val topLine  = if (isDark) TopBorderDark else TopBorderLight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(navBg)
            .navigationBarsPadding()
    ) {
        // Top border line (exact PhonePe)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(topLine)
                .align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Left two items
            leftItems.forEach { item ->
                PhonePeNavItem(
                    item       = item,
                    isSelected = currentRoute == item.route,
                    isDark     = isDark,
                    modifier   = Modifier.weight(1f)
                ) {
                    if (BuildConfig.DEBUG) Log.d("NAV", "Bottom nav: ${item.route}")
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) { launchSingleTop = true }
                    }
                }
            }

            // Center spacer for FAB
            Spacer(Modifier.weight(1f))

            // Right two items
            rightItems.forEach { item ->
                PhonePeNavItem(
                    item       = item,
                    isSelected = currentRoute == item.route,
                    isDark     = isDark,
                    modifier   = Modifier.weight(1f)
                ) {
                    if (BuildConfig.DEBUG) Log.d("NAV", "Bottom nav: ${item.route}")
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) { launchSingleTop = true }
                    }
                }
            }
        }

        // Center floating Check FAB — lifts above bar (PhonePe-style)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer ring separator (matches nav background for clean lift effect)
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(navBg),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(ScanPurple)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) {
                            if (currentRoute != Screen.Scan.route) {
                                navController.navigate(Screen.Scan.route) { launchSingleTop = true }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Security,
                        contentDescription = "Check",
                        tint               = Color.White,
                        modifier           = Modifier.size(22.dp)
                    )
                }
            }
        }

        // "Check" label under FAB — clear action name, not confusing QR/scan label
        Text(
            text       = "Check",
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
            color      = ScanPurple,
            modifier   = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        )
    }
}

// =============================================================================
// PhonePeNavItem — flat icon + label, no pill background
// =============================================================================
@Composable
private fun PhonePeNavItem(
    item:       NavItem,
    isSelected: Boolean,
    isDark:     Boolean,
    modifier:   Modifier,
    onClick:    () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val activeColor   = if (isDark) ActiveDark   else ActiveLight
    val inactiveColor = if (isDark) InactiveDark else InactiveLight

    val iconScale by animateFloatAsState(
        targetValue   = if (isSelected) 1.12f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label         = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = item.icon,
            contentDescription = item.label,
            tint               = if (isSelected) activeColor else inactiveColor,
            modifier           = Modifier
                .size(22.dp)
                .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text       = item.label,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color      = if (isSelected) activeColor else inactiveColor
        )
    }
}