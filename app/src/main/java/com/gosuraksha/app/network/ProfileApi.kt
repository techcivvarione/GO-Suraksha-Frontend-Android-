package com.gosuraksha.app.network

import com.gosuraksha.app.profile.model.ProfileResponse
import com.gosuraksha.app.profile.model.UpgradePlanRequest
import com.gosuraksha.app.profile.model.UserProfile
import com.gosuraksha.app.profile.model.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ProfileApi {

    @GET("/auth/me")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("profile/")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Map<String, String>

    @POST("/billing/upgrade")
    suspend fun upgradePlan(
        @Body request: UpgradePlanRequest
    ): Response<Map<String, String>>
}

