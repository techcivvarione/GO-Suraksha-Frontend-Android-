package com.gosuraksha.app.navigation

sealed class Screen(val route: String) {
    object Entry : Screen("entry")
    object Language : Screen("language")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object News : Screen("news")
    object Scan : Screen("scan")
    object Alerts : Screen("alerts")
    object Profile : Screen("profile")
    object SetPin : Screen("set_pin")
    object UnlockPin : Screen("unlock_pin")
    object History : Screen("history")
    object TrustedContacts : Screen("trusted_contacts")
    object CyberSos : Screen("cyber_sos")
    object RiskInternal : Screen("risk_internal")
}
