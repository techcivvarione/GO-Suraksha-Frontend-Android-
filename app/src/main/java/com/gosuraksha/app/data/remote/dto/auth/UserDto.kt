package com.gosuraksha.app.data.remote.dto.auth

data class UserResponse(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val plan: String? = null,
    val phone: String? = null,
    val profile_image_url: Any? = null,
    val auth_provider: String? = null
)

