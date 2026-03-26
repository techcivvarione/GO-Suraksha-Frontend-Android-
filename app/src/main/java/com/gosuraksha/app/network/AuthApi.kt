package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import com.gosuraksha.app.data.remote.dto.auth.DeleteAccountRequest
import com.gosuraksha.app.data.remote.dto.auth.EmailOtpVerifyResponse
import com.gosuraksha.app.data.remote.dto.auth.EmailRequest
import com.gosuraksha.app.data.remote.dto.auth.GenericResponse
import com.gosuraksha.app.data.remote.dto.auth.GoogleAuthRequest
import com.gosuraksha.app.data.remote.dto.auth.LoginRequest
import com.gosuraksha.app.data.remote.dto.auth.LoginResponse
import com.gosuraksha.app.data.remote.dto.auth.PhoneOtpRequest
import com.gosuraksha.app.data.remote.dto.auth.RegisterDeviceRequest
import com.gosuraksha.app.data.remote.dto.auth.SignupRequest
import com.gosuraksha.app.data.remote.dto.auth.SignupResponse
import com.gosuraksha.app.data.remote.dto.auth.UserResponse
import com.gosuraksha.app.data.remote.dto.auth.VerifyPhoneOtpRequest
import com.gosuraksha.app.data.remote.dto.auth.VerifyOtpRequest
import com.gosuraksha.app.data.remote.dto.auth.VerifyOtpResponse
import com.gosuraksha.app.ui.main.CyberCardResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<LoginResponse>

    @POST("auth/google-login")
    suspend fun googleLoginV2(
        @Body request: GoogleAuthRequest
    ): ApiResponse<LoginResponse>

    @POST("auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): SignupResponse

    @POST("auth/send-phone-otp")
    suspend fun sendPhoneOtp(
        @Body request: PhoneOtpRequest
    ): ApiResponse<GenericResponse>

    @POST("auth/verify-phone-otp")
    suspend fun verifyPhoneOtp(
        @Body request: VerifyPhoneOtpRequest
    ): ApiResponse<VerifyOtpResponse>

    @POST("auth/send-email-otp")
    suspend fun sendEmailOtp(
        @Body request: EmailRequest
    ): Response<GenericResponse>

    @POST("auth/verify-email-otp")
    suspend fun verifyEmailOtp(
        @Body request: VerifyOtpRequest
    ): Response<ApiResponse<EmailOtpVerifyResponse>>

    @GET("auth/me")
    suspend fun getMe(): ApiResponse<UserResponse>

    @POST("auth/delete-account")
    suspend fun deleteAccount(
        @Body request: DeleteAccountRequest
    ): ApiResponse<Unit>

    @GET("cyber-card")
    suspend fun getCyberCard(): ApiResponse<CyberCardResponse>

    @POST("devices/register")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest
    ): Response<GenericResponse>
}
