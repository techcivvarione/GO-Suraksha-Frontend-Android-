package com.gosuraksha.app.navigation




sealed class MainRoutes(val route: String) {
    object Home : MainRoutes("home")
    object News : MainRoutes("news")
    object Scan : MainRoutes("scan")
    object Alerts : MainRoutes("alerts")
    object Profile : MainRoutes("profile")
}
