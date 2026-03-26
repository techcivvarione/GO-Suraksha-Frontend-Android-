package com.gosuraksha.app.ui.main

import android.annotation.SuppressLint
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.navigation.Screen
import com.gosuraksha.app.scam.ScamAlertNavigationStore
import com.gosuraksha.app.scan.SharedScanIntentStore
import kotlinx.coroutines.launch

@SuppressLint("ComposableDestinationInComposeScope")
@Composable
fun MainShell(onLogout: () -> Unit) {
    val navController   = rememberNavController()
    val isDark          = ColorTokens.LocalAppDarkMode.current
    val drawerState     = rememberDrawerState(DrawerValue.Closed)
    val scope           = rememberCoroutineScope()
    val user            by SessionManager.user.collectAsStateWithLifecycle()
    val sharedPayload   by SharedScanIntentStore.pending.collectAsStateWithLifecycle()
    val pendingScamRoute by ScamAlertNavigationStore.pending.collectAsStateWithLifecycle()
    val currentRoute    = navController.currentBackStackEntryAsState().value?.destination?.route

    LaunchedEffect(sharedPayload) {
        if (sharedPayload != null && navController.currentDestination?.route != Screen.Scan.route) {
            navController.navigate(Screen.Scan.route) { launchSingleTop = true }
        }
    }

    LaunchedEffect(pendingScamRoute) {
        val route = pendingScamRoute ?: return@LaunchedEffect
        val destination = if (route.alertId.isNullOrBlank()) route.route
        else Screen.ScamAlertDetail.createRoute(route.alertId)
        navController.navigate(destination) { launchSingleTop = true }
        ScamAlertNavigationStore.consume()
    }

    MainScaffold(
        drawerState  = drawerState,
        drawerContent = {
            DrawerMenu(
                isDark          = isDark,
                currentRoute    = currentRoute,
                userName        = user?.name?.ifBlank { "GO Suraksha" } ?: "GO Suraksha",
                phoneNumber     = user?.phone?.ifBlank { "Phone not added" } ?: "Phone not added",
                profileImageUrl = user?.profileImageUrl,
                onProfileClick  = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                },
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    if (navController.currentDestination?.route != route) {
                        navController.navigate(route) { launchSingleTop = true }
                    }
                },
                // ← close button wired here
                onClose = { scope.launch { drawerState.close() } },
                // Plan from SessionManager (live — updates instantly after upgrade)
                plan = when (user?.plan) {
                    com.gosuraksha.app.domain.model.Plan.GO_ULTRA -> "GO_ULTRA"
                    com.gosuraksha.app.domain.model.Plan.GO_PRO   -> "GO_PRO"
                    else -> "FREE"
                }
            )
        },
        onMenuClick    = { scope.launch { drawerState.open() } },
        onCyberSosClick = { navController.navigate(Screen.CyberSos.route) { launchSingleTop = true } },
        bottomBar = { BottomNavigationBar(navController = navController, isDark = isDark) }
    ) { modifier ->
        NavigationHost(navController = navController, onLogout = onLogout, modifier = modifier)
    }
}