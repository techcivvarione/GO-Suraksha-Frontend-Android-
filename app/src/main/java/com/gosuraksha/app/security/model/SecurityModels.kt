package com.gosuraksha.app.security.model

// --------------------
// CHANGE PASSWORD
// --------------------

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String,
    val confirm_password: String
)

// --------------------
// SECURITY STATUS
// --------------------

data class SecurityStatusResponse(
    val password_last_changed: String,
    val session_model: String,
    val note: String
)

// --------------------
// HEALTH SCORE
// --------------------

data class HealthScoreResponse(
    val score: Int,
    val level: String,
    val window: String,
    val signals: HealthSignals,
    val last_scan_at: String?
)

data class HealthSignals(
    val total_scans: Int,
    val high_risk: Int,
    val medium_risk: Int,
    val low_risk: Int
)

// --------------------
// AUDIT LOGS
// --------------------

data class AuditLogsResponse(
    val count: Int,
    val data: List<AuditLogItem>
)

data class AuditLogItem(
    val event_type: String,
    val event_description: String,
    val ip_address: String?,
    val created_at: String
)

// --------------------
// HEALTH TREND
// --------------------

data class HealthTrendResponse(
    val window_days: Int,
    val current_score: Int,
    val trend: List<HealthTrendPoint>
)

data class HealthTrendPoint(
    val date: String,
    val score: Int
)

// --------------------
// SCAM REPORT
// --------------------

data class ScamReportRequest(
    val scan_type: String,
    val title: String,
    val description: String,
    val source: String,
    val scan_value: String
)

data class ScamReportResponse(
    val status: String,
    val report_id: String
)

// --------------------
// SCAM CONFIRM
// --------------------

data class ScamConfirmResponse(
    val status: String,
    val penalty: Int,
    val help_link: String,
    val next_steps: List<String>
)

// --------------------
// CYBER COMPLAINT PREVIEW
// --------------------

data class CyberComplaintPreviewRequest(
    val scan_type: String,
    val incident_date: String,
    val description: String,
    val loss_amount: String
)

data class CyberComplaintPreviewResponse(
    val headline: String,
    val portal: String,
    val complaint_text: String,
    val note: String
)


