package com.gosuraksha.app.data.mapper

import com.gosuraksha.app.data.remote.dto.scan.*
import com.gosuraksha.app.domain.model.scan.*

fun ScanResponse.toDomain(): ScanAnalysisResult {
    return ScanAnalysisResult(
        id = scan_id,
        risk = risk_level,
        score = risk_score,
        confidence = confidence,
        reasons = reasons,
        recommendation = recommendation,
        breachCount = breach_count,
        breaches = breaches?.map { it.toDomain() }
    )
}

fun AiExplainResponseDto.toDomain(): AiExplainResult {
    return AiExplainResult(
        aiExplanation = explanation ?: ai_explanation ?: ""
    )
}

fun ScanResponse.toRealityDomain(): AiImageScanResult {
    return AiImageScanResult(
        riskLevel = risk_level,
        confidence = confidence,
        reasons = reasons,
        recommendation = recommendation
    )
}

private fun com.gosuraksha.app.data.remote.dto.scan.BreachItem.toDomain(): com.gosuraksha.app.domain.model.scan.BreachItem {
    return com.gosuraksha.app.domain.model.scan.BreachItem(
        name = name,
        domain = domain,
        breachDate = breach_date,
        compromisedData = compromised_data
    )
}
