package com.gosuraksha.app.domain.model.scan

data class ScanAnalysisResult(
    val id: String? = null,
    val risk: String = "UNKNOWN",
    val score: Int = 0,
    val confidence: Float? = null,
    val confidenceLabel: String? = null,
    val summary: String? = null,
    val highlights: List<String> = emptyList(),
    val reasons: List<String> = emptyList(),
    val recommendation: String? = null,
    val breachCount: Int? = null,
    val breaches: List<BreachItem>? = null
) {
    init {
        require(risk.isNotBlank()) { "risk must not be blank" }
        require(score >= 0) { "score must be >= 0" }
        require(breachCount == null || breachCount >= 0) { "breachCount must be >= 0" }
    }
}

data class BreachItem(
    val name: String = "",
    val domain: String? = null,
    val breachDate: String? = null,
    val compromisedData: List<String>? = null
) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
    }
}

data class AiExplainResult(
    val aiExplanation: String = ""
) {
    init {
        require(aiExplanation.isNotBlank()) { "aiExplanation must not be blank" }
    }
}

data class AiImageScanResult(
    val riskLevel: String? = "UNKNOWN",
    val riskScore: Int = 0,
    val confidence: Float? = null,
    val confidenceLabel: String? = null,
    val summary: String? = null,
    val highlights: List<String> = emptyList(),
    val technicalSignals: List<String> = emptyList(),
    val recommendation: String? = null
) {
    init {
        require(!riskLevel.isNullOrBlank()) { "riskLevel must not be blank" }
    }
}

data class QrScanAnalysis(
    val riskScore: Int = 0,
    val riskLevel: String? = "UNKNOWN",
    val detectedType: String? = null,
    val reasons: List<String> = emptyList(),
    val recommendedAction: String? = null,
    val isFlagged: Boolean = false,
    // UPI / payment fields
    val isPayment: Boolean = false,
    val merchantName: String? = null,
    val upiId: String? = null,
    val amount: Double? = null,
    val summary: String? = null,
) {
    init {
        require(riskScore >= 0) { "riskScore must be >= 0" }
        require(!riskLevel.isNullOrBlank()) { "riskLevel must not be blank" }
    }
}
