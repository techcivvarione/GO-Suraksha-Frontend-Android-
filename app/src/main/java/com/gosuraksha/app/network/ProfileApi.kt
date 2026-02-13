package com.gosuraksha.app.network

import com.gosuraksha.app.profile.model.ProfileResponse
import com.gosuraksha.app.profile.model.UpdateProfileRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProfileApi {

    @GET("profile/")
    suspend fun getProfile(): ProfileResponse

    @PUT("profile/")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Map<String, String>
}

