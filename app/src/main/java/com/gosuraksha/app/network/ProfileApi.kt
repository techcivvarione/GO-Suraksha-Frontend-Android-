package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import com.gosuraksha.app.profile.model.ProfileUpdateResponse
import com.gosuraksha.app.profile.model.UpdateProfileRequest
import com.gosuraksha.app.profile.model.UploadProfilePhotoResponse
import com.gosuraksha.app.profile.model.UpgradePlanRequest
import com.gosuraksha.app.profile.model.UserProfile
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT

interface ProfileApi {

    @GET("/auth/me")
    suspend fun getProfile(): Response<ApiResponse<UserProfile>>

    @PUT("profile/")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<ProfileUpdateResponse>>

    @POST("/billing/upgrade")
    suspend fun upgradePlan(
        @Body request: UpgradePlanRequest
    ): Response<Map<String, String>>

    @Multipart
    @POST("/profile/upload-photo")
    suspend fun uploadProfilePhoto(
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<UploadProfilePhotoResponse>>

    /** Fetch current user's scan quota for the billing period. */
    @GET("/user/quota")
    suspend fun getQuota(): Response<ApiResponse<QuotaResponse>>
}
