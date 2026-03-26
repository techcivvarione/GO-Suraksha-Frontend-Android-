package com.gosuraksha.app.data.repository

import android.util.Log
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.core.network.NetworkErrorMapper
import com.gosuraksha.app.core.result.AppError
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.data.local.TokenLocalDataSource
import com.gosuraksha.app.data.mapper.toDomain
import com.gosuraksha.app.data.remote.AuthRemoteDataSource
import com.gosuraksha.app.data.remote.dto.auth.DeleteAccountRequest
import com.gosuraksha.app.data.remote.dto.auth.GoogleAuthRequest
import com.gosuraksha.app.data.remote.dto.auth.LoginRequest
import com.gosuraksha.app.data.remote.dto.auth.SignupRequest
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
            val sanitizedIdentifier = identifier.trim()
            val sanitizedPassword = password.trim()
            if (sanitizedIdentifier.isBlank() || sanitizedPassword.isBlank()) {
                return AppResult.Failure(AppError.Validation("Login failed. Please try again."))
            }
            tokenLocal.clearToken()
            val response = remote.login(LoginRequest(sanitizedIdentifier, sanitizedPassword))
            val payload = response.data
            if (response.status != "success") {
                return AppResult.Failure(AppError.Validation("Login failed. Please try again."))
            }
            val accessToken = payload.access_token?.takeIf { it.isNotBlank() }
                ?: return AppResult.Failure(AppError.Validation("Login failed. Please try again."))
            tokenLocal.saveToken(accessToken)
            val persistedToken = tokenLocal.getToken()
            require(!persistedToken.isNullOrBlank()) { "Access token missing after login" }
            if (BuildConfig.DEBUG) {
                Log.d("Auth", "Login token persisted")
            }
            val user = remote.getMe().toDomain()
            if (BuildConfig.DEBUG) {
                Log.d("Auth", "Login flow verified")
            }
            AppResult.Success(
                AuthSession(
                    accessToken = persistedToken,
                    user = user,
                    needsPhoneVerification = payload.needs_phone_verification == true || payload.phone_verified == false,
                    phoneVerified = payload.phone_verified != false,
                    isNewUser = payload.is_new_user == true
                )
            )
        } catch (t: Throwable) {
            tokenLocal.clearToken()
            Log.e("Auth", "Login parsing failed", t)
            AppResult.Failure(t.toAuthSessionError())
        }
    }

    override suspend fun googleLogin(idToken: String): AppResult<AuthSession> {
        return try {
            val sanitizedIdToken = idToken.trim()
            if (sanitizedIdToken.isBlank()) {
                return AppResult.Failure(AppError.Validation("Login failed. Please try again."))
            }
            tokenLocal.clearToken()
            val response = remote.googleLogin(GoogleAuthRequest(google_id_token = sanitizedIdToken))
            val payload = response.data
            if (response.status != "success") {
                return AppResult.Failure(AppError.Validation("Login failed. Please try again."))
            }
            val accessToken = payload.access_token?.takeIf { it.isNotBlank() }
                ?: return AppResult.Failure(AppError.Validation("Login failed. Please try again."))
            tokenLocal.saveToken(accessToken)
            val persistedToken = tokenLocal.getToken()
            require(!persistedToken.isNullOrBlank()) { "Access token missing after Google login" }
            if (BuildConfig.DEBUG) {
                Log.d("Auth", "Google login token persisted")
            }
            val user = remote.getMe().toDomain()
            if (BuildConfig.DEBUG) {
                Log.d("Auth", "Google login flow verified")
            }
            AppResult.Success(
                AuthSession(
                    accessToken = persistedToken,
                    user = user,
                    needsPhoneVerification = payload.needs_phone_verification == true || payload.phone_verified == false,
                    phoneVerified = payload.phone_verified != false,
                    isNewUser = payload.is_new_user == true
                )
            )
        } catch (t: Throwable) {
            tokenLocal.clearToken()
            AppResult.Failure(t.toAuthSessionError())
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
                    confirm_password = confirmPassword,
                    accepted_terms = true,
                    terms_version = "v1",
                    privacy_version = "v1"
                )
            )
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(t))
        }
    }

    override suspend fun getMe(): AppResult<AuthSession> {
        return try {
            val token = tokenLocal.getToken().orEmpty()
            if (token.isBlank()) {
                AppResult.Failure(AppError.Unauthorized)
            } else {
                val user = remote.getMe().toDomain()
                AppResult.Success(AuthSession(token, user))
            }
        } catch (t: Throwable) {
            AppResult.Failure(t.toAuthSessionError())
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

    override suspend fun deleteAccount(username: String): AppResult<Unit> {
        return try {
            remote.deleteAccount(DeleteAccountRequest(username.trim()))
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(t))
        }
    }
}

private fun Throwable.toAuthSessionError(): AppError = when (this) {
    is IllegalStateException -> AppError.Validation("Something went wrong. Please login again.")
    else -> NetworkErrorMapper.map(this)
}
