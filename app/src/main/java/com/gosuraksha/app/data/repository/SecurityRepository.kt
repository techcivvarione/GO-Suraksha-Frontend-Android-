package com.gosuraksha.app.data.repository

import com.gosuraksha.app.network.SecurityApi
import com.gosuraksha.app.data.remote.dto.CyberSosRequest
import retrofit2.Response

class SecurityRepository(
    private val api: SecurityApi
) {
    suspend fun triggerCyberSos(request: CyberSosRequest): Response<com.gosuraksha.app.data.remote.dto.CyberSosResponse> {
        return api.triggerCyberSos(request)
    }
}
