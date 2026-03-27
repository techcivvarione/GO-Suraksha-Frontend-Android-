package com.gosuraksha.app.ui.main

data class CyberCardResponse(
    val card_status: String,
    val card_id: String? = null,
    val name: String? = null,
    val is_paid: Boolean? = null,
    val score: Int? = null,
    val max_score: Int? = null,
    val risk_level: String? = null,
    // V2 fields
    val level: String? = null,                          // machine: EXCELLENT, MOSTLY_SAFE, …
    val signals: Map<String, Any?>? = null,
    val factors: Map<String, Any?>? = null,             // per-component score breakdown
    val insights: List<String>? = null,                 // human-readable findings
    val actions: List<Map<String, Any?>>? = null,       // suggested next steps
    val score_month: String? = null,
    val updated_at: String? = null,
    val score_version: String? = null,
    val message: String? = null,
    // Eligibility signals — present on PENDING responses
    val eligible: Boolean? = null,           // true = eligible but score still computing
    val distinct_scan_types: Int? = null     // how many unique scan types completed so far
)
