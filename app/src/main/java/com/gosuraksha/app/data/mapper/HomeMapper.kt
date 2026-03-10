package com.gosuraksha.app.data.mapper

import com.gosuraksha.app.data.remote.dto.home.*
import com.gosuraksha.app.domain.model.home.*

fun HomeOverviewDto.toDomain(): HomeOverview {
    return HomeOverview(
        securitySnapshot = security_snapshot.toDomain(),
        threatPulse = threat_pulse,
        financialImpact = financial_impact?.toDomain()
    )
}

private fun SecuritySnapshotDto.toDomain(): SecuritySnapshot {
    return SecuritySnapshot(
        scansDone = scans_done,
        threatsDetected = threats_detected,
        lastScanAt = last_scan_at,
        overallRisk = overall_risk
    )
}

private fun FinancialImpactDto.toDomain(): FinancialImpact {
    return FinancialImpact(
        global = global?.toDomain()
    )
}

private fun GlobalImpactDto.toDomain(): GlobalImpact {
    return GlobalImpact(
        scope = scope,
        regionCode = region_code,
        payload = payload?.toDomain(),
        confidence = confidence,
        sources = sources,
        generatedAt = generated_at
    )
}

private fun ImpactPayloadDto.toDomain(): ImpactPayload {
    return ImpactPayload(
        year = year,
        trend = trend,
        displayText = display_text,
        estimatedLossUsd = estimated_loss_usd
    )
}
