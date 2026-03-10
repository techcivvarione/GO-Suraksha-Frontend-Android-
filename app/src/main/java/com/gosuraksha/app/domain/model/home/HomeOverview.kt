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
)

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
)

data class ImpactPayload(
    val year: Int,
    val trend: String,
    val displayText: String,
    val estimatedLossUsd: Long
)
