package com.gosuraksha.app.scan.model

data class AnalyzeRequest(
    val type: String,      // THREAT | EMAIL | PASSWORD
    val content: String
)

data class AnalyzeResponse(
    val id: String? = null,   // 🔥 ADD THIS

    val risk: String,
    val score: Int,
    val reasons: List<String>,

    val count: Int? = null,
    val sites: List<String>? = null,
    val domains: List<String>? = null,
    val breach_analysis: BreachAnalysis? = null,
    val upgrade: UpgradeInfo? = null
)


data class BreachAnalysis(
    val total_breaches: Int,
    val highest_risk_category: String?,
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

data class AiExplainRequest(
    val scan_id: String
)

data class AiExplainResponse(
    val scan_id: String,
    val ai_explanation: String
)