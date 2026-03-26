package com.gosuraksha.app.data.repository

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import com.gosuraksha.app.data.remote.dto.auth.UserResponse
import com.gosuraksha.app.network.AuthApi
import com.gosuraksha.app.network.ProfileApi
import com.gosuraksha.app.network.QuotaResponse
import com.gosuraksha.app.profile.model.ProfileUpdateResponse
import com.gosuraksha.app.profile.model.UpdateProfileRequest
import com.gosuraksha.app.profile.model.UploadProfilePhotoResponse
import com.gosuraksha.app.profile.model.UpgradePlanRequest
import com.gosuraksha.app.profile.model.UserProfile
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class ProfileRepository(
    private val profileApi: ProfileApi,
    private val authApi: AuthApi
) {
    /**
     * Fetches the current user profile from /auth/me and unwraps the envelope.
     * Returns Response<UserProfile> so ProfileViewModel.fetchProfile() needs no changes.
     */
    suspend fun getProfile(): Response<UserProfile> {
        val wrapped = profileApi.getProfile()
        return wrapped.unwrapEnvelope()
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Response<ProfileUpdateResponse> {
        val wrapped = profileApi.updateProfile(request)
        return wrapped.unwrapEnvelope()
    }

    suspend fun uploadProfilePhoto(image: MultipartBody.Part): Response<UploadProfilePhotoResponse> {
        val wrapped = profileApi.uploadProfilePhoto(image)
        return wrapped.unwrapEnvelope()
    }

    suspend fun upgradePlan(request: UpgradePlanRequest): Response<Map<String, String>> {
        return profileApi.upgradePlan(request)
    }

    suspend fun getMe(): UserResponse {
        val response = authApi.getMe()
        return response.data ?: throw IllegalStateException("Missing user data")
    }

    /**
     * Fetches the scan quota for the current billing period.
     * Returns null on any error so callers can handle gracefully.
     */
    suspend fun getQuota(): QuotaResponse? {
        return runCatching {
            val response = profileApi.getQuota()
            if (response.isSuccessful) response.body()?.data else null
        }.getOrNull()
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    /**
     * Safely unwraps Response<ApiResponse<T>> → Response<T>.
     * For non-2xx responses the generic type is erased at runtime, so the cast is safe.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> Response<ApiResponse<T>>.unwrapEnvelope(): Response<T> {
        if (!isSuccessful) return this as Response<T>
        val data = body()?.data
            ?: return Response.error(502, "Empty envelope".toResponseBody())
        return Response.success(data, raw())
    }
}
