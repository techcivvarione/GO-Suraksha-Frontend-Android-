package com.gosuraksha.app.profile.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val profile_image_url: String? = null,
    val role: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val plan: String? = null
)

data class UpdateProfileRequest(
    val name: String,
    val profile_image_url: String? = null
)

data class ProfileUpdateResponse(
    val status: String,
    val message: String? = null
)

data class UpgradePlanRequest(
    val plan: String
)

data class UploadProfilePhotoResponse(
    val profile_image_url: String
)

typealias ProfileResponse = UserProfile
