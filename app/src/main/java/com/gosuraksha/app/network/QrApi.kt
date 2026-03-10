package com.gosuraksha.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class QrAnalyzeRequest(
    val raw_payload: String
)

data class QrAnalyzeResponse(
    val qr_hash: String,
    val risk_score: Int,
    val risk_level: String,
    val detected_type: String,
    val reasons: List<String>,
    val recommended_action: String,
    val is_flagged: Boolean
)

data class ApiError(
    val detail: String
)

interface QrApi {
    @POST("qr/analyze")
    suspend fun analyzeQr(
        @Body request: QrAnalyzeRequest
    ): Response<QrAnalyzeResponse>
}

