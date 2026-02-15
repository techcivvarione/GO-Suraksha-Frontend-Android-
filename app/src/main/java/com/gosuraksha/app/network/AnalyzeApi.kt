package com.gosuraksha.app.network

import com.gosuraksha.app.scan.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface AnalyzeApi {

    @POST("analyze/")   // <-- ADD TRAILING SLASH
    suspend fun analyze(
        @Body request: AnalyzeRequest
    ): AnalyzeResponse

    @POST("ai/explain/")
    suspend fun explain(
        @Body request: AiExplainRequest
    ): AiExplainResponse
}
