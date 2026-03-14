package com.gosuraksha.app.alerts.model

data class AlertEvent(
    val id: String,
    val media_hash: String? = null,
    val analysis_type: String? = null,
    val alert_type: String? = null,
    val severity: String? = null,
    val title: String? = null,
    val description: String? = null,
    val risk_score: Int? = null,
    val status: String? = null,
    val created_at: String? = null,
    val related_scan_id: Long? = null
)

data class RefreshAlertsResponse(
    val status: String? = null,
    val new_alerts_created: Int? = null
)

data class AlertsSummaryResponse(
    val risk_level_today: String? = null,
    val unread_alerts: UnreadAlerts? = null,
    val generated_at: String? = null
)

data class UnreadAlerts(
    val high: Int? = null,
    val medium: Int? = null,
    val low: Int? = null
)

data class SubscribeAlertsResponse(
    val status: String? = null,
    val categories: List<String>? = null
)

data class TrustedAlertsResponse(
    val count: Int? = null,
    val alerts: List<TrustedAlertItem> = emptyList()
)

data class TrustedAlertItem(
    val id: String,
    val alert_type: String? = null,
    val created_at: String? = null,
    val contact_name: String? = null,
    val contact_email: String? = null,
    val contact_phone: String? = null
)

data class TrustedAlertReadResponse(
    val status: String? = null
)
