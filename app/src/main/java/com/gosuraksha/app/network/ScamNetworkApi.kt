package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import com.gosuraksha.app.scam.model.CheckNumberRequest
import com.gosuraksha.app.scam.model.CheckNumberResponse
import com.gosuraksha.app.scam.model.ReportScamRequest
import com.gosuraksha.app.scam.model.ReportScamResponse
import com.gosuraksha.app.scam.model.ScamAlertCampaign
import com.gosuraksha.app.scam.model.ScamAlertPageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ScamNetworkApi {

    @POST("scam/report")
    suspend fun submitScamReport(
        @Body request: ReportScamRequest
    ): ReportScamResponse

    @POST("scam/check-number")
    suspend fun checkNumber(
        @Body request: CheckNumberRequest
    ): CheckNumberResponse

    @POST("/scam/verify-call")
    suspend fun verifyCall(
        @Body request: CheckNumberRequest
    ): CheckNumberResponse

    @GET("scam/trending")
    suspend fun getTrendingScams(): ApiResponse<List<ScamAlertCampaign>>

    @GET("scam/alerts")
    suspend fun getScamAlerts(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): ApiResponse<ScamAlertPageResponse>
}
