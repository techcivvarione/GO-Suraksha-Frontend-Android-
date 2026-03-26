package com.gosuraksha.app.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gosuraksha.app.navigation.Screen
import com.gosuraksha.app.scam.ui.ReportScamScreen
import com.gosuraksha.app.scam.ui.ScamAlertDetailScreen
import com.gosuraksha.app.scam.ui.ScamAlertHubScreen
import com.gosuraksha.app.scam.ui.ScamAlertsFeedScreen
import com.gosuraksha.app.scam.ui.ScamNumberCheckerScreen
import com.gosuraksha.app.scan.core.ScanScreen
import com.gosuraksha.app.ui.alerts.AlertsScreen
import com.gosuraksha.app.ui.history.HistoryScreen
import com.gosuraksha.app.ui.home.HomeScreen
import com.gosuraksha.app.ui.news.NewsScreen
import com.gosuraksha.app.ui.search.SearchScreen
import com.gosuraksha.app.ui.trusted.TrustedContactsScreen

@Composable
fun NavigationHost(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToRisk = { navController.navigate(Screen.RiskInternal.route) },
                onNavigateToRealityScan = { navController.navigate(Screen.Scan.route) },
                onNavigateToCyberCard = { navController.navigate(Screen.CyberCard.route) },
                onNavigateToCyberSos = { navController.navigate(Screen.CyberSos.route) },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                onNavigateToScamNetwork = { navController.navigate(Screen.ScamAlertHub.route) },
                onNavigateToFamily = { navController.navigate(Screen.TrustedContacts.route) },
                onNavigateToSecuritySettings = { navController.navigate(Screen.Profile.route) },
                onNavigateToNews = { navController.navigate(Screen.News.route) },
                onNavigateToScamLookup = { navController.navigate(Screen.CheckNumber.route) },
                onNavigateToReportScam = { navController.navigate(Screen.ReportScam.route) },
                onNavigateToScamAlertsFeed = { navController.navigate(Screen.ScamAlertsFeed.route) },
                onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToTrustedContacts = { navController.navigate(Screen.TrustedContacts.route) }
            )
        }
        composable(Screen.News.route) { NewsScreen() }
        composable(Screen.Search.route) { SearchScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Scan.route) { ScanScreen(onUpgradePlan = { navController.navigate(Screen.Profile.route) }) }
        composable(Screen.Alerts.route) {
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
                onTrendingClick = { navController.navigate(Screen.ScamAlertsFeed.route) },
                onAlertClick = { navController.navigate(Screen.ScamAlertDetail.createRoute(it)) }
            )
        }
        composable(Screen.ReportScam.route) { ReportScamScreen(onBackToHub = { navController.popBackStack() }) }
        composable(Screen.CheckNumber.route) { ScamNumberCheckerScreen() }
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
        composable(Screen.History.route) { HistoryScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.TrustedContacts.route) { TrustedContactsScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.RiskInternal.route) { RiskScreen() }
        composable(Screen.CyberSos.route) { com.gosuraksha.app.ui.screens.CyberSosScreen() }
    }
}
