package com.gosuraksha.app.domain.repository

import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.domain.model.AuthSession

interface AuthRepository {
    suspend fun login(identifier: String, password: String): AppResult<AuthSession>
    suspend fun googleLogin(idToken: String): AppResult<AuthSession>
    suspend fun signup(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): AppResult<Unit>

    suspend fun getMe(): AppResult<AuthSession>
    suspend fun logout(): AppResult<Unit>
}
