package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class QrAnalyzeRequest(
    val raw_payload: String
)

data class QrAnalyzeResponse(
    val qr_hash: String?,
    val risk_score: Int?,
    val risk_level: String?,
    val detected_type: String?,
    val reasons: List<String>?,
    val recommended_action: String?,
    val is_flagged: Boolean?,
    // UPI payment fields
    val is_payment: Boolean?,
    val merchant_name: String?,
    val upi_id: String?,
    val amount: Double?,
    val summary: String?,
)

data class ApiError(
    val detail: String
)

interface QrApi {
    @POST("qr/analyze")
    suspend fun analyzeQr(
        @Body request: QrAnalyzeRequest
    ): Response<ApiResponse<QrAnalyzeResponse>>
}

