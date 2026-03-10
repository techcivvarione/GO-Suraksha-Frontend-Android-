package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.LoginRequest
import com.gosuraksha.app.data.remote.dto.auth.LoginResponse
import com.gosuraksha.app.data.remote.dto.auth.SignupRequest
import com.gosuraksha.app.data.remote.dto.auth.SignupResponse
import com.gosuraksha.app.data.remote.dto.auth.UserResponse
import com.gosuraksha.app.data.remote.dto.auth.EmailRequest
import com.gosuraksha.app.data.remote.dto.auth.VerifyOtpRequest
import com.gosuraksha.app.data.remote.dto.auth.GenericResponse
import com.gosuraksha.app.data.remote.dto.auth.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.gosuraksha.app.ui.main.CyberCardResponse


interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): SignupResponse

    // OTP WIRING START
    @POST("auth/send-email-otp")
    suspend fun sendEmailOtp(
        @Body request: EmailRequest
    ): Response<GenericResponse>

    @POST("auth/verify-email-otp")
    suspend fun verifyEmailOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>
    // OTP WIRING END

    @GET("auth/me")
    suspend fun getMe(): UserResponse

    @GET("cyber-card")
    suspend fun getCyberCard(): CyberCardResponse

}
