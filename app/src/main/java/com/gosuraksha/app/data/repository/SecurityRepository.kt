package com.gosuraksha.app.data.repository

import com.gosuraksha.app.data.remote.dto.CyberSosRequest
import com.gosuraksha.app.network.SecurityApi
import com.gosuraksha.app.security.model.AuditLogsResponse
import com.gosuraksha.app.security.model.ChangePasswordRequest
import com.gosuraksha.app.security.model.HealthScoreResponse
import com.gosuraksha.app.security.model.HealthTrendResponse
import com.gosuraksha.app.security.model.SecurityStatusResponse
import retrofit2.Response

class SecurityRepository(
    private val api: SecurityApi
) {
    suspend fun getSecurityStatus(): SecurityStatusResponse = api.getSecurityStatus().data

    suspend fun getHealthScore(): HealthScoreResponse = api.getHealthScore().data

    // AuditLogsResponse has a "data" field that collides with the envelope key.
    // The middleware extracts only the list, so AuditLogsResponse.data receives the
    // items correctly but .count is lost (defaults to 0). Leave unwrapping here
    // until backend renames the field.
    suspend fun getAuditLogs(
        limit: Int = 20,
        offset: Int = 0,
        eventType: String? = null
    ): AuditLogsResponse {
        return api.getAuditLogs(limit, offset, eventType)
    }

    suspend fun getHealthTrend(days: Int = 30): HealthTrendResponse = api.getHealthTrend(days).data

    suspend fun logoutAll(): Map<String, String> = api.logoutAll().data

    suspend fun changePassword(request: ChangePasswordRequest): Map<String, String> =
        api.changePassword(request).data

    suspend fun exportEvidence() = api.exportEvidence()

    suspend fun triggerCyberSos(request: CyberSosRequest): Response<com.gosuraksha.app.data.remote.dto.CyberSosResponse> {
        return api.triggerCyberSos(request)
    }
}
