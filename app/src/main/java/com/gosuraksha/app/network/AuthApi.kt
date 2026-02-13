package com.gosuraksha.app.network

import com.gosuraksha.app.auth.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.gosuraksha.app.auth.model.UserResponse
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

    @GET("auth/me")
    suspend fun getMe(): UserResponse

    @GET("cyber-card")
    suspend fun getCyberCard(): CyberCardResponse

}
