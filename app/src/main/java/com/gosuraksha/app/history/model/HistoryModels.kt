package com.gosuraksha.app.history.model

data class HistoryListResponse(
    val count: Int,
    val limit: Int,
    val offset: Int,
    val history: List<HistoryItem>? = emptyList()
)

data class HistoryItem(
    val id: String,
    val input_text: String,
    val risk: String,
    val score: Int,
    val reasons: List<String>? = emptyList(),
    val created_at: String
)

data class HistoryDetailResponse(
    val id: String,
    val input_text: String,
    val risk: String,
    val score: Int,
    val reasons: List<String>? = emptyList(),
    val created_at: String
)

data class DeleteHistoryResponse(
    val status: String
)
