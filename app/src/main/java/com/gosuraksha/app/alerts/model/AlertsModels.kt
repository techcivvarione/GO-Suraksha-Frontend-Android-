package com.gosuraksha.app.alerts.model

data class AlertsResponse(
    val alerts: List<AlertEvent> = emptyList()
)

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

// Backend returns: total_alerts, high_risk, medium_risk, low_risk
// NOTE: Gson bypasses Kotlin defaults via Unsafe — keep all fields Int (JVM default = 0)
//       so even a missing field deserializes to 0, never null.
data class AlertsSummaryResponse(
    val total_alerts: Int,
    val high_risk: Int,
    val medium_risk: Int,
    val low_risk: Int,
) {
    // Derived risk label — computed at read time from the deserialized counts
    val risk_level_today: String
        get() = when {
            high_risk   > 0 -> "high"
            medium_risk > 0 -> "medium"
            low_risk    > 0 -> "low"
            else            -> "safe"
        }

    companion object {
        /** Safe empty sentinel — used as the initial / fallback value. */
        fun empty() = AlertsSummaryResponse(
            total_alerts = 0,
            high_risk    = 0,
            medium_risk  = 0,
            low_risk     = 0,
        )
    }
}

data class FamilyActivityItem(
    val member_name: String?,
    val scan_type: String?,
    val risk_level: String?,
    val scan_input: String?,
    val created_at: String?,
)

data class FamilyActivityResponse(
    val count: Int = 0,
    val activity: List<FamilyActivityItem> = emptyList(),
)

// STEP 2: family-feed — alert_events based (richer than scan_history based family-activity)
data class FamilyFeedItem(
    val member_name: String?,
    val scan_type: String?,
    val risk_level: String?,
    val risk_score: Int?,
    val created_at: String?,
)

data class FamilyFeedResponse(
    val count: Int = 0,
    val feed: List<FamilyFeedItem> = emptyList(),
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
