package com.gosuraksha.app.domain.model

data class AuthSession(
    val accessToken: String,
    val user: User,
    val needsPhoneVerification: Boolean = false,
    val phoneVerified: Boolean = true,
    val isNewUser: Boolean = false
)
