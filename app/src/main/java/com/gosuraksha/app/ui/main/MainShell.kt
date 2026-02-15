package com.gosuraksha.app.ui.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.gosuraksha.app.navigation.MainRoutes
import com.gosuraksha.app.navigation.Routes
import com.gosuraksha.app.ui.components.AppTopBar
import com.gosuraksha.app.ui.home.HomeScreen
import com.gosuraksha.app.ui.history.HistoryScreen
import com.gosuraksha.app.ui.main.*

@SuppressLint("ComposableDestinationInComposeScope")
@Composable
fun MainShell(
    onLogout: () -> Unit,
) {

    val navController = rememberNavController()
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            AppTopBar(
                onCyberSosClick = { }
            )

        },

        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {

                NavigationBarItem(
                    selected = currentRoute == MainRoutes.Home.route,
                    onClick = {
                        navController.navigate(MainRoutes.Home.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = currentRoute == MainRoutes.News.route,
                    onClick = {
                        navController.navigate(MainRoutes.News.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Article, "News") },
                    label = { Text("News") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = currentRoute == MainRoutes.Scan.route,
                    onClick = {
                        navController.navigate(MainRoutes.Scan.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.CameraAlt, "Scan") },
                    label = { Text("Scan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = currentRoute == MainRoutes.Alerts.route,
                    onClick = {
                        navController.navigate(MainRoutes.Alerts.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Notifications, "Alerts") },
                    label = { Text("Alerts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = currentRoute == MainRoutes.Profile.route,
                    onClick = {
                        navController.navigate(MainRoutes.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = MainRoutes.Home.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(MainRoutes.Home.route) {
                HomeScreen(
                    onLogout = onLogout,
                    onNavigateToHistory = {
                        navController.navigate(Routes.HISTORY)
                    },
                    onNavigateToRisk = {
                        navController.navigate("risk_internal")
                    }
                )
            }

            composable(MainRoutes.News.route) { NewsScreen() }
            composable(MainRoutes.Scan.route) { ScanScreen() }
            composable(MainRoutes.Alerts.route) { AlertsScreen() }
            composable(MainRoutes.Profile.route) { ProfileScreen(onLogout = onLogout) }

            composable(Routes.HISTORY) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }

            composable("risk_internal") {
                RiskScreen()
            }
        }
    }
}
