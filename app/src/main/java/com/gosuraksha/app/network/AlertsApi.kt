package com.gosuraksha.app.network

import com.gosuraksha.app.alerts.model.*
import retrofit2.Response
import retrofit2.http.*

interface AlertsApi {

    @GET("alerts")
    suspend fun getAlerts(): Response<List<AlertEvent>>

    @GET("alerts/summary")
    suspend fun getAlertsSummary(): AlertsSummaryResponse

    @GET("alerts/refresh")
    suspend fun refreshAlerts(): RefreshAlertsResponse

    @POST("alerts/subscribe")
    suspend fun subscribeAlerts(
        @Body request: Map<String, List<String>>
    ): SubscribeAlertsResponse

    @GET("trusted/alerts")
    suspend fun getTrustedAlerts(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): TrustedAlertsResponse

    @PATCH("trusted/alerts/{alert_id}/read")
    suspend fun markTrustedAlertRead(
        @Path("alert_id") id: String
    ): TrustedAlertReadResponse
}
