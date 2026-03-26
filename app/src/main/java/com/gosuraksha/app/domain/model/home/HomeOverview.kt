package com.gosuraksha.app.domain.model.home

data class HomeOverview(
    val securitySnapshot: SecuritySnapshot,
    val threatPulse: Map<String, Any?>?,
    val financialImpact: FinancialImpact?
)

data class SecuritySnapshot(
    val scansDone: Int,
    val threatsDetected: Int,
    val lastScanAt: String,
    val overallRisk: String
) {
    init {
        require(scansDone >= 0) { "scansDone must be >= 0" }
        require(threatsDetected >= 0) { "threatsDetected must be >= 0" }
        require(lastScanAt.isNotBlank()) { "lastScanAt must not be blank" }
        require(overallRisk.isNotBlank()) { "overallRisk must not be blank" }
    }
}

data class FinancialImpact(
    val global: GlobalImpact?
)

data class GlobalImpact(
    val scope: String,
    val regionCode: String?,
    val payload: ImpactPayload?,
    val confidence: String?,
    val sources: List<String>?,
    val generatedAt: String?
) {
    init {
        require(scope.isNotBlank()) { "scope must not be blank" }
    }
}

data class ImpactPayload(
    val year: Int,
    val trend: String,
    val displayText: String,
    val estimatedLossUsd: Long
) {
    init {
        require(year >= 0) { "year must be >= 0" }
        require(trend.isNotBlank()) { "trend must not be blank" }
        require(displayText.isNotBlank()) { "displayText must not be blank" }
        require(estimatedLossUsd >= 0) { "estimatedLossUsd must be >= 0" }
    }
}
