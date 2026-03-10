package com.gosuraksha.app.data.remote.dto.auth

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val plan: String
)
