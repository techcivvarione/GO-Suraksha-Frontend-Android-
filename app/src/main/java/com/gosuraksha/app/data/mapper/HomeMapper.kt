package com.gosuraksha.app.data.mapper

import com.gosuraksha.app.data.remote.dto.home.*
import com.gosuraksha.app.domain.model.home.*

fun HomeOverviewDto.toDomain(): HomeOverview {
    return HomeOverview(
        securitySnapshot = security_snapshot.toDomain(),
        threatPulse = safeMap(threat_pulse).takeIf { it.isNotEmpty() },
        financialImpact = financial_impact?.toDomain()
    )
}

private fun SecuritySnapshotDto.toDomain(): SecuritySnapshot {
    return SecuritySnapshot(
        scansDone = safeInt(scans_done),
        threatsDetected = safeInt(threats_detected),
        lastScanAt = safeString(last_scan_at),
        overallRisk = safeString(overall_risk)
    )
}

private fun FinancialImpactDto.toDomain(): FinancialImpact {
    return FinancialImpact(
        global = global?.toDomain()
    )
}

private fun GlobalImpactDto.toDomain(): GlobalImpact {
    return GlobalImpact(
        scope = safeString(scope),
        regionCode = region_code?.trim()?.takeIf { it.isNotEmpty() },
        payload = payload?.toDomain(),
        confidence = confidence?.trim()?.takeIf { it.isNotEmpty() },
        sources = safeList(sources).map { safeString(it) }.takeIf { it.isNotEmpty() },
        generatedAt = generated_at?.trim()?.takeIf { it.isNotEmpty() }
    )
}

private fun ImpactPayloadDto.toDomain(): ImpactPayload {
    return ImpactPayload(
        year = safeInt(year),
        trend = safeString(trend),
        displayText = safeString(display_text),
        estimatedLossUsd = safeLong(estimated_loss_usd)
    )
}
