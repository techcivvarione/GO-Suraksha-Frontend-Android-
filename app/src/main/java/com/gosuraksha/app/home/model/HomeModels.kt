package com.gosuraksha.app.home.model

data class HomeOverviewResponse(
    val security_snapshot: SecuritySnapshot,
    val threat_pulse: Map<String, Any>?,
    val financial_impact: FinancialImpact?
)

data class SecuritySnapshot(
    val scans_done: Int,
    val threats_detected: Int,
    val last_scan_at: String,
    val overall_risk: String
)

data class FinancialImpact(
    val global: GlobalImpact?
)

data class GlobalImpact(
    val scope: String,
    val region_code: String?,
    val payload: ImpactPayload?,
    val confidence: String?,
    val sources: List<String>?,
    val generated_at: String?
)

data class ImpactPayload(
    val year: Int,
    val trend: String,
    val display_text: String,
    val estimated_loss_usd: Long
)
