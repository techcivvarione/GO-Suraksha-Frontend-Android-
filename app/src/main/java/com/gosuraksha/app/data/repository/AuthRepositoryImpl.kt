package com.gosuraksha.app.data.repository

import com.gosuraksha.app.data.remote.dto.auth.LoginRequest
import com.gosuraksha.app.data.remote.dto.auth.SignupRequest
import com.gosuraksha.app.core.network.NetworkErrorMapper
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.data.local.TokenLocalDataSource
import com.gosuraksha.app.data.mapper.toDomain
import com.gosuraksha.app.data.remote.AuthRemoteDataSource
import com.gosuraksha.app.domain.model.AuthSession
import com.gosuraksha.app.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val tokenLocal: TokenLocalDataSource
) : AuthRepository {

    override suspend fun login(
        identifier: String,
        password: String
    ): AppResult<AuthSession> {
        return try {
            val response = remote.login(LoginRequest(identifier, password))
            tokenLocal.saveToken(response.access_token)
            val user = remote.getMe().toDomain()
            AppResult.Success(AuthSession(response.access_token, user))
        } catch (t: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(t))
        }
    }

    override suspend fun signup(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): AppResult<Unit> {
        return try {
            remote.signup(
                SignupRequest(
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    confirm_password = confirmPassword
                )
            )
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(t))
        }
    }

    override suspend fun getMe(): AppResult<AuthSession> {
        return try {
            val user = remote.getMe().toDomain()
            val token = tokenLocal.getToken().orEmpty()
            AppResult.Success(AuthSession(token, user))
        } catch (t: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(t))
        }
    }

    override suspend fun logout(): AppResult<Unit> {
        return try {
            tokenLocal.clearToken()
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(t))
        }
    }
}
