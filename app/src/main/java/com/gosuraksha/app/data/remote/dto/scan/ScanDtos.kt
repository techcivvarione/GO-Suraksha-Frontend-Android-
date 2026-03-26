package com.gosuraksha.app.data.remote.dto.scan

import com.google.gson.annotations.SerializedName

data class PasswordScanRequest(
    val password: String
)

data class EmailScanRequest(
    val email: String
)

data class ThreatScanRequest(
    val text: String
)

data class ScanResponse(
    @SerializedName(value = "scan_id", alternate = ["scanId"])
    val scan_id: String?,
    @SerializedName(value = "risk_score", alternate = ["riskScore", "score"])
    val risk_score: Int?,
    @SerializedName(value = "risk_level", alternate = ["riskLevel", "risk"])
    val risk_level: String?,
    val confidence: Float?,
    val reasons: List<String>?,
    val recommendation: String?,
    @SerializedName(value = "breach_count", alternate = ["breachCount"])
    val breach_count: Int?,
    val breaches: List<BreachItem>?,
    // ── Image scan enrichment fields (null for non-image endpoints) ──────────
    val summary: String?,
    val highlights: List<String>?,
    @SerializedName("technical_signals")
    val technical_signals: List<String>?,
    @SerializedName("confidence_label")
    val confidence_label: String?,
)

data class ScanApiError(
    val detail: String?
)

data class BreachItem(
    val name: String?,
    val domain: String?,
    @SerializedName(value = "breach_date", alternate = ["breachDate"])
    val breach_date: String?,
    @SerializedName(value = "compromised_data", alternate = ["compromisedData"])
    val compromised_data: List<String>?
)

data class AiExplainRequestDto(
    val text: String
)

data class AiExplainResponseDto(
    val explanation: String?,
    val ai_explanation: String?
)

// ── Image explain endpoint DTOs ──────────────────────────────────────────────

data class ImageExplainRequest(
    @SerializedName("risk_level")
    val riskLevel: String,
    @SerializedName("risk_score")
    val riskScore: Int,
    val highlights: List<String>,
    val recommendation: String,
    // Optional backward-compat field — kept so old server versions still work
    val summary: String? = null,
)

data class ImageExplainResponse(
    val explanation: String?
)
