package com.gosuraksha.app.domain.model.scan

data class ScanAnalysisResult(
    val id: String? = null,
    val risk: String,
    val score: Int,
    val confidence: Float? = null,
    val reasons: List<String>,
    val recommendation: String? = null,
    val breachCount: Int? = null,
    val breaches: List<BreachItem>? = null,
    val count: Int? = null,
    val sites: List<String>? = null,
    val domains: List<String>? = null,
    val breachAnalysis: BreachAnalysis? = null,
    val upgrade: UpgradeInfo? = null
)

data class BreachItem(
    val name: String,
    val domain: String?,
    val breachDate: String?,
    val compromisedData: List<String>?
)

data class BreachAnalysis(
    val totalBreaches: Int,
    val highestRiskCategory: String?,
    val categories: Map<String, CategoryDetail>?
)

data class CategoryDetail(
    val count: Int,
    val sites: List<String>,
    val severity: String
)

data class UpgradeInfo(
    val required: Boolean,
    val message: String
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
