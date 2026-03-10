package com.gosuraksha.app.data.repository

import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.data.local.TokenLocalDataSource
import com.gosuraksha.app.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val tokenLocal: TokenLocalDataSource
) : SessionRepository {
    override suspend fun saveToken(token: String): AppResult<Unit> {
        return try {
            tokenLocal.saveToken(token)
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Failure(com.gosuraksha.app.core.network.NetworkErrorMapper.map(t))
        }
    }

    override suspend fun getToken(): AppResult<String?> {
        return try {
            AppResult.Success(tokenLocal.getToken())
        } catch (t: Throwable) {
            AppResult.Failure(com.gosuraksha.app.core.network.NetworkErrorMapper.map(t))
        }
    }

    override suspend fun clearToken(): AppResult<Unit> {
        return try {
            tokenLocal.clearToken()
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Failure(com.gosuraksha.app.core.network.NetworkErrorMapper.map(t))
        }
    }
}
