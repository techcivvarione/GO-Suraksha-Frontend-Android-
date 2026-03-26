package com.gosuraksha.app.data.repository

import android.util.Log
import com.google.android.datatransport.BuildConfig
import com.gosuraksha.app.core.network.NetworkErrorMapper
import com.gosuraksha.app.core.result.AppError
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.data.local.TokenLocalDataSource
import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import com.gosuraksha.app.data.remote.dto.auth.EmailOtpVerifyResponse
import com.gosuraksha.app.data.remote.dto.auth.EmailRequest
import com.gosuraksha.app.data.remote.dto.auth.GenericResponse
import com.gosuraksha.app.data.remote.dto.auth.PhoneOtpRequest
import com.gosuraksha.app.data.remote.dto.auth.VerifyOtpRequest
import com.gosuraksha.app.data.remote.dto.auth.VerifyPhoneOtpRequest
import com.gosuraksha.app.network.AuthApi
import retrofit2.Response

data class PhoneOtpVerificationResult(
    val token: String,
    val needsPhoneVerification: Boolean,
    val phoneVerified: Boolean,
    val isNewUser: Boolean
)

class AuthOtpRepository(
    private val api: AuthApi,
    private val tokenLocalDataSource: TokenLocalDataSource
) {

    // ✅ OK (no change)
    suspend fun sendEmailOtp(request: EmailRequest): Response<GenericResponse> {
        return api.sendEmailOtp(request)
    }

    // 🔥 FIXED HERE
    suspend fun verifyEmailOtp(request: VerifyOtpRequest): AppResult<EmailOtpVerifyResponse> {
        return try {
            val response = api.verifyEmailOtp(request)

            if (!response.isSuccessful) {
                return AppResult.Failure(AppError.Validation("OTP verification failed"))
            }

            val body = response.body()

            if (body?.status != "success") {
                return AppResult.Failure(AppError.Validation("OTP verification failed"))
            }

            val data = body.data
                ?: return AppResult.Failure(AppError.Validation("Invalid server response"))

            AppResult.Success(data)

        } catch (t: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(t))
        }
    }

    // ✅ already correct (uses ApiResponse directly)
    suspend fun sendPhoneOtp(request: PhoneOtpRequest): ApiResponse<GenericResponse> {
        return api.sendPhoneOtp(request)
    }

    // ✅ already correct
    suspend fun verifyPhoneOtp(request: VerifyPhoneOtpRequest): AppResult<PhoneOtpVerificationResult> {
        return try {
            if (BuildConfig.DEBUG) {
                Log.d("AUTH_FLOW", "OTP request sent")
            }

            val response = api.verifyPhoneOtp(request)

            if (BuildConfig.DEBUG) {
                Log.d("AUTH_FLOW", "API response received")
                Log.d("AUTH_FLOW", "Envelope status = ${response.status}")
            }

            if (response.status != "success") {
                return AppResult.Failure(AppError.Validation("OTP verification failed"))
            }

            val token = response.data.access_token?.takeIf { it.isNotBlank() }
                ?: return AppResult.Failure(AppError.Validation("OTP verification failed"))

            if (BuildConfig.DEBUG) {
                Log.d("AUTH_FLOW", "Token extracted = ${token.isNotBlank()}")
            }

            tokenLocalDataSource.saveToken(token)

            AppResult.Success(
                PhoneOtpVerificationResult(
                    token = token,
                    needsPhoneVerification = response.data.needs_phone_verification == true,
                    phoneVerified = response.data.phone_verified != false,
                    isNewUser = response.data.is_new_user == true
                )
            )

        } catch (t: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(t))
        }
    }
}