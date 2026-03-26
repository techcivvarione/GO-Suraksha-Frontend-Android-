package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import com.gosuraksha.app.risk.model.*
import retrofit2.http.GET

interface RiskApi {

    @GET("risk/score")
    suspend fun getRiskScore(): ApiResponse<RiskScoreResponse>

    @GET("risk/timeline")
    suspend fun getRiskTimeline(): ApiResponse<RiskTimelineResponse>

    @GET("risk/insights")
    suspend fun getRiskInsights(): ApiResponse<RiskInsightsResponse>
}
