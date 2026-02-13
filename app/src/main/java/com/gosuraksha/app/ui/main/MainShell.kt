package com.gosuraksha.app.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.gosuraksha.app.navigation.MainRoutes
import com.gosuraksha.app.ui.components.AppTopBar
import com.gosuraksha.app.ui.home.HomeScreen

@Composable
fun MainShell(
    onLogout: () -> Unit
) {

    val bottomNavController = rememberNavController()
    val currentDestination =
        bottomNavController.currentBackStackEntryAsState().value?.destination?.route

    var isDarkMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                isDarkMode = isDarkMode,
                onToggleDarkMode = {
                    isDarkMode = !isDarkMode
                },
                onCyberSosClick = {
                    // TODO: Navigate to Cyber SOS screen
                }
            )
        },
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = currentDestination == MainRoutes.Home.route,
                    onClick = {
                        bottomNavController.navigate(MainRoutes.Home.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = currentDestination == MainRoutes.News.route,
                    onClick = {
                        bottomNavController.navigate(MainRoutes.News.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Article, null) },
                    label = { Text("News") }
                )

                NavigationBarItem(
                    selected = currentDestination == MainRoutes.Scan.route,
                    onClick = {
                        bottomNavController.navigate(MainRoutes.Scan.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.CameraAlt, null) },
                    label = { Text("Scan") }
                )

                NavigationBarItem(
                    selected = currentDestination == MainRoutes.Alerts.route,
                    onClick = {
                        bottomNavController.navigate(MainRoutes.Alerts.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Notifications, null) },
                    label = { Text("Alerts") }
                )

                NavigationBarItem(
                    selected = currentDestination == MainRoutes.Profile.route,
                    onClick = {
                        bottomNavController.navigate(MainRoutes.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = bottomNavController,
            startDestination = MainRoutes.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable(MainRoutes.Home.route) {
                HomeScreen(onLogout = onLogout)
            }

            composable(MainRoutes.News.route) {
                NewsScreen()
            }

            composable(MainRoutes.Scan.route) {
                ScanScreen()
            }

            composable(MainRoutes.Alerts.route) {
                AlertsScreen()
            }

            composable(MainRoutes.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}
