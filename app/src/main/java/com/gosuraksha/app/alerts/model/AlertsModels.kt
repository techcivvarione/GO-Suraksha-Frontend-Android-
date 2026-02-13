package com.gosuraksha.app.alerts.model

data class AlertsResponse(
    val count: Int,
    val alerts: List<AlertItem>
)

data class AlertItem(
    val id: String,
    val alert_type: String,
    val created_at: String,
    val message: String? = null
)

data class RefreshAlertsResponse(
    val status: String,
    val new_alerts_created: Int
)

data class AlertsSummaryResponse(
    val risk_level_today: String,
    val unread_alerts: UnreadAlerts,
    val generated_at: String
)

data class UnreadAlerts(
    val high: Int,
    val medium: Int,
    val low: Int
)

data class SubscribeAlertsResponse(
    val status: String,
    val categories: List<String>
)

data class TrustedAlertsResponse(
    val count: Int,
    val alerts: List<TrustedAlertItem>
)

data class TrustedAlertItem(
    val id: String,
    val alert_type: String,
    val created_at: String,
    val contact_name: String,
    val contact_email: String?,
    val contact_phone: String?
)

data class TrustedAlertReadResponse(
    val status: String
)
