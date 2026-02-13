package com.gosuraksha.app.auth.model

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val plan: String   // "FREE" or "PAID"
)
