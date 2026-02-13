package com.gosuraksha.app.auth.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val identifier: String,
    val password: String
)


data class LoginResponse(
    @SerializedName("access_token")
    val access_token: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirm_password: String
)

data class SignupResponse(
    val status: String
)
