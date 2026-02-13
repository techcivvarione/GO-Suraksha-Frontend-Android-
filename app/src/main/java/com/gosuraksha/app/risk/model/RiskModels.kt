package com.gosuraksha.app.risk.model

data class RiskScoreResponse(
    val score: Int,
    val risk_level: String,
    val window: String,
    val total_scans: Int,
    val generated_at: String,
    val summary: String
)

data class RiskTimelineResponse(
    val window: String,
    val points: List<RiskTimelinePoint>
)

data class RiskTimelinePoint(
    val date: String,
    val score: Int,
    val high: Int,
    val medium: Int,
    val low: Int
)

data class RiskInsightsResponse(
    val window: String,
    val summary: RiskInsightsSummary
)

data class RiskInsightsSummary(
    val peak_risk_days: List<RiskPeakDay>?,
    val top_scan_keywords: List<RiskKeyword>?,
    val recommendations: List<String>?
)

data class RiskPeakDay(
    val date: String,
    val high: Int,
    val medium: Int,
    val low: Int
)

data class RiskKeyword(
    val keyword: String,
    val count: Int
)
