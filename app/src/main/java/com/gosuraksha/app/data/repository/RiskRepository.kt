package com.gosuraksha.app.data.repository

import com.gosuraksha.app.network.RiskApi
import com.gosuraksha.app.risk.model.RiskInsightsResponse
import com.gosuraksha.app.risk.model.RiskScoreResponse
import com.gosuraksha.app.risk.model.RiskTimelineResponse

class RiskRepository(
    private val api: RiskApi
) {
    suspend fun getRiskScore(): RiskScoreResponse = api.getRiskScore().data

    suspend fun getRiskTimeline(): RiskTimelineResponse = api.getRiskTimeline().data

    suspend fun getRiskInsights(): RiskInsightsResponse = api.getRiskInsights().data
}
