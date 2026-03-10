package com.gosuraksha.app.alerts.model

data class AlertEvent(
    val id: String,
    val media_hash: String?,
    val analysis_type: String?,
    val risk_score: Int?,
    val status: String?,
    val created_at: String
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
