package com.gosuraksha.app.domain.model

data class AuthSession(
    val accessToken: String,
    val user: User
)
