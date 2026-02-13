package com.gosuraksha.app.network

import com.gosuraksha.app.risk.model.*
import retrofit2.http.GET

interface RiskApi {

    @GET("risk/score")
    suspend fun getRiskScore(): RiskScoreResponse

    @GET("risk/timeline")
    suspend fun getRiskTimeline(): RiskTimelineResponse

    @GET("risk/insights")
    suspend fun getRiskInsights(): RiskInsightsResponse
}
