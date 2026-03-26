package com.gosuraksha.app.network

import com.gosuraksha.app.alerts.model.*
import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface AlertsApi {

    @GET("alerts")
    suspend fun getAlerts(): Response<ApiResponse<AlertsResponse>>

    @GET("alerts/summary")
    suspend fun getAlertsSummary(): ApiResponse<AlertsSummaryResponse>

    @GET("alerts/refresh")
    suspend fun refreshAlerts(): ApiResponse<RefreshAlertsResponse>

    @POST("alerts/subscribe")
    suspend fun subscribeAlerts(
        @Body request: Map<String, List<String>>
    ): ApiResponse<SubscribeAlertsResponse>

    @GET("trusted/alerts")
    suspend fun getTrustedAlerts(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ApiResponse<TrustedAlertsResponse>

    @PATCH("trusted/alerts/{alert_id}/read")
    suspend fun markTrustedAlertRead(
        @Path("alert_id") id: String
    ): ApiResponse<TrustedAlertReadResponse>

    @GET("alerts/family-activity")
    suspend fun getFamilyActivity(
        @Query("limit") limit: Int = 20
    ): ApiResponse<FamilyActivityResponse>

    @GET("alerts/family-feed")
    suspend fun getFamilyFeed(
        @Query("limit") limit: Int = 20
    ): ApiResponse<FamilyFeedResponse>
}
