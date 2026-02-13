package com.gosuraksha.app.ui.main

data class CyberCardResponse(
    val card_status: String,
    val card_id: String? = null,
    val name: String? = null,
    val is_paid: Boolean? = null,
    val score: Int? = null,
    val max_score: Int? = null,
    val risk_level: String? = null,
    val signals: Map<String, Any>? = null,
    val score_month: String? = null,
    val message: String? = null
)
