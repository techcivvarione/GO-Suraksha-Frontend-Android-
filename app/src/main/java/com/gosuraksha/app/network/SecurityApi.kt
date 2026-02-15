package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.CyberSosRequest
import com.gosuraksha.app.data.remote.dto.CyberSosResponse
import com.gosuraksha.app.security.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface SecurityApi {

    @POST("security/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Map<String, String>

    @POST("security/logout-all")
    suspend fun logoutAll(): Map<String, String>

    @GET("security/status")
    suspend fun getSecurityStatus(): SecurityStatusResponse

    @GET("security/health-score")
    suspend fun getHealthScore(): HealthScoreResponse

    @GET("security/audit-logs")
    suspend fun getAuditLogs(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("event_type") eventType: String? = null
    ): AuditLogsResponse

    @GET("security/health-trend")
    suspend fun getHealthTrend(
        @Query("days") days: Int = 30
    ): HealthTrendResponse

    @POST("security/scam-report")
    suspend fun reportScam(
        @Body request: ScamReportRequest
    ): ScamReportResponse


    @POST("security/scam-confirm")
    suspend fun confirmScam(
        @Body request: ScamReportRequest
    ): ScamConfirmResponse


    @POST("security/cyber-complaint/preview")
    suspend fun previewCyberComplaint(
        @Body request: CyberComplaintPreviewRequest
    ): CyberComplaintPreviewResponse


    @POST("security/cyber-sos/confirm")
    suspend fun confirmCyberSOS(
        @Body request: CyberSOSRequest
    ): CyberSOSResponse


    @GET("security/evidence/export")
    suspend fun exportEvidence(): okhttp3.ResponseBody

    @POST("security/cyber-sos/confirm")
    suspend fun triggerCyberSos(
        @Body request: CyberSosRequest
    ): Response<CyberSosResponse>

}
