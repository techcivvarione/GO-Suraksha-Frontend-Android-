package com.gosuraksha.app.domain.model.scan

data class ScanAnalysisResult(
    val id: String? = null,
    val risk: String,
    val score: Int,
    val confidence: Float? = null,
    val reasons: List<String>,
    val recommendation: String? = null,
    val breachCount: Int? = null,
    val breaches: List<BreachItem>? = null
)

data class BreachItem(
    val name: String,
    val domain: String?,
    val breachDate: String?,
    val compromisedData: List<String>?
)

data class AiExplainResult(
    val aiExplanation: String
)

data class AiImageScanResult(
    val riskLevel: String,
    val confidence: Float?,
    val reasons: List<String>,
    val recommendation: String
)

data class QrScanAnalysis(
    val riskScore: Int,
    val riskLevel: String,
    val detectedType: String,
    val reasons: List<String>,
    val recommendedAction: String,
    val isFlagged: Boolean
)
