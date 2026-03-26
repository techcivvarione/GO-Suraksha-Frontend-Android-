package com.gosuraksha.app.navigation

sealed class Screen(val route: String) {
    object Entry : Screen("entry")
    object Onboarding : Screen("onboarding")
    object Language : Screen("language")
    object Login : Screen("login")
    object Otp : Screen("otp")
    object VerifyPhone : Screen("verify_phone")
    object ProfileSetup : Screen("profile_setup")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object CyberCard : Screen("cyberCardScreen")
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
    object ScamAlertHub : Screen("scam_alert_hub")
    object ReportScam : Screen("report_scam")
    object CheckNumber : Screen("check_number")
    object ScamAlertsFeed : Screen("scam_alerts_feed")
    object ScamAlertDetail : Screen("scam_alert_detail/{alertId}") {
        fun createRoute(alertId: String): String = "scam_alert_detail/$alertId"
    }
    object Search : Screen("search")
}
