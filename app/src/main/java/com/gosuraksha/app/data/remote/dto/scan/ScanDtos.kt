package com.gosuraksha.app.data.remote.dto.scan

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
    val scan_id: String,
    val analysis_type: String,
    val risk_score: Int,
    val risk_level: String,
    val confidence: Float?,
    val reasons: List<String>,
    val recommendation: String,
    val breach_count: Int?,
    val breaches: List<BreachItem>?
)

data class ScanApiError(
    val detail: String?
)

data class BreachItem(
    val name: String,
    val domain: String?,
    val breach_date: String?,
    val compromised_data: List<String>?
)

data class AiExplainRequestDto(
    val text: String
)

data class AiExplainResponseDto(
    val explanation: String?,
    val ai_explanation: String?
)
