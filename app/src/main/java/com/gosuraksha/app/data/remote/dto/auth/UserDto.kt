package com.gosuraksha.app.data.remote.dto.auth

data class UserResponse(
    val id: String,
    val name: String? = null,
    val email: String,
    val role: String? = null,
    val plan: String,
    val phone_number: String? = null,
    val auth_provider: String? = null
)
