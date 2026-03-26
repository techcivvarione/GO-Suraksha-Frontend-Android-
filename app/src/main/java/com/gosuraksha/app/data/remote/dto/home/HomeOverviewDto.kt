package com.gosuraksha.app.data.remote.dto.home

data class HomeOverviewDto(
    val security_snapshot: SecuritySnapshotDto,
    val threat_pulse: Map<String, Any?>?,
    val financial_impact: FinancialImpactDto?
)

data class SecuritySnapshotDto(
    val scans_done: Int?,
    val threats_detected: Int?,
    val last_scan_at: String?,
    val overall_risk: String?
)

data class FinancialImpactDto(
    val global: GlobalImpactDto?
)

data class GlobalImpactDto(
    val scope: String?,
    val region_code: String?,
    val payload: ImpactPayloadDto?,
    val confidence: String?,
    val sources: List<String>?,
    val generated_at: String?
)

data class ImpactPayloadDto(
    val year: Int?,
    val trend: String?,
    val display_text: String?,
    val estimated_loss_usd: Long?
)
