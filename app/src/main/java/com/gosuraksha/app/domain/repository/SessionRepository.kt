package com.gosuraksha.app.domain.repository

import com.gosuraksha.app.core.result.AppResult

interface SessionRepository {
    suspend fun saveToken(token: String): AppResult<Unit>
    suspend fun getToken(): AppResult<String?>
    suspend fun clearToken(): AppResult<Unit>
}
