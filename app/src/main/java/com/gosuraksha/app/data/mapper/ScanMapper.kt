package com.gosuraksha.app.data.mapper

import android.util.Log
import com.gosuraksha.app.data.remote.dto.scan.AiExplainResponseDto
import com.gosuraksha.app.data.remote.dto.scan.ScanResponse
import com.gosuraksha.app.domain.model.scan.AiExplainResult
import com.gosuraksha.app.domain.model.scan.AiImageScanResult
import com.gosuraksha.app.domain.model.scan.ScanAnalysisResult

fun ScanResponse.toDomain(): ScanAnalysisResult {
    Log.d(
        "SCAN_DEBUG",
        "Mapping ScanResponse -> ScanAnalysisResult: scan_id=$scan_id, risk_score=$risk_score, risk_level=$risk_level, confidence=$confidence"
    )
    val resolvedRisk = resolveRiskLevel(risk_level, risk_score)
    // Prefer highlights from new schema; fall back to reasons for legacy responses
    val resolvedHighlights = safeList(highlights).map { safeString(it) }
        .ifEmpty { safeList(reasons).map { safeString(it) } }
    return ScanAnalysisResult(
        id             = scan_id?.trim()?.takeIf { it.isNotEmpty() },
        risk           = resolvedRisk,
        score          = safeInt(risk_score),
        confidence     = confidence,
        confidenceLabel = confidence_label?.trim()?.takeIf { it.isNotEmpty() },
        summary        = summary?.trim()?.takeIf { it.isNotEmpty() },
        highlights     = resolvedHighlights,
        reasons        = safeList(reasons).map { safeString(it) },
        recommendation = recommendation?.trim()?.takeIf { it.isNotEmpty() },
        breachCount    = breach_count?.let { safeInt(it) },
        breaches       = breaches?.map { it.toDomain() }
    ).also { mapped ->
        Log.d(
            "SCAN_DEBUG",
            "Mapped domain result: id=${mapped.id}, risk=${mapped.risk}, score=${mapped.score}, confidence=${mapped.confidence}, summary=${mapped.summary?.take(40)}"
        )
    }
}

fun AiExplainResponseDto.toDomain(): AiExplainResult {
    return AiExplainResult(
        aiExplanation = safeString(explanation ?: ai_explanation, fallback = "No explanation available")
    )
}

/**
 * Maps a ScanResponse from POST /scan/image to an AiImageScanResult domain object.
 * Prefers the enriched `highlights` field (from the new schema) and falls back to
 * `reasons` for backward compatibility. resolveRiskLevel provides a last-mile guard
 * against unexpected nulls from the backend.
 */
fun ScanResponse.toRealityDomain(): AiImageScanResult {
    Log.d(
        "SCAN_DEBUG",
        "Mapping ScanResponse -> AiImageScanResult: scan_id=$scan_id, risk_score=$risk_score, risk_level=$risk_level, confidence=$confidence"
    )
    val resolvedRisk = resolveRiskLevel(risk_level, risk_score)
    // Prefer highlights (new schema); fall back to reasons (legacy / other endpoints)
    val resolvedHighlights = safeList(highlights).map { safeString(it) }
        .ifEmpty { safeList(reasons).map { safeString(it) } }
    return AiImageScanResult(
        riskLevel        = resolvedRisk,
        riskScore        = safeInt(risk_score),
        confidence       = confidence,
        confidenceLabel  = confidence_label?.trim()?.takeIf { it.isNotEmpty() },
        summary          = summary?.trim()?.takeIf { it.isNotEmpty() },
        highlights       = resolvedHighlights,
        technicalSignals = safeList(technical_signals).map { safeString(it) },
        recommendation   = recommendation?.trim()?.takeIf { it.isNotEmpty() }
    ).also { mapped ->
        Log.d(
            "SCAN_DEBUG",
            "Mapped image result: riskLevel=${mapped.riskLevel}, riskScore=${mapped.riskScore}, confidence=${mapped.confidence}, highlights=${mapped.highlights.size}"
        )
    }
}

private fun com.gosuraksha.app.data.remote.dto.scan.BreachItem.toDomain(): com.gosuraksha.app.domain.model.scan.BreachItem {
    return com.gosuraksha.app.domain.model.scan.BreachItem(
        name = safeString(name, fallback = "Unknown breach"),
        domain = domain?.trim()?.takeIf { it.isNotEmpty() },
        breachDate = breach_date?.trim()?.takeIf { it.isNotEmpty() },
        compromisedData = safeList(compromised_data).map { safeString(it) }.takeIf { it.isNotEmpty() }
    )
}

/**
 * Resolves a risk level string to a guaranteed non-null, non-"UNKNOWN" value.
 *
 * Priority:
 *  1. Use the provided risk_level if it is a known valid value (LOW / MEDIUM / HIGH).
 *  2. Otherwise derive from the numeric score using the same thresholds as the backend:
 *       0-30  → LOW
 *       31-60 → MEDIUM
 *       61+   → HIGH
 *
 * This provides last-mile protection against "UNKNOWN Risk Detected" in the UI
 * even if an edge case slips through the backend sanitisation.
 */
private fun resolveRiskLevel(riskLevel: String?, riskScore: Int?): String {
    val normalised = riskLevel?.trim()?.uppercase()
    if (!normalised.isNullOrEmpty() && normalised in setOf("LOW", "MEDIUM", "HIGH")) {
        return normalised
    }
    val score = riskScore ?: 0
    return when {
        score <= 30 -> "LOW"
        score <= 60 -> "MEDIUM"
        else        -> "HIGH"
    }
}
