package com.gosuraksha.app.profile.model

data class ProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val created_at: String,
    val updated_at: String
)

data class UpdateProfileRequest(
    val name: String,
    val phone: String
)
