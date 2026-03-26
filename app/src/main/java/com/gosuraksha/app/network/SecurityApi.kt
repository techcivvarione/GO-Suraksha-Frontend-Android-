package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.CyberSosRequest
import com.gosuraksha.app.data.remote.dto.CyberSosResponse
import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
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
    ): ApiResponse<Map<String, String>>

    @POST("security/logout-all")
    suspend fun logoutAll(): ApiResponse<Map<String, String>>

    @GET("security/status")
    suspend fun getSecurityStatus(): ApiResponse<SecurityStatusResponse>

    @GET("security/health-score")
    suspend fun getHealthScore(): ApiResponse<HealthScoreResponse>

    // AuditLogsResponse has a field named "data" which the backend middleware detects
    // and extracts, producing {"status":"success","data":[...]} rather than the full
    // AuditLogsResponse dict. Keeping bare type so the list is at least available.
    @GET("security/audit-logs")
    suspend fun getAuditLogs(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("event_type") eventType: String? = null
    ): AuditLogsResponse

    @GET("security/health-trend")
    suspend fun getHealthTrend(
        @Query("days") days: Int = 30
    ): ApiResponse<HealthTrendResponse>

    @POST("security/scam-report")
    suspend fun reportScam(
        @Body request: ScamReportRequest
    ): ApiResponse<ScamReportResponse>

    @POST("security/scam-confirm")
    suspend fun confirmScam(
        @Body request: ScamReportRequest
    ): ApiResponse<ScamConfirmResponse>

    @POST("security/cyber-complaint/preview")
    suspend fun previewCyberComplaint(
        @Body request: CyberComplaintPreviewRequest
    ): ApiResponse<CyberComplaintPreviewResponse>

    @GET("security/evidence/export")
    suspend fun exportEvidence(): okhttp3.ResponseBody

    @POST("security/cyber-sos/confirm")
    suspend fun triggerCyberSos(
        @Body request: CyberSosRequest
    ): Response<CyberSosResponse>

}


