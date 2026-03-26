package com.gosuraksha.app.data.repository

import com.gosuraksha.app.network.ScamNetworkApi
import com.gosuraksha.app.scam.model.CheckNumberRequest
import com.gosuraksha.app.scam.model.CheckNumberResponse
import com.gosuraksha.app.scam.model.ReportScamRequest
import com.gosuraksha.app.scam.model.ReportScamResponse
import com.gosuraksha.app.scam.model.ScamAlertCampaign
import com.gosuraksha.app.scam.model.ScamAlertPageResponse

class ScamNetworkRepository(
    private val api: ScamNetworkApi
) {
    suspend fun submitScamReport(request: ReportScamRequest): ReportScamResponse {
        return api.submitScamReport(request)
    }

    suspend fun checkNumber(request: CheckNumberRequest): CheckNumberResponse {
        return api.checkNumber(request)
    }

    suspend fun getTrendingScams(): List<ScamAlertCampaign> = api.getTrendingScams().data

    suspend fun getScamAlerts(page: Int, pageSize: Int): ScamAlertPageResponse {
        return api.getScamAlerts(page = page, pageSize = pageSize).data
    }
}
