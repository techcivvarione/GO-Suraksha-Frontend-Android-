package com.gosuraksha.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val access_token: String
)

data class GoogleAuthRequest(
    val id_token: String
)

data class GoogleAuthResponse(
    @SerializedName("access_token")
    val access_token: String,
    val token_type: String,
    val user: UserResponse
)

data class SignupRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirm_password: String,
    val accepted_terms: Boolean,
    val terms_version: String = "v1",
    val privacy_version: String = "v1"
)

data class SignupResponse(
    val status: String
)

// OTP WIRING START
data class EmailRequest(val email: String)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class GenericResponse(val message: String)

data class VerifyOtpResponse(val success: Boolean?)
// OTP WIRING END

data class RegisterDeviceRequest(
    val device_token: String,
    val device_type: String
)

